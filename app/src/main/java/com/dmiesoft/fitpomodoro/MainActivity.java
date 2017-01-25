package com.dmiesoft.fitpomodoro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private static final String TAG = "TAGAS";
    private static final int BTN_START = 1001;
    private static final int BTN_PAUSE = 1002;
    private CountDownTimer timer;
    private TextView timerText;
    private static boolean timerRunning = false, timerPaused = false, workTimer = true;
    private long millisecs;
    private int longBreakCounter;
    private SharedPreferences sharedPref;
    private NavigationView navigationView;
    private FloatingActionButton btnStartPauseTimer, btnStopTimer, btnSkipTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        longBreakCounter = 0;
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        initializeViews();
        setTimer();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    //  **Handle millisecs and timer**
    private void setTimer() {
        if (workTimer) {
            timerText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            millisecs = getMillisecs(getDefaultMins(true));
        } else {
            timerText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            millisecs = getMillisecs(getDefaultMins(false));
        }
        timerText.setText(getTimerString(millisecs));
    }

    private int getWhenLongBreak() {
        return sharedPref.getInt(SettingsActivity.PREF_KEY_WHEN_LONG_BREAK, 4);
    }

    private boolean isContinuous() {
        return sharedPref.getBoolean(SettingsActivity.PREF_CONTINUOUS_MODE, false);
    }

    private long getMillisecs(long minutes) {
        //Testavimui pasidaryti minutes * 1000(bus sekundes), naudojimui minutes * 60000(bus minutes)
        return minutes * 1000;
    }

    private long getDefaultMins(boolean workTimer) {
        int defMinutes = 0;
        if (workTimer) {

            defMinutes = sharedPref.getInt(SettingsActivity.PREF_KEY_WORK_TIME, 25);

        } else {
            if (longBreakCounter == getWhenLongBreak()) {
                defMinutes = sharedPref.getInt(SettingsActivity.PREF_KEY_LONG_BREAK_TIME, 15);
                longBreakCounter = 0;
            } else {
                defMinutes = sharedPref.getInt(SettingsActivity.PREF_KEY_REST_TIME, 5);
            }
        }
        return (long) defMinutes;
    }
//  *********************************

    private void initializeViews() {

        timerText = (TextView) findViewById(R.id.timerText);
        timerText.setText(getTimerString(millisecs));

        btnStartPauseTimer = (FloatingActionButton) findViewById(R.id.btnStartPauseTimer);
        btnStartPauseTimer.setOnClickListener(this);
        btnStartPauseTimer.setTag(BTN_START);

        btnStopTimer = (FloatingActionButton) findViewById(R.id.btnStopTimer);
        btnStopTimer.setOnClickListener(this);

        btnSkipTimer = (FloatingActionButton) findViewById(R.id.btnSkipTimer);
        btnSkipTimer.setOnClickListener(this);
        btnSkipTimer.setVisibility(View.GONE);

        btnStopTimer.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initTimer(long timeMilli) {
        timer = new CountDownTimer(timeMilli, 1) {
            @Override
            public void onTick(long millisUntilFinished) {
                String time = getTimerString(millisUntilFinished);
                millisecs = millisUntilFinished;
                timerText.setText(time);
            }

            @Override
            public void onFinish() {
                timer.cancel();
                if (workTimer) {
                    longBreakCounter++;
                }
                workTimer = !workTimer;
                setTimer();
                if (isContinuous()) {
                    initTimer(millisecs);
                } else {
                    timerRunning = false;
                    timerPaused = false;
                    setStartPauseBtn(BTN_START);
                }
            }
        };
        timer.start();
    }

    private String getTimerString(long millisUntilFinished) {
        long sec = (millisUntilFinished / 1000) % 60;
        long min = (millisUntilFinished / 60000);
        return String.format("%02d:%02d", min, sec);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStartPauseTimer:
                handleBtnStartPause();
                break;

            case R.id.btnSkipTimer:

                break;

            case R.id.btnStopTimer:
                handleBtnStop();
                break;
        }
    }

    //  ***Handle buttons***
    private void handleBtnStop() {
        if (timer != null) {
            timer.cancel();
        }
        longBreakCounter = 0;
        workTimer = true;
        setTimer();
        btnStopTimer.setVisibility(View.GONE);
        setStartPauseBtn(BTN_START);
        timerPaused = false;
        timerRunning = false;
    }

    private void handleBtnStartPause() {
        if (btnStartPauseTimer.getTag().equals(BTN_START)) {
            initTimer(millisecs);
            timerRunning = true;
            timerPaused = false;
            btnStopTimer.setVisibility(View.VISIBLE);
            setStartPauseBtn(BTN_PAUSE);
        } else if (btnStartPauseTimer.getTag().equals(BTN_PAUSE)) {
            if (timer != null) {
                timer.cancel();
            }
            timerPaused = true;
            timerRunning = false;
            setStartPauseBtn(BTN_START);
        }
    }
//  *********************************************

    //  Change buttons and timer textview according to btn tag
    private void setStartPauseBtn(int tag) {
        if (tag == BTN_START) {
            btnStartPauseTimer.setTag(tag);
            btnStartPauseTimer.setImageResource(android.R.drawable.ic_media_play);
        } else {
            btnStartPauseTimer.setTag(tag);
            btnStartPauseTimer.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!timerRunning && !timerPaused) {
            setTimer();
        }
        navigationView.getMenu().getItem(0).setChecked(true);
    }
}
