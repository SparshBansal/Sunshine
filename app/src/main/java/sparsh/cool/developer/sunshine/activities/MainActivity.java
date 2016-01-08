package sparsh.cool.developer.sunshine.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import sparsh.cool.developer.sunshine.fragments.DetailFragment;
import sparsh.cool.developer.sunshine.fragments.ForecastFragment;
import sparsh.cool.developer.sunshine.R;


public class MainActivity extends AppCompatActivity implements ForecastFragment.ItemClickListener {

    private static final String KEY_DATETEXT = "KEY_DATETEXT";
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (findViewById(R.id.weather_detail_container) != null) {

            //  The detail container view will only be present in large-screen layout
            //  If this view is present then the activity must be in two pane view
            mTwoPane = true;

            //If two pane view is present then we add the fragment to the container

            if (savedInstanceState == null) {
                DetailFragment fragment = new DetailFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, fragment)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.action_viewOnMap) {
            openPreferredLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationInMap() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String preferredLocation = sharedPreferences.getString(getString(R.string.KEY_LOCATION_PREFERENCE), "110006");

        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q", preferredLocation).build();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d("DEBUG", "Activity not Found");
        }
    }


    @Override
    public void OnItemClick(int position, String datetext) {
        if (!mTwoPane) {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(KEY_DATETEXT, datetext);
            startActivity(intent);
        } else {
            Bundle data = new Bundle();
            data.putString(KEY_DATETEXT, datetext);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(data);
            getSupportFragmentManager().beginTransaction().
                    replace(R.id.weather_detail_container, fragment).
                    commit();
        }
    }
}
