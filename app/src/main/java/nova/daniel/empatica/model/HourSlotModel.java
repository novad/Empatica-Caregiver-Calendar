package nova.daniel.empatica.model;

import java.util.List;

/**
 * Represents the associated appointments for a given hour.
 */
public class HourSlotModel {

    private String mHourLabel;  // Hour label
    private List<Appointment> mAppointmentsList;  // List of appointments for the set mDate

    /**
     * @param hourLabel    Hour label for the slot, e.g "9:00"
     * @param appointments List of appointments
     */
    HourSlotModel(String hourLabel, List<Appointment> appointments) {
        this.mHourLabel = hourLabel;
        mAppointmentsList = appointments;
    }

    public String getHourLabel() {
        return mHourLabel;
    }

    public List<Appointment> getItemArrayList() {
        return mAppointmentsList;
    }
}