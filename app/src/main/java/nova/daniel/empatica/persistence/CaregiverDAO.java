package nova.daniel.empatica.persistence;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import nova.daniel.empatica.model.Caregiver;

@Dao
public interface CaregiverDAO {

    @Query("SELECT * FROM Caregiver")
    LiveData<List<Caregiver>> getAll();

    @Query("SELECT * FROM Caregiver WHERE uuid IN (:caregiverIds)")
    LiveData<List<Caregiver>> loadAllByIds(String[] caregiverIds);
//
//    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    AppointmentEntity findByName(String first, String last);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertCaregivers(Caregiver... caregiverEntities);

    @Update
    void updateCaregivers(Caregiver... caregiverEntities);

    @Delete
    void delete(Caregiver caregiver);

    @Query("DELETE FROM Caregiver")
    void deleteAll();
}