package nova.daniel.empatica.model;

import java.util.Date;
import java.util.List;

public class HourSlotModel {

    private String mHourLabel;
    private List<Appointment> mSlotsList;
    private Date mDate;


    public HourSlotModel(String hourLabel, Date date, List<Appointment> slots) {
        this.mHourLabel = hourLabel;
        this.mDate = date;
        mSlotsList = slots;
    }

    public List<Appointment> getAppointmentByDate(Date date){
        System.out.println("asshole");
        return mSlotsList;
    }


    public String getHourLabel() {
        return mHourLabel;
    }

    public List<Appointment> getItemArrayList() {
        return getAppointmentByDate(mDate);
    }
}