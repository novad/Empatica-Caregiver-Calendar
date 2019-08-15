package nova.daniel.empatica.model;

import java.util.List;

/**
 * Represents the number of appointments for a given hour.
 */
public class HourSlotModel {

    private String mHourLabel;  // Hour label
    private List<Appointment> mAppointmentsList;  // List of appointments for the set mDate

    HourSlotModel(String hourLabel, List<Appointment> slots) {
        this.mHourLabel = hourLabel;
        mAppointmentsList = slots;
    }

    public String getHourLabel() {
        return mHourLabel;
    }

    public List<Appointment> getItemArrayList() {
        return mAppointmentsList;
    }
}