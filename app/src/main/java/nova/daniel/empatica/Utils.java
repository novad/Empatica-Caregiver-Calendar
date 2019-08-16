package nova.daniel.empatica;

import java.util.Calendar;
import java.util.Date;

public class Utils {
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
     * where DayDateMonthYearModel contains the right month name but not the number.
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

}
