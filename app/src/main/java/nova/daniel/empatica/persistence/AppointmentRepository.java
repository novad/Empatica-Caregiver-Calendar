package nova.daniel.empatica.persistence;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import nova.daniel.empatica.model.Appointment;
import nova.daniel.empatica.model.Caregiver;

public class AppointmentRepository {
    private Context mContext;
    private AppointmentDAO mAppointmentDAO;
    private LiveData<List<Appointment>> mAppointments;

    public AppointmentRepository(Application application){
        mContext = application.getApplicationContext();
        AppDatabase db = AppDatabase.getInMemoryDatabase(application);
        mAppointmentDAO = db.appointmentDAO();
    }
    public LiveData<List<Appointment>> getAllAppointments(){
        mAppointments = mAppointmentDAO.getAll();
        return mAppointments;
    }

    /**
     * Gets all appointments for a given day (between 0:00 - 23:59)
     * @param date
     * @return
     */
    public LiveData<List<Appointment>> getAppointmentsForDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        long start = cal.getTime().getTime();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        long end = cal.getTime().getTime();

        mAppointments = mAppointmentDAO.getAppointmentsByDate(start, end);
        return mAppointments;
    }

    public void insert(Appointment appointment) {
        new AppointmentRepository.insertAsyncTask(mAppointmentDAO).execute(appointment);
    }

    public interface APICallbackListener{
        public void resultCallback(JSONObject response);
        public void resultError();
    }

    private static class insertAsyncTask extends AsyncTask<Appointment, Void, Void> {

        private AppointmentDAO mAsyncTaskDao;

        insertAsyncTask(AppointmentDAO dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Appointment... params) {
            mAsyncTaskDao.insertAll(params);
            return null;
        }
    }
}
