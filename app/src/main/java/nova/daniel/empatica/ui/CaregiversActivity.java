package nova.daniel.empatica.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import nova.daniel.empatica.R;
import nova.daniel.empatica.adapter.CaregiverAdapter;
import nova.daniel.empatica.model.Caregiver;
import nova.daniel.empatica.viewmodel.CaregiverViewModel;

/**
 * Activity that displays the list of caregivers.
 * The list is populated by using the {@link CaregiverViewModel} instance.
 * A caregiver can be tapped to be selected, and its respective ID is returned in the result intent.
 */
@SuppressWarnings("deprecation")
public class CaregiversActivity extends AppCompatActivity implements CaregiverAdapter.onItemClickListener {

    // Intent names
    public static final String SELECTED_CAREGIVER = "SELECTED_CAREGIVER";

    public RecyclerView mRecyclerView;
    public CaregiverAdapter mAdapter; // mRecyclerView adapter

    private int mPage = 1; // Page of caregivers to fetch

    ProgressDialog progressDialog;

    CaregiverViewModel mCaregiverViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caregivers);

        // Show loading dialog
        progressDialog = new ProgressDialog(CaregiversActivity.this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.show();

        // Set up recycler view
        mRecyclerView = findViewById(R.id.caregivers_recyclerView);
        mAdapter = new CaregiverAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplication()));

        // Created ViewModel and update adapter in case of changes.
        mCaregiverViewModel = ViewModelProviders.of(this).get(CaregiverViewModel.class);
        mCaregiverViewModel.getAllCaregivers().observe(this, caregiverEntities -> {
            if (caregiverEntities.size() == 0) {
                Toast.makeText(this, getString(R.string.toast_no_available_caregivers), Toast.LENGTH_LONG).show();
                finish();

            }
            progressDialog.dismiss();
            mAdapter.setCaregiversList(caregiverEntities);
        });
    }

    /**
     * Call for more caregivers to be fetched from the view model depending on the current page of fetched results.
     * A maximum of 100 caregivers can be fetched, if over that limit, an error Toast is displayed,
     * and disable the "load more" button.
     *
     * @param view Clicked view
     */
    public void loadMoreOnClick(View view) {
        // Check for fetch limit and disable the "load more" button
        if (mCaregiverViewModel.isOverLimit()){
            Toast.makeText(this, getString(R.string.toast_max_caregivers_reached), Toast.LENGTH_SHORT).show();
            View button = findViewById(R.id.loadmore_button);
            button.setEnabled(false);
        } else {
            progressDialog.show();
            mPage += 1;
            mCaregiverViewModel.fetchMoreCaregivers(mPage);
        }
    }

    /**
     * Calls the adapter to sort the list of caregivers.
     * @param view Clicked View
     */
    public void sortOnClick(View view){
        Toast.makeText(this, getString(R.string.toast_sortingname), Toast.LENGTH_SHORT).show();
        mAdapter.doSort();
    }


    /**
     * Implementation of the listener when a single Caregiver is clicked.
     * Sends a result intent containing the selected caregiver's ID and then finishes the activity.
     * @param caregiver Chosen caregiver
     * @param position Position of the selected item
     */
    @Override
    public void onCaregiverClick(Caregiver caregiver, int position) {
        Intent intent = new Intent();
        intent.putExtra(CaregiversActivity.SELECTED_CAREGIVER, caregiver.uuid);
        setResult(RESULT_OK, intent);
        finish();
    }
}
