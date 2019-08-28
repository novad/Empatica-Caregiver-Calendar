package nova.daniel.empatica.persistence.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import nova.daniel.empatica.model.Caregiver;
import nova.daniel.empatica.model.CaregiverWAppointments;
import nova.daniel.empatica.model.CountWork;

/**
 * Direct access object for {@link Caregiver} entities
 */
@Dao
public interface CaregiverDAO {

    @Query("SELECT * FROM Caregiver")
    LiveData<List<Caregiver>> getAll();

    @Query("SELECT uuid FROM Caregiver")
    LiveData<List<String>> getAllIds();

    @Query("SELECT * FROM Caregiver WHERE uuid IN (:caregiverIds)")
    LiveData<List<Caregiver>> loadAllByIds(String[] caregiverIds);

    @Query("SELECT * FROM Caregiver WHERE uuid IN (:caregiverIds)")
    List<Caregiver> loadAllByIdsSync(String[] caregiverIds);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Caregiver... caregiverEntities);

    @Update
    void updateCaregivers(Caregiver... caregiverEntities);

    @Query("DELETE FROM Caregiver")
    void deleteAll();

    @Transaction
    @Query("SELECT DISTINCT caregiver.* " +
            "FROM caregiver, appointment ")
    List<CaregiverWAppointments> getAllWAppointments();

    @Transaction
    @Query("SELECT DISTINCT caregiver.* " +
            "FROM caregiver, appointment " +
            "WHERE appointment.date BETWEEN :start AND :end ")
    List<CaregiverWAppointments> getWAppointmentsForDate(long start, long end);

    @Query("SELECT caregiver.uuid, COUNT(app_uuid) as counts " +
            "FROM caregiver LEFT JOIN " +
            "(SELECT appointment.uuid as app_uuid FROM appointment WHERE appointment.date BETWEEN :start AND :end ) " +
            "ON caregiver.uuid=app_uuid " +
            "GROUP BY caregiver.uuid " +
            "ORDER BY COUNT(app_uuid)")
    List<CountWork> getByAppointmentCountForDate(long start, long end);

}