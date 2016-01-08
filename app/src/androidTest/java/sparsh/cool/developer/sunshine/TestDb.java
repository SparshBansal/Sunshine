package sparsh.cool.developer.sunshine;

import android.content.ContentValues;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.Map;
import java.util.Set;

import sparsh.cool.developer.sunshine.data.WeatherContract.LocationEntry;
import sparsh.cool.developer.sunshine.data.WeatherContract.WeatherEntry;

import sparsh.cool.developer.sunshine.data.WeatherContract;
import sparsh.cool.developer.sunshine.data.WeatherDbHelper;

/**
 * Created by Sparsha on 5/5/2015.
 */
public class TestDb extends AndroidTestCase {
    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {

        SQLiteDatabase mDatabase = new WeatherDbHelper(mContext).getWritableDatabase();

        ContentValues locationValues = createNorthPoleLocationValues();
        long location_row_id = mDatabase.insert(WeatherContract.LocationEntry.TABLE_NAME, null, locationValues);

        assertTrue(location_row_id != -1);
        Log.d("TEST", "Location Row id : " + location_row_id);

        Cursor cursor = mDatabase.query(LocationEntry.TABLE_NAME, null, null, null, null, null, null);
        validateCursor(cursor,locationValues);

        ContentValues weatherValues = createWeatherValues(location_row_id);
        long weather_row_id = mDatabase.insert(WeatherEntry.TABLE_NAME, null, weatherValues);

        assertTrue(weather_row_id!=-1);
        Log.d("Test" , "Insert to weather Table Successful");
        //Reading from the database
        Cursor weather_cursor = mDatabase.query(WeatherEntry.TABLE_NAME,null,null,null,null,null,null);
        validateCursor(weather_cursor,weatherValues);
    }


    public ContentValues createWeatherValues(long location_row_id){

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

    public ContentValues createNorthPoleLocationValues(){

        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_CITY_NAME, "North Pole");
        values.put(LocationEntry.COLUMN_LOCATION_SETTING, "99705");
        values.put(LocationEntry.COLUMN_COORD_LAT, 64.7488);
        values.put(LocationEntry.COLUMN_COORD_LONG, -147.353);

        return values;
    }

    public void validateCursor(Cursor valueCursor , ContentValues expectedValues){

        assertTrue(valueCursor.moveToFirst());
        Set<Map.Entry<String,Object>> valueSet = expectedValues.valueSet();
        for(Map.Entry<String,Object> entry : valueSet){
            int idx = valueCursor.getColumnIndex(entry.getKey());
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue , valueCursor.getString(idx));
        }
        valueCursor.close();
    }
}
