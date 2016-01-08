package sparsh.cool.developer.sunshine;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.Map;
import java.util.Set;

import sparsh.cool.developer.sunshine.data.WeatherContract;
import sparsh.cool.developer.sunshine.data.WeatherContract.LocationEntry;
import sparsh.cool.developer.sunshine.data.WeatherContract.WeatherEntry;
import sparsh.cool.developer.sunshine.data.WeatherDbHelper;

/**
 * Created by Sparsha on 5/5/2015.
 */
public class TestProvider extends AndroidTestCase {

    private static final String TEST_LOCATION = "99705";
    private static final String TEST_DATE = "1103351321";
    private static final String TEST_CITY_NAME = "North Pole";

    public void testDeleteAllRows() throws Throwable {
        int weatherRowsDeleted = mContext.getContentResolver().delete(
                WeatherEntry.CONTENT_URI,
                null,
                null
        );

        int locationRowsDeleted = mContext.getContentResolver().delete(
                LocationEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        assertEquals(cursor.getCount(), 0);

        cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        assertEquals(cursor.getCount(), 0);
    }

    public void testInsertReadProvider() {

        ContentValues locationValues = createNorthPoleLocationValues();
        Uri locationInsertUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, locationValues);

        long location_row_id = ContentUris.parseId(locationInsertUri);
        assertTrue(location_row_id != -1);
        Log.d("TEST", "Location Row id : " + location_row_id);


        ContentValues weatherValues = createWeatherValues(location_row_id);
        Uri weatherInsertUri = mContext.getContentResolver().insert(WeatherEntry.CONTENT_URI, weatherValues);
        long weather_row_id = ContentUris.parseId(weatherInsertUri);
        assertTrue(weather_row_id != -1);
        Log.d("Test", "Insert to weather Table Successful");

        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        validateCursor(cursor, locationValues);

        Uri testUri = LocationEntry.buildLocationUri(location_row_id);

        cursor = mContext.getContentResolver().query(
                testUri,
                null,
                null,
                null,
                null
        );
        validateCursor(cursor, locationValues);

        //Reading from the database
        Cursor weather_cursor = mContext.getContentResolver().query(WeatherEntry.CONTENT_URI, null, null, null, null);
        validateCursor(weather_cursor, weatherValues);

        weather_cursor.close();

        weather_cursor = mContext.getContentResolver().query(WeatherEntry.buildWeatherLocation(TEST_LOCATION),
                null,
                null,
                null,
                null);
        validateCursor(weather_cursor, weatherValues);
        weather_cursor.close();

        weather_cursor = mContext.getContentResolver().query(WeatherEntry.buildWeatherLocationWithStartDate(TEST_LOCATION, TEST_DATE),
                null,
                null,
                null,
                null);
        validateCursor(weather_cursor, weatherValues);
        weather_cursor.close();


        weather_cursor = mContext.getContentResolver().query(WeatherEntry.buildWeatherLocationWithDate(TEST_LOCATION, TEST_DATE),
                null,
                null,
                null,
                null);
        validateCursor(weather_cursor, weatherValues);
        weather_cursor.close();
    }

    public void testUpdateRecords() throws Throwable {
        testDeleteAllRows();

        ContentValues values = createNorthPoleLocationValues();
        Uri insert_uri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, values);
        long insert_row_idx = ContentUris.parseId(insert_uri);

        ContentValues values2 = new ContentValues(values);
        values2.put(LocationEntry._ID , insert_row_idx);
        values2.put(LocationEntry.COLUMN_CITY_NAME, "Santa's City" );

        int count = mContext.getContentResolver().update(LocationEntry.CONTENT_URI,
                values2,
                LocationEntry._ID + " = ?",
                new String[]{String.valueOf(insert_row_idx)}
        );
        assertEquals(count,1);

        Cursor cursor = mContext.getContentResolver().query(LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        validateCursor(cursor,values2);
    }


    public void testGetType() {

        String actualValue = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        Log.d("MESSAGE", actualValue);
        assertEquals(WeatherEntry.CONTENT_TYPE, actualValue);

        String testLocation = "94043";
        Uri testUri = WeatherEntry.buildWeatherLocation(testLocation);
        actualValue = mContext.getContentResolver().getType(testUri);
        assertEquals(WeatherEntry.CONTENT_TYPE, actualValue);


        String testDate = "1110135135";
        testUri = WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate);
        actualValue = mContext.getContentResolver().getType(testUri);
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, actualValue);
    }


    public ContentValues createWeatherValues(long location_row_id) {

        // Content Values for inserting into Weather Table
        ContentValues weatherValues = new ContentValues();

        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, location_row_id);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, "1103351321");
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 123);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Sky is clear");
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 37.63);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 43.56);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 83.02);
        weatherValues.put(WeatherEntry.COLUMN_WINDSPEED, 120);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.2);

        return weatherValues;
    }

    public ContentValues createNorthPoleLocationValues() {

        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_CITY_NAME, "North Pole");
        values.put(LocationEntry.COLUMN_LOCATION_SETTING, "99705");
        values.put(LocationEntry.COLUMN_COORD_LAT, 64.7488);
        values.put(LocationEntry.COLUMN_COORD_LONG, -147.353);

        return values;
    }

    public void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

        assertTrue(valueCursor.moveToFirst());
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            int idx = valueCursor.getColumnIndex(entry.getKey());
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }

}
