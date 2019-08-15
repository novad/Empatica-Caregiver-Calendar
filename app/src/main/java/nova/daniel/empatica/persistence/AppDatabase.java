package nova.daniel.empatica.persistence;


import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import nova.daniel.empatica.model.Appointment;
import nova.daniel.empatica.model.Caregiver;

/**
 * Room class to persist {@link Appointment} and {@link Caregiver} objects.
 */
@Database(entities = {Appointment.class, Caregiver.class}, version = 1,
        exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    // DAOs
    public abstract AppointmentDAO appointmentDAO();

    public abstract CaregiverDAO caregiverDAO();

    static AppDatabase getInMemoryDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                INSTANCE =
                        Room.databaseBuilder(
                                context.getApplicationContext(),
                                AppDatabase.class,
                                "caregivers_db")
                                .fallbackToDestructiveMigration()
                                .addCallback(dbCallback)
                                .build();
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    private static AppDatabase.Callback dbCallback = new RoomDatabase.Callback(){
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsync(INSTANCE).execute();
        }
    };

    /**
     * Method to populate the database
     */
    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final CaregiverDAO mDao;

        PopulateDbAsync(AppDatabase db) {
            mDao = db.caregiverDAO();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            //TODO
            mDao.deleteAll();
            return null;
        }
    }

}
