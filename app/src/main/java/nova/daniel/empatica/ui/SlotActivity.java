package nova.daniel.empatica.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import nova.daniel.empatica.R;
import nova.daniel.empatica.api.GlideApp;
import nova.daniel.empatica.model.Appointment;
import nova.daniel.empatica.model.Caregiver;
import nova.daniel.empatica.viewmodel.AppointmentViewModel;
import nova.daniel.empatica.viewmodel.CaregiverViewModel;

public class SlotActivity extends AppCompatActivity {

    public static final String SLOTDATE = "SLOT_HOUR";
    private TextView mCarerFirstName;
    private TextView mCarerLastName;
    private ImageView mCaregiverPicture;
    private Spinner mSpinner;
    private EditText mPatientName;

    CaregiverViewModel mCaregiverViewModel;
    AppointmentViewModel mAppointmentViewModel;

    private boolean isCaregiverSelected = false;

    private Date mDate;
    private Caregiver mCaregiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slot);
        Intent intent = getIntent();

        // Date
        Date slotDate = new Date(intent.getLongExtra(SLOTDATE, 0));
        mDate = slotDate;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        TextView reservingTextView = findViewById(R.id.slotHourtextView);
        reservingTextView.setText(formatter.format(slotDate));

        mPatientName = findViewById(R.id.patientname_editText);

        // Rooms
        int numRooms = getResources().getInteger(R.integer.num_rooms);
        List<Integer> rooms = IntStream.rangeClosed(1, numRooms).boxed().collect(Collectors.toList());

        mSpinner = findViewById(R.id.roomSpinner);
        ArrayAdapter<Integer> roomsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, rooms);
        roomsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        mSpinner.setAdapter(roomsAdapter);

        mCarerFirstName = findViewById(R.id.caregiver_firstname_textView);
        mCarerFirstName.setText(getApplicationContext().getString(R.string.nocaregiver));
        mCarerLastName = findViewById(R.id.caregiver_lastname_textView);
        mCaregiverPicture = findViewById(R.id.caregiverlist_pic);
        mCaregiverPicture.setVisibility(View.INVISIBLE);

        mCaregiverViewModel = ViewModelProviders.of(this).get(CaregiverViewModel.class);
        mAppointmentViewModel = ViewModelProviders.of(this).get(AppointmentViewModel.class);
    }

    public void openCareGiversActivity(View view){
        Intent newIntent = new Intent(this, CaregiversActivity.class);
        startActivityForResult(newIntent, 1);
    }

    public void updateCaregiverViews(String caregiverId){
        String[] ids = {caregiverId};
        mCaregiverViewModel.getCaregiverByID(ids).observe(this, new Observer<List<Caregiver>>() {
            @Override
            public void onChanged(List<Caregiver> caregivers) {
                Caregiver caregiver = caregivers.get(0);

                mCaregiver = caregiver;

                String name = String.format("%s%s", caregiver.mFirstName.substring(0, 1).toUpperCase(), caregiver.mFirstName.substring(1));
                mCarerFirstName.setText(name);
                String lastName = String.format("%s%s", caregiver.mLastName.substring(0, 1).toUpperCase(), caregiver.mLastName.substring(1));
                mCarerLastName.setText(lastName);

                mCaregiverPicture.setVisibility(View.VISIBLE);

                GlideApp.with(SlotActivity.this)
                        .load(caregiver.mPictureURL)
                        .placeholder(R.drawable.ic_person_outline_white_24dp)
                        .centerCrop()
                        .transform(new RoundedCornersTransformation(45, 2))
                        .into(mCaregiverPicture);

                isCaregiverSelected = true;
            }
        });
    }

    public void onAddAppointment(View view){
        /*TODO
        * Check if caregiver has been selected
        * Check if the room is available
        * Check if the caregiver can work more than week
        */
        if (!isCaregiverSelected){
            Toast.makeText(this, "Select a caregiver form the list", Toast.LENGTH_SHORT).show();
            return;
        }

        int room = Integer.parseInt(mSpinner.getSelectedItem().toString());
        String patientName = mPatientName.getText().toString().toLowerCase();

        Appointment newAppointment = new Appointment(mDate, mCaregiver, patientName, room);

        mAppointmentViewModel.add(newAppointment);

        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        if(resultCode == RESULT_OK){
            if (intent != null){
                String caregiverId = intent.getStringExtra(CaregiversActivity.SELECTED_CAREGIVER);
                updateCaregiverViews(caregiverId);
            }
        }
    }
}
