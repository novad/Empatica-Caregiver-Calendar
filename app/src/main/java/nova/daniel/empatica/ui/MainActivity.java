package nova.daniel.empatica.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.view.calender.horizontal.umar.horizontalcalendarview.DayDateMonthYearModel;
import com.view.calender.horizontal.umar.horizontalcalendarview.HorizontalCalendarListener;
import com.view.calender.horizontal.umar.horizontalcalendarview.HorizontalCalendarView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import nova.daniel.empatica.R;
import nova.daniel.empatica.Utils;
import nova.daniel.empatica.adapter.AppointmentViewAdapter;
import nova.daniel.empatica.adapter.HoursViewAdapter;
import nova.daniel.empatica.model.Hospital;
import nova.daniel.empatica.model.HourSlotModel;

/**
 * Activity that displays the calendar with the appointments displayed by date.
 */
public class MainActivity extends AppCompatActivity
        implements HorizontalCalendarListener, HoursViewAdapter.NewAppointmentClickListener,
        AppointmentViewAdapter.SlotClickListener, Hospital.OnUpdateListener {

    // Adapter to contain the views for every hour of the day
    RecyclerView hoursRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    HoursViewAdapter mAdapter;

    // Displays the current month
    TextView mMonthTextView;
    // Calendar View
    HorizontalCalendarView mCalendarView;

    // Date attributes
    DayDateMonthYearModel mCurrentDayModel; // Attribute returned by HorizontalCalendarView callbacks
    Date mSelectedDate;

    // Hospital model, containing all appointments by the current mSelectedDate
    Hospital mHospitalModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSelectedDate = new Date(); // Current date

        // Fab to trigger the auto-fit caregiver's appointment slots
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view ->
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show());

        // Get views
        hoursRecyclerView = findViewById(R.id.hoursRecyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        hoursRecyclerView.setLayoutManager(mLayoutManager);

        //Setting up calendar view
        mCalendarView = findViewById(R.id.horizontalcalendarview);
        mCalendarView.setContext(this);
        mMonthTextView = findViewById(R.id.month);
        setCurrentDayModel(mSelectedDate);
        updateMonthOnScroll(mCurrentDayModel);

        // Initialize Hospital model
        mHospitalModel = new Hospital(this, mSelectedDate, this);
        mHospitalModel.initializeSlots(null);

        // Set up adapter for hoursRecyclerView
        mAdapter = new HoursViewAdapter(this,
                mHospitalModel.getHourSlotModelArrayList(),
                this, this);
        hoursRecyclerView.setAdapter(mAdapter);
    }

    // Calendar methods, required by the library
    @Override
    public void updateMonthOnScroll(DayDateMonthYearModel selectedDate) {
        mMonthTextView.setText(String.format("%s %s", selectedDate.month, selectedDate.year));
        mCurrentDayModel = selectedDate;
    }

    @Override
    public void newDateSelected(DayDateMonthYearModel selectedDate) {
        mCurrentDayModel = selectedDate;
        mSelectedDate = getCurrentDate(0);
        mHospitalModel.updateModelDate(mSelectedDate);
    }

    // Listeners

    /**
     * Listener to create a new Appointment for the given hour of the day.
     * Starts a {@link SlotActivity}.
     *
     * @param hour Hour of the day
     */
    @Override
    public void onNewSlotClick(int hour) {
        Date slotDate = getCurrentDate(hour);

        Intent newIntent = new Intent(this, SlotActivity.class);
        newIntent.putExtra(SlotActivity.SLOTDATE, slotDate.getTime());
        startActivityForResult(newIntent, 1);
    }

    /**
     * Listener to open an {@link SlotActivity} activity to edit the appointment details.
     * By passing SlotActivity.APPOINTMENT_ID the activity is auto-filled with the appointment details.
     * @param appointmentID ID of the appointment to edit.
     * @param date Date of the appointment to edit.
     */
    @Override
    public void onSlotClick(int appointmentID, long date) {
        Intent newIntent = new Intent(this, SlotActivity.class);
        newIntent.putExtra(SlotActivity.APPOINTMENT_ID, appointmentID);
        newIntent.putExtra(SlotActivity.SLOTDATE, date);
        startActivityForResult(newIntent, 1);
    }

    /**
     * Set the current date model during onCreate and by the other activity callbacks
     * @param date New current date
     */
    private void setCurrentDayModel(Date date){
        mSelectedDate = date;
        DateFormat dateFormat;
        dateFormat = new SimpleDateFormat("MMMM-EEE-yyyy-MM-dd", Locale.getDefault());

        mCurrentDayModel = new DayDateMonthYearModel();
        String currentDate = dateFormat.format(date);
        String[] partsDate = currentDate.split("-");
        mCurrentDayModel.month = partsDate[0];
        mCurrentDayModel.date = partsDate[4];
        mCurrentDayModel.day = partsDate[1];
        mCurrentDayModel.year = partsDate[2];
        mCurrentDayModel.monthNumeric = partsDate[3];
    }

    /**
     * Get the current Date of the selected date.
     *
     * @param hour Hour of the day.
     * @return Unix time of the new date.
     */
    private Date getCurrentDate(int hour) {
        // Create a Calendar instance from the mCurrentDayModel and return its Unix time,
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Integer.parseInt(mCurrentDayModel.year));
        cal.set(Calendar.MONTH, Utils.getNumericMonth(mCurrentDayModel.month)); // bug on the calendar library FIXME
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mCurrentDayModel.date));
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    /**
     * Implemented interface to update the adapters of the recycler views.
     *
     * @param model New model
     */
    @Override
    public void updateAdapter(List<HourSlotModel> model) {
        mAdapter.notifyModelChanged(model);
    }

    /**
     * Gets called when done adding an appointment.
     *
     * We need the date of the appointment back from the child activity for two reasons:
     *  - In case the parent has to be recreated so it won't jump to the system's current day.
     *  - The HorizontalCalendarView callbacks can be buggy and jump back to the current system's date.
     *
     *  This guarantees that the user sees the day of the recently added/edited appointment
     *
     * @param requestCode Request code RESULT_OK if a response is returned
     * @param resultCode Result code
     * @param intent Intent containing the date of the new/edited slot.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        if(resultCode == RESULT_OK){
            if (intent != null) {
                mSelectedDate = new Date(intent.getLongExtra(SlotActivity.SLOTDATE, mSelectedDate.getTime()));
                setCurrentDayModel(mSelectedDate);
                newDateSelected(mCurrentDayModel);
            }
        }
    }
}
