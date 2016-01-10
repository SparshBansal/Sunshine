package sparsh.cool.developer.sunshine.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;

import sparsh.cool.developer.sunshine.ForecastAdapter;
import sparsh.cool.developer.sunshine.R;
import sparsh.cool.developer.sunshine.Utility;
import sparsh.cool.developer.sunshine.data.WeatherContract.LocationEntry;
import sparsh.cool.developer.sunshine.data.WeatherContract.WeatherEntry;
import sparsh.cool.developer.sunshine.sync.SyncAdapter;

/**
 * Created by Sparsha on 5/1/2015.
 */
public class ForecastFragment extends Fragment implements
        android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = ForecastFragment.class.getName();

    //Message String when Database is Empty

    //Uniquely Identify a Loader
    private final int LOADER_ID = 0;

    final String[] projections = new String[]{
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATETEXT,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_WEATHER_ID,
            LocationEntry.COLUMN_LOCATION_SETTING
    };

    public static final int COL_ID = 0;
    public static final int COL_DATETEXT = 1;
    public static final int COL_SHORT_DESC = 2;
    public static final int COL_MAX_TEMP = 3;
    public static final int COL_MIN_TEMP = 4;
    public static final int COL_WEATHER_ID = 5;
    public static final int COL_LOCATION_SETTING = 6;


    private String locationSetting;
    private ItemClickListener mCallback;
    private ForecastAdapter mArrayAdapter;
    private RecyclerView forecastRecyclerView;
    private TextView emptyView;

    public ForecastFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (ItemClickListener) activity;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);

    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the empty textview and pass it in the constructor
        emptyView = (TextView) rootView.findViewById(R.id.empty_textView);
        mArrayAdapter = new ForecastAdapter(getActivity(), new ForecastAdapter.ForecastAdapterOnClickHandler() {
            @Override
            public void ForecastAdapterOnClick(String date, ForecastAdapter.ViewHolder viewHolder) {
                mCallback.OnItemClick(viewHolder.getAdapterPosition(),date);
            }
        },emptyView);

        // Using Recycler View instead of List View
        forecastRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView_forecast);
        forecastRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        forecastRecyclerView.setAdapter(mArrayAdapter);


        updateWeather();
        return rootView;
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        if (locationSetting != null && !Utility.getPreferredLocation(getActivity()).equals(locationSetting)) {
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            updateWeather();
        }

        return super.onOptionsItemSelected(item);

    }


    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        locationSetting = Utility.getPreferredLocation(getActivity());
        Uri baseUri = WeatherEntry.buildWeatherLocationWithStartDate(locationSetting, WeatherEntry.getWeatherDbDate(new Date()));
        return new CursorLoader(
                getActivity(),
                baseUri,
                projections,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        mArrayAdapter.swapCursor(data);

        //If Data Cursor is null then we check whether the network is Connected or not
        if (data == null) {
            updateEmptyView();
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        mArrayAdapter.swapCursor(null);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_location_status_key)))
            updateEmptyView();
    }


    public void updateWeather() {
        SyncAdapter.initializeSyncAdapter(getActivity());
        SyncAdapter.syncImmediately(getActivity());
    }


    private String getDateText(int position) {
        Cursor cursor = mArrayAdapter.getCursor();
        boolean isSuccessfull = cursor.moveToPosition(position);
        if (isSuccessfull) {
            String DATETEXT = cursor.getString(COL_DATETEXT);
            return DATETEXT;
        } else
            return null;
    }


    private void updateEmptyView() {

         TextView emptyTextView = (TextView) getView().findViewById(R.id.empty_textView);
        //Utility method called for checking network connectivity
        boolean isConnected = Utility.isConnected(getActivity());
        if (!isConnected) {
            //if no network available then set the corresponding text
            emptyTextView.setText(getString(R.string.empty_forecast_list_no_network));
        } else {
            int locationStatus = Utility.getLocationStatus(getActivity());
            if (locationStatus == SyncAdapter.LOCATION_STATUS_SERVER_DOWN)
                //if server is not available then set the corresponding text
                emptyTextView.setText(getString(R.string.empty_forecast_list_server_down));
            if (locationStatus == SyncAdapter.LOCATION_STATUS_SERVER_INVALID)
                //if valid data is not available then set the corresponding text
                emptyTextView.setText(getString(R.string.empty_forecast_list_server_error));
            if(locationStatus == SyncAdapter.LOCATION_STATUS_INVALID)
                //if Location entered by the user is invalid
                emptyTextView.setText(getString(R.string.empty_forecast_list_location_invalid));
        }
    }


    public interface ItemClickListener {
        public void OnItemClick(int position, String datetext);
    }

}
