package barqsoft.footballscores.widget;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.R;
import barqsoft.footballscores.db.DatabaseContract;

/**
 * Created by Jose on 7/18/15.
 */
public class CollectionRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new mRemoteViewsFactory(this, intent);
    }

    public class mRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        private Cursor itemsCursor;
        @SuppressLint("SimpleDateFormat")
        private final SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
        private int mAppWidgetId;

        public mRemoteViewsFactory(Context context, Intent intent) {
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        public void onCreate() {
            Date fragmentdate = new Date(System.currentTimeMillis());
            itemsCursor = getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(), null, null, new String[]{mformat.format(fragmentdate)}, null);

        }

        @Override
        public void onDataSetChanged() {
            itemsCursor.close();

            Date fragmentdate = new Date(System.currentTimeMillis());
            itemsCursor = getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(), null, null, new String[]{mformat.format(fragmentdate)}, null);
        }

        @Override
        public void onDestroy() {
            itemsCursor.close();
        }


        @Override
        public int getCount() {
            return itemsCursor.getCount();
        }

        public RemoteViews getViewAt(int position) {

            if(itemsCursor.moveToPosition(position)){
                String homeTeam = itemsCursor.getString(itemsCursor.getColumnIndex(DatabaseContract.scores_table.HOME_COL));
                String awayTeam = itemsCursor.getString(itemsCursor.getColumnIndex(DatabaseContract.scores_table.AWAY_COL));
                String date = itemsCursor.getString(itemsCursor.getColumnIndex(DatabaseContract.scores_table.DATE_COL));

                SimpleDateFormat newFormat = new SimpleDateFormat("MM-dd-yyyy");
                try {
                    date = newFormat.format(mformat.parse(date));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                // Get the layout for the App Widget and attach an on-click listener
                // to the button
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.item_collection_widget);

                views.setTextViewText(R.id.widget_teams_playing, String.format(getString(R.string.widget_teams_playing), homeTeam, awayTeam));
                views.setTextViewText(R.id.widget_score, date);

                return views;

            }
            // Return the remote views object.
            return null;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}
