package nova.daniel.empatica.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import nova.daniel.empatica.R;
import nova.daniel.empatica.model.Caregiver;
import nova.daniel.empatica.persistence.CaregiverRepository;

/**
 * ViewModel for caregivers
 */
public class CaregiverViewModel extends AndroidViewModel {

    private CaregiverRepository mRepository;
    private LiveData<List<Caregiver>> mCaregivers;

    public CaregiverViewModel(@NonNull Application application) {
        super(application);
        mRepository = new CaregiverRepository(application, true);
    }

    public LiveData<List<Caregiver>> getAll() {
        mCaregivers = mRepository.getAll();
        return mCaregivers;
    }

    // Fetch all IDs
    public LiveData<List<String>> getAllIDs() {
        return mRepository.getAllIDs();
    }

    public LiveData<List<Caregiver>> getByID(String[] caregiverIds) {
        mCaregivers = mRepository.getByID(caregiverIds);
        return mCaregivers;
    }

    /**
     * Calls the repository to fetch more caregivers. Initially fetches a page of 1.
     *
     * @param mPage Page to fetch.
     */
    public void fetchMoreCaregivers(int mPage) {
        mRepository.fetchCaregivers(mPage);
    }

    public void add(Caregiver caregiver) {
        mRepository.insert(caregiver);
    }

    /**
     * Checks if the current number of caregivers exceeds the maximum.
     * This maximum is set in the integers resource file, under api_max_results, with a default value of 100.
     *
     * @return True if there are equal or more than api_max_results entries in the repository, false if the total number is less than api_max_results
     */
    public boolean isOverLimit(){
        int max = getApplication().getResources().getInteger(R.integer.api_max_results);
        return mCaregivers.getValue() != null && mCaregivers.getValue().size() >= max;
    }
}
