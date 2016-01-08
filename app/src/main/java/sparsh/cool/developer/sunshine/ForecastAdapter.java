package sparsh.cool.developer.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;

import sparsh.cool.developer.sunshine.fragments.ForecastFragment;

/**
 * Created by Sparsha on 6/14/2015.
 */
public class ForecastAdapter extends android.support.v4.widget.CursorAdapter {

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(context);
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        if (viewType == VIEW_TYPE_TODAY)
            layoutId = R.layout.list_item_forecast_today;
        else
            layoutId = R.layout.list_item_forecast;
        View view = inflater.inflate(layoutId, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        int VIEW_TYPE = getItemViewType(cursor.getPosition());
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);

        //If error occurs while fetching data from URL then by default the
        // Icon/Art wil fall to this resource
        int fallback_id = 0;

        //Switch Case to determine the VIEW Type as Today or Future Day and accordingly setting
        //the image resource
        switch (VIEW_TYPE) {
            case VIEW_TYPE_TODAY:
                viewHolder.ivIcon.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

                break;
            case VIEW_TYPE_FUTURE_DAY:
                viewHolder.ivIcon.setImageResource(Utility.getIconResourceForWeatherCondition(weatherId));

                break;
        }

        String DATETEXT = cursor.getString(ForecastFragment.COL_DATETEXT);
        viewHolder.tvDate.setText(Utility.getFriendlyDayString(context, DATETEXT));

        String SHORT_DESC = cursor.getString(ForecastFragment.COL_SHORT_DESC);
        viewHolder.tvForecast.setText(SHORT_DESC);

        double min = cursor.getDouble(ForecastFragment.COL_MIN_TEMP);
        viewHolder.tvMin.setText(Utility.formatTemperature(min, Utility.isMetric(context)));

        double max = cursor.getDouble(ForecastFragment.COL_MAX_TEMP);
        viewHolder.tvMax.setText(Utility.formatTemperature(max, Utility.isMetric(context)));
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return VIEW_TYPE_TODAY;
        else
            return VIEW_TYPE_FUTURE_DAY;
    }

    public class ViewHolder {
        TextView tvDate, tvForecast, tvMin, tvMax;
        ImageView ivIcon;

        public ViewHolder(View view) {
            tvDate = (TextView) view.findViewById(R.id.list_item_date_textView);
            tvForecast = (TextView) view.findViewById(R.id.list_item_forecast_textView);
            tvMax = (TextView) view.findViewById(R.id.list_item_max_textView);
            tvMin = (TextView) view.findViewById(R.id.list_item_min_textView);
            ivIcon = (ImageView) view.findViewById(R.id.ivIcon);
        }
    }
}
