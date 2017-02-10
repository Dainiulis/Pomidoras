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

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.database.ExercisesDataSource;
import com.dmiesoft.fitpomodoro.events.navigation.DrawerItemClickedEvent;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;
import com.dmiesoft.fitpomodoro.ui.fragments.ExercisesFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.ExercisesGroupsFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.ExitDialogFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.TimerUIFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.TimerTaskFragment;
import com.dmiesoft.fitpomodoro.utils.InitialDatabasePopulation;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,
        ExitDialogFragment.ExitListener,
        ExercisesGroupsFragment.ExercisesGroupsListFragmentListener,
        ExercisesFragment.ExercisesListFragmentListener {

    private static final String TAG = "TAGAS";

    public static final String TIMER_FRAGMENT_TAG = "timer_fragment_tag";
    public static final String TIMER_TASK_FRAGMENT_TAG = "timer_task_fragment_tag";
    public static final String EXERCISE_GROUP_FRAGMENT_TAG = "exercise_group_fragment_tag";
    public static final String HISTORY_FRAGMENT_TAG = "history_fragment_tag";
    public static final String EXERCISES_FRAGMENT_TAG = "exercises_fragment";
    public static final String EXERCISE_DETAIL_FRAGMENT_TAG = "exercise_detail_fragment_tag";

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
            TimerTaskFragment timerTaskFragment = new TimerTaskFragment();
            TimerUIFragment timerUIFragment = new TimerUIFragment();
            fragmentManager
                    .beginTransaction()
                    .add(timerTaskFragment, TIMER_TASK_FRAGMENT_TAG)
                    .add(R.id.main_fragment_container, timerUIFragment, TIMER_FRAGMENT_TAG)
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
        } else if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else if (getSupportFragmentManager().findFragmentByTag(TIMER_FRAGMENT_TAG) == null) {
            EventBus.getDefault().post(new DrawerItemClickedEvent(fragmentManager, TIMER_FRAGMENT_TAG, false));
            navigationView.getMenu().getItem(0).setChecked(true);
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
                EventBus.getDefault().post(new DrawerItemClickedEvent(fragmentManager, TIMER_FRAGMENT_TAG, false));
                break;

            case R.id.nav_exercise_group:
                EventBus.getDefault().post(new DrawerItemClickedEvent(fragmentManager, EXERCISE_GROUP_FRAGMENT_TAG, exercisesGroups, false));
                break;
            case R.id.nav_history:
                EventBus.getDefault().post(new DrawerItemClickedEvent(fragmentManager, HISTORY_FRAGMENT_TAG, false));
                break;

            case R.id.nav_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;

        }
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
                }
            }
        } catch (NullPointerException e) {
            Log.i(TAG, "Error " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        setCheckedCurrentNavigationDrawer();
        dataSource.open();
        Log.i(TAG, "onResume: MainActivity");
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        dataSource.close();
    }

    @Override
    public void onExit(boolean exit) {
        if (exit) {
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
            case R.id.action_log:
                Log.i(TAG, "fragai po visu sou " + getSupportFragmentManager().getFragments());
                Log.i(TAG, "Fragu skaicius " + getSupportFragmentManager().getFragments().size());
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onExercisesGroupItemClicked(long exercisesGroupId) {
        List<Exercise> exercises = dataSource.findAllGroupExercises(exercisesGroupId);
        EventBus.getDefault().post(new DrawerItemClickedEvent(fragmentManager, EXERCISES_FRAGMENT_TAG, exercises, true));
    }

    @Override
    public void onExerciseClicked(Exercise exercise) {
        EventBus.getDefault().post(new DrawerItemClickedEvent(fragmentManager, EXERCISE_DETAIL_FRAGMENT_TAG, exercise, true));
    }
    
    @Subscribe
    public void onDrawerItemClicked(DrawerItemClickedEvent event) {
        Log.i(TAG, "onDrawerItemClicked: ");
        if (event.getFragmentTransaction() != null) {
            event.getFragmentTransaction().commit();
        }
    }
}
