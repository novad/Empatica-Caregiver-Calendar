package nova.daniel.empatica.persistence;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

import nova.daniel.empatica.R;
import nova.daniel.empatica.Utils;
import nova.daniel.empatica.api.CaregiversApi;
import nova.daniel.empatica.model.Caregiver;
import nova.daniel.empatica.model.CaregiverWAppointments;
import nova.daniel.empatica.model.CountWork;
import nova.daniel.empatica.persistence.dao.CaregiverDAO;

/**
 * Deals with local persistence and remote API calls for {@link Caregiver}s
 */
public class CaregiverRepository {

    private Context mContext;

    private CaregiverDAO mCaregiverDAO;
    private LiveData<List<Caregiver>> mCaregivers;

    private int mResultsToFetch;
    private FetchListener mFetchListener;

    /**
     * Constructor for the caregiver repository.
     * @param context Context
     * @param initialFetch True if the repository fetches a first page of caregivers from the API, false if no initial fetching is done.
     */
    public CaregiverRepository(Context context, boolean initialFetch) {
        mContext = context;
        AppDatabase db = AppDatabase.getInMemoryDatabase(context);
        mCaregiverDAO = db.caregiverDAO();
        mResultsToFetch = mContext.getResources().getInteger(R.integer.api_results);

        if (initialFetch)
            fetchCaregivers(1); // By default, on the first call fetch the first page
    }

    public LiveData<List<Caregiver>> getAll() {
        mCaregivers = mCaregiverDAO.getAll();
        return mCaregivers;
    }

    public LiveData<Integer> countAll() {
        return mCaregiverDAO.countAll();
    }

    /**
     * Return IDs of all caregivers
     *
     * @return Caregivers IDs
     */
    public LiveData<List<String>> getAllIDs() {
        return mCaregiverDAO.getAllIds();
    }

    /**
     * Syncrhonously returns IDs of all caregivers
     *
     * @return Caregivers IDs
     */
    public List<String> getAllIDsSync() {
        return mCaregiverDAO.getAllIdsSync();
    }

    public LiveData<List<Caregiver>> getByID(String[] caregiverIds) {
        mCaregivers =  mCaregiverDAO.loadAllByIds(caregiverIds);
        return mCaregivers;
    }

    public List<Caregiver> getByIDSync(String[] caregiverIds) {
        return mCaregiverDAO.loadAllByIdsSync(caregiverIds);
    }

    public List<CaregiverWAppointments> getAllWAppointments() {
        return mCaregiverDAO.getAllWAppointments();
    }

    /**
     * Fetches the number of appointments for each caregiver for the last 4 weeks of the given date.
     * Each entry is represented as a tuple, using {@link CountWork} objects, containing the caregiver ID and appointment count.
     *
     * @return List of {@link CountWork} with appointment counts for each caregiver in the repository
     */
    public List<CountWork> getByAppointmentCountLastWeeks(Date date) {
        Date end = Utils.getDayEnd(date);
        end = Utils.getEndOfWeek(end);

        Date start = Utils.getDayStart(date);
        start = Utils.goBackWeeks(start, 4); // last four weeks
        start = Utils.getStartOfWeek(start);
        return mCaregiverDAO.getByAppointmentCountForDate(start.getTime(), end.getTime());
    }
    public void insert(Caregiver caregiver) {
        new insertAsyncTask(mCaregiverDAO).execute(caregiver);
    }

    private void deleteAll() {
        new deleteAllAsyncTask(mCaregiverDAO).execute();
    }

    /**
     * Fetches all the caregivers from the API.
     * As a default it fetches 100, this value is set in the integers resource file, under api_max_results.
     *
     * @param listener Listener implementation when the fetch is complete.
     */
    public void fetchAllCaregivers(FetchListener listener) {
        mFetchListener = listener;
        int maxResults = mContext.getResources().getInteger(R.integer.api_max_results);
        int resultsPerCall = mContext.getResources().getInteger(R.integer.api_results);

        mResultsToFetch = maxResults;
        fetchCaregivers(1);
        mResultsToFetch = resultsPerCall;
    }

    /**
     * Fetch caregivers from the API and update the database,
     * if API is not available only the local db is used.
     * <p/>
     * Once the fetch is completed, if set, the {@link FetchListener} callback implementation is called.
     */
    public void fetchCaregivers(int page){
        CaregiversApi.getInstance(mContext);
        CaregiversApi.fetchCaregivers(page, mResultsToFetch, new APICallbackListener() {
            @Override
            public void resultCallback(JSONObject response) {

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
                if (mFetchListener != null)
                    mFetchListener.onCompleted(true);
            }

            @Override
            public void resultError() {
                System.out.println("CaregiversRepository: Error loading caregivers form API, using local database");
                if (mFetchListener != null)
                    mFetchListener.onCompleted(false);
            }
        });
    }

    //todo javadoc

    /**
     * Listeners interfaces when the results of the Volley calls for the API arrive.
     */
    public interface APICallbackListener {
        void resultCallback(JSONObject response);

        void resultError();
    }

    /**
     * Listener when the caregivers have been completely fetched from the API call.
     * Can be left null in case no callback is wanted.
     * Implemented in {@link nova.daniel.empatica.model.Hospital}
     */
    public interface FetchListener {
        void onCompleted(boolean fetchSuccess);
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
            mAsyncTaskDao.insert(params[0]);
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
