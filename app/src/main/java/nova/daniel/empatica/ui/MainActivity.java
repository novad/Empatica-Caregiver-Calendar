package nova.daniel.empatica.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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

import nova.daniel.empatica.R;
import nova.daniel.empatica.adapter.HoursViewAdapter;
import nova.daniel.empatica.adapter.SlotViewAdapter;
import nova.daniel.empatica.model.Appointment;
import nova.daniel.empatica.model.Hospital;
import nova.daniel.empatica.model.HourSlotModel;

public class MainActivity extends AppCompatActivity implements HorizontalCalendarListener,
        HoursViewAdapter.NewSlotClickListener, SlotViewAdapter.SlotClickListener, Hospital.OnUpdateListener{

    RecyclerView hoursRecyclerView;
    HoursViewAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    TextView mMonthTextView;
    HorizontalCalendarView mCalendarView;

    DayDateMonthYearModel mCurrentDayModel;
    Date mSelectedDate;

    Hospital mHospitalModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", 1)
                        .setAction("Action", null).show();
            }
        });

        hoursRecyclerView = findViewById(R.id.hoursRecyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        hoursRecyclerView.setLayoutManager(mLayoutManager);

        //Setting up calendar view
        mCalendarView = findViewById(R.id.horizontalcalendarview);
        mCalendarView.setContext(this);
        mMonthTextView = findViewById(R.id.month);
        setCurrentDayModel();
        updateMonthOnScroll(mCurrentDayModel);

        mHospitalModel = new Hospital(this, mSelectedDate, this);
        mHospitalModel.initializeSlots(null);

        //set up adapter
        mAdapter = new HoursViewAdapter(this,
                mHospitalModel.getHourSlotModelArrayList(),
                this, this);
        hoursRecyclerView.setAdapter(mAdapter);
    }

    // Calendar methods
    @Override
    public void updateMonthOnScroll(DayDateMonthYearModel selectedDate) {
        mMonthTextView.setText(String.format("%s %s", selectedDate.month, selectedDate.year));
        mCurrentDayModel = selectedDate;
    }

    @Override
    public void newDateSelected(DayDateMonthYearModel selectedDate) {
        mCurrentDayModel = selectedDate;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Integer.parseInt(mCurrentDayModel.year));
        cal.set(Calendar.MONTH, Integer.parseInt(mCurrentDayModel.monthNumeric) - 1);
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mCurrentDayModel.date));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);

        mSelectedDate = cal.getTime();
    }


    // Listeners
    @Override
    public void onNewSlotClick(int hour) {
        Toast.makeText(getApplicationContext(), "New slot at " + hour, Toast.LENGTH_SHORT).show();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Integer.parseInt(mCurrentDayModel.year));
        cal.set(Calendar.MONTH, Integer.parseInt(mCurrentDayModel.monthNumeric) - 1);
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mCurrentDayModel.date));
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, 0);

        Date slotDate = cal.getTime();

//        Appointment newAppointment = new Appointment(slotDate,
//                new Caregiver("Caregiver", "coso"),
//                "Patient peppini", 666);
//
//        HourSlotModel modifiedModel = mHospitalModel.getHourSlotModelArrayList().get(hour);
//        modifiedModel.addSlot(newAppointment);
//        mAdapter.notifySlotChanged(hour);

        Intent newIntent = new Intent(this, SlotActivity.class);
        newIntent.putExtra(SlotActivity.SLOTDATE, slotDate.getTime());
        startActivity(newIntent);
    }
    @Override
    public void onSlotClick(int position) {
        Toast.makeText(getApplicationContext(), "Editing slot #" + position, Toast.LENGTH_SHORT).show();
//        Intent newIntent = new Intent(getActivity(), SlotActivity.class);
//        newIntent.putExtra(SlotActivity.SLOTDATE, hour);
//        startActivity(newIntent);
    }


    /**
     * User to set the current date model during onCreate
     */
    private void setCurrentDayModel(){
        Date date = new Date();
        mSelectedDate = date;
        DateFormat dateFormat;
        dateFormat = new SimpleDateFormat("MMMM-EEE-yyyy-MM-dd");

        mCurrentDayModel = new DayDateMonthYearModel();
        String currentDate= dateFormat.format(date).toString();
        String[] parts = currentDate.split(" ");
        String[] partsDate = currentDate.split("-");
        mCurrentDayModel.month = partsDate[0];
        mCurrentDayModel.date = partsDate[4];
        mCurrentDayModel.day = partsDate[1];
        mCurrentDayModel.year = partsDate[2];
        mCurrentDayModel.monthNumeric = partsDate[3];
    }


    /**
     * Gets called when done adding an appointment
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        if(resultCode == RESULT_OK){
            if (intent != null){
                // TODO reload
//                String caregiverId = intent.getStringExtra(CaregiversActivity.SELECTED_CAREGIVER);
//                updateCaregiverViews(caregiverId);
            }
        }
    }


    @Override
    public void updateAdapter(List<HourSlotModel> model) {
        mAdapter.notifySlotChanged(model);
    }
}
