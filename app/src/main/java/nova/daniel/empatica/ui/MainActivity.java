package nova.daniel.empatica.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import nova.daniel.empatica.AutoFitOperationTask;
import nova.daniel.empatica.R;
import nova.daniel.empatica.Utils;
import nova.daniel.empatica.adapter.AppointmentViewAdapter;
import nova.daniel.empatica.adapter.HoursViewAdapter;
import nova.daniel.empatica.model.Hospital;
import nova.daniel.empatica.model.HourSlotModel;

/**
 * Activity that displays the calendar with the appointments displayed by date.
 *
 * Displays a date picker, and a list of 1-hour time-slots for the selected date,
 * each with the caregivers assigned for each room in the hospital.
 */
@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity
        implements HoursViewAdapter.NewAppointmentClickListener,
        AppointmentViewAdapter.SlotClickListener, Hospital.OnUpdateListener,
        AutoFitOperationTask.AutoFitCallBack {

    // Adapter to contain the views for every hour of the day
    RecyclerView hoursRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    HoursViewAdapter mAdapter;

    // Calendar View
    HorizontalCalendar mCalendarView;
    Calendar startDate = Calendar.getInstance();
    Date mSelectedDate;

    // Progress dialog used when the AutoFit task is running
    ProgressDialog mProgressDialog;

    // Hospital model, containing all appointments by the current mSelectedDate
    Hospital mHospitalModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSelectedDate = new Date(); // Current date

        mProgressDialog = new ProgressDialog(this);

        // Get views
        hoursRecyclerView = findViewById(R.id.hoursRecyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        hoursRecyclerView.setLayoutManager(mLayoutManager);

        //Setting up calendar view
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH, 1);
        mCalendarView = new HorizontalCalendar.Builder(this, R.id.horizontalcalendarview)
                .range(startDate, endDate)
                .datesNumberOnScreen(5)
                .build();
        mCalendarView.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                MainActivity.this.onDateSelected(date);
            }
        });

        // Initialize Hospital model
        mHospitalModel = new Hospital(this, mSelectedDate, this);
        mHospitalModel.initializeSlots(null);

        // Set up adapter for hoursRecyclerView
        mAdapter = new HoursViewAdapter(this,
                mHospitalModel.getHourSlotModelArrayList(),
                this, this);
        hoursRecyclerView.setAdapter(mAdapter);
    }

    /* When the activity resumes, recheck the selected date. This is a temporary solution to a bug where the date of the
     * calendar view date, and mSelectedDate are not equal.*/
    @Override
    protected void onResume() {
        super.onResume();
        Calendar cal = Calendar.getInstance();
        cal.setTime(mSelectedDate != null ? mSelectedDate : new Date());
        onDateSelected(cal);
    }

    /**
     * Calls for the update of the mHospitalModel with the new given date.
     * Called when a new date is selected
     *
     * @param date new date
     */
    public void onDateSelected(Calendar date) {
        mSelectedDate = date.getTime();
        mHospitalModel.updateModelDate(mSelectedDate);
    }

    /// Listeners ///

    /**
     * Responds to clicks of the fab to start the auto-fit task.
     * @param view Calling view
     */
    public void onClickAutoFit(View view) {
        mProgressDialog.setMessage(getString(R.string.fittinINProgress));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        mHospitalModel.autoFitCaregiver(mSelectedDate);
    }

    /**
     * Listener to the creation of a new Appointment for the given hour of the day.
     * Starts a {@link AppointmentActivity}.
     *
     * @param hour Hour of the day
     */
    @Override
    public void onNewSlotClick(int hour) {
        Date slotDate = Utils.setHourDate(mSelectedDate, hour);

        if (!mHospitalModel.checkHourRoomLimits(hour)) {
            Toast.makeText(this, getString(R.string.error_no_more_rooms), Toast.LENGTH_SHORT).show();
        } else {
            Intent newIntent = new Intent(this, AppointmentActivity.class);
            newIntent.putExtra(AppointmentActivity.SLOT_DATE, slotDate.getTime());
            startActivity(newIntent);
        }
    }

    /**
     * Listener to open an {@link AppointmentActivity} activity to edit the appointment details.
     * By passing SlotActivity.APPOINTMENT_ID the activity is auto-filled with the appointment details.
     * @param appointmentID ID of the appointment to edit.
     * @param date Date of the appointment to edit.
     */
    @Override
    public void onSlotClick(int appointmentID, long date) {
        Intent newIntent = new Intent(this, AppointmentActivity.class);
        newIntent.putExtra(AppointmentActivity.APPOINTMENT_ID, appointmentID);
        newIntent.putExtra(AppointmentActivity.SLOT_DATE, date);
        startActivityForResult(newIntent, 1);
    }

    /**
     * Implemented interface to update the adapters of the recycler views.
     * @param model New model
     */
    @Override
    public void updateAdapter(List<HourSlotModel> model) {
        mAdapter.notifyModelChanged(model);
    }

    /**
     * Callback after the called {@link AppointmentActivity} activity has been closed.
     * Gets called when done adding an appointment, or the user hits back.
     * TODO: check calendar jumps
     * @param requestCode Request code RESULT_OK if a response is returned
     * @param resultCode Result code
     * @param intent Intent containing the date of the new/edited slot.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        if(resultCode == RESULT_OK){
            if (intent != null) {
//                Date date = new Date(intent.getLongExtra(AppointmentActivity.SLOT_DATE, mSelectedDate.getTime()));
//                mHospitalModel.updateModelDate(date);
//
//                setCurrentDayModel(mSelectedDate);
//                newDateSelected(mCurrentDayModel);
            }
        }
    }

    /**
     * Callback when the {@link AutoFitOperationTask} finishes executing.
     * Dismisses the progress dialog.
     *
     * @param date
     */
    @Override
    public void onFinishedAutoFit(long date) {
        mProgressDialog.dismiss();
//        mHospitalModel.updateModelDate(mSelectedDate);

//        mSelectedDate = new Date(date);
//        setCurrentDayModel(mSelectedDate);
//        newDateSelected(mCurrentDayModel);
//        mHospitalModel.updateModelDate(mSelectedDate);
    }
}
