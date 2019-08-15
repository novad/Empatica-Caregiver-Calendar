package nova.daniel.empatica.api;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import nova.daniel.empatica.R;
import nova.daniel.empatica.persistence.CaregiverRepository;

public class CaregiversApi {

    private static CaregiversApi instance;
    private RequestQueue requestQueue;
    private static Context mContext;

    public final static String API_URL = "https://randomuser.me/api/?seed=empatica";
    public final static String PAGE_PARAM = "page=";
    public final static String RESULTS_PARAM = "results=";

    private CaregiversApi(Context context) {
        mContext = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized CaregiversApi getInstance(Context context) {
        if (instance == null) {
            instance = new CaregiversApi(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    private static String buildURL(int page, int results){
        return API_URL + "&" + PAGE_PARAM + page + "&" + RESULTS_PARAM + results;

    }

    public static void fetchCaregivers(int page, CaregiverRepository.APICallbackListener callbackListener){
        RequestQueue queue = Volley.newRequestQueue(mContext);

        String url = buildURL(page, mContext.getResources().getInteger(R.integer.api_results));

        JsonObjectRequest caregiverJsonRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    callbackListener.resultCallback(response);

                }, error -> {
                    callbackListener.resultError();

                });

        queue.add(caregiverJsonRequest);
    }
}
