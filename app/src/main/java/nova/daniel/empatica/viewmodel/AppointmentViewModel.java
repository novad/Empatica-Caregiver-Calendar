package nova.daniel.empatica.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.Date;
import java.util.List;

import nova.daniel.empatica.model.Appointment;
import nova.daniel.empatica.persistence.AppointmentRepository;

public class AppointmentViewModel extends AndroidViewModel {

    private AppointmentRepository mRepository;
    private LiveData<List<Appointment>> mAppointments;

    public AppointmentViewModel(@NonNull Application application){
        super(application);
        mRepository = new AppointmentRepository(application);
    }

    public LiveData<List<Appointment>> getAllAppointments(){
        mAppointments = mRepository.getAllAppointments();
        return mAppointments;
    }

    public LiveData<List<Appointment>> getAppointmentsForDate(Date date) {
        mAppointments = mRepository.getAppointmentsForDate(date);
        return mAppointments;
    }

    public void add(Appointment appointment){
        mRepository.insert(appointment);
    }

}
