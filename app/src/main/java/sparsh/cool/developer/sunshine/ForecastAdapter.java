package sparsh.cool.developer.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
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
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {

    private final Context mContext;
    private Cursor mCursor;
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;


    public ForecastAdapter(Context context) {
        mContext = context;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Using the viewType parameter inflate the corresponding view
        if (parent instanceof RecyclerView) {
            int layoutId = -1;
            switch (viewType) {
                case VIEW_TYPE_TODAY :
                    layoutId = R.layout.list_item_forecast_today;
                    break;
                case VIEW_TYPE_FUTURE_DAY :
                    layoutId = R.layout.list_item_forecast;
                    break;
            }
            View view = LayoutInflater.from(mContext).inflate(layoutId,parent,false);
            view.setFocusable(true);
            return new ViewHolder(view);
        } else {
            throw new RuntimeException("Not Bound to Recycler View");
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        mCursor.moveToPosition(position);
        int VIEW_TYPE = getItemViewType(position);
        int weatherId = mCursor.getInt(ForecastFragment.COL_WEATHER_ID);

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

        String DATETEXT = mCursor.getString(ForecastFragment.COL_DATETEXT);
        viewHolder.tvDate.setText(Utility.getFriendlyDayString(mContext, DATETEXT));

        String SHORT_DESC = mCursor.getString(ForecastFragment.COL_SHORT_DESC);
        viewHolder.tvForecast.setText(SHORT_DESC);

        double min = mCursor.getDouble(ForecastFragment.COL_MIN_TEMP);
        viewHolder.tvMin.setText(Utility.formatTemperature(min, Utility.isMetric(mContext)));

        double max = mCursor.getDouble(ForecastFragment.COL_MAX_TEMP);
        viewHolder.tvMax.setText(Utility.formatTemperature(max, Utility.isMetric(mContext)));
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return VIEW_TYPE_TODAY;
        else
            return VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }else {
            return mCursor.getCount();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvDate, tvForecast, tvMin, tvMax;
        ImageView ivIcon;

        public ViewHolder(View view) {
            super(view);
            tvDate = (TextView) view.findViewById(R.id.list_item_date_textView);
            tvForecast = (TextView) view.findViewById(R.id.list_item_forecast_textView);
            tvMax = (TextView) view.findViewById(R.id.list_item_max_textView);
            tvMin = (TextView) view.findViewById(R.id.list_item_min_textView);
            ivIcon = (ImageView) view.findViewById(R.id.ivIcon);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

        }
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return mCursor;
    }
}
