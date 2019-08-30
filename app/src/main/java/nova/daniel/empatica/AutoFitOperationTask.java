package nova.daniel.empatica;

import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nova.daniel.empatica.model.Appointment;
import nova.daniel.empatica.model.Caregiver;
import nova.daniel.empatica.model.CountWork;
import nova.daniel.empatica.persistence.AppointmentRepository;
import nova.daniel.empatica.persistence.CaregiverRepository;
import nova.daniel.empatica.ui.MainActivity;

/**
 * Task that implements the auto-fit feature of the application.
 * For every time-slot of the day, and for every available room in the hospital,
 * the task assigns a caregiver.
 * <p>
 * Caregivers are added depending on which caregiver is best suited based on a score.
 * Initially all caregivers are candidates, depending on the amount of hours they have worked in the
 * current week, and last 4 weeks as well, a double-valued score is associated with each caregiver.
 * <p>
 * <p/>
 * Caregiver restrictions:
 * - A caregiver can work at most 5 hours per week
 * - A caregiver can have at most 1 hour of over-time per week
 * <p>
 * <p/>
 * The task scores each caregiver using the following criteria:
 * - If the caregiver does not have any over-time for the current week, a score of 1 is added
 * - If the caregiver is already working that day, a score is added depending on the distance of the rooms they are working on
 * - If the caregiver is amongst those that have worked the least in the past 4 weeks, a score of 3 is added.
 * <p>
 * <p>
 * All calls to the repositories are done synchronously, as it is needed to guarantee consistency and because
 * the queueing nature of AsyncTasks in android.
 */
public class AutoFitOperationTask extends AsyncTask<Void, Void, Void> {

    private static final int START_HOUR_WORKDAY = 9;
    private static final int END_HOUR_WORKDAY = 17;
    private static final int MAX_WORK_HOURS = 5;
    private static final int MAX_OVERTIME_HOURS = 1;

    private WeakReference<Context> mContext; // Calling context reference
    private Date mDate;

    private CaregiverRepository mCaregiverRepository;
    private AppointmentRepository mAppointmentRepository;
    private AutoFitCallBack uiCallback;

    public AutoFitOperationTask(Context context, Date date) {
        mContext = new WeakReference<>(context);
        mDate = date;
        uiCallback = (MainActivity) mContext.get();
        mCaregiverRepository = new CaregiverRepository(context, false);
        mAppointmentRepository = new AppointmentRepository(context);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        List<String> mCaregiverIds = mCaregiverRepository.getAllIDsSync();

        // Go through each time-slot of the working day
        for (int hour = START_HOUR_WORKDAY; hour < END_HOUR_WORKDAY; hour++) {
            mDate = Utils.setHourDate(mDate, hour);

            List<Integer> takenRooms = mAppointmentRepository.getRoomsForCaregiverByHour(mDate);
            List<Integer> availableRooms = getAvailableRooms(takenRooms);

            // Go through each available room
            for (int roomNumber : availableRooms) {
                // Initialize candidates
                Map<String, Double> candidates = initializeCandidates(mCaregiverIds);

                // Remove those that are already assigned for the current time-slot, as they are considered unavailable
                candidates.keySet().removeAll(mAppointmentRepository.getCaregiversForHourSync(mDate));

                // Get caregivers that have worked the least amount of hours in the past 4 weeks
                List<String> caregiversLeastWorkedHours = getCaregiversLessWorkedHours();

                // Go through each candidate
                for (String candidateId : new ArrayList<>(candidates.keySet())) {
                    // Check how many hours this candidate has worked the current week
                    int countWeekAppointments = mAppointmentRepository.countCaregiverSlotsForWeekSync(mDate, candidateId);

                    // Ignore caregivers that could exceed the max amount of work hours
                    if (countWeekAppointments >= MAX_WORK_HOURS + MAX_OVERTIME_HOURS) { // 5 hours per week + 1 overtime
                        // remove this candidate form the task, as they cannot be possible assigned to any slot the current day
                        mCaregiverIds.remove(candidateId);
                        candidates.remove(candidateId);
                    } else { // caregiver is an eligible candidate, so now compute a score for them
                        if (countWeekAppointments < MAX_WORK_HOURS)  // No overtime
                            candidates.put(candidateId, 1d); // add a score of 1

                        // For caregivers working the same day, add a score based on the distance of the room
                        double roomScore = roomProximityScore(roomNumber, candidateId);
                        candidates.merge(candidateId, roomScore, Double::sum);

                        // If the caregiver is amongst those who have worked less hours in the last 4 weeks, add an extra score of 3
                        if (caregiversLeastWorkedHours.contains(candidateId))
                            candidates.merge(candidateId, 3d, Double::sum);
                    }
                }
                // Now, pick candidate with best score
                String candidateID = Utils.getMaxScoreCandidate(candidates);
                // Check if a best candidate exists, if so insert into the repository
                if (!candidateID.equalsIgnoreCase("")) {
                    createAndInsertAppointment(candidateID, mDate, roomNumber);
                }
            }
        }
        return null;
    }

    /**
     * Once the task is done, callback {@link MainActivity} to dismiss the progress dialog.
     * If the activity has been destroyed, the method does not do anything else.
     *
     * @param aVoid void
     */
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (mContext.get() != null) {
            uiCallback.onFinishedAutoFit();
        }
    }

    /**
     * Get a list of the caregivers that have worked the least number of hours in the past 4 weeks.
     *
     * @return List of caregivers IDs
     */
    private List<String> getCaregiversLessWorkedHours() {
        List<CountWork> caregiversHoursCount = mCaregiverRepository.getByAppointmentCountLastWeeks(mDate); // result truncated to 10
        List<String> result = new ArrayList<>();

        if (caregiversHoursCount.size() > 0) {
            // the list is sorted by counts, so the first element is always guaranteed to have the minimum
            int minHours = caregiversHoursCount.get(0).counts;
            for (CountWork c : caregiversHoursCount)
                if (c.counts <= minHours)
                    result.add(c.uuid);
        }
        return result;
    }

    /**
     * Initialize the candidates Map
     *
     * @param caregivers list of caregivers ids
     * @return Candidates Map, with the caregiver ID as ID, and its score as value. Initialized to 0
     */
    private Map<String, Double> initializeCandidates(List<String> caregivers) {
        Map<String, Double> candidates = new HashMap<>(caregivers.size());
        for (String caregiverId : caregivers)
            candidates.put(caregiverId, 0d);  // initialize all scores to 0
        return candidates;
    }

    /**
     * Gets the list of available rooms given a list of appointments
     *
     * @param takenRooms List of the room numbers already taken
     * @return List of numbers of the available rooms
     */
    private List<Integer> getAvailableRooms(List<Integer> takenRooms) {
        return Utils.getAvailableRooms(mContext.get(), takenRooms);
    }

    /**
     * Computes the room proximity score of a given room and candidate based on the distance between rooms.
     * It fetches the appointment rooms for a specific candidate on the current date.
     * If no appointments exist, the score remains 0.
     * <p>
     * If the rooms are the same, a score of 2 is assigned. Otherwise it is computed as 2/distance
     *
     * @param currentRoom Room number being analyzed
     * @param candidateId Target candidate ID
     * @return Score
     */
    private double roomProximityScore(int currentRoom, String candidateId) {
        double score = 0;
        // Get the rooms the caregiver has been assigned to in the current day
        List<Integer> dayRooms = mAppointmentRepository.getRoomsForCaregiverByDay(mDate, candidateId);
        for (int room : dayRooms) {
            int distance = Math.abs(room - currentRoom);
            double newScore = distance == 0 ? 2d : 2d / distance;
            if (newScore > score)
                score = newScore;
        }
        return score;
    }

    /**
     * Creates a new Appointment object and sends it to the repository to be saved
     *
     * @param caregiverID Id of che caregiver
     * @param date        Date
     * @param room        Room number
     */
    private void createAndInsertAppointment(final String caregiverID, final Date date, final int room) {
        List<Caregiver> caregivers = mCaregiverRepository.getByIDSync(new String[]{caregiverID});

        Caregiver caregiver = caregivers.get(0);
        Appointment newAppointment = new Appointment(date, caregiver, "", room);
        mAppointmentRepository.insertSync(newAppointment);
    }

    // Callback when the task is completed
    public interface AutoFitCallBack {
        void onFinishedAutoFit();
    }
}