package nova.daniel.empatica.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nova.daniel.empatica.R;
import nova.daniel.empatica.Utils;
import nova.daniel.empatica.model.Appointment;
import nova.daniel.empatica.model.Caregiver;
import nova.daniel.empatica.persistence.AppointmentRepository;

/**
 * ViewModel for appointments
 */
public class AppointmentViewModel extends AndroidViewModel {

    private static final int CHECKS_THRESHOLD = 2;
    public int editingAppointmentId = -1;     // Update params, -1 if no update is done
    // Number of checks performed before an appointment can be added
    // One check for hourly caregiver conflicts, and one for weekly conflicts.
    private List<CONFLICT_CODES> mChecks;

    private AppointmentRepository mRepository;
    private LiveData<List<Appointment>> mAppointments; //data set
    public LiveData<List<String>> caregiversByHourData;
    public LiveData<Integer> caregiversByWeek;
    public Caregiver newAppointmentCaregiver;
    /**
     * Callback listener for the conflict analysis results
     **/
    private AppointmentModelCallback modelCallback;
    public int currentRoomNumber;


    public interface AppointmentModelCallback {
        void conflictResultCallback(Appointment appointment, CONFLICT_CODES errorCode);
    }

    /**
     * Fetch all appointments.
     *
     * @return Returns all appointments from the repository.
     */
    public LiveData<List<Appointment>> getAll() {
        mAppointments = mRepository.getAll();
        return mAppointments;
    }

    /**
     * Constructor, initializes the {@link AppointmentRepository} instance.
     * @param application Application context
     */
    public AppointmentViewModel(@NonNull Application application){
        super(application);
        mRepository = new AppointmentRepository(application);
    }
    /**
     * Adds an appointment to the repository.
     * @param appointment Appointment to add.
     */
    public void add(Appointment appointment){
        mRepository.insert(appointment);
    }

    /**
     * Updates an appointment in the repository.
     * Updates by ID.
     * @param appointment Appointment to update.
     */
    public void update(Appointment appointment) {
        mRepository.update(appointment);
    }


    /**
     * Deletes an appointment in the repository.
     * Deletes by ID.
     *
     * @param id ID of appointment to delete.
     */
    public void delete(int id) {
        mRepository.deleteById(id);
    }

    /**
     * Returns all appointments (between 0:00 - 23:59) for the day given in the date parameter from the repository.
     *
     * @param date Date of the day to query
     * @return Appointments for the given day.
     */
    public LiveData<List<Appointment>> getForDay(Date date) {
        long start = Utils.getDayStart(date).getTime();
        long end = Utils.getDayEnd(date).getTime();

        mAppointments = mRepository.getForDate(start, end);
        return mAppointments;
    }

    /**
     * Fetches the rooms used for the given date.
     *
     * @param date Appointment query date.
     * @return Live data of the list of rooms used in the given date.
     */
    public LiveData<List<Integer>> getTakenRooms(Date date) {
        long start = Utils.removeMinutesSecondsAndMillis(date).getTime(); // set minute to 0 for the current hour
        long end = Utils.getHourEnd(date).getTime();

        return mRepository.getUsedRoomsForTimeRange(start, end);
    }

    /**
     * Returns all appointments for a set of Ids from the repository.
     *
     * @param ids IDs to query.
     * @return List of appointments for the given ids.
     */
    public LiveData<List<Appointment>> getAppointmentsForID(int[] ids) {
        mAppointments = mRepository.getByID(ids);
        return mAppointments;
    }

    /**
     * Checks for possible conflicts of a new or edited appointment. Possible conflicts are identified by a {@link CONFLICT_CODES} code;
     * possible conflicts include:
     *  - CAREGIVER_BUSY: caregiver is already working at the current hour of the day.
     *  - CAREGIVER_BUSY_MAX_SLOTS: caregiver has exceeded the maximum number of allowed work hours for the current week. Default set to 5, see the integers resource max_caregiver_slots_per_week
     *
     * Once the conflict checks are carried out, the modelCallback.conflictResultCallback method is called, specifying any potential conflict.
     *
     * @param appointment Target appointment
     * @param owner Owner for the asynchronous results form the repository, generally the {@link nova.daniel.empatica.ui.AppointmentActivity} instance.
     */
    public void checkCaregiverForConflict(Appointment appointment, LifecycleOwner owner) {
        mChecks = new ArrayList<>();
        modelCallback = (AppointmentModelCallback) owner;

        // Check if the caregiver is already assigned during that time-slot
        caregiversByHourData = getCaregiversForHourDate(appointment.mDate, editingAppointmentId);
        caregiversByHourData.observe(owner, uuids -> {

            boolean caregiverBusy = uuids.contains(appointment.mCaregiver.uuid);

            CONFLICT_CODES code = caregiverBusy ? CONFLICT_CODES.CAREGIVER_BUSY : CONFLICT_CODES.NONE;

            mChecks.add(code);
            caregiversByHourData.removeObservers(owner);
            modelCallback.conflictResultCallback(appointment, code);
        });

        // Check if the caregiver can work more hours that week
        caregiversByWeek = countCaregiversForWeek(appointment.mDate, appointment.mCaregiver.uuid);
        caregiversByWeek.observe(owner, count -> {

            int max_slots = getApplication().getResources().getInteger(R.integer.max_caregiver_slots_per_week);

            CONFLICT_CODES code = count >= max_slots ? CONFLICT_CODES.CAREGIVER_BUSY_MAX_SLOTS : CONFLICT_CODES.NONE;

            mChecks.add(code);
            caregiversByWeek.removeObservers(owner);
            modelCallback.conflictResultCallback(appointment, code);
        });
    }

    /// Methods for checking constraints ///

    /**
     * Get caregivers ids that have associated appointments for the selected date, excluding the given appointment
     * @param date          Date
     * @param appointmentId Appointment id
     * @return List of caregivers uuids.
     */
    private LiveData<List<String>> getCaregiversForHourDate(Date date, int appointmentId) {
        return mRepository.getCaregiversForHour(date, appointmentId);
    }

    /**
     * Counts how many appointments the given caregiver has for the week
     *
     * @param date        Target date of the week
     * @param caregiverID Caregiver ID
     * @return Count of caregivers for the week
     */
    private LiveData<Integer> countCaregiversForWeek(Date date, String caregiverID) {
        return mRepository.countCaregiverSlotsForWeek(date, caregiverID);
    }

    /**
     * Checks if there are any unresolved conflicts in the mChecks list
     * @return True if no conflicts exist, false if there is at least one conflict code in mChecks.
     */
    public boolean allChecksPassed(){
        boolean anyChecks = !mChecks.contains(CONFLICT_CODES.CAREGIVER_BUSY) && !mChecks.contains(CONFLICT_CODES.CAREGIVER_BUSY_MAX_SLOTS)
                && !mChecks.contains(CONFLICT_CODES.ROOM_UNAVAILABLE);

        return mChecks.size() >= CHECKS_THRESHOLD && anyChecks;
    }


    /// END Methods for checking constraints ///

    // Codes used for specifying the possible conflicts when trying to add/edit an appointment.
    public enum CONFLICT_CODES {
        CAREGIVER_BUSY,    // Caregiver busy
        CAREGIVER_BUSY_MAX_SLOTS,    // Caregiver busy
        ROOM_UNAVAILABLE,  // Room not available
        NONE              // No conflict found
    }
}
