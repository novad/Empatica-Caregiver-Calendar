package nova.daniel.empatica.persistence.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import nova.daniel.empatica.model.Caregiver;

/**
 * Direct access object for {@link Caregiver} entities
 */
@Dao
public interface CaregiverDAO {

    @Query("SELECT * FROM Caregiver")
    LiveData<List<Caregiver>> getAll();

    @Query("SELECT * FROM Caregiver WHERE uuid IN (:caregiverIds)")
    LiveData<List<Caregiver>> loadAllByIds(String[] caregiverIds);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertCaregivers(Caregiver... caregiverEntities);

    @Update
    void updateCaregivers(Caregiver... caregiverEntities);

    @Query("DELETE FROM Caregiver")
    void deleteAll();
}