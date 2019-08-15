package nova.daniel.empatica.model;

import android.content.Context;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nova.daniel.empatica.ui.MainActivity;
import nova.daniel.empatica.viewmodel.AppointmentViewModel;

public class Hospital {

    private ArrayList<HourSlotModel> mHourSlotModelArrayList;
    private Date mCurrentDate;

    private Context mContext;

    AppointmentViewModel mAppointmentViewModel;

    OnUpdateListener listener;

    public Hospital(Context context, Date date, OnUpdateListener listener){
        mContext = context;
        mCurrentDate = date;
        this.listener = listener;

        mAppointmentViewModel = ViewModelProviders.of((FragmentActivity) mContext).get(AppointmentViewModel.class);
        mAppointmentViewModel.getAppointmentsForDate(date).observe((FragmentActivity) mContext, new Observer<List<Appointment>>() {
            @Override
            public void onChanged(List<Appointment> appointments) {
                System.out.println("hewwo");
                initializeSlots(appointments);
                listener.updateAdapter(mHourSlotModelArrayList);

            }
        });

    }

    public ArrayList<HourSlotModel> getHourSlotModelArrayList() {
        return mHourSlotModelArrayList;
    }

    public void initializeSlots(List<Appointment> appointments){
        mHourSlotModelArrayList = new ArrayList<>();
        //for loop for sections
        for (int i = 0; i <= 23; i++) {
            mHourSlotModelArrayList.add(new HourSlotModel(
                    i + ":00", mCurrentDate,
                    getAppointmentsByHour(appointments, i)));
        }
    }

    public List<Appointment> getAppointmentsByHour(List<Appointment> appointments, int hour){
        List<Appointment> hourAppointments = new ArrayList<>();
        if (appointments!=null)
            for(Appointment appointment : appointments) {
                if(appointment.getHour() == hour)
                    hourAppointments.add(appointment);
            }

        return hourAppointments;
    }

    public interface OnUpdateListener{
        public void updateAdapter(List<HourSlotModel> model);
    }
}
