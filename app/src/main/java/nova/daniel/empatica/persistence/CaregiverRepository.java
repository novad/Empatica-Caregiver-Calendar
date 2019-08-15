package nova.daniel.empatica.persistence;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import nova.daniel.empatica.api.CaregiversApi;
import nova.daniel.empatica.model.Caregiver;

public class CaregiverRepository {

    private Context mContext;

    private CaregiverDAO mCaregiverDAO;
    private LiveData<List<Caregiver>> mCaregivers;

    // Note that in order to unit test the repository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    public CaregiverRepository(Application application) {
        mContext = application.getApplicationContext();
        AppDatabase db = AppDatabase.getInMemoryDatabase(application);
        mCaregiverDAO = db.caregiverDAO();

        loadCaregivers(1);
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    public LiveData<List<Caregiver>> getAllCaregivers() {
        mCaregivers = mCaregiverDAO.getAll();
        return mCaregivers;
    }

    public LiveData<List<Caregiver>> getCaregiverById(String[] caregiverIds){
        mCaregivers =  mCaregiverDAO.loadAllByIds(caregiverIds);
        return mCaregivers;
    }

    public void insert(Caregiver caregiver) {
        new insertAsyncTask(mCaregiverDAO).execute(caregiver);
    }


    /**
     * Fetch caregivers from the API and update the database,
     * if API is not available only the local db is used
     */
    public void loadCaregivers(int page){
        CaregiversApi.getInstance(mContext);
        CaregiversApi.fetchCaregivers(page, new APICallbackListener() {
            @Override
            public void resultCallback(JSONObject response) {
                try {
                    JSONArray results = response.getJSONArray("results");

                    for(int i = 0 ; i < results.length(); i++){
                        JSONObject nameJSON = results.getJSONObject(i).getJSONObject("name");
                        String firstName =  nameJSON.getString("first");
                        String lastName =  nameJSON.getString("last");

                        JSONObject picJSON = results.getJSONObject(i).getJSONObject("picture");
                        String picUrl =  picJSON.getString("thumbnail");

                        JSONObject uuidJSON = results.getJSONObject(i).getJSONObject("login");
                        String uuid =  uuidJSON.getString("uuid");

                        Caregiver newCaregiver = new Caregiver();
                        newCaregiver.uuid = uuid;
                        newCaregiver.mFirstName = firstName;
                        newCaregiver.mLastName = lastName;
                        newCaregiver.mPictureURL = picUrl;

                        insert(newCaregiver);
                        System.out.printf("Caregiver %s added%n", uuid);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void resultError() {
                System.out.println("CaregiversActivity: Error loading caregivers form API, using local database");
            }
        });
    }


    public interface APICallbackListener{
        public void resultCallback(JSONObject response);
        public void resultError();
    }

    private static class insertAsyncTask extends AsyncTask<Caregiver, Void, Void> {

        private CaregiverDAO mAsyncTaskDao;

        insertAsyncTask(CaregiverDAO dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Caregiver... params) {
            mAsyncTaskDao.insertCaregivers(params[0]);
            return null;
        }
    }
}
