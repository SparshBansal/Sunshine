package sparsh.cool.developer.sunshine.fragments;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;

import org.w3c.dom.Text;

import sparsh.cool.developer.sunshine.R;
import sparsh.cool.developer.sunshine.Utility;
import sparsh.cool.developer.sunshine.data.WeatherContract;

import static android.support.v4.view.MenuItemCompat.*;

/**
 * Created by Sparsha on 5/4/2015.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    String forecast;
    final String FORECAST_SHARE_HASHTAG = "#SunshineApp";

    //Ceating Strings for PRESSURE,HUMIDITY and WINDSPEED TextViews
    final String STRING_HUMIDITY = "HUMIDITY : ";
    final String STRING_PRESSURE = "PRESSURE : ";
    final String STRING_WINDSPEED = "WINDSPEED : ";

    //Key to retreive DATETEXT from Intent
    String DATETEXT, KEY_DATETEXT = "KEY_DATETEXT";

    TextView tvDate, tvDesc, tvMin, tvMax, tvHumidity, tvWindspeed, tvPressure, tvFriendlyDateView;
    ImageView ivArt;
    double max, min, humidity, windspeed, pressure;
    String SHORT_DESC;

    //Projections to be Queried
    final String[] mProjections = {
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WINDSPEED,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    //Integer indices matching projections
    final int COL_MIN_TEMP = 0;
    final int COL_MAX_TEMP = 1;
    final int COL_SHORT_DESC = 2;
    final int COL_HUMIDITY = 3;
    final int COL_PRESSURE = 4;
    final int COL_WINDSPEED = 5;
    final int COL_WEATHER_ID = 6;

    public DetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        DATETEXT = getArguments().getString(KEY_DATETEXT);

        tvDate = (TextView) rootView.findViewById(R.id.detail_forecast_date_textView);
        tvDesc = (TextView) rootView.findViewById(R.id.detail_forecast_desc_textView);
        tvMax = (TextView) rootView.findViewById(R.id.detail_forecast_max_textView);
        tvMin = (TextView) rootView.findViewById(R.id.detail_forecast_min_textView);
        tvHumidity = (TextView) rootView.findViewById(R.id.detail_forecast_humidity_textView);
        tvPressure = (TextView) rootView.findViewById(R.id.detail_forecast_pressure_textView);
        tvWindspeed = (TextView) rootView.findViewById(R.id.detail_forecast_windspeed_textView);
        ivArt = (ImageView) rootView.findViewById(R.id.detail_forecast_imageView);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(KEY_DATETEXT))
            getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_detail_fragment, menu);

        MenuItem item = menu.findItem(R.id.action_share);
        Intent shareIntent = createShareIntent();

        android.support.v7.widget.ShareActionProvider mShareActionProvider = (android.support.v7.widget.ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if (mShareActionProvider != null) {
            Log.d("DEBUG", "Share Intent is Null");
            mShareActionProvider.setShareIntent(shareIntent);
        }

    }

    public Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, forecast + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri baseUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                Utility.getPreferredLocation(getActivity()),
                DATETEXT);
        return new CursorLoader(
                getActivity(),
                baseUri,
                mProjections,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        boolean isSuccessful = data.moveToFirst();  //Checking if data is successfully queried
        if (isSuccessful) {
            max = data.getDouble(COL_MAX_TEMP);
            min = data.getDouble(COL_MIN_TEMP);
            humidity = data.getDouble(COL_HUMIDITY);
            windspeed = data.getDouble(COL_WINDSPEED);
            pressure = data.getDouble(COL_PRESSURE);
            SHORT_DESC = data.getString(COL_SHORT_DESC);
        }

        String date = DATETEXT;

        tvDate.setText(Utility.getFullFriendlyDayString(getActivity(),date));


        tvDesc.setText(SHORT_DESC);
        tvMax.setText(Utility.formatTemperature(max, Utility.isMetric(getActivity())));
        tvMin.setText(Utility.formatTemperature(min, Utility.isMetric(getActivity())));
        tvHumidity.setText(""+humidity);
        tvWindspeed.setText("" + windspeed);
        tvPressure.setText("" + pressure);

        int weatherId = data.getInt(COL_WEATHER_ID);

        ivArt.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        tvMax.setText("");
        tvDesc.setText("");
        tvDate.setText("");
        tvMin.setText("");
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, this);
    }
}