package com.dmiesoft.fitpomodoro;

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

import com.dmiesoft.fitpomodoro.exercisesFragments.ExerciseGroupFragment;
import com.dmiesoft.fitpomodoro.historyFragments.HistoryFragment;
import com.dmiesoft.fitpomodoro.timerFragments.TimerFragment;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "TAGAS";
    private static final String TIMER_FRAGMENT_TAG = "timer_fragment_tag";
    private static final String EXERCISE_GROUP_FRAGMENT_TAG = "exercise_group_fragment_tag";
    private static final String HISTORY_FRAGMENT_TAG = "history_fragment_tag";
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
                if (isFragmentCreated(TIMER_FRAGMENT_TAG)) {
                    fragment = getSupportFragmentManager()
                            .findFragmentByTag(TIMER_FRAGMENT_TAG);
                }
                fragTag = TIMER_FRAGMENT_TAG;
                break;

            case R.id.nav_exercise_group:
                /*
                if fragment is created then it finds it by tag
                but the problem is that it can find it by tag but
                fragment could be null so if it's null it creates new fragment
                 */
                if (isFragmentCreated(EXERCISE_GROUP_FRAGMENT_TAG)) {
                    fragment = getSupportFragmentManager()
                            .findFragmentByTag(EXERCISE_GROUP_FRAGMENT_TAG);
                    if (fragment == null) {
                        fragment = new ExerciseGroupFragment();
                    }
                } else {
                    fragment = new ExerciseGroupFragment();
                }
                fragTag = EXERCISE_GROUP_FRAGMENT_TAG;
                break;
                /*
                if fragment is created then it finds it by tag
                but the problem is that it can find it by tag but
                fragment could be null so if it's null it creates new fragment
                 */
            case R.id.nav_history:
                if (isFragmentCreated(HISTORY_FRAGMENT_TAG)) {
                    fragment = getSupportFragmentManager()
                            .findFragmentByTag(HISTORY_FRAGMENT_TAG);
                    if (fragment == null) {
                        fragment = new HistoryFragment();
                    }
                } else {
                    fragment = new HistoryFragment();
                }
                fragTag = HISTORY_FRAGMENT_TAG;
                break;

            case R.id.nav_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;

        }
        if (fragment != null && !fragment.isVisible()) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            if (fragment.getTag() != null) {
                /*
                if fragment is TimerFragment then it
                gets all fragments removes all but not the TimerFragment
                */
                if (fragment.getTag().equals(TIMER_FRAGMENT_TAG)) {
                    removeAllFragmentsExceptTimer(fragmentTransaction);
                    // then it shows the TimerFragment
                    fragmentTransaction.show(fragment);
                }
            /*
            if it's not TimeFragment then it hides TimerFragment and adds
            other passed fragment
            */
            } else {
                if (getSupportFragmentManager().findFragmentByTag(TIMER_FRAGMENT_TAG).isVisible()) {
                    fragmentTransaction.hide(getSupportFragmentManager().findFragmentByTag(TIMER_FRAGMENT_TAG));
                    fragmentTransaction.add(R.id.main_fragment_container, fragment, fragTag);
                } else if (getSupportFragmentManager().findFragmentByTag(TIMER_FRAGMENT_TAG).isHidden()) {
                    removeAllFragmentsExceptTimer(fragmentTransaction);
                    fragmentTransaction.add(R.id.main_fragment_container, fragment, fragTag);
                }
            }
            fragmentTransaction.commit();
        }
        Log.i(TAG, "fragai po visu sou " + getSupportFragmentManager().getFragments());
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void removeAllFragmentsExceptTimer(FragmentTransaction fragmentTransaction) {
        for (Fragment frag : getSupportFragmentManager().getFragments()) {
            if (frag != null && frag != getSupportFragmentManager().findFragmentByTag(TIMER_FRAGMENT_TAG)) {
                fragmentTransaction.remove(frag);
            }
        }
    }

    private boolean isFragmentCreated(String tag) {
        return fragments.contains(getSupportFragmentManager().findFragmentByTag(tag));
    }

    private void setCheckedCurrentNavigationDrawer() {
        fragments = getSupportFragmentManager().getFragments();
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
        setCheckedCurrentNavigationDrawer();
    }
}
