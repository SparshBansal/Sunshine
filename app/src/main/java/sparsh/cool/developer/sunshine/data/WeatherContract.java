package sparsh.cool.developer.sunshine.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Sparsha on 5/5/2015.
 */
public class WeatherContract {

    // Content Authority that uniquely defines our Content Provider
    public static final String CONTENT_AUTHORITY = "com.sparsh.cool.developer.sunshine.app";

    //Base Content URI that will appended by suitable paths
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //Paths to weather and location table for our Content Provider
    public static final String PATH_WEATHER = "weather";
    public static final String PATH_LOCATION = "location";

    public static final class WeatherEntry implements BaseColumns {
        //Content URI
        public static Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_WEATHER).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" +
                CONTENT_AUTHORITY + "/" + PATH_WEATHER;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" +
                CONTENT_AUTHORITY + "/" + PATH_WEATHER;

        //Name of the table containing weather info
        public static final String TABLE_NAME = "weather";

        //Column with foreign key into Location Table
        public static final String COLUMN_LOC_KEY = "location_id";

        //Column for storing Date as String format
        public static final String COLUMN_DATETEXT = "date";

        //Primary Key for our table as int
        public static final String COLUMN_WEATHER_ID = "weather_id";

        //Column conating short description of the weather
        //Stored as Strings e.g ("Sky is clear")
        public static final String COLUMN_SHORT_DESC = "short_desc";

        //Column for storing min and max temperatures for the day
        public static final String COLUMN_MIN_TEMP = "min";
        public static final String COLUMN_MAX_TEMP = "max";

        //Humidity is stored as float representing percentage
        public static final String COLUMN_HUMIDITY = "humdity";

        //Pressure is stored as float representing percentage
        public static final String COLUMN_PRESSURE = "pressure";

        //Pressure is stored as float representing windspeed
        public static final String COLUMN_WINDSPEED = "windspeed";

        //Date Format of DATETEXT in our Database
        public static final String DATE_FORMAT = "yyyyMMdd";

        public static Uri buildWeatherUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }

        public static Uri buildWeatherLocation(String locationSetting){
            return CONTENT_URI.buildUpon().appendPath(locationSetting).build();
        }

        public static Uri buildWeatherLocationWithStartDate(String locationSetting , String start_date){
            return CONTENT_URI.buildUpon().appendPath(locationSetting)
                    .appendQueryParameter(COLUMN_DATETEXT, start_date).build();
        }

        public static Uri buildWeatherLocationWithDate(String locationSetting , String date){
            return CONTENT_URI.buildUpon().appendPath(locationSetting).appendPath(date).build();
        }

        public static String getLocationSettingFromUri(Uri uri){
            String locationSetting = uri.getPathSegments().get(1);
            return locationSetting;
        }

        public static String getDateFromUri(Uri uri){
            String date = uri.getPathSegments().get(2);
            return date;
        }

        public static String getStartDateFromUri(Uri uri){
            String start_date = uri.getQueryParameter(COLUMN_DATETEXT);
            return start_date;
        }



        public static String getWeatherDbDate(Date date){
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            return sdf.format(date);
        }

        public static Date getDateFromDb(String dateText){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
            try {
                Date date = simpleDateFormat.parse(dateText);
                return date;
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static final class LocationEntry implements BaseColumns {
        //Content URI for Location URI
        public static Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" +
                CONTENT_AUTHORITY + "/" + PATH_LOCATION;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" +
                CONTENT_AUTHORITY + "/" + PATH_LOCATION;


        //Name of the table containing Location Details
        public static final String TABLE_NAME = "location";

        //Postal Code of the preferred location
        public static final String COLUMN_LOCATION_SETTING = "location_setting";

        //Human Readable Location String provided by the API
        public static final String COLUMN_CITY_NAME = "city_name";

        //Latitude and longitude returned by OpenWeatherMap API
        public static final String COLUMN_COORD_LAT = "coord_lat";
        public static final String COLUMN_COORD_LONG = "coord_long";

        public static Uri buildLocationUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }

    }
}
