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

/**
 * Deals with local persistence and remote API calls for {@link Caregiver}s
 */
public class CaregiverRepository {

    private Context mContext;

    private CaregiverDAO mCaregiverDAO;
    private LiveData<List<Caregiver>> mCaregivers;

    /**
     * Listeners interfaces when the API results arrive.
     */
    public interface APICallbackListener {
        void resultCallback(JSONObject response);

        void resultError();
    }

    public CaregiverRepository(Application application) {
        mContext = application.getApplicationContext();
        AppDatabase db = AppDatabase.getInMemoryDatabase(application);
        mCaregiverDAO = db.caregiverDAO();

        loadCaregivers(1); // By default, on the first call fetch the first page
    }

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

    private void deleteAll() {
        new deleteAllAsyncTask(mCaregiverDAO).execute();
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

                // There is a connection and the server respond, then invalidate the cache we saved in the database
                deleteAll();

                try {
                    JSONArray results = response.getJSONArray("results");

                    for(int i = 0; i < results.length(); i++){
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
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void resultError() {
                System.out.println("CaregiversRepository: Error loading caregivers form API, using local database");
            }
        });
    }

    /**
     * Async task to insert caregivers into the database
     */
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

    /**
     * Async Task to delete all caregivers from the database
     */
    private static class deleteAllAsyncTask extends AsyncTask<Void, Void, Void> {

        private CaregiverDAO mAsyncTaskDao;

        deleteAllAsyncTask(CaregiverDAO dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mAsyncTaskDao.deleteAll();
            return null;
        }
    }
}
