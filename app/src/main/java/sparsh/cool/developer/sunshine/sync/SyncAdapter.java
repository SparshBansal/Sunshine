package sparsh.cool.developer.sunshine.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bumptech.glide.Glide;

import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import sparsh.cool.developer.sunshine.R;
import sparsh.cool.developer.sunshine.Utility;
import sparsh.cool.developer.sunshine.activities.MainActivity;
import sparsh.cool.developer.sunshine.data.WeatherContract;

/**
 * Created by Sparsha on 7/10/2015.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String LOG_TAG = SyncAdapter.class.getName();
    // Interval at which to sync with the weather, in milliseconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    private static final String STRING_DATA_NOT_FOUND = "404";


    //Projections for Notifications
    private String[] mProjections = {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC
    };

    private static int COL_WEATHER_ID = 0;
    private static int COL_MAX_TEMP = 1;
    private static int COL_MIN_TEMP = 2;
    private static int COL_SHORT_DESC = 3;

    private static final long DAY_IN_MILLISECONDS = 24 * 60 * 60 * 1000;
    private static final int WEATHER_NOTIFICATION_ID = 3004;

    ContentResolver mContentResolver;
    Context mContext;


    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "com.sparsh.cool.developer.sunshine.app";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "com.example.sparsh";
    // The account name
    public static final String ACCOUNT = "dummyaccount";
    // Instance fields
    static Account mAccount;

    final String OWM_PRESSURE = "pressure";
    final String OWM_HUMIDITY = "humidity";
    final String OWM_WINDSPEED = "speed";
    final String OWM_LIST = "list";
    final String OWM_DATETIME = "dt";
    final String OWM_MAX = "max";
    final String OWM_MIN = "min";
    final String OWM_DESC = "description";
    final String OWM_LATITUDE = "lat";
    final String OWM_LONGITUDE = "lon";
    final String OWM_CITY = "city";
    final String OWM_CITY_NAME = "name";
    final String OWM_COORDINATES = "coord";
    final String OWM_WEATHER_ID = "id";
    final String OWM_COD = "cod";

    private double PRESSURE, HUMIDITY, LATITUDE, LONGITUDE, MIN, MAX, WEATHER_ID, WINDSPEED;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LOCATION_STATUS_OK,
            LOCATION_STATUS_SERVER_DOWN,
            LOCATION_STATUS_SERVER_INVALID,
            LOCATION_STATUS_SERVER_UNKNOWN,
            LOCATION_STATUS_INVALID})

    public @interface LocationStatus {
    }

    public static final int LOCATION_STATUS_OK = 0;
    public static final int LOCATION_STATUS_SERVER_DOWN = 1;
    public static final int LOCATION_STATUS_SERVER_INVALID = 2;
    public static final int LOCATION_STATUS_SERVER_UNKNOWN = 3;
    public static final int LOCATION_STATUS_INVALID = 4;


    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
        mContext = context;
    }


    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        HttpURLConnection urlConnection = null;
        BufferedReader bufferedReader = null;
        String forcastJsonStr = null;
        String locationQuery = Utility.getPreferredLocation(mContext);

        try {

            String format = "json";
            String units = "metric";
            String num_days = "14";
            String api_key = "4204728bc40e9259fef9d9514065d7fc";

            String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            String QUERY_PARAM = "q";
            String MODE_PARAM = "mode";
            String UNITS_PARAM = "units";
            String COUNT_PARAM = "cnt";
            String APPID_PARAM = "APPID";

            Uri weatherUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, locationQuery)
                    .appendQueryParameter(MODE_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(COUNT_PARAM, num_days)
                    .appendQueryParameter(APPID_PARAM, api_key)
                    .build();

            URL weatherForcastUrl = new URL(weatherUri.toString());

            Log.d("DEBUG", weatherForcastUrl.toString());

            urlConnection = (HttpURLConnection) weatherForcastUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                Log.d("ERR", "Input Stream is Null");
                return;
            } else {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    setLocationStatus(mContext, LOCATION_STATUS_SERVER_DOWN);
                    return;
                } else {
                    forcastJsonStr = buffer.toString();
                    inputStream.close();
                    parseJson(forcastJsonStr, locationQuery);
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            setLocationStatus(mContext, LOCATION_STATUS_SERVER_DOWN);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    private void notifyWeather() {
        Context context = getContext();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        long lastNotification = sharedPreferences.getLong(context.getString(R.string.pref_last_notification), 0);

        if (System.currentTimeMillis() - lastNotification >= DAY_IN_MILLISECONDS) {
            String locationQuery = Utility.getPreferredLocation(context);
            Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationQuery,
                    WeatherContract.WeatherEntry.getWeatherDbDate(new Date()));

            Cursor resultCursor = getContext().getContentResolver().query(weatherUri, mProjections, null, null, null);

            if (resultCursor != null && resultCursor.moveToFirst()) {
                String desc = resultCursor.getString(COL_SHORT_DESC);
                double max = resultCursor.getDouble(COL_MAX_TEMP);
                double min = resultCursor.getDouble(COL_MIN_TEMP);
                int weatherId = resultCursor.getInt(COL_WEATHER_ID);

                int iconID = Utility.getIconResourceForWeatherCondition(weatherId);

                Resources resources = context.getResources();
                Bitmap largeIcon = BitmapFactory.decodeResource(resources, Utility.getArtResourceForWeatherCondition(weatherId));
                int artResourceId = Utility.getArtResourceForWeatherCondition(weatherId);
                String artUrl = Utility.getArtUrlFromWeatherCondition(context, weatherId);

                // On Honeycomb and higher devices, we can retrieve the size of the large icon
                // Prior to that, we use a fixed size
                @SuppressLint("InlinedApi")
                int largeIconWidth = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                        ? resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_width)
                        : resources.getDimensionPixelSize(R.dimen.notification_large_icon_default);
                @SuppressLint("InlinedApi")
                int largeIconHeight = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                        ? resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height)
                        : resources.getDimensionPixelSize(R.dimen.notification_large_icon_default);

                try {
                    largeIcon = Glide.with(context).
                            load(artUrl).
                            asBitmap().
                            error(artResourceId).
                            fitCenter().
                            into(largeIconWidth, largeIconHeight).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    largeIcon = BitmapFactory.decodeResource(resources, Utility.getArtResourceForWeatherCondition(weatherId));
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    largeIcon = BitmapFactory.decodeResource(resources, Utility.getArtResourceForWeatherCondition(weatherId));
                }

                String title = context.getString(R.string.app_name);


                // Define the text of the forecast.
                String contentText = String.format(context.getString(R.string.format_notification),
                        desc,
                        Utility.formatTemperature(max, Utility.isMetric(context)),
                        Utility.formatTemperature(min, Utility.isMetric(context)));

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong(context.getString(R.string.pref_last_notification), System.currentTimeMillis());
                editor.commit();

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
                mBuilder.setSmallIcon(iconID);
                mBuilder.setLargeIcon(largeIcon);
                mBuilder.setContentTitle(title);
                mBuilder.setContentText(contentText);

                Intent resultIntent = new Intent(context, MainActivity.class);

                TaskStackBuilder builder = TaskStackBuilder.create(context);
                builder.addNextIntent(resultIntent);

                PendingIntent resultPendingIntent = builder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(resultPendingIntent);
                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                // mId allows you to update the notification later on.
                mNotificationManager.notify(WEATHER_NOTIFICATION_ID, mBuilder.build());
            }

        }
    }

    private void parseJson(String forecastStr, String locationQuery) {
        Vector<ContentValues> cVector = new Vector<>();
        try {
            JSONObject rootObject = new JSONObject(forecastStr);
            if (rootObject != null) {

                String cod = rootObject.getString(OWM_COD);
                if (cod.equals(HttpURLConnection.HTTP_NOT_FOUND)) {
                    Log.d(LOG_TAG, "Data not Found!!!");
                    setLocationStatus(mContext, LOCATION_STATUS_INVALID);
                    return;
                }
                JSONObject city = rootObject.getJSONObject(OWM_CITY);
                if (city != null && city.has(OWM_COORDINATES)) {
                    JSONObject coordinates = city.getJSONObject(OWM_COORDINATES);
                    LATITUDE = coordinates.getDouble(OWM_LATITUDE);
                    LONGITUDE = coordinates.getDouble(OWM_LONGITUDE);
                }
                String city_name = city.getString(OWM_CITY_NAME);
                long loc_key = addLocation(LATITUDE, LONGITUDE, city_name, locationQuery);
                if (rootObject.has(OWM_LIST)) {
                    JSONArray weatherDetails = rootObject.getJSONArray(OWM_LIST);
                    for (int i = 0; i < weatherDetails.length(); i++) {
                        JSONObject weatherForecast = weatherDetails.getJSONObject(i);

                        PRESSURE = weatherForecast.getDouble(OWM_PRESSURE);
                        HUMIDITY = weatherForecast.getDouble(OWM_HUMIDITY);
                        WINDSPEED = weatherForecast.getDouble(OWM_WINDSPEED);

                        JSONObject temp = weatherForecast.getJSONObject("temp");

                        MAX = temp.getDouble(OWM_MAX);
                        MIN = temp.getDouble(OWM_MIN);

                        JSONArray weather = weatherForecast.getJSONArray("weather");
                        String desc = weather.getJSONObject(0).getString(OWM_DESC);
                        WEATHER_ID = weather.getJSONObject(0).getDouble(OWM_WEATHER_ID);
                        long timestamp = weatherForecast.getLong(OWM_DATETIME);
                        String day = getReadableDateString(weatherForecast.getLong(OWM_DATETIME));

                        ContentValues contentValues = new ContentValues();
                        contentValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, loc_key);
                        contentValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, HUMIDITY);
                        contentValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, desc);
                        contentValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, PRESSURE);
                        contentValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, MAX);
                        contentValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, MIN);
                        contentValues.put(WeatherContract.WeatherEntry.COLUMN_DATETEXT,
                                WeatherContract.WeatherEntry.getWeatherDbDate(new Date(Long.valueOf(timestamp) * 1000)));
                        contentValues.put(WeatherContract.WeatherEntry.COLUMN_WINDSPEED, WINDSPEED);
                        contentValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, WEATHER_ID);

                        cVector.add(contentValues);
                    }
                    if (cVector.size() > 0) {
                        ContentValues[] contentValues = new ContentValues[cVector.size()];
                        cVector.toArray(contentValues);
                        int rowsInserted = mContentResolver.bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, contentValues);
                        notifyWeather();
                        Log.d("DATABASE", rowsInserted + " rows Inserted");
                        setLocationStatus(mContext, LOCATION_STATUS_OK);

                    }

                }
            }
        } catch (JSONException e) {
            setLocationStatus(mContext, LOCATION_STATUS_SERVER_INVALID);
            e.printStackTrace();
        }
    }


    private String getReadableDateString(long time) {
        Date date = new Date(time * 1000);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date).toString();
    }

    private long addLocation(Double LATITUDE, Double LONGITUDE, String CITY_NAME, String locationQuery) {
        String selection = WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?";
        Cursor cursor = mContentResolver.query(WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                selection,
                new String[]{locationQuery},
                null
        );
        if (cursor.moveToFirst()) {
            long _idx = cursor.getLong(0);
            return _idx;
        } else {
            ContentValues values = new ContentValues();
            values.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, CITY_NAME);
            values.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, LATITUDE);
            values.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, LONGITUDE);
            values.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationQuery);
            Uri insertedRowUri = mContentResolver.insert(WeatherContract.LocationEntry.CONTENT_URI, values);

            long _idx = ContentUris.parseId(insertedRowUri);
            return _idx;
        }
    }

    public static void syncImmediately(Context context) {
        mAccount = getSyncAccount(context);
        Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(mAccount,
                context.getResources().getString(R.string.content_authority),
                extras);
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String contentAuthority = context.getResources().getString(R.string.content_authority);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //We can enable inexact Timers in our Sync Adapter
            SyncRequest syncRequest = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, contentAuthority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(syncRequest);
        } else {
            context.getContentResolver().addPeriodicSync(account, contentAuthority,
                    Bundle.EMPTY, syncInterval);
        }
    }


    private static Account getSyncAccount(Context context) {
        Account newAccount = new Account(ACCOUNT, ACCOUNT_TYPE);
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        if (accountManager.getPassword(newAccount) == null) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    public static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }


    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }


    private void setLocationStatus(Context mContext, int locationStatus) {
        SharedPreferences.Editor editor =
                PreferenceManager.getDefaultSharedPreferences(mContext).edit();
        editor.putInt(mContext.getString(R.string.pref_location_status_key), locationStatus);
        editor.commit();
    }
}
