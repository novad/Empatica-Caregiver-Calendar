package nova.daniel.empatica.api;

import android.annotation.SuppressLint;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import nova.daniel.empatica.persistence.CaregiverRepository;

/**
 * Singleton class to retrieve caregivers data from randomuser.me API.
 * Calls are made using the Volley HTTP library.
 */
@SuppressLint("StaticFieldLeak")
public class CaregiversApi {

    private static CaregiversApi instance = null;
    private static Context mContext;

    private final static String API_URL = "https://randomuser.me/api/?seed=empatica";
    private final static String PAGE_PARAM = "page=";
    private final static String RESULTS_PARAM = "results=";

    private CaregiversApi(Context context) {
        mContext = context;
    }

    public static synchronized void getInstance(Context context) {
        if (instance == null) {
            instance = new CaregiversApi(context);
        } else {
            mContext = context;
        }
    }

    /**
     * Concatenates the API_URL and page and results parameters into a single URL.
     * For example, for a value page=3, results=5, the resulting url is:
     * https://randomuser.me/api/?seed=empatica&page=%3Cpage%3E&results=5
     *
     * @param page    Page to fetch
     * @param results Results to fetch
     * @return Complete URL
     */
    private static String buildURL(int page, int results){
        return API_URL + "&" + PAGE_PARAM + page + "&" + RESULTS_PARAM + results;
    }

    /**
     * Sends a JsonObjectRequest to fetch the caregivers list from API_URL and sends result to the given callback listener.
     *
     * @param page             Page number to fetch.
     * @param callbackListener Result listener.
     */
    public static void fetchCaregivers(int page, int results, CaregiverRepository.APICallbackListener callbackListener) {
        RequestQueue queue = Volley.newRequestQueue(mContext);

        // Build complete URL, the default number of results is set in api_results in the integer resources.
        String url = buildURL(page, results);

        // Build json request
        JsonObjectRequest caregiverJsonRequest = new JsonObjectRequest
                (Request.Method.GET, url, null,
                        callbackListener::resultCallback,
                        error -> callbackListener.resultError());

        // Queue request
        queue.add(caregiverJsonRequest);
    }
}
