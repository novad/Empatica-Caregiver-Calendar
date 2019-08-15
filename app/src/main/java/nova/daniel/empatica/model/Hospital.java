package nova.daniel.empatica.model;

import android.content.Context;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nova.daniel.empatica.viewmodel.AppointmentViewModel;

/**
 * Represents the hospital, with a set of appointments for a specific date.
 */
public class Hospital {
    private Context mContext;
    private List<HourSlotModel> mHourSlotModelList; // Appointments by hour for the given date in mCurrentDate
    private Date mCurrentDate; // date of the model

    private AppointmentViewModel mAppointmentViewModel; // Appointments ViewModel

    private OnUpdateListener modelUpdateListener;

    public Hospital(Context context, Date date, OnUpdateListener modelUpdateListener) {
        mContext = context;
        mCurrentDate = date;
        this.modelUpdateListener = modelUpdateListener;
        mAppointmentViewModel = ViewModelProviders.of((FragmentActivity) mContext).get(AppointmentViewModel.class);
    }

    /**
     * Updates the mAppointmentViewModel for the given date, fetching the respective appointments form the database.
     *
     * @param newDate New date of the model.
     */
    public void updateModelDate(Date newDate) {
        this.mCurrentDate = newDate;
        mAppointmentViewModel.getAppointmentsForDate(mCurrentDate).observe((FragmentActivity) mContext, appointments -> {
            initializeSlots(appointments);
            modelUpdateListener.updateAdapter(mHourSlotModelList);
        });
    }

    public List<HourSlotModel> getHourSlotModelArrayList() {
        return mHourSlotModelList;
    }

    /**
     * Initialize each HourSlot model for each hour of the day
     * @param appointments List of appointments
     */
    public void initializeSlots(List<Appointment> appointments) {
        mHourSlotModelList = new ArrayList<>();
        for (int i = 0; i <= 23; i++) {
            mHourSlotModelList.add(new HourSlotModel(i + ":00", getAppointmentsByHour(appointments, i)));
        }
    }

    /**
     * Appends all appointments for the given hour to a list and return it. In case no appointments
     * for that hour exist, then an empty non-null List is returned.
     * see method initializeSlots.
     *
     * @param appointments Appointments
     * @param hour         Hour of the day
     * @return List of appointments for the given hour.
     */
    private List<Appointment> getAppointmentsByHour(List<Appointment> appointments, int hour){
        List<Appointment> hourAppointments = new ArrayList<>();
        if (appointments!=null)
            for(Appointment appointment : appointments) {
                if(appointment.getHour() == hour)
                    hourAppointments.add(appointment);
            }
        return hourAppointments;
    }

    /**
     * Interface to respond to updates in the list of {@link HourSlotModel}
     */
    public interface OnUpdateListener {
        void updateAdapter(List<HourSlotModel> model);
    }
}
