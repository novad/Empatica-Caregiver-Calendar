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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Appointment... appointments);

    @Update
    void update(Appointment... appointments);

    @Delete
    void delete(Appointment... appointment);

    @Query("DELETE FROM Appointment")
    void deleteAll();

    @Query("DELETE FROM Appointment WHERE appointmentId = :id")
    void deleteById(int id);

    @Query("SELECT * FROM Appointment")
    LiveData<List<Appointment>> getAll();

    @Query("SELECT * FROM Appointment WHERE appointmentId IN (:appointmentIds)")
    LiveData<List<Appointment>> getAllByIds(int[] appointmentIds);

    @Query("SELECT * FROM Appointment WHERE date BETWEEN :start AND :end ORDER BY room_number")
    LiveData<List<Appointment>> getByDate(long start, long end);

    @Query("SELECT room_number FROM Appointment WHERE date BETWEEN :start AND :end")
    LiveData<List<Integer>> getRoomsByDate(long start, long end);

    @Query("SELECT room_number FROM Appointment WHERE date BETWEEN :start AND :end")
    List<Integer> getRoomsByDateSync(long start, long end);

    @Query("SELECT room_number FROM Appointment WHERE uuid=:caregiverId AND date BETWEEN :start AND :end")
    List<Integer> getRoomsForCaregiverByDate(long start, long end, String caregiverId);

    //    Get caregivers for appointments in a given date range, with the option to exclude certain ids
    @Query("SELECT uuid FROM Appointment WHERE " +
            "appointmentId NOT IN (:appointmentIds) " +
            "AND  date BETWEEN :start AND :end")
    LiveData<List<String>> getCaregiversByDate(long start, long end, int[] appointmentIds);

    @Query("SELECT uuid FROM Appointment " +
            "WHERE date BETWEEN :start AND :end")
    List<String> getCaregiversByDateSync(long start, long end);

    @Query("SELECT COUNT(*) FROM Appointment WHERE uuid=:caregiverId AND date BETWEEN :start AND :end")
    LiveData<Integer> countByCaregiverForDate(long start, long end, String caregiverId);

    @Query("SELECT COUNT(*) FROM Appointment WHERE uuid=:caregiverId AND date BETWEEN :start AND :end")
    int countByCaregiverForDateSync(long start, long end, String caregiverId);

    @Query("SELECT MIN(counts) " +
            "FROM(" +
            "SELECT uuid, COUNT(uuid) as counts " +
            "FROM Appointment " +
            "WHERE date BETWEEN :start AND :end " +
            "GROUP BY uuid " +
            "ORDER BY COUNT(*))")
    int getMinNumAppointments(long start, long end);

}
