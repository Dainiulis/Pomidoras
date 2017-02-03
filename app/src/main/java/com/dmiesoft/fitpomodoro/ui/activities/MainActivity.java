package com.dmiesoft.fitpomodoro.ui.activities;

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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.database.DatabaseContract;
import com.dmiesoft.fitpomodoro.database.ExercisesDataSource;
import com.dmiesoft.fitpomodoro.events.DrawerItemClickedEvent;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;
import com.dmiesoft.fitpomodoro.ui.fragments.ExitDialogFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.TimerFragment;
import com.dmiesoft.fitpomodoro.utils.EventBus;
import com.dmiesoft.fitpomodoro.utils.InitialDatabasePopulation;
import com.squareup.otto.Subscribe;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ExitDialogFragment.ExitListener {

    private static final String TAG = "TAGAS";
    private static final String TIMER_FRAGMENT_TAG = "timer_fragment_tag";
    private static final String EXERCISE_GROUP_FRAGMENT_TAG = "exercise_group_fragment_tag";
    private static final String HISTORY_FRAGMENT_TAG = "history_fragment_tag";
    private static final String EXERCISES_FRAGMENT = "exercises_fragment";
    private static final String EXIT_DIALOG = "EXIT_DIALOG";
    private NavigationView navigationView;
    private List<Fragment> fragments;
    private FragmentManager fragmentManager;
    private ExercisesDataSource dataSource;
    private List<ExercisesGroup> exercisesGroups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        initData();

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            TimerFragment timerFragment = new TimerFragment();
            timerFragment.setRetainInstance(true);
            fragmentManager
                    .beginTransaction()
                    .add(R.id.main_fragment_container, timerFragment, TIMER_FRAGMENT_TAG)
//                    .addToBackStack(null)
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

    private void initData() {
        dataSource = new ExercisesDataSource(this);
        dataSource.open();
        exercisesGroups = dataSource.findAllExerciseGroups();
        if (exercisesGroups.size() == 0) {
            InitialDatabasePopulation idp = new InitialDatabasePopulation(this, dataSource);
            try {
                exercisesGroups = idp.readJson();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            ExitDialogFragment exitDialogFragment = new ExitDialogFragment();
            exitDialogFragment.show(getSupportFragmentManager(), EXIT_DIALOG);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_timer:
                EventBus.getInstance().post(new DrawerItemClickedEvent(fragmentManager, TIMER_FRAGMENT_TAG, null)) ;
                break;

            case R.id.nav_exercise_group:
                EventBus.getInstance().post(new DrawerItemClickedEvent(fragmentManager, EXERCISE_GROUP_FRAGMENT_TAG, exercisesGroups));
                break;
            case R.id.nav_history:
                EventBus.getInstance().post(new DrawerItemClickedEvent(fragmentManager, HISTORY_FRAGMENT_TAG, null));
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
        dataSource.open();
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getInstance().unregister(this);
        dataSource.close();
    }

    @Subscribe
    public void onDrawerItemClicked(DrawerItemClickedEvent event){
        if (event.getFragmentTransaction() != null) {
            event.getFragmentTransaction().commit();
        }
    }

    @Override
    public void onExit(boolean exit) {
        if(exit) {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_deleteDB:
                File dbStorage = getDatabasePath(DatabaseContract.DATABASE_NAME);
                dbStorage.delete();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}
