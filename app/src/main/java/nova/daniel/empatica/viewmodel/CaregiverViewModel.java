package nova.daniel.empatica.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import nova.daniel.empatica.R;
import nova.daniel.empatica.model.Caregiver;
import nova.daniel.empatica.persistence.CaregiverRepository;

public class CaregiverViewModel extends AndroidViewModel {

    private CaregiverRepository mRepository;

    private LiveData<List<Caregiver>> mCaregivers;

    public CaregiverViewModel(@NonNull Application application) {
        super(application);
        mRepository = new CaregiverRepository(application);
    }

    public LiveData<List<Caregiver>> getAllCaregivers(){
        mCaregivers = mRepository.getAllCaregivers();
        return mCaregivers;
    }

    public LiveData<List<Caregiver>> getCaregiverByID(String[] caregiverIds){
        mCaregivers =  mRepository.getCaregiverById(caregiverIds);
        return mCaregivers;
    }

    public void fetchMoreCaregivers(int mPage) {
        mRepository.loadCaregivers(mPage);
    }

    public void add(Caregiver caregiver){mRepository.insert(caregiver);}

    public boolean isOverLimit(){
        int max = getApplication().getResources().getInteger(R.integer.api_max_results);
        return mCaregivers.getValue().size() >= max;
    }
}
