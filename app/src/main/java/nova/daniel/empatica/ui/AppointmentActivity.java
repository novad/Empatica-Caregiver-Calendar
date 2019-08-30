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

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import nova.daniel.empatica.R;
import nova.daniel.empatica.Utils;
import nova.daniel.empatica.api.GlideApp;
import nova.daniel.empatica.model.Appointment;
import nova.daniel.empatica.model.Caregiver;
import nova.daniel.empatica.viewmodel.AppointmentViewModel;
import nova.daniel.empatica.viewmodel.CaregiverViewModel;

/**
 * Activity that displays the information to add or edit appointments.
 * A user can set the patient's name, select an available room from a list, and open an {@link CaregiversActivity} to select the desired caregiver.
 *
 * Newly created appointments are saved in the repository.<p/>
 *
 * If editing an existing appointment, the ID of the appointment to edit is obtained from the Intent under the APPOINTMENT_ID extra.
 */
public class AppointmentActivity extends AppCompatActivity implements AppointmentViewModel.AppointmentModelCallback {

    public static final String SLOT_DATE = "SLOT_HOUR";
    public static final String APPOINTMENT_ID = "APPOINTMENT_ID";

    /**
     * Determines if the activity is being used to edit an existing appointment
     **/
    public boolean mUpdate = false;

    //View Models
    CaregiverViewModel mCaregiverViewModel;
    AppointmentViewModel mAppointmentViewModel;

    // Views
    private TextView mCarerFirstNameTextView;
    private TextView mCarerLastNameTextView;
    private ImageView mCaregiverPictureView;
    private Spinner mRoomSpinner;
    private EditText mPatientNameEditText;
    private Button mSaveButton;
    private FloatingActionButton mDeleteFab;

    private boolean isCaregiverSelected = false;

    /** Appointment date **/
    private Date mDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);
        Intent intent = getIntent();

        // Get views
        TextView mDateTextView = findViewById(R.id.slotHourtextView);
        mPatientNameEditText = findViewById(R.id.patientname_editText);
        mRoomSpinner = findViewById(R.id.roomSpinner);
        mCarerFirstNameTextView = findViewById(R.id.caregiver_firstname_textView);
        mCarerLastNameTextView = findViewById(R.id.caregiver_lastname_textView);
        mCaregiverPictureView = findViewById(R.id.caregiverlist_pic);
        mSaveButton = findViewById(R.id.saveAppointment_Button);
        mDeleteFab = findViewById(R.id.delete_fab);

        // Get and display Date form the intent
        mDate = new Date(intent.getLongExtra(SLOT_DATE, 0));
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        mDateTextView.setText(formatter.format(mDate));

        // View Models for appointments and caregivers
        mCaregiverViewModel = ViewModelProviders.of(this).get(CaregiverViewModel.class);
        mAppointmentViewModel = ViewModelProviders.of(this).get(AppointmentViewModel.class);

        // If editing an existing appointment an extra with the respective id
        // is obtained from the intent. Alternatively a default value -1 is set.
        int id = intent.getIntExtra(APPOINTMENT_ID, -1);
        if (intent.hasExtra(APPOINTMENT_ID)) {
            if (id != -1) {
                mUpdate = true;
                setUpAppointmentViews(id);  // set up views for editing an appointment
            }
        } else {
            setUpEmptyViews();   // set up views
            getAvailableRooms(); // find available rooms and update spinner
        }
    }

    /**
     * Clears errors before finishing the activity.
     */
    public void finish() {
        mCarerLastNameTextView.setError(null);
        super.finish();
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
     * @param id ID of the appointment to edit
     */
    private void setUpAppointmentViews(int id) {
        mAppointmentViewModel.getAppointmentsForID(new int[]{id}).observe(this, appointments -> {
            if (appointments.size() > 0) {
                Appointment appointment = appointments.get(0);
                mAppointmentViewModel.editingAppointmentId = appointment.appointmentId;
                mPatientNameEditText.setText(appointment.mPatientName);
                mSaveButton.setText(getString(R.string.update_appointment));
                mAppointmentViewModel.currentRoomNumber = appointment.mRoom;
                updateCaregiverForAppointment(appointment.mCaregiver.uuid);

                mDeleteFab.show();
                getAvailableRooms(); // we gotta wait for the respective appointment to be fetched before setting up the spinner
            }
        });
    }

    /**
     * Queries the view model to obtain the available rooms for the selected time.
     * Then calls setUpSpinnerData to set the view adapter.
     */
    public void getAvailableRooms() {
        mAppointmentViewModel.getTakenRooms(mDate).observe(this, takenRooms -> {
            List<Integer> rooms = Utils.getAvailableRooms(this, takenRooms,
                    mAppointmentViewModel.currentRoomNumber);
            setUpSpinnerData(rooms);
        });
    }

    /**
     * Sets up the spinner data for the available rooms and sets the adapter.
     *
     * @param rooms List of available room numbers
     */
    void setUpSpinnerData(List<Integer> rooms) {
        ArrayAdapter<Integer> mRoomsAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, rooms);
        mRoomsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        mRoomSpinner.setAdapter(mRoomsAdapter);

        int spinnerPosition = mRoomsAdapter.getPosition(mAppointmentViewModel.currentRoomNumber);
        mRoomSpinner.setSelection(spinnerPosition);
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
        mCaregiverViewModel.getByID(ids).observe(this, caregivers -> {
            if(caregivers != null && caregivers.size()>0) {
                Caregiver caregiver = caregivers.get(0);
                mAppointmentViewModel.newAppointmentCaregiver = caregiver;

                String name = Utils.capitalizeString(caregiver.mFirstName);
                mCarerFirstNameTextView.setText(name);
                String lastName = Utils.capitalizeString(caregiver.mLastName);
                mCarerLastNameTextView.setText(lastName);

                mCaregiverPictureView.setVisibility(View.VISIBLE);

                // Set pic with Glide
                GlideApp.with(AppointmentActivity.this)
                        .load(caregiver.mPictureURL)
                        .placeholder(R.drawable.ic_person_outline_white_24dp)
                        .centerCrop()
                        .transform(new RoundedCornersTransformation(45, 2))
                        .into(mCaregiverPictureView);

                isCaregiverSelected = true;
            }
        });
    }

    /**
     * Checks if the input data is valid and sets a view error for the respective view:
     * - Checks if a caregiver has been selected
     * @return False if any input is invalid. True if all checked input is valid.
     */
    public boolean isInputValid() {
        // Check if a caregiver has been selected
        if (!isCaregiverSelected) {
            mCarerLastNameTextView.requestFocus();
            mCarerLastNameTextView.setError(getString(R.string.error_no_caregiver_selected));
            return false;
        }

        // Check if a patient name has been set
//        String inputEditText = mPatientNameEditText.getText().toString();
//        if (inputEditText.isEmpty() || inputEditText.trim().length() <= 0) {
//            mPatientNameEditText.setError("Patient name cannot be empty");
//            return false;
//        }
        return true;
    }

    /**
     * Checks if the input is valid, and then checks if the caregiver selected can be added
     * to the new appointment. Since new queries are needed, these are sent to the view model,
     * analyzed there and trigger a callback to this activity.
     *
     * Check method conflictResultCallback for the results.
     *
     * @param view Caller onClick view
     */
    public void onAddAppointment(View view) {
        if (!isInputValid())
            return;
        mSaveButton.setEnabled(false);
        Appointment newAppointment = new Appointment(
                mDate,
                mAppointmentViewModel.newAppointmentCaregiver,              // caregiver
                mPatientNameEditText.getText().toString().toLowerCase(),    // patient name
                Integer.parseInt(mRoomSpinner.getSelectedItem().toString()) // room
        );

        // Check for conflicts
        mAppointmentViewModel.checkCaregiverForConflict(newAppointment, this);
    }


    /**
     * Callback implementation that saves the new appointment if no conflict was found.
     * Otherwise displays the respective error message.
     *
     * @param newAppointment New appointment to add
     * @param code           Result code of the conflict check, see {@link AppointmentViewModel.CONFLICT_CODES}
     */
    @Override
    public void conflictResultCallback(Appointment newAppointment, AppointmentViewModel.CONFLICT_CODES code) {

        if (code == AppointmentViewModel.CONFLICT_CODES.NONE) {
            //Two checks have to be passed, hour and weekly conflicts or caregivers
            if(mAppointmentViewModel.allChecksPassed()) {
                if (mUpdate) {
                    newAppointment.appointmentId = mAppointmentViewModel.editingAppointmentId;
                    mAppointmentViewModel.update(newAppointment);
                } else {
                    mAppointmentViewModel.add(newAppointment);
                }

                mAppointmentViewModel.caregiversByHourData.removeObservers(this);
                mAppointmentViewModel.caregiversByWeek.removeObservers(this);
                finish();
            }
        } else {
            mSaveButton.setEnabled(true);
            if (code == AppointmentViewModel.CONFLICT_CODES.CAREGIVER_BUSY) {
                mCarerLastNameTextView.requestFocus();
                mCarerLastNameTextView.setError(getString(R.string.error_busy_caregiver));
            }
            if(code == AppointmentViewModel.CONFLICT_CODES.CAREGIVER_BUSY_MAX_SLOTS){
                mCarerLastNameTextView.requestFocus();
                mCarerLastNameTextView.setError(getString(R.string.error_caregiver_max_slots_week));
            }
        }
    }

    /**
     * Delete appointment form the view model, and finish activity.
     *
     * @param view Clicked view
     */
    public void onDeleteAppointment(View view) {
        mAppointmentViewModel.delete(mAppointmentViewModel.editingAppointmentId);
        this.finish();
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
                mCarerLastNameTextView.setError(null);
                mSaveButton.setEnabled(true);
            }
        }
    }
}
