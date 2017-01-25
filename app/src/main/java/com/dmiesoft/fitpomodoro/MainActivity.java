package com.dmiesoft.fitpomodoro;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.dmiesoft.fitpomodoro.exercisesFragments.ExerciseGroupFragment;
import com.dmiesoft.fitpomodoro.timerFragments.TimerFragment;

import java.sql.Time;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "TAGAS";
    private static final String TIMER_FRAGMENT_TAG = "timer_fragment_tag";
    private static final String EXERCISE_GROUP_FRAGMENT_TAG = "exercise_group_fragment_tag";
    private NavigationView navigationView;
    private List<Fragment> fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        if (savedInstanceState == null) {
            TimerFragment timerFragment = new TimerFragment();
            timerFragment.setRetainInstance(true);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.main_fragment_container, timerFragment, TIMER_FRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Log.i(TAG, "onCreate Activity: ");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Toast.makeText(this, "Ot ir neiseisiu is fragmento", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment fragment = null;
        String fragTag = "";
        switch (id) {
            case R.id.nav_timer:
                if (isFragmentCreated(TIMER_FRAGMENT_TAG)){
                    fragment = getSupportFragmentManager()
                            .findFragmentByTag(TIMER_FRAGMENT_TAG);
                } else {
                    fragment = new TimerFragment();
                }
                fragTag = TIMER_FRAGMENT_TAG;
                break;

            case R.id.nav_exercise_group:
                if (isFragmentCreated(EXERCISE_GROUP_FRAGMENT_TAG)) {
                    fragment = getSupportFragmentManager()
                            .findFragmentByTag(EXERCISE_GROUP_FRAGMENT_TAG);
                } else {
                    fragment = new ExerciseGroupFragment();
                }
                fragTag = EXERCISE_GROUP_FRAGMENT_TAG;
                break;

            case R.id.nav_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;

        }

        if (fragment != null && !fragment.isVisible()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_fragment_container, fragment, fragTag)
                    .addToBackStack(null)
                    .commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean isFragmentCreated(String tag) {
        return fragments.contains(getSupportFragmentManager().findFragmentByTag(tag));
    }

    private void setCheckedCurrentNavigationDrawer(){
        fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment.isVisible()){
                switch (fragment.getTag()) {
                    case TIMER_FRAGMENT_TAG:
                        navigationView.getMenu().getItem(0).setChecked(true);
                        break;
                    case EXERCISE_GROUP_FRAGMENT_TAG:
                        navigationView.getMenu().getItem(1).setChecked(true);
                        break;
                }
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setCheckedCurrentNavigationDrawer();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
