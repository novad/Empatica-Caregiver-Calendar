package nova.daniel.empatica.model;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nova.daniel.empatica.AutoFitOperationTask;
import nova.daniel.empatica.R;
import nova.daniel.empatica.persistence.CaregiverRepository;
import nova.daniel.empatica.ui.MainActivity;
import nova.daniel.empatica.viewmodel.AppointmentViewModel;

/**
 * Represents the hospital model, with a set of appointments for a specific date.
 */
public class Hospital implements CaregiverRepository.FetchListener {

    private Context mContext;
    private List<HourSlotModel> mHourSlotModelList; // Appointments by hour for the given date in mCurrentDate
    private Date mCurrentDate; // date of the model
    private LiveData<List<Appointment>> mAppointmentLiveData;
    private AppointmentViewModel mAppointmentViewModel; // ViewModel of the Appointments

    public Hospital(Context context, Date date, OnUpdateListener modelUpdateListener) {
        mContext = context;
        mCurrentDate = date;
        this.modelUpdateListener = modelUpdateListener;
        mAppointmentViewModel = ViewModelProviders.of((MainActivity) mContext).get(AppointmentViewModel.class);
    }

    private OnUpdateListener modelUpdateListener;

    // Accessor
    public List<HourSlotModel> getHourSlotModelArrayList() {
        return mHourSlotModelList;
    }

    /**
     * Updates the mAppointmentViewModel for the given date, fetching the respective appointments form the database.
     * @param newDate New date of the model.
     */
    public void updateModelDate(Date newDate) {
        this.mCurrentDate = newDate;
        mAppointmentLiveData = mAppointmentViewModel.getAppointmentsForDate(mCurrentDate);
        mAppointmentLiveData.observe((MainActivity) mContext, appointments -> {
            if (mAppointmentLiveData != null) {
                initializeSlots(appointments);
                modelUpdateListener.updateAdapter(mHourSlotModelList);
            }
        });
    }

    /**
     * Entry point to the auto-fit caregivers functions.
     * <p>
     * Before the fitting process, the complete list of caregivers must be fetched,
     * so a {@link CaregiverRepository} instance is created.
     * The repository is initialized with a callback {@link CaregiverRepository.FetchListener},
     * so when all ca
     *
     * @param date Date
     */
    public void autoFitCaregiver(Date date) {
        this.mCurrentDate = date;
        mAppointmentLiveData.removeObservers((MainActivity) mContext);
        mAppointmentLiveData = null;
        CaregiverRepository repository = new CaregiverRepository(mContext, false);
        repository.fetchAllCaregivers(this);
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
     * Checks if there are available rooms for the given hour slot.
     * Since only one room can be assigned for each time-slot, we check if the number of appointments for the
     * given hour exceed the number of available rooms.
     * @param hour Appointment hour
     * @return True if there are rooms available, false if no rooms are available.
     */
    public boolean checkHourRoomLimits(int hour){
        String hourLabel = hour + ":00";
        int maxRooms = mContext.getResources().getInteger(R.integer.num_rooms);

        for (HourSlotModel hourSlot : mHourSlotModelList){
            if (hourSlot.getHourLabel().equalsIgnoreCase(hourLabel)){
                if (hourSlot.getItemArrayList().size() >= maxRooms)
                    return false;
            }
        }
        return true;
    }

    ///// Auto fit caregivers functionality functions /////

    /**
     * Callback from the fetchAllCaregivers function form {@link CaregiverRepository}.
     * It instantiates and run the task {@link AutoFitOperationTask}.
     */
    @Override
    public void onCompleted() {
        new AutoFitOperationTask(mContext, mCurrentDate).execute();
    }

    /**
     * Interface to respond to updates in the list of {@link HourSlotModel}
     */
    public interface OnUpdateListener {
        void updateAdapter(List<HourSlotModel> model);
    }

}
