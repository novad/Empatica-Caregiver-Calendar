package nova.daniel.empatica.persistence.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import nova.daniel.empatica.model.Appointment;

/**
 * Direct access object for {@link Appointment} entities
 */
@Dao
public interface AppointmentDAO {

    @Query("SELECT * FROM Appointment")
    LiveData<List<Appointment>> getAll();

    @Query("SELECT * FROM Appointment WHERE appointmentId IN (:appointmentIds)")
    LiveData<List<Appointment>> getAllAllByIds(int[] appointmentIds);

    @Query("SELECT * FROM Appointment WHERE date BETWEEN :start AND :end")
    LiveData<List<Appointment>> getAppointmentsByDate(long start, long end);

    @Query("SELECT room_number FROM Appointment WHERE date BETWEEN :start AND :end")
    LiveData<List<Integer>> getAppointmentRoomsByDate(long start, long end);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Appointment... appointments);

    @Update
    void update(Appointment... appointments);

    @Delete
    void delete(Appointment... appointment);

    @Query("DELETE FROM Appointment WHERE appointmentId = :id")
    void deleteById(int id);

}
