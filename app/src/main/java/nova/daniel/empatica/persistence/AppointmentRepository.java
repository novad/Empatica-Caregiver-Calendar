package nova.daniel.empatica.persistence;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import nova.daniel.empatica.model.Appointment;
import nova.daniel.empatica.persistence.dao.AppointmentDAO;

/**
 * Deals with local persistence calls for {@link Appointment} objects.
 */
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

    public LiveData<List<Appointment>> getAppointmentsById(int[] ids) {
        mAppointments = mAppointmentDAO.getAllAllByIds(ids);
        return mAppointments;
    }

    /**
     * Gets all appointments for a given day (between 0:00 - 23:59).
     * @param date Date of appointments to fetch.
     * @return List of appointments for the given date.
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


    /**
     * Gets room numbers of appointments scheduled for a specific hour of the day
     * @param date Date of the appointments to query
     * @return List of rooms for the given date
     */
    public LiveData<List<Integer>> getUsedRoomsForDateHour(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MINUTE, 0);
        long start = cal.getTime().getTime();

        cal.set(Calendar.MINUTE, 59);
        long end = cal.getTime().getTime();

        return mAppointmentDAO.getAppointmentRoomsByDate(start, end);
    }


    /**
     * Gets ids of caregivers associated with appointments scheduled for a specific hour of the day
     *
     * @param date Date of the appointments to query
     * @return List of caregivers ids for the given date
     */
    public LiveData<List<String>> getCaregiversForDateHour(Date date, Appointment appointment) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MINUTE, 0);
        long start = cal.getTime().getTime();

        cal.set(Calendar.MINUTE, 59);
        long end = cal.getTime().getTime();

        return mAppointmentDAO.getCaregiversByDate(start, end, new int[]{appointment.appointmentId});
    }


    public void insert(Appointment appointment) {
        new AppointmentRepository.insertAsyncTask(mAppointmentDAO).execute(appointment);
    }

    public void update(Appointment appointment) {
        new AppointmentRepository.updateAsyncTask(mAppointmentDAO).execute(appointment);
    }

    public void delete(Appointment appointment) {
        new AppointmentRepository.deleteAsyncTask(mAppointmentDAO).execute(appointment);
    }

    public void deleteById(int id) {
        new AppointmentRepository.deleteAsyncTask(mAppointmentDAO, id).execute();
    }

    /**
     * Async task to insert appointments into the database
     */
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

    /**
     * Async task to update appointments in the database
     */
    private static class updateAsyncTask extends AsyncTask<Appointment, Void, Void> {
        private AppointmentDAO mAsyncTaskDao;

        updateAsyncTask(AppointmentDAO dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Appointment... appointments) {
            mAsyncTaskDao.update(appointments);
            return null;
        }
    }

    /**
     * Async task to delete appointments from the database, either by object or ID, depending on the constructor used.
     */
    private static class deleteAsyncTask extends AsyncTask<Appointment, Void, Void> {
        private AppointmentDAO mAsyncTaskDao;
        private int mId = -1;

        /**
         * Constructor to delete appointments by object.
         *
         * @param dao appointments DAO
         */
        deleteAsyncTask(AppointmentDAO dao) {
            mAsyncTaskDao = dao;
        }

        /**
         * Constructor to delete appointments by ID.
         *
         * @param dao appointments DAO.
         * @param id  Id of the appointment to delete.
         */
        deleteAsyncTask(AppointmentDAO dao, int id) {
            mId = id;
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Appointment... appointments) {
            if (mId == -1)
                mAsyncTaskDao.delete(appointments);
            else
                mAsyncTaskDao.deleteById(mId);
            return null;
        }
    }
}
