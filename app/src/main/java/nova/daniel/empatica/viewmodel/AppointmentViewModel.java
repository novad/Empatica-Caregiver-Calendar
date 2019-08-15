package nova.daniel.empatica.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.Date;
import java.util.List;

import nova.daniel.empatica.model.Appointment;
import nova.daniel.empatica.persistence.AppointmentRepository;

/**
 * ViewModel for appointments
 */
public class AppointmentViewModel extends AndroidViewModel {

    private AppointmentRepository mRepository;
    private LiveData<List<Appointment>> mAppointments; //data set

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
}
