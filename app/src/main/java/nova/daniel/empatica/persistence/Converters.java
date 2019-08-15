package nova.daniel.empatica.persistence;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * Converter class to save Date attributes as longs in the database
 */
public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
