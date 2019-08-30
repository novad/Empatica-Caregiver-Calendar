package nova.daniel.empatica.persistence;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.Date;
import java.util.List;

import nova.daniel.empatica.Utils;
import nova.daniel.empatica.model.Appointment;
import nova.daniel.empatica.persistence.dao.AppointmentDAO;

/**
 * Repository that deals with local persistence calls for {@link Appointment} objects.
 */
public class AppointmentRepository {

    private AppointmentDAO mAppointmentDAO;
    private LiveData<List<Appointment>> mAppointments;

    public AppointmentRepository(Context context) {
        AppDatabase db = AppDatabase.getInMemoryDatabase(context);
        mAppointmentDAO = db.appointmentDAO();
    }

    public LiveData<List<Appointment>> getAll() {
        mAppointments = mAppointmentDAO.getAll();
        return mAppointments;
    }

    public LiveData<List<Appointment>> getByID(int[] ids) {
        mAppointments = mAppointmentDAO.getAllByIds(ids);
        return mAppointments;
    }

    public void insert(Appointment appointment) {
        new AppointmentRepository.insertAsyncTask(mAppointmentDAO).execute(appointment);
    }

    // Synchronous calls of insert
    public void insertSync(Appointment appointment) {
        mAppointmentDAO.insertAll(appointment);
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
     * Get all appointments from a given epoch time range
     *
     * @param start Start epoch time
     * @param end   End epoch tome
     * @return List of appointments in the given range
     */
    public LiveData<List<Appointment>> getForDate(long start, long end) {
        mAppointments = mAppointmentDAO.getByDate(start, end);
        return mAppointments;
    }


    /**
     * Gets room numbers of appointments scheduled for a epoch time range
     *
     * @param start Start time to query
     * @param end   End time to query
     * @return List of rooms for the given date
     */
    public LiveData<List<Integer>> getUsedRoomsForTimeRange(long start, long end) {
        return mAppointmentDAO.getRoomsByDate(start, end);
    }

    /**
     * Gets ids of caregivers associated with appointments scheduled for a the given hour.
     * The given appointment id is excluded from the query.
     *
     * @param date          Target date
     * @param appointmentId Appointment ID to exclude.
     * @return List of caregivers ids for the given date
     */
    public LiveData<List<String>> getCaregiversForHour(Date date, int appointmentId) {
        long start = Utils.removeMinutesSecondsAndMillis(date).getTime(); // set minute to 0 for the current hour
        long end = Utils.getHourEnd(date).getTime();

        return mAppointmentDAO.getCaregiversByDate(start, end, new int[]{appointmentId});
    }

    /**
     * Synchronous call for getCaregiversForHour. Gets ids of caregivers associated with appointments scheduled for a the given hour.
     * The given appointment id is excluded from the query.
     *
     * @param date Target date
     * @return List of caregivers ids for the given date
     */
    public List<String> getCaregiversForHourSync(Date date) {
        long start = Utils.removeMinutesSecondsAndMillis(date).getTime(); // set minute to 0 for the current hour
        long end = Utils.getHourEnd(date).getTime();

        return mAppointmentDAO.getCaregiversByDateSync(start, end);
    }

    /**
     * Counts how many appointments have been assigned to the given caregiver in during the week in the given date.
     *
     * @param caregiverId id of the caregiver
     * @return Number of times the given caregiver is assigned to appointments in the given week.
     */
    public LiveData<Integer> countCaregiverSlotsForWeek(Date date, String caregiverId) {
        Date start = Utils.getStartOfWeek(date);
        Date end = Utils.advanceWeek(start);
        return mAppointmentDAO.countByCaregiverForDate(start.getTime(), end.getTime(), caregiverId);
    }

    /**
     * Sync impl of countCaregiverSlotsForWeek
     * Counts how many appointments have been assigned to the given caregiver in during the week in the given date.
     *
     * @param caregiverId id of the caregiver
     * @return Number of times the given caregiver is assigned to appointments in the given week.
     */
    public int countCaregiverSlotsForWeekSync(Date date, String caregiverId) {
        Date start = Utils.getStartOfWeek(date);
        Date end = Utils.advanceWeek(start);
        return mAppointmentDAO.countByCaregiverForDateSync(start.getTime(), end.getTime(), caregiverId);
    }

    /**
     * Synchronously gets the list of room numbers that have been assigned to a single caregiver in a given day.
     *
     * @param date        Target date
     * @param caregiverId Caregiver to query
     * @return List of room numbers
     */
    public List<Integer> getRoomsForCaregiverByDay(Date date, String caregiverId) {
        Date start = Utils.getDayStart(date);
        Date end = Utils.getDayEnd(date);
        return mAppointmentDAO.getRoomsForCaregiverByDate(start.getTime(), end.getTime(), caregiverId);
    }

    /**
     * Synchronously gets the list of room numbers that have been assigned in a given hour.
     *
     * @param date Target date
     * @return List of room numbers
     */
    public List<Integer> getRoomsForCaregiverByHour(Date date) {
        long start = Utils.removeMinutesSecondsAndMillis(date).getTime(); // set minute to 0 for the current hour
        long end = Utils.getHourEnd(date).getTime();
        return mAppointmentDAO.getRoomsByDateSync(start, end);
    }

    /**
     * Async task to insert {@link Appointment} objects into the database
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
     * Async task to update {@link Appointment} objects in the database
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
     * Async task to delete {@link Appointment} objects from the database, either by object or ID, depending on the constructor used.
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
