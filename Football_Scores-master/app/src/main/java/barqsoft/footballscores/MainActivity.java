package barqsoft.footballscores;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import barqsoft.footballscores.service.myFetchService;

//Changed Deprecated ActionBarActivity to AppCompatActivity
public class MainActivity extends AppCompatActivity
{
    public static final String PAGER_CURRENT = "Pager_Current";
    public static final String SELECTED_MATCH = "Selected_match";
    public static final String MY_MAIN = "my_main";

    public static int selected_match_id;
    public static int current_fragment = 2;
    private final String save_tag = "Save Test";
    private PagerFragment my_main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            my_main = new PagerFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, my_main)
                    .commit();

            refreshMatches();

        }



    }

    public void refreshMatches() {
        //Service was started once for each Fragment instantiation, here it will be called at
        //launch and when the user triggers it through pull-to-refresh.
        Intent service_start = new Intent(this, myFetchService.class);
        startService(service_start);
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
        if (id == R.id.action_about)
        {
            Intent start_about = new Intent(this,AboutActivity.class);
            startActivity(start_about);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        Log.v(save_tag,getString(R.string.log_will_save));
        Log.v(save_tag,getString(R.string.log_fragment)+String.valueOf(my_main.mPagerHandler.getCurrentItem()));
        Log.v(save_tag,getString(R.string.log_selected_id)+selected_match_id);

        outState.putInt(PAGER_CURRENT, my_main.mPagerHandler.getCurrentItem());
        outState.putInt(SELECTED_MATCH, selected_match_id);

        getSupportFragmentManager().putFragment(outState, MY_MAIN, my_main);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        Log.v(save_tag, getString(R.string.log_will_retrieve));
        Log.v(save_tag, getString(R.string.log_fragment)+String.valueOf(savedInstanceState.getInt(PAGER_CURRENT)));
        Log.v(save_tag, getString(R.string.log_selected_id)+savedInstanceState.getInt(SELECTED_MATCH));

        current_fragment = savedInstanceState.getInt(PAGER_CURRENT);
        selected_match_id = savedInstanceState.getInt(SELECTED_MATCH);
        my_main = (PagerFragment) getSupportFragmentManager().getFragment(savedInstanceState, MY_MAIN);
        super.onRestoreInstanceState(savedInstanceState);
    }
}
