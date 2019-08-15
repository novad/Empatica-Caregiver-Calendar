package nova.daniel.empatica.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import nova.daniel.empatica.Utils;

/**
 * Class to represent each caregiver. Consisting of an id, first and last name, and a picture url.
 * The ID used is the same as the uuid returned by the API form where we fetch the caregivers.
 * <p>
 * Marked as entity to be used directly with Room persistence library.
 */
@Entity
public class Caregiver {

    @PrimaryKey
    @NonNull
    public String uuid = "1";

    @ColumnInfo(name = "first_name")
    public String mFirstName;

    @ColumnInfo(name = "last_name")
    public String mLastName;

    @ColumnInfo(name = "pic_url")
    public String mPictureURL;

    public Caregiver() {
    }

    /**
     * Returns the name of the caregiver capitalized, using only the first letter of the last name.
     *
     * @return Capitalized truncated full name.
     */
    @Ignore
    public String getName() {
        return String.format("%s %s.",
                Utils.capitalizeString(mFirstName),
                mLastName.toUpperCase().charAt(0));
    }
}
