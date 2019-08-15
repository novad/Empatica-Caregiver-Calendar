package nova.daniel.empatica.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import nova.daniel.empatica.R;
import nova.daniel.empatica.Utils;
import nova.daniel.empatica.api.GlideApp;
import nova.daniel.empatica.model.Appointment;
import nova.daniel.empatica.model.Caregiver;
import nova.daniel.empatica.viewmodel.AppointmentViewModel;
import nova.daniel.empatica.viewmodel.CaregiverViewModel;

/**
 * Displays the information to add or edit appointments.
 */
public class AppointmentActivity extends AppCompatActivity {

    public static final String SLOT_DATE = "SLOT_HOUR";
    public static final String APPOINTMENT_ID = "APPOINTMENT_ID";

    public boolean mUpdate = false;

    private TextView mDateTextView;
    private TextView mCarerFirstNameTextView;
    private TextView mCarerLastNameTextView;
    private ImageView mCaregiverPictureView;
    private Spinner mRoomSpinner;
    private EditText mPatientNameEditText;
    private Button mSaveButton;
    private FloatingActionButton mDeleteFab;

    CaregiverViewModel mCaregiverViewModel;
    AppointmentViewModel mAppointmentViewModel;

    private boolean isCaregiverSelected = false;

    private Date mDate;
    private Caregiver mCaregiver;
    private int mAppointmentId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slot);
        Intent intent = getIntent();

        // Get views
        mDateTextView = findViewById(R.id.slotHourtextView);
        mPatientNameEditText = findViewById(R.id.patientname_editText);
        mRoomSpinner = findViewById(R.id.roomSpinner);
        mCarerFirstNameTextView = findViewById(R.id.caregiver_firstname_textView);
        mCarerLastNameTextView = findViewById(R.id.caregiver_lastname_textView);
        mCaregiverPictureView = findViewById(R.id.caregiverlist_pic);
        mSaveButton = findViewById(R.id.saveAppointment_Button);
        mDeleteFab = findViewById(R.id.delete_fab);

        // Set up rooms
        int numRooms = getResources().getInteger(R.integer.num_rooms);
        List<Integer> rooms = IntStream.rangeClosed(1, numRooms).boxed().collect(Collectors.toList());

        // Get and display Date form the intent
        mDate = new Date(intent.getLongExtra(SLOT_DATE, 0));
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        mDateTextView.setText(formatter.format(mDate));

        // Rooms spinner
        ArrayAdapter<Integer> roomsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, rooms);
        roomsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        mRoomSpinner.setAdapter(roomsAdapter);

        // View Models for appointments and caregivers
        mCaregiverViewModel = ViewModelProviders.of(this).get(CaregiverViewModel.class);
        mAppointmentViewModel = ViewModelProviders.of(this).get(AppointmentViewModel.class);

        // If editing an existing appointment
        if (intent.hasExtra(APPOINTMENT_ID)) {
            int id = intent.getIntExtra(APPOINTMENT_ID, -1);
            if (id != -1) {
                mUpdate = true;
                setUpAppointmentViews(id);
            }
        } else {
            setUpEmptyViews();
        }
    }

    /**
     * Set up the views for creating a new appointment. Every view is set to empty/default values.
     */
    private void setUpEmptyViews() {
        // Carer info
        mCarerFirstNameTextView.setText(getApplicationContext().getString(R.string.nocaregiver));
        mCaregiverPictureView.setVisibility(View.INVISIBLE);
        mDeleteFab.hide();
    }

    /**
     * Set ups the appointment view model from an existing ID including the caregiver, and sets the view values.
     *
     * @param id ID of the appointment to edit
     */
    private void setUpAppointmentViews(int id) {
        mAppointmentViewModel.getAppointmentsForID(new int[]{id}).observe(this, appointments -> {
            if (appointments.size() > 0) {
                Appointment appointment = appointments.get(0);
                mAppointmentId = appointment.appointmentId;
                mPatientNameEditText.setText(appointment.mPatientName);
                mRoomSpinner.setSelection(appointment.mRoom - 1);
                mSaveButton.setText(getString(R.string.update_appointment));
                updateCaregiverForAppointment(appointment.mCaregiver.uuid);

                mDeleteFab.show();
            }
        });
    }

    /**
     * Starts the activity to select the caregiver.
     * Check onActivityResult after the caregiver has been selected.
     *
     * @param view View that triggered the click.
     */
    public void openCaregiversActivity(View view) {
        Intent newIntent = new Intent(this, CaregiversActivity.class);
        startActivityForResult(newIntent, 1);
    }

    /**
     * Updates the caregiver information view given the selected caregiver.
     *
     * @param caregiverId ID (uuid) of the selected caregiver.
     */
    public void updateCaregiverForAppointment(String caregiverId) {
        String[] ids = {caregiverId};

        // Set up view model for the selected caregiver and its respective views
        mCaregiverViewModel.getCaregiverByID(ids).observe(this, caregivers -> {
            mCaregiver = caregivers.get(0);

            String name = Utils.capitalizeString(mCaregiver.mFirstName);
            mCarerFirstNameTextView.setText(name);
            String lastName = Utils.capitalizeString(mCaregiver.mLastName);
            mCarerLastNameTextView.setText(lastName);

            mCaregiverPictureView.setVisibility(View.VISIBLE);

            // Set pic with Glide
            GlideApp.with(AppointmentActivity.this)
                    .load(mCaregiver.mPictureURL)
                    .placeholder(R.drawable.ic_person_outline_white_24dp)
                    .centerCrop()
                    .transform(new RoundedCornersTransformation(45, 2))
                    .into(mCaregiverPictureView);

            isCaregiverSelected = true;
        });
    }

    /**
     * Checks if the input data is valid and sets a view error for the respective view:
     * - Checks if the EditText mPatientName is not empty.
     * - Checks if a caregiver has been selected
     *
     * @return False if any input is invalid. True if all checked input is valid.
     */
    public boolean isInputValid() {
        // Check if a caregiver has been selected
        if (!isCaregiverSelected) {
            mCarerFirstNameTextView.setError("Select a caregiver from the list");
            return false;
        }

        // Check if a patient name has been set
        String inputEditText = mPatientNameEditText.getText().toString();
        if (inputEditText.isEmpty() || inputEditText.trim().length() <= 0) {
            mPatientNameEditText.setError("Patient name cannot be empty");
            return false;
        }
        return true;
    }

    /**
     * Add appointment to the database and close activity. Sets a RESULT_OK to the intent so the
     * parent activity can be updated.
     *
     * @param view Caller onClick view
     */
    public void onAddAppointment(View view) {
        /*TODO: When adding an appointment, check for room and caregiver limitations
         * Check if caregiver has been selected
         * Check if the room is available
         * Check if the caregiver can work more than week
         */
        if (!isInputValid()) {
            return;
        }

        int room = Integer.parseInt(mRoomSpinner.getSelectedItem().toString());
        String patientName = mPatientNameEditText.getText().toString().toLowerCase();

        Appointment newAppointment = new Appointment(mDate, mCaregiver, patientName, room);

        if (mUpdate) {
            newAppointment.appointmentId = mAppointmentId;
            mAppointmentViewModel.update(newAppointment);
        } else {
            mAppointmentViewModel.add(newAppointment);
        }

        this.finish();
    }

    /**
     * Delete appointment form the view model, and finish activity.
     *
     * @param view Clicked view
     */
    public void onDeleteAppointment(View view) {
        mAppointmentViewModel.delete(mAppointmentId);
        this.finish();
    }

    /**
     * Finish activity and send the respective date back to the calling activity.
     */
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra(SLOT_DATE, mDate.getTime());
        setResult(RESULT_OK, intent);
        super.finish();
    }

    /**
     * Receives result back from {@link CaregiversActivity} when it finishes.
     * If no result is given, then no updates are carried out.
     * Given a result, the views from the caregiver information are updated.
     *
     * @param requestCode Request code.
     * @param resultCode  Result Code, RESULT_OK is used to determine if a caregiver has been selected.
     * @param intent      Intent, that in the case of a RESULT_OK code, contains the extra CarCaregiversActivity.SELECTED_CAREGIVER with the selected caregiver ID
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            if (intent != null) {
                String caregiverId = intent.getStringExtra(CaregiversActivity.SELECTED_CAREGIVER);
                updateCaregiverForAppointment(caregiverId);
            }
        }
    }
}
