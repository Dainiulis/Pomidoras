package com.dmiesoft.fitpomodoro.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.database.DatabaseContract;
import com.dmiesoft.fitpomodoro.database.ExercisesDataSource;
import com.dmiesoft.fitpomodoro.events.DeleteObjects;
import com.dmiesoft.fitpomodoro.events.navigation.DrawerItemClickedEvent;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;
import com.dmiesoft.fitpomodoro.ui.fragments.ExerciseDetailFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.ExercisesFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.ExercisesGroupsFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.dialogs.AddExerciseDialog;
import com.dmiesoft.fitpomodoro.ui.fragments.dialogs.AddExerciseGroupDialog;
import com.dmiesoft.fitpomodoro.ui.fragments.dialogs.ExitDialogFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.TimerUIFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.TimerTaskFragment;
import com.dmiesoft.fitpomodoro.utils.AsyncFirstLoad;
import com.dmiesoft.fitpomodoro.utils.DisplayWidthHeight;
import com.dmiesoft.fitpomodoro.utils.InitialDatabasePopulation;
import com.dmiesoft.fitpomodoro.utils.MultiSelectionHelper;
import com.dmiesoft.fitpomodoro.utils.ObjectsHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,
        ExitDialogFragment.ExitListener,
        ExercisesGroupsFragment.ExercisesGroupsListFragmentListener,
        ExercisesFragment.ExercisesListFragmentListener,
        AddExerciseGroupDialog.AddExerciseGroupDialogListener,
        AddExerciseDialog.AddExerciseDialogListener,
        ExerciseDetailFragment.ExerciseDetailFragmentListener,
        AsyncFirstLoad.AsyncFirstLoadListrener{

    private static final String TAG = "MAct";

    /*
     * @Fragments tags
     */
    public static final String TIMER_FRAGMENT_TAG = "timer_fragment_tag";
    public static final String TIMER_TASK_FRAGMENT_TAG = "timer_task_fragment_tag";
    public static final String EXERCISE_GROUP_FRAGMENT_TAG = "exercise_group_fragment_tag";
    public static final String HISTORY_FRAGMENT_TAG = "history_fragment_tag";
    public static final String EXERCISES_FRAGMENT_TAG = "exercises_fragment";
    public static final String EXERCISE_DETAIL_FRAGMENT_TAG = "exercise_detail_fragment_tag";
    private static final String EXIT_DIALOG = "EXIT_DIALOG";
    private static final String ADD_EXERCISE_GROUP_DIALOG = "add_exercise_group_dialog";
    private static final String ADD_EXERCISE_DIALOG = "add_exercise_dialog";
    /*
     * @Other constants
     */
    private static final String EXERCISES_GROUPS = "EXERCISES_GROUPS";
    private static final String EXERCISES = "EXERCISES";
    private static final String OBJ_TO_DELETE = "OBJ_TO_DELETE";
    private static final String WHAT_TO_DELETE = "WHAT_TO_DELETE";
    private static final String DELETE_BACKGROUND_COLOR = "#585859";
    private static final String TREE_MAP = "TREE_MAP";
    /*
     * @PERMISSIONS CODES
     */
    public static final int PERMISSIONS_REQUEST_R_W_STORAGE = 1;

    private NavigationView navigationView;
    private List<Fragment> fragments;
    private FragmentManager fragmentManager;
    private ExercisesDataSource dataSource;
    private List<ExercisesGroup> exercisesGroups;
    private List<Exercise> exercises;
    private CoordinatorLayout mainLayout;

    private List<Integer> deleteIdList;
    private String whatToDelete;
    private TreeMap<Integer, ?> map;
    private Snackbar snackbar;
    private Snackbar.Callback snackbarCallback;

    private FloatingActionButton mainFab, addFab, addFavFab, deleteFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        deleteIdList = new ArrayList<>();
        if (savedInstanceState != null) {
            deleteIdList = savedInstanceState.getIntegerArrayList(OBJ_TO_DELETE);
            exercisesGroups = savedInstanceState.getParcelableArrayList(EXERCISES_GROUPS);
            exercises = savedInstanceState.getParcelableArrayList(EXERCISES);
            whatToDelete = savedInstanceState.getString(WHAT_TO_DELETE);
            map = (TreeMap<Integer, ?>) savedInstanceState.getSerializable(TREE_MAP);
        }

        mainLayout = (CoordinatorLayout) findViewById(R.id.mainLayout);
        initData();
        initFabs();

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

    private void initFabs() {
        mainFab = (FloatingActionButton) findViewById(R.id.fab_main);
        addFab = (FloatingActionButton) findViewById(R.id.fab_add);
        addFavFab = (FloatingActionButton) findViewById(R.id.fab_add_favorites);
        deleteFab = (FloatingActionButton) findViewById(R.id.fab_delete);
    }

    public FloatingActionButton getDeleteFab() {
        return deleteFab;
    }

    public FloatingActionButton getMainFab() {
        return mainFab;
    }

    public FloatingActionButton getAddFab() {
        return addFab;
    }

    public FloatingActionButton getAddFavFab() {
        return addFavFab;
    }

    private void initData() {
        dataSource = new ExercisesDataSource(this);
        dataSource.open();
        if (exercisesGroups == null) {
            exercisesGroups = dataSource.findExerciseGroups(null, null);
            if (exercisesGroups.size() == 0) {
                AsyncFirstLoad asyncFirstLoad = new AsyncFirstLoad(this, dataSource);
                asyncFirstLoad.execute();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(EXERCISES_GROUPS, (ArrayList<ExercisesGroup>) exercisesGroups);
        outState.putParcelableArrayList(EXERCISES, (ArrayList<Exercise>) exercises);
        outState.putIntegerArrayList(OBJ_TO_DELETE, (ArrayList<Integer>) deleteIdList);
        outState.putString(WHAT_TO_DELETE, whatToDelete);
        if (map != null && map.size() > 0) {
            outState.putSerializable(TREE_MAP, map);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (fragmentManager.getBackStackEntryCount() > 0) {
            clearMultiSelection();
            if (snackbar != null) {
                snackbar.dismiss();
            }
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
                EventBus.getDefault().post(new DrawerItemClickedEvent(fragmentManager, EXERCISE_GROUP_FRAGMENT_TAG, exercisesGroups, false, -1));
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
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        setCheckedCurrentNavigationDrawer();
        dataSource.open();
        if (map != null) {
            snackbarCallback = getSnackbarCallback();
            snackbar = getSnackbar();
            snackbar.addCallback(snackbarCallback);
            snackbar.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        if (snackbar != null) {
            snackbar.removeCallback(snackbarCallback);
        }
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
    public boolean onPrepareOptionsMenu(Menu menu) {

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(EXERCISE_DETAIL_FRAGMENT_TAG);
        boolean isFragVisible = false;
        if (fragment != null) {
            isFragVisible = getSupportFragmentManager().findFragmentByTag(EXERCISE_DETAIL_FRAGMENT_TAG).isVisible();
        }
        if (!isFragVisible) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        } else {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(((ExerciseDetailFragment) fragment).getColor()));
        }

        if (deleteIdList.size() > 0) {
            menu.findItem(R.id.action_delete).setVisible(true);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(DELETE_BACKGROUND_COLOR)));
        } else {
            menu.findItem(R.id.action_delete).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_log:
                DisplayWidthHeight displayWidthHeight = new DisplayWidthHeight(this);
                float width = (int) displayWidthHeight.getWidth();
                float density = getResources().getDisplayMetrics().density;
                float dp = width / density;
                Log.i(TAG, "width: " + width + " density " + density + " dp " + dp);
                break;
            case R.id.action_delete:
                map = new TreeMap<>();
                map = MultiSelectionHelper.removeItems(exercisesGroups, exercises, deleteIdList, whatToDelete);
                snackbar = getSnackbar();
                snackbarCallback = getSnackbarCallback();
                snackbar.addCallback(snackbarCallback);
                snackbar.show();
                updateListViews();
                clearMultiSelection();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @NonNull
    private Snackbar getSnackbar() {
        Snackbar snackbar = Snackbar.make(mainLayout, "Deleted " + map.size() + " items ", Snackbar.LENGTH_LONG)
                .setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (Object o : map.entrySet()) {
                            Map.Entry pair = (Map.Entry) o;
                            if (whatToDelete.equals(ExercisesGroup.class.toString())) {
                                exercisesGroups.add((int) pair.getKey(), (ExercisesGroup) pair.getValue());
                            } else {
                                exercises.add((int) pair.getKey(), (Exercise) pair.getValue());
                            }
                        }
                        updateListViews();
                        whatToDelete = null;
                        map.clear();
                    }
                });
        return snackbar;
    }

    private Snackbar.Callback getSnackbarCallback() {
        return new Snackbar.Callback() {

            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                    MultiSelectionHelper.removeItemsFromDatabase(map, MainActivity.this, dataSource, whatToDelete);
                    updateListViews();
                    whatToDelete = null;
                    map.clear();
                }
            }
        };
    }

    private void updateListViews() {
        if (whatToDelete == null) {
            return;
        }
        if (whatToDelete.equals(ExercisesGroup.class.toString())) {
            ExercisesGroupsFragment fragment = (ExercisesGroupsFragment) getSupportFragmentManager().findFragmentByTag(EXERCISE_GROUP_FRAGMENT_TAG);
            if (fragment != null) {
                fragment.updateListView(null);
            }
        } else {
            ExercisesFragment fragment = (ExercisesFragment) getSupportFragmentManager().findFragmentByTag(EXERCISES_FRAGMENT_TAG);
            if (fragment != null) {
                fragment.updateListView(null);
            }
        }
    }

    /*
     * Callback methods
     */

    /*
     * -------------------------------
     * Exercises callbacks
     * -------------------------------
     */

    @Override
    public void onAddExerciseBtnClicked(long exercisesGroupId) {
        List<Exercise> exercises = dataSource.findExercises(null, null);
        AddExerciseDialog dialog = AddExerciseDialog.newInstance(exercises, null, exercisesGroupId, AddExerciseDialog.NO_EDIT);
        dialog.setCancelable(false);
        dialog.show(getSupportFragmentManager(), ADD_EXERCISE_DIALOG);
    }

    @Override
    public void onEditExerciseLongClicked(Exercise exercise, boolean description) {
        openEditExerciseDialog(exercise, description);
    }

    private void openEditExerciseDialog(Exercise exercise, boolean description) {
        List<Exercise> exercises = dataSource.findExercises(null, null);
        int editCode = 0;
        if (description) {
            editCode = AddExerciseDialog.EDIT_DESCRIPTION;
        } else {
            editCode = AddExerciseDialog.EDIT_IMAGE_LAYOUT;
        }
        AddExerciseDialog dialog = AddExerciseDialog.newInstance(exercises, exercise, exercise.getExerciseGroupId(), editCode);
        dialog.setCancelable(false);
        dialog.show(getSupportFragmentManager(), ADD_EXERCISE_DIALOG);
    }

    @Override
    public void onExerciseClicked(Exercise exercise) {
        EventBus.getDefault().post(new DrawerItemClickedEvent(fragmentManager, EXERCISE_DETAIL_FRAGMENT_TAG, exercise, true));
    }

    @Override
    public void onExerciseLongClicked(Exercise exercise) {
        openEditExerciseDialog(exercise, false);
    }

    @Override
    public void onSaveExerciseClicked(Exercise exercise) {
        Exercise newExercise = dataSource.createExercise(exercise);
        ExercisesFragment fragment = (ExercisesFragment) getSupportFragmentManager().findFragmentByTag(EXERCISES_FRAGMENT_TAG);
        if (fragment != null) {
            fragment.updateListView(newExercise);
        }
    }

    @Override
    public void onUpdateExerciseClicked(Exercise exercise) {
        dataSource.updateExercise(exercise);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(EXERCISES_FRAGMENT_TAG);
        if (fragment != null && fragment.isVisible()) {
            ((ExercisesFragment) fragment).updateListView(exercise);
        } else {
            fragment = getSupportFragmentManager().findFragmentByTag(EXERCISE_DETAIL_FRAGMENT_TAG);
            if (fragment != null && fragment.isVisible()) {
                ((ExerciseDetailFragment) fragment).setExercise(exercise);
            }
        }
    }

    //---------------------------------------------------------------------------------------

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~
     * ExercisesGroups callbacks
     * ~~~~~~~~~~~~~~~~~~~~~~~~~
     */
    @Override
    public void onSaveExercisesGroupClicked(ExercisesGroup exercisesGroup) {
        ExercisesGroup newExercisesGroup = dataSource.createExercisesGroup(exercisesGroup);
        ExercisesGroupsFragment fragment = (ExercisesGroupsFragment) getSupportFragmentManager().findFragmentByTag(EXERCISE_GROUP_FRAGMENT_TAG);
        if (fragment != null) {
            fragment.updateListView(newExercisesGroup);
        }
//        exercisesGroups.add(newExercisesGroup);
    }

    @Override
    public void onUpdateExercisesGroupClicked(ExercisesGroup exercisesGroup) {
        dataSource.updateExercisesGroup(exercisesGroup);
        ExercisesGroupsFragment fragment = (ExercisesGroupsFragment) getSupportFragmentManager().findFragmentByTag(EXERCISE_GROUP_FRAGMENT_TAG);
        if (fragment != null) {
            fragment.updateListView(exercisesGroup);
        }
//        exercisesGroups = dataSource.findExerciseGroups(null, null);
    }

    @Override
    public void onExercisesGroupItemClicked(long exercisesGroupId) {
        String selection = DatabaseContract.ExercisesTable.COLUMN_GROUP_ID + " = ?";
        String[] selectionArgs = {String.valueOf(exercisesGroupId)};
        exercises = dataSource.findExercises(selection, selectionArgs);
        EventBus.getDefault().post(new DrawerItemClickedEvent(fragmentManager, EXERCISES_FRAGMENT_TAG, exercises, true, exercisesGroupId));
    }

    @Override
    public void onExercisesGroupItemLongClicked(long exercisesGroupId) {
        String selection = DatabaseContract.ExercisesGroupsTable._ID + "=?";
        String[] selectionArgs = {String.valueOf(exercisesGroupId)};
        List<ExercisesGroup> group = dataSource.findExerciseGroups(selection, selectionArgs);
        ExercisesGroup exercisesGroup = group.get(0);
        AddExerciseGroupDialog dialog = AddExerciseGroupDialog.newInstance(exercisesGroups, exercisesGroup, true);
        dialog.setCancelable(false);
        dialog.show(getSupportFragmentManager(), ADD_EXERCISE_GROUP_DIALOG);
    }


    @Override
    public void onAddExerciseGroupBtnClicked() {
        AddExerciseGroupDialog dialog = AddExerciseGroupDialog.newInstance(exercisesGroups, null, false);
        dialog.setCancelable(false);
        dialog.show(getSupportFragmentManager(), ADD_EXERCISE_GROUP_DIALOG);
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /*
     * ..........................................................
     * Subscriptions
     * ..........................................................
     */

    @Subscribe
    public void onDrawerItemClicked(DrawerItemClickedEvent event) {
        if (event.getFragmentTransaction() != null) {
            clearMultiSelection();
            if (snackbar != null) {
                snackbar.dismiss();
            }
            if (exercisesGroups != null) {
                ObjectsHelper.uncheckExercisesGroups(exercisesGroups);
            }
            if (exercises != null) {
                ObjectsHelper.uncheckExercises(exercises);
            }
            event.getFragmentTransaction().commit();
        }
    }

    private void clearMultiSelection() {
        deleteIdList.clear();
        invalidateOptionsMenu();
    }

    @Subscribe
    public void onDeleteObject(DeleteObjects event) {
        Integer id = event.getId();
        whatToDelete = event.getClassName();
        if (deleteIdList.contains(id)) {
            deleteIdList.remove(id);
        } else {
            deleteIdList.add(id);
        }
        invalidateOptionsMenu();
    }

    //........................................................................................

    /*
     * ***************************************
     * Permission handler
     * ***************************************
     */
    public boolean hasPermissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission(String[] permissions, int PERMISSION_REQUEST_CODE) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_R_W_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Snackbar.make(mainLayout, "Please enable storage permission", Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_R_W_STORAGE);
                            }
                        }).show();
            } else {
                Snackbar.make(mainLayout, "Please enable storage permissions from settings", Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        }).show();
            }
        }
    }

    @Override
    public void onPostFirstLoadExecute(List<ExercisesGroup> exercisesGroups) {
        this.exercisesGroups = exercisesGroups;
    }

    //**********************************************************************************************
}
