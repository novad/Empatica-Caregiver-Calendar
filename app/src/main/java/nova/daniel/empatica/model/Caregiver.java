package nova.daniel.empatica.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Caregiver {

    @PrimaryKey
    @NonNull
    public String uuid;

    @ColumnInfo(name = "first_name")
    public String mFirstName;

    @ColumnInfo(name = "last_name")
    public String mLastName;

    @ColumnInfo(name = "pic_url")
    public String mPictureURL;

    @Ignore
    public String getName(){
        return String.format("%s %s.", mFirstName, mLastName.charAt(0));
    }

    @Ignore
    public Caregiver(String firstName, String lastName){
        mFirstName = firstName;
        mLastName = lastName;
    }

    public Caregiver() {
    }
}
