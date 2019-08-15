package nova.daniel.empatica.model;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Calendar;
import java.util.Date;

/**
 * Class that represents an appointment. Consisting of a patient name, room number, date, and
 * reference to the caregiver.
 * <p>
 * Marked as entity to be used directly with Room persistence library.
 */
@Entity
public class Appointment {

    @PrimaryKey(autoGenerate = true)
    public int appointmentId;
    @ColumnInfo(name = "patient_name")
    public String mPatientName;
    @ColumnInfo(name = "room_number")
    public int mRoom;
    @Embedded
    public Caregiver mCaregiver;
    @ColumnInfo(name = "date")
    public Date mDate;

    public Appointment() {
    }

    @Ignore
    public Appointment(Date date, Caregiver caregiver, String patientName, int room) {
        this.mDate = date;
        this.mCaregiver = caregiver;
        this.mPatientName = patientName;
        this.mRoom = room;
    }

    @Ignore
    public int getHour() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(mDate);
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    @Ignore
    public int getDay() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(mDate);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    @Ignore
    public int getMonth() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(mDate);
        return cal.get(Calendar.MONTH);
    }

    @Ignore
    public int getYear() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(mDate);
        return cal.get(Calendar.YEAR);
    }

    @Ignore
    public Caregiver getCaregiver() {
        return mCaregiver;
    }
}
