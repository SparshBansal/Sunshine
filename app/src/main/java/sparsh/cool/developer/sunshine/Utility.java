package sparsh.cool.developer.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.style.MetricAffectingSpan;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import sparsh.cool.developer.sunshine.data.WeatherContract;
import sparsh.cool.developer.sunshine.sync.SyncAdapter;

/**
 * Created by Sparsha on 6/11/2015.
 */
public class Utility {

    public static final String DATE_FORMAT = "yyyyMMdd";

    public static String getPreferredLocation(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String location = sharedPreferences.getString(context.getResources().getString(R.string.KEY_LOCATION_PREFERENCE), "110006");
        return location;
    }

    public static boolean isMetric(Context context) {
        String metric = "Metric";
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String units = sharedPreferences.getString(context.getString(R.string.KEY_UNITS_PREFERENCE), metric);
        if (units.equals(metric)) {
            return true;
        } else {
            return false;
        }
    }

    public static String formatTemperature(double temperature, boolean isMetric) {
        double temp;
        if (isMetric) {
            temp = temperature;
        } else {
            temp = 9 * temperature / 5 + 32;
        }
        return String.format("%.0f", temp);
    }

    public static String formatDate(String dateText) {
        Date date = WeatherContract.WeatherEntry.getDateFromDb(dateText);
        return DateFormat.getDateInstance().format(date);
    }

    public static String getFriendlyDayString(Context context, String dateStr) {
        // The day string for forecast uses the following logic:
        // For today: "Today, June 8"
        // For tomorrow:  "Tomorrow"
        // For the next 5 days: "Wednesday" (just the day name)
        // For all days after that: "Mon Jun 8"


        Date todayDate = new Date();
        String todayStr = WeatherContract.WeatherEntry.getWeatherDbDate(todayDate);
        Date inputDate = WeatherContract.WeatherEntry.getDateFromDb(dateStr);


        // If the date we're building the String for is today's date, the format
        // is "Today, June 24"
        if (todayStr.equals(dateStr)) {
            String today = context.getString(R.string.today);
            int formatId = R.string.format_full_friendly_date;
            return String.format(context.getString(
                    formatId,
                    today,
                    getFormattedMonthDay(context, dateStr)));
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(todayDate);
            cal.add(Calendar.DATE, 7);
            String weekFutureString = WeatherContract.WeatherEntry.getWeatherDbDate(cal.getTime());


            if (dateStr.compareTo(weekFutureString) < 0) {
                // If the input date is less than a week in the future, just return the day name.
                return getDayName(context, dateStr);
            } else {
                // Otherwise, use the form "Mon Jun 3"
                SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
                return shortenedDateFormat.format(inputDate);
            }
        }
    }


    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param context Context to use for resource localization
     * @param dateStr The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return
     */
    public static String getDayName(Context context, String dateStr) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        try {
            Date inputDate = dbDateFormat.parse(dateStr);
            Date todayDate = new Date();
            // If the date is today, return the localized version of "Today" instead of the actual
            // day name.
            if (WeatherContract.WeatherEntry.getWeatherDbDate(todayDate).equals(dateStr)) {
                return context.getString(R.string.today);
            } else {
                // If the date is set for tomorrow, the format is "Tomorrow".
                Calendar cal = Calendar.getInstance();
                cal.setTime(todayDate);
                cal.add(Calendar.DATE, 1);
                Date tomorrowDate = cal.getTime();
                if (WeatherContract.WeatherEntry.getWeatherDbDate(tomorrowDate).equals(
                        dateStr)) {
                    return context.getString(R.string.tomorrow);
                } else {
                    // Otherwise, the format is just the day of the week (e.g "Wednesday".
                    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
                    return dayFormat.format(inputDate);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            // It couldn't process the date correctly.
            return "";
        }
    }


    /**
     * Converts db date format to the format "Month day", e.g "June 24".
     *
     * @param context Context to use for resource localization
     * @param dateStr The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return The day in the form of a string formatted "December 6"
     */
    public static String getFormattedMonthDay(Context context, String dateStr) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        try {
            Date inputDate = dbDateFormat.parse(dateStr);
            SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
            String monthDayString = monthDayFormat.format(inputDate);
            return monthDayString;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getIconResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.ic_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.ic_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.ic_rain;
        } else if (weatherId == 511) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.ic_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.ic_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.ic_storm;
        } else if (weatherId == 800) {
            return R.drawable.ic_clear;
        } else if (weatherId == 801) {
            return R.drawable.ic_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.ic_cloudy;
        }
        return -1;
    }

    public static int getArtResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.art_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.art_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.art_rain;
        } else if (weatherId == 511) {
            return R.drawable.art_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.art_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.art_rain;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.art_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.art_storm;
        } else if (weatherId == 800) {
            return R.drawable.art_clear;
        } else if (weatherId == 801) {
            return R.drawable.art_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.art_clouds;
        }
        return -1;
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isConnected = networkInfo.isConnectedOrConnecting();
        return isConnected;
    }

    public static int getLocationStatus(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int locationStatus = sharedPreferences.
                getInt(context.getString(R.string.pref_location_status_key),
                        SyncAdapter.LOCATION_STATUS_SERVER_UNKNOWN);
        return locationStatus;
    }

    public static void resetLocationStatus(Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(context.getString(R.string.pref_location_status_key), SyncAdapter.LOCATION_STATUS_SERVER_UNKNOWN);
        editor.commit();
    }

    public static String getArtUrlFromWeatherCondition(Context context, int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return context.getString(R.string.format_art_url, "storm");
        } else if (weatherId >= 300 && weatherId <= 321) {
            return context.getString(R.string.format_art_url, "light_rain");
        } else if (weatherId >= 500 && weatherId <= 504) {
            return context.getString(R.string.format_art_url, "rain");
        } else if (weatherId == 511) {
            return context.getString(R.string.format_art_url, "snow");
        } else if (weatherId >= 520 && weatherId <= 531) {
            return context.getString(R.string.format_art_url, "rain");
        } else if (weatherId >= 600 && weatherId <= 622) {
            return context.getString(R.string.format_art_url, "snow");
        } else if (weatherId >= 701 && weatherId <= 761) {
            return context.getString(R.string.format_art_url, "fog");
        } else if (weatherId == 761 || weatherId == 781) {
            return context.getString(R.string.format_art_url, "storm");
        } else if (weatherId == 800) {
            return context.getString(R.string.format_art_url, "clear");
        } else if (weatherId == 801) {
            return context.getString(R.string.format_art_url, "light_clouds");
        } else if (weatherId >= 802 && weatherId <= 804) {
            return context.getString(R.string.format_art_url, "clouds");
        }
        return null;
    }

    public static String getFullFriendlyDayString(Context context, String dateInMillis) {

        String day = getDayName(context, dateInMillis);
        int formatId = R.string.format_full_friendly_date;
        return String.format(context.getString(
                formatId,
                day,
                getFormattedMonthDay(context, dateInMillis)));
    }
}
