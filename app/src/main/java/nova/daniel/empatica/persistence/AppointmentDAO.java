package nova.daniel.empatica.persistence;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import nova.daniel.empatica.model.Appointment;

@Dao
public interface AppointmentDAO {

    @Query("SELECT * FROM Appointment")
    LiveData<List<Appointment>> getAll();

    @Query("SELECT * FROM Appointment WHERE appointmentId IN (:appointmentIds)")
    LiveData<List<Appointment>> loadAllByIds(int[] appointmentIds);

    @Query("SELECT * FROM Appointment WHERE date BETWEEN :start AND :end")
    LiveData<List<Appointment>> getAppointmentsByDate(long start, long end);

//
//    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    AppointmentEntity findByName(String first, String last);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Appointment... appointments);

    @Delete
    void delete(Appointment appointment);

}
