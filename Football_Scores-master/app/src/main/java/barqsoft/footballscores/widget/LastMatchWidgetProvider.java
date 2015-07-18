package barqsoft.footballscores.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.db.DatabaseContract;

/**
 * Created by Jose on 7/18/15.
 */
public class LastMatchWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            Date fragmentdate = new Date(System.currentTimeMillis()-86400000);
            SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");

            Cursor cursor = context.getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(), null, null, new String[]{mformat.format(fragmentdate)}, null);
            if(cursor.moveToFirst()){
                String homeTeam = cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.HOME_COL));
                String awayTeam = cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.AWAY_COL));
                int homeGoals = cursor.getInt(cursor.getColumnIndex(DatabaseContract.scores_table.HOME_GOALS_COL));
                int awayGoals = cursor.getInt(cursor.getColumnIndex(DatabaseContract.scores_table.AWAY_GOALS_COL));


                // Get the layout for the App Widget and attach an on-click listener
                // to the button
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.simple_widget);

                views.setOnClickPendingIntent(R.id.lastmatch_widget_root, pendingIntent);
                views.setTextViewText(R.id.widget_teams_playing, String.format(context.getString(R.string.widget_teams_playing), homeTeam, awayTeam));
                views.setTextViewText(R.id.widget_score, String.format(context.getString(R.string.score_format), homeGoals, awayGoals));


                // Tell the AppWidgetManager to perform an update on the current app widget
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }


        }

    }
}
