package nova.daniel.empatica.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import nova.daniel.empatica.R;
import nova.daniel.empatica.model.Appointment;
import nova.daniel.empatica.model.Caregiver;
import nova.daniel.empatica.persistence.AppointmentRepository;

/**
 * ViewModel for appointments
 */
public class AppointmentViewModel extends AndroidViewModel {

    public enum CONFLICT_CODES {
        CAREGIVER_BUSY,    // Caregiver busy
        ROOM_UNAVAILABLE,  // Room not available
        NONE              // No conflict found
    }

    private AppointmentRepository mRepository;
    private LiveData<List<Appointment>> mAppointments; //data set
    private LiveData<List<Integer>> mSpinnerData;
    public LiveData<List<String>> caregiversByHourData;
    public Caregiver newAppointmentCaregiver;
    // Update params
    public int editingAppointmentId = -1;
    public int currentRoomNumber;

    public interface AppointmentModelCallback {
        void conflictResultCallback(Appointment appointment, CONFLICT_CODES errorCode);
    }

    private AppointmentModelCallback modelCallback;

    /**
     * Constructor, initializes the {@link AppointmentRepository} instance.
     *
     * @param application Application context
     */
    public AppointmentViewModel(@NonNull Application application){
        super(application);
        mRepository = new AppointmentRepository(application);
    }

    /**
     * Fetch all appointments.
     * @return Returns all appointments from the repository.
     */
    public LiveData<List<Appointment>> getAllAppointments(){
        mAppointments = mRepository.getAllAppointments();
        return mAppointments;
    }

    /**
     * Returns all appointments for the day given in the date parameter from the repository.
     * @param date Date of the day to query
     * @return Appointments for the given day.
     */
    public LiveData<List<Appointment>> getAppointmentsForDate(Date date) {
        mAppointments = mRepository.getAppointmentsForDate(date);
        return mAppointments;
    }

    /**
     * Returns all appointments for a set of Ids from the repository.
     *
     * @param ids IDs to query.
     * @return List of appointments for the given ids.
     */
    public LiveData<List<Appointment>> getAppointmentsForID(int[] ids) {
        mAppointments = mRepository.getAppointmentsById(ids);
        return mAppointments;
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
     *
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

    // Methods for checking constraints

    public void checkCaregiverForConflict(Appointment appointment, LifecycleOwner owner) {

        modelCallback = (AppointmentModelCallback) owner;
        caregiversByHourData = getCaregiversForHourDate(appointment.mDate, editingAppointmentId);
        caregiversByHourData.observe(owner, uuids -> {
            boolean caregiverBusy = uuids.contains(appointment.mCaregiver.uuid);

            CONFLICT_CODES code = CONFLICT_CODES.NONE;

            if (caregiverBusy)
                code = CONFLICT_CODES.CAREGIVER_BUSY;

            modelCallback.conflictResultCallback(appointment, code);
        });
    }

    /**
     * Fetches the rooms used for the given date.
     * @param date Appointment query date.
     * @return Live data of the list of rooms used in the given date.
     */
    public LiveData<List<Integer>> getTakenRooms(Date date) {
        mSpinnerData = mRepository.getUsedRoomsForDateHour(date);
        return mSpinnerData;
    }

    /**
     * Fetch caregivers ids for the selected date, excluding the given appointment
     *
     * @param date          Date
     * @param appointmentId Appointment id
     * @return List of caregivers uuids.
     */
    public LiveData<List<String>> getCaregiversForHourDate(Date date, int appointmentId) {
        return mRepository.getCaregiversForDateHour(date, appointmentId);
    }


    // Non repository based

    /**
     * Returns a list containing consecutive integers until the value set in num_rooms in the
     * integers resources file.
     *
     * @return List of integers from 1 up to num_rooms
     */
    private List<Integer> getAllRoomsList(){
        int maxRooms = getApplication().getResources().getInteger(R.integer.num_rooms);
        return IntStream.rangeClosed(1, maxRooms).boxed().collect(Collectors.toList());
    }

    /**
     * Finds the available rooms finding the difference between all rooms and the taken ones.
     * A current room is added to ensure that the appointment being edited (if applicable) is excluded from the list to avoid conflicts.
     * @param takenRooms List of taken rooms
     * @param currentRoom Room of the appointment being edited.
     * @return List of available room numbers
     */
    public List<Integer> getAvailableRooms(List<Integer> takenRooms, int currentRoom) {
        Collection totalRooms = getAllRoomsList();
        totalRooms.removeAll(takenRooms);
        List<Integer> rooms = new ArrayList<Integer>(totalRooms);
        if(currentRoom!=0) rooms.add(currentRoom);
        Collections.sort(rooms);
        return rooms;
    }
}
