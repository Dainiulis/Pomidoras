package com.dmiesoft.fitpomodoro.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.events.DrawerItemClickedEvent;
import com.dmiesoft.fitpomodoro.ui.fragments.ExerciseGroupFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.AlertDialogFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.HistoryFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.TimerFragment;
import com.dmiesoft.fitpomodoro.utils.EventBus;
import com.squareup.otto.Subscribe;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "TAGAS";
    private static final String TIMER_FRAGMENT_TAG = "timer_fragment_tag";
    private static final String EXERCISE_GROUP_FRAGMENT_TAG = "exercise_group_fragment_tag";
    private static final String HISTORY_FRAGMENT_TAG = "history_fragment_tag";
    private static final String EXIT_DIALOG = "EXIT_DIALOG";
    private NavigationView navigationView;
    private List<Fragment> fragments;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            TimerFragment timerFragment = new TimerFragment();
            timerFragment.setRetainInstance(true);
            fragmentManager
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
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            AlertDialogFragment alertDialogFragment = new AlertDialogFragment();
            alertDialogFragment.show(getSupportFragmentManager(), EXIT_DIALOG);
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
                EventBus.getInstance().post(new DrawerItemClickedEvent(fragmentManager, TIMER_FRAGMENT_TAG)) ;
                break;

            case R.id.nav_exercise_group:
                EventBus.getInstance().post(new DrawerItemClickedEvent(fragmentManager, EXERCISE_GROUP_FRAGMENT_TAG));
                break;
            case R.id.nav_history:
                EventBus.getInstance().post(new DrawerItemClickedEvent(fragmentManager, HISTORY_FRAGMENT_TAG));
                break;

            case R.id.nav_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;

        }
        Log.i(TAG, "fragai po visu sou " + getSupportFragmentManager().getFragments());
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setCheckedCurrentNavigationDrawer() {
        fragments = fragmentManager.getFragments();
        try {
            for (Fragment fragment : fragments) {
                if (fragment.isAdded() && !fragment.isHidden()) {
                    switch (fragment.getTag()) {
                        case TIMER_FRAGMENT_TAG:
                            navigationView.getMenu().getItem(0).setChecked(true);
                            break;
                        case EXERCISE_GROUP_FRAGMENT_TAG:
                            navigationView.getMenu().getItem(1).setChecked(true);
                            break;
                        case HISTORY_FRAGMENT_TAG:
                            navigationView.getMenu().getItem(2).setChecked(true);
                            break;
                    }
                    break;
                }
            }
        } catch (NullPointerException e) {
            Log.i(TAG, "Error " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getInstance().register(this);
        setCheckedCurrentNavigationDrawer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getInstance().unregister(this);
    }

    @Subscribe
    public void onDrawerItemClicked(DrawerItemClickedEvent event){
        if (event.getFragmentTransaction() != null) {
            event.getFragmentTransaction().commit();
        }
    }

}
