package nova.daniel.empatica.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import nova.daniel.empatica.R;
import nova.daniel.empatica.adapter.CaregiverAdapter;
import nova.daniel.empatica.model.Caregiver;
import nova.daniel.empatica.viewmodel.CaregiverViewModel;

public class CaregiversActivity extends AppCompatActivity implements CaregiverAdapter.onItemClickListener {

    public static final String SELECTED_CAREGIVER = "SELECTEDCAREGIVER";

    public RecyclerView mRecyclerView;
    public CaregiverAdapter mAdapter;

    private int mPage = 1;

    ProgressDialog progressDialog;

    CaregiverViewModel mCaregiverViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caregivers);

        progressDialog = new ProgressDialog(CaregiversActivity.this);
        progressDialog.setMessage("Loading....");
        progressDialog.show();

        mRecyclerView = findViewById(R.id.caregivers_recyclerView);
        mAdapter = new CaregiverAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplication()));

        mCaregiverViewModel = ViewModelProviders.of(this).get(CaregiverViewModel.class);
        mCaregiverViewModel.getAllCaregivers().observe(this, new Observer<List<Caregiver>>() {
            @Override
            public void onChanged(List<Caregiver> caregiverEntities) {
                if(caregiverEntities.size() != 0)
                    progressDialog.dismiss();
                mAdapter.setCaregiversList(caregiverEntities);
            }
        });
    }

    public void loadMoreOnClick(View view){
        if (mCaregiverViewModel.isOverLimit()){
            Toast.makeText(this, "Maximum allowed of fetched caregivers reached", Toast.LENGTH_SHORT).show();
            View button = findViewById(R.id.loadmore_button);
            button.setEnabled(false);
        }else {
            progressDialog.show();
            mPage += 1;
            mCaregiverViewModel.fetchMoreCaregivers(mPage);
        }
    }

    public void sortOnClick(View view){
        Toast.makeText(this, "Sorting by last name", Toast.LENGTH_SHORT).show();
        mAdapter.doSort();
    }

    @Override
    public void onItemClick(Caregiver caregiver, int position) {
        Intent intent = new Intent();
        intent.putExtra(CaregiversActivity.SELECTED_CAREGIVER, caregiver.uuid);
        setResult(RESULT_OK, intent);
        finish();
    }
}
