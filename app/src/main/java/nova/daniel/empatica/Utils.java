package nova.daniel.empatica;

import android.content.Context;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Utils {

    /**
     * Returns a list containing consecutive integers until the value set in num_rooms in the
     * integers resources file.
     *
     * @return List of integers from 1 up to num_rooms
     */
    public static List<Integer> getAllRoomsList(Context context) {
        int maxRooms = context.getResources().getInteger(R.integer.num_rooms);
        return IntStream.rangeClosed(1, maxRooms).boxed().collect(Collectors.toList());
    }

    /**
     * Finds the available rooms finding the difference between all rooms and the taken ones.
     * <p>
     * A current room is added to ensure that the appointment being edited (if applicable) is excluded from the list to avoid conflicts.
     *
     * @param context     Context, needed to access the resources values
     * @param takenRooms  List of taken rooms
     * @param currentRoom Room of the appointment being edited. If non applicable set to 0.
     * @return List of available room numbers
     */
    public static List<Integer> getAvailableRooms(Context context, List<Integer> takenRooms, int currentRoom) {
        List<Integer> allRooms = getAllRoomsList(context);
        allRooms.removeAll(takenRooms);  // remove occupied rooms

        if (currentRoom != 0) allRooms.add(currentRoom);

        Collections.sort(allRooms);
        return allRooms;
    }

    public static List<Integer> getAvailableRooms(Context context, List<Integer> takenRooms) {
        return getAvailableRooms(context, takenRooms, 0);
    }


    /**
     * Capitalizes the first letter of a given string.
     *
     * @param string Source string
     * @return Result string, capitalized
     */
    public static String capitalizeString(String string) {
        return String.format("%s%s", string.substring(0, 1).toUpperCase(), string.substring(1));
    }

    /**
     * Returns the number of a month.
     * This util function exists only because there is a bug in the HorizontalCalendarView calls
     * where DayDateMonthYearModel in some cases contains the right month name but not the number.
     *
     * @param month Month string
     * @return Month number, from january=0
     */
    public static int getNumericMonth(String month) {
        month = month.toLowerCase();
        switch (month) {
            case "january":
                return 0;
            case "february":
                return 1;
            case "march":
                return 2;
            case "april":
                return 3;
            case "may":
                return 4;
            case "june":
                return 5;
            case "july":
                return 6;
            case "august":
                return 7;
            case "september":
                return 8;
            case "october":
                return 9;
            case "november":
                return 10;
            case "december":
                return 11;
            default:
                return -1;
        }
    }


    /**
     * Sets the minute, second, and millisecond of a given date to 0s-
     * @param date Target Date
     * @return Date with minute, second, and millisecond set to 0.
     */
    public static Date removeMinutesSecondsAndMillis(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    /**
     * Returns a date with a specific hour.
     *
     * @param date target date to change
     * @param hour target hour
     * @return new Date with the given hour
     */
    public static Date setHourDate(Date date, int hour) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, hour);

        return removeMinutesSecondsAndMillis(cal.getTime());
    }

    public static Date getHourEnd(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);

        return cal.getTime();
    }

    /**
     * Returns the date of the first day of the week for a given date
     *
     * @param date date
     * @return start of the week of the given date
     */
    public static Date getStartOfWeek(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        return removeMinutesSecondsAndMillis(cal.getTime());
    }

    /**
     * Returns the end of the week for a given date
     *
     * @param date date
     * @return end of the week of the given date
     */
    public static Date getEndOfWeek(Date date) {
        Calendar cal = Calendar.getInstance();
        Date startdate = getStartOfWeek(date);
        cal.setTime(startdate);
        cal.add(Calendar.DAY_OF_YEAR, 6);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);

        return cal.getTime();
    }

    /**
     * Returns the midnight of a given date. i.e. the start of the day 00:00
     *
     * @param date Date
     * @return Midnight of the given date
     */
    public static Date getDayStart(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        return removeMinutesSecondsAndMillis(cal.getTime());
    }

    /**
     * Returns the last minute of the day, i.e. 23:59 of the given date
     *
     * @param date Target date
     * @return Last minute of the given date
     */
    public static Date getDayEnd(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);

        return cal.getTime();
    }

    /**
     * Returns a date a week ahead of the date parameter
     *
     * @param date date
     * @return given date plus one week
     */
    public static Date advanceWeek(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        return cal.getTime();
    }

    /**
     * Returns the date a given number of weeks before the given date.
     *
     * @param date     Target date
     * @param numWeeks Number of weeks to substract form the given date
     * @return Date with numWeeks weeks behind the given date
     */
    public static Date goBackWeeks(Date date, int numWeeks) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.WEEK_OF_YEAR, -numWeeks);
        return cal.getTime();
    }

    @SuppressWarnings("ConstantConditions")
    // warning on possible null on candidates.get(key), which it's already guaranteed to exist in the loop
    public static String getMaxScoreCandidate(Map<String, Double> candidates) {
        String bestCandidate = ""; // ID of the best candidate
        if (!candidates.isEmpty()) {
            // Take first candidate as the best
            Set<String> keys = candidates.keySet();
            bestCandidate = keys.iterator().next();
            double bestScore = candidates.get(bestCandidate);
            for (String key : keys) {
                if (candidates.get(key) > bestScore) {
                    bestCandidate = key;
                    bestScore = candidates.get(key);
                }
            }
        }
        return bestCandidate;
    }
}
