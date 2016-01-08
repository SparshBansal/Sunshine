package sparsh.cool.developer.sunshine.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import sparsh.cool.developer.sunshine.data.WeatherContract.*;


/**
 * Created by Sparsha on 5/7/2015.
 */
public class WeatherProvider extends ContentProvider {

    private static final int WEATHER = 100;
    private static final int WEATHER_WITH_LOCATION = 101;
    private static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    private static final int LOCATION = 300;
    private static final int LOCATION_ID = 301;
    UriMatcher sUriMatcher = buildUriMatcher();

    private WeatherDbHelper mHelper;
    private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;

    static {
        sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();
        sWeatherByLocationSettingQueryBuilder.setTables(WeatherEntry.TABLE_NAME + " INNER JOIN " +
                LocationEntry.TABLE_NAME + " ON " + WeatherEntry.TABLE_NAME + "." +
                WeatherEntry.COLUMN_LOC_KEY + "=" + LocationEntry.TABLE_NAME + "." +
                LocationEntry._ID);
    }

    private static final String sLocationSettingSelection = LocationEntry.TABLE_NAME + "." +
            LocationEntry.COLUMN_LOCATION_SETTING + " = ? ";
    private static final String sLocationSettingWithStartDateSelection = LocationEntry.TABLE_NAME + "." +
            LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " + WeatherEntry.TABLE_NAME + "." +
            WeatherEntry.COLUMN_DATETEXT + " >= ? ";
    private static final String sWeatherWithLocationAndDateSelection = LocationEntry.TABLE_NAME + "." +
            LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " + WeatherEntry.TABLE_NAME + "." +
            WeatherEntry.COLUMN_DATETEXT + " = ? ";

    @Override
    public boolean onCreate() {
        mHelper = new WeatherDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor = null;
        SQLiteDatabase db = mHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case WEATHER:
                retCursor = null;
                retCursor = db.query(WeatherEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case WEATHER_WITH_LOCATION:
                retCursor = getWeatherWithLocationSetting(uri, projection, sortOrder);
                break;
            case WEATHER_WITH_LOCATION_AND_DATE:
                retCursor = null;
                retCursor = getWeatherWithLocationAndDate(uri, projection, sortOrder);
                break;
            case LOCATION:
                retCursor = null;
                retCursor = db.query(LocationEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case LOCATION_ID:
                retCursor = null;
                long id = ContentUris.parseId(uri);
                selection = "_ID = ?";
                selectionArgs = new String[]{String.valueOf(id)};
                retCursor = db.query(LocationEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {

        int match = sUriMatcher.match(uri);

        switch (match) {
            case WEATHER:
                return WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION:
                return WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherEntry.CONTENT_ITEM_TYPE;
            case LOCATION:
                return LocationEntry.CONTENT_TYPE;
            case LOCATION_ID:
                return LocationEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown Uri : " + uri.toString());
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        int match = sUriMatcher.match(uri);
        SQLiteDatabase db = mHelper.getWritableDatabase();
        Uri returnUri = null;
        switch (match) {
            case WEATHER: {
                long insertId = db.insert(WeatherEntry.TABLE_NAME, null, contentValues);
                if (insertId > 0) {
                    returnUri = WeatherEntry.buildWeatherUri(insertId);
                } else {
                    returnUri = null;
                }
                break;
            }
            case LOCATION: {
                long insertId = db.insert(LocationEntry.TABLE_NAME, null, contentValues);
                if (insertId > 0) {
                    returnUri = LocationEntry.buildLocationUri(insertId);
                } else {
                    returnUri = null;
                }
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int rowsDeleted = 0;
        switch (match) {
            case WEATHER: {
                rowsDeleted = db.delete(WeatherEntry.TABLE_NAME, where, whereArgs);
                break;
            }
            case LOCATION: {
                rowsDeleted = db.delete(LocationEntry.TABLE_NAME, where, whereArgs);
                break;
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String where, String[] whereArgs) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int rowsUpdated = 0;
        switch (match) {
            case WEATHER: {
                rowsUpdated = db.update(WeatherEntry.TABLE_NAME, contentValues, where, whereArgs);
                break;
            }
            case LOCATION: {
                rowsUpdated = db.update(LocationEntry.TABLE_NAME, contentValues, where, whereArgs);
                break;
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private UriMatcher buildUriMatcher() {
        UriMatcher sMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sMatcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_WEATHER, WEATHER);
        sMatcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_WEATHER + "/*", WEATHER_WITH_LOCATION);
        sMatcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_WEATHER + "/*/*", WEATHER_WITH_LOCATION_AND_DATE);

        sMatcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_LOCATION, LOCATION);
        sMatcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_LOCATION + "/#", LOCATION_ID);

        return sMatcher;
    }

    private Cursor getWeatherWithLocationSetting(Uri uri, String[] projection, String sortOrder) {
        String startDate = WeatherEntry.getStartDateFromUri(uri);
        String locationSetting = WeatherEntry.getLocationSettingFromUri(uri);
        String[] selectionArgs;
        String selection;
        if (startDate == null) {
            selection = sLocationSettingSelection;
            selectionArgs = new String[]{locationSetting};
        } else {
            selection = sLocationSettingWithStartDateSelection;
            selectionArgs = new String[]{locationSetting, startDate};
        }

        Cursor retCursor = sWeatherByLocationSettingQueryBuilder.query(mHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
        return retCursor;
    }

    private Cursor getWeatherWithLocationAndDate(Uri uri, String[] projection, String sortOrder) {
        String locationSeeting = WeatherEntry.getLocationSettingFromUri(uri);
        String date = WeatherEntry.getDateFromUri(uri);
        String[] selectionArgs = new String[]{locationSeeting, date};
        Cursor retCursor = sWeatherByLocationSettingQueryBuilder.query(mHelper.getReadableDatabase(),
                projection,
                sWeatherWithLocationAndDateSelection,
                selectionArgs,
                null,
                null,
                sortOrder);
        return retCursor;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        switch (match) {
            case WEATHER:
                db.beginTransaction();
                int count = 0;
                try {
                    for (ContentValues contentValues : values) {
                        long idx = db.insert(WeatherEntry.TABLE_NAME, null, contentValues);
                        if (idx != -1)
                            count++;
                    }
                    db.setTransactionSuccessful();
                }finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri,null);
                return count;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
