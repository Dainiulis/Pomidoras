package com.dmiesoft.fitpomodoro.ui.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.database.DatabaseContract;
import com.dmiesoft.fitpomodoro.database.ExercisesDataSource;
import com.dmiesoft.fitpomodoro.events.DeleteObjects;
import com.dmiesoft.fitpomodoro.events.exercises.RequestForNewExerciseEvent;
import com.dmiesoft.fitpomodoro.events.exercises.UpdateNestedExerciseHistoryEvent;
import com.dmiesoft.fitpomodoro.events.navigation.DrawerItemClickedEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerHandlerEvent;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.model.ExerciseHistory;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;
import com.dmiesoft.fitpomodoro.model.Favorite;
import com.dmiesoft.fitpomodoro.ui.fragments.ExerciseDetailFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.ExercisesFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.ExercisesGroupsFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.dialogs.AddExerciseDialog;
import com.dmiesoft.fitpomodoro.ui.fragments.dialogs.AddExerciseGroupDialog;
import com.dmiesoft.fitpomodoro.ui.fragments.dialogs.ExitDialogFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.TimerUIFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.TimerTaskFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.nested.NestedExerciseHistoryListFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.nested.NestedSaveExerciseFragment;
import com.dmiesoft.fitpomodoro.utils.helpers.AlertDialogHelper;
import com.dmiesoft.fitpomodoro.utils.helpers.DisplayHelper;
import com.dmiesoft.fitpomodoro.utils.MultiSelectionFragment;
import com.dmiesoft.fitpomodoro.utils.helpers.CheckUncheckExerciseHelper;
import com.dmiesoft.fitpomodoro.utils.FirstTimeDataLoader;
import com.dmiesoft.fitpomodoro.utils.preferences.TimerPreferenceManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,
        ExitDialogFragment.ExitListener,
        ExercisesGroupsFragment.ExercisesGroupsListFragmentListener,
        ExercisesFragment.ExercisesListFragmentListener,
        MultiSelectionFragment.MultiSelectionFragmentListener,
        TimerUIFragment.TimerUIFragmentListener,
        NestedSaveExerciseFragment.NestedExerciseFragListener,
        NestedExerciseHistoryListFragment.OnListFragmentInteractionListener,
        FirstTimeDataLoader.AsyncFirstDataLoaderListener {

    private static final String TAG = "MAct";

    /*
     * @Fragments tags
     */
    public static final String TIMER_UI_FRAGMENT_TAG = "timer_fragment_tag";
    public static final String TIMER_TASK_FRAGMENT_TAG = "timer_task_fragment_tag";
    public static final String EXERCISE_GROUP_FRAGMENT_TAG = "exercise_group_fragment_tag";
    public static final String STATISTICS_FRAGMENT_TAG = "history_fragment_tag";
    public static final String EXERCISES_FRAGMENT_TAG = "exercises_fragment";
    public static final String EXERCISE_DETAIL_FRAGMENT_TAG = "exercise_detail_fragment_tag";
    private static final String EXIT_DIALOG = "EXIT_DIALOG";
    public static final String ADD_EXERCISE_GROUP_DIALOG = "add_exercise_group_dialog";
    public static final String ADD_EXERCISE_DIALOG = "add_exercise_dialog";
    public static final String MULTI_SELECTION_FRAGMENT = "multi_selection_fragment";

    /*
     * @Other constants
     */
    private static final String EXERCISES_GROUPS = "EXERCISES_GROUPS";
    private static final String EXERCISES = "EXERCISES";
    private static final String OBJ_TO_DELETE = "OBJ_TO_DELETE";
    private static final String WHAT_TO_DELETE = "WHAT_TO_DELETE";
    public static final String DELETE_BACKGROUND_COLOR = "#585859";
    /*
     * Model Packages names
     */
    public static final String FAVORITE_PACKAGE_NAME = ".model.Favorite";

    /*
     * @PERMISSIONS CODES
     */
    public static final int PERMISSIONS_REQUEST_R_W_STORAGE = 1;

    private NavigationView navigationView;
    private List<Fragment> fragments;
    private FragmentManager fragmentManager;
    private List<ExercisesGroup> exercisesGroups;
    private List<Exercise> exercises;
    private CoordinatorLayout mainLayout;
    private TimerTaskFragment timerTaskFragment;
    private List<Integer> deleteIdList;
    private String whatToDelete;
    private Snackbar snackbar;
    private Snackbar.Callback snackbarCallback;
    private MultiSelectionFragment multiSelectionFragment;
    private TreeMap<Integer, ?> map;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawer;
    private FloatingActionButton mainFab;
    private Toolbar toolbar;
    private View.OnClickListener navigationClickListener;
    private boolean isDeleteToolbar;
    private ImageView tempMenuDelBtn, tempMenuFavBtn;
    private Menu menu;
    private ValueAnimator animToolbarColor, burgerAnim, tempBtnAnimatorAppear, tempBtnAnimatorDisappear;
//    private FitPomodoroApplication appContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TimerPreferenceManager.initPreferences(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");

        deleteIdList = new ArrayList<>();

//        appContext = (FitPomodoroApplication) getApplicationContext();

        if (savedInstanceState != null) {
            exercisesGroups = savedInstanceState.getParcelableArrayList(EXERCISES_GROUPS);
            exercises = savedInstanceState.getParcelableArrayList(EXERCISES);
            deleteIdList = savedInstanceState.getIntegerArrayList(OBJ_TO_DELETE);
            whatToDelete = savedInstanceState.getString(WHAT_TO_DELETE);
            isDeleteToolbar = deleteIdList.size() != 0;
        }
        mainLayout = (CoordinatorLayout) findViewById(R.id.mainLayout);
        initData();
        initFabs();

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        fragmentManager = getSupportFragmentManager();

        multiSelectionFragment = (MultiSelectionFragment) fragmentManager.findFragmentByTag(MULTI_SELECTION_FRAGMENT);
        if (multiSelectionFragment != null) {
            map = multiSelectionFragment.getMap();
        }

        if (savedInstanceState == null) {
            timerTaskFragment = new TimerTaskFragment();
            TimerUIFragment timerUIFragment = new TimerUIFragment();
            fragmentManager
                    .beginTransaction()
                    .add(timerTaskFragment, TIMER_TASK_FRAGMENT_TAG)
                    .add(R.id.main_fragment_container, timerUIFragment, TIMER_UI_FRAGMENT_TAG)
                    .commit();
        }

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationClickListener = toggle.getToolbarNavigationClickListener();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initFabs() {
        mainFab = (FloatingActionButton) findViewById(R.id.fab_main);
    }

    public FloatingActionButton getMainFab() {
        return mainFab;
    }

    private void initData() {
//        dataSource = new ExercisesDataSource(this);
//        dataSource.open();
        if (exercisesGroups == null) {
//            exercisesGroups = dataSource.findExerciseGroups(null, null);
            exercisesGroups = ExercisesDataSource.findExerciseGroups(this, null, null);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(EXERCISES_GROUPS, (ArrayList<ExercisesGroup>) exercisesGroups);
        outState.putParcelableArrayList(EXERCISES, (ArrayList<Exercise>) exercises);
        outState.putIntegerArrayList(OBJ_TO_DELETE, (ArrayList<Integer>) deleteIdList);
        outState.putString(WHAT_TO_DELETE, whatToDelete);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (deleteIdList.size() > 0) {
            clearMultiSelection();
            updateListViews(false);
        } else if (fragmentManager.getBackStackEntryCount() > 0) {
            clearMultiSelection();
            if (snackbar != null) {
                snackbar.dismiss();
            }
            fragmentManager.popBackStack();
        } else if (getSupportFragmentManager().findFragmentByTag(TIMER_UI_FRAGMENT_TAG) == null) {
            EventBus.getDefault().post(new DrawerItemClickedEvent(fragmentManager, TIMER_UI_FRAGMENT_TAG, false));
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
                EventBus.getDefault().post(new DrawerItemClickedEvent(fragmentManager, TIMER_UI_FRAGMENT_TAG, false));
                break;

            case R.id.nav_exercise_group:
                EventBus.getDefault().post(new DrawerItemClickedEvent(fragmentManager, EXERCISE_GROUP_FRAGMENT_TAG, exercisesGroups, false, -1));
                break;
            case R.id.nav_history:
                EventBus.getDefault().post(new DrawerItemClickedEvent(fragmentManager, STATISTICS_FRAGMENT_TAG, false));
                break;

            case R.id.nav_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;

            case R.id.nav_about:
                Intent intent1 = new Intent(this, AboutActivity.class);
                startActivity(intent1);
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
                        case TIMER_UI_FRAGMENT_TAG:
                            navigationView.getMenu().getItem(0).setChecked(true);
                            break;
                        case EXERCISE_GROUP_FRAGMENT_TAG:
                            navigationView.getMenu().getItem(1).setChecked(true);
                            break;
                        case STATISTICS_FRAGMENT_TAG:
                            navigationView.getMenu().getItem(2).setChecked(true);
                            break;
                    }
                }
            }
        } catch (NullPointerException e) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        setCheckedCurrentNavigationDrawer();
        if (multiSelectionFragment != null) {
            if (multiSelectionFragment.isAdded() && map != null) {
                if (map.size() > 0) {
                    snackbarCallback = multiSelectionFragment.getSnackbarCallback(this, map);
                    snackbar = multiSelectionFragment.getSnackbar(mainLayout, exercises, exercisesGroups, map);
                    snackbar.addCallback(snackbarCallback);
                    snackbar.show();
                }
            }
        }
        if (TimerPreferenceManager.isFirstTimeLoad()) {
            firstTimeDatabaseInitialize();
        }
    }

    private void firstTimeDatabaseInitialize() {
        if (exercisesGroups.size() == 0 && !FirstTimeDataLoader.isLoading) {
            new FirstTimeDataLoader(this, getSupportLoaderManager()).startLoader();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        if (snackbar != null) {
            snackbar.removeCallback(snackbarCallback);
        }
        if (isFinishing()) {
            fragmentManager.beginTransaction().remove(multiSelectionFragment);
        }
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
        this.menu = menu;
        return true;
    }

    /**
     * Initializes temporary button and sets it's animation.
     * Firstly it sets the menu button as ActionView so it is possible to animate it.
     * The pLR and pTB are paddings of left right and top bottom, which I calculated as dp. So far it looks fine
     * When the animation ends the actionView is set to null and menu button can be used normally.
     * This button is used to temporary animate delete menu icon
     * <p>
     * ...Remove if bugs occur
     */
    private void initTempMenuButton() {
        // issiskaiciavau siuos paddingus, tikiuosi geri
        int pLR = (int) getResources().getDimension(R.dimen.menu_del_padd_L_R);
        int pTB = (int) getResources().getDimension(R.dimen.menu_del_padd_T_B);

        tempMenuDelBtn = new ImageView(this);
        tempMenuDelBtn.setImageResource(R.drawable.delete);
        tempMenuDelBtn.setPadding(pLR, pTB, pLR, pTB);

        tempMenuFavBtn = new ImageView(this);
        tempMenuFavBtn.setImageResource(R.drawable.ic_star);
        tempMenuFavBtn.setPadding(pLR, pTB, pLR, pTB);

        menu.findItem(R.id.action_delete).setActionView(tempMenuDelBtn);
        menu.findItem(R.id.action_add_to_favorite).setActionView(tempMenuFavBtn);

        tempBtnAnimatorAppear = ValueAnimator.ofFloat(0, 1);
        tempBtnAnimatorAppear.setDuration(500);
        tempBtnAnimatorAppear.setInterpolator(new LinearInterpolator());
        tempBtnAnimatorAppear.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                tempMenuDelBtn.setAlpha((Float) animation.getAnimatedValue());
                tempMenuFavBtn.setAlpha((Float) animation.getAnimatedValue());
            }
        });
        tempBtnAnimatorAppear.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                menu.findItem(R.id.action_delete).setActionView(null);
                menu.findItem(R.id.action_add_to_favorite).setActionView(null);
            }
        });
        tempBtnAnimatorDisappear = ValueAnimator.ofFloat(1, 0);
        tempBtnAnimatorDisappear.setDuration(300);
        tempBtnAnimatorDisappear.setInterpolator(new LinearInterpolator());
        tempBtnAnimatorDisappear.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                tempMenuDelBtn.setAlpha((Float) animation.getAnimatedValue());
                tempMenuFavBtn.setAlpha((Float) animation.getAnimatedValue());
            }
        });
        tempBtnAnimatorDisappear.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                menu.findItem(R.id.action_delete).setActionView(null);
                menu.findItem(R.id.action_delete).setVisible(false);
                menu.findItem(R.id.action_add_to_favorite).setActionView(null);
                menu.findItem(R.id.action_add_to_favorite).setVisible(false);
                if (deleteIdList.size() > 0) {
                    menu.findItem(R.id.action_delete).setVisible(true);
                    menu.findItem(R.id.action_add_to_favorite).setVisible(true);
                }
            }
        });
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        /**
         * This piece of code checks if any of list fragments with exercises is visible
         * and sets menu items if action_add_fav and action_manage_fav visible or not accordingly
         */
        menu.findItem(R.id.action_add_fav).setVisible(false);
        menu.findItem(R.id.action_manage_fav).setVisible(false);

        Fragment fragmentExGr = fragmentManager.findFragmentByTag(EXERCISE_GROUP_FRAGMENT_TAG);
        if (fragmentExGr != null) {
            if (fragmentExGr.isVisible()) {
                menu.findItem(R.id.action_add_fav).setVisible(true);
                menu.findItem(R.id.action_manage_fav).setVisible(true);
            }
        }
        Fragment fragmentEx = fragmentManager.findFragmentByTag(EXERCISES_FRAGMENT_TAG);
        if (fragmentEx != null) {
            if (fragmentEx.isVisible()) {
                menu.findItem(R.id.action_add_fav).setVisible(true);
                menu.findItem(R.id.action_manage_fav).setVisible(true);
            }
        }

        /**
         * This piece of code checks if fragment is ExerciseDetailFragment
         * and sets the background of the toolbar according to the exercise name
         */
        boolean isFragVisible = false;
        Fragment fragmentExDetail = fragmentManager.findFragmentByTag(EXERCISE_DETAIL_FRAGMENT_TAG);
        if (fragmentExDetail != null) {
            isFragVisible = fragmentManager.findFragmentByTag(EXERCISE_DETAIL_FRAGMENT_TAG).isVisible();
        }
        if (!isFragVisible && deleteIdList.size() == 0) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        } else if (isFragVisible) {
            if (animToolbarColor != null)
                animToolbarColor.cancel();
        }

        /**
         * This piece of code is responsible for showing delete toolbar if there are exercises selected
         * It locks the drawer and also changes onBackPress and homeAsUpEnabled function.
         * If user tries to go back it clears the delete toolbar and sets it to the default one.
         *
         * If there are no exercises selected then the delete toolbar is not shown adn the regular one is visible
         *
         */
        if (deleteIdList.size() > 0) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            getSupportActionBar().setTitle("" + deleteIdList.size());
            toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearMultiSelection();
                    updateListViews(false);
                }
            });
            menu.findItem(R.id.action_delete).setVisible(true);
            if (!isDeleteToolbar) {
                initTempMenuButton();
                tempBtnAnimatorAppear.start();
                initToolbarAnimation();
                animToolbarColor.start();
                burgerAnim.start();
            } else {
                toolbar.setBackgroundColor(Color.parseColor(DELETE_BACKGROUND_COLOR));
                toggle.setDrawerIndicatorEnabled(false);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            isDeleteToolbar = true;
        } else {
            if (isDeleteToolbar) {
                initTempMenuButton();
                initToolbarAnimation();
                tempBtnAnimatorDisappear.start();
                animToolbarColor.reverse();
                burgerAnim.reverse();
            } else {
                menu.findItem(R.id.action_delete).setVisible(false);
                menu.findItem(R.id.action_add_to_favorite).setVisible(false);
            }
            isDeleteToolbar = false;
            getSupportActionBar().setTitle("");
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            toggle.setDrawerIndicatorEnabled(true);
            toggle.setToolbarNavigationClickListener(navigationClickListener);
//            menu.findItem(R.id.action_delete).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }


    /**
     * Initializes toolbar animations to let them use whenever they are required
     * burgerAnim is for animating drawer button to arrow
     * <p>
     * animToolbarColor is for animating toolbar color to transition for smooth experience
     */
    private void initToolbarAnimation() {
        burgerAnim = ValueAnimator.ofFloat(0, 1);
        burgerAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float slideOffset = (float) animation.getAnimatedValue();
                toggle.onDrawerSlide(drawer, slideOffset);
            }
        });
        burgerAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (isDeleteToolbar) {
                    toggle.setDrawerIndicatorEnabled(false);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
                menu.findItem(R.id.action_delete).setActionView(null);
            }
        });
        burgerAnim.setInterpolator(new DecelerateInterpolator());
        burgerAnim.setDuration(500);
        animToolbarColor = ObjectAnimator.ofObject(new ArgbEvaluator(), getResources().getColor(R.color.colorPrimary), Color.parseColor(DELETE_BACKGROUND_COLOR));
        animToolbarColor.setDuration(1000);
        animToolbarColor.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                toolbar.setBackgroundColor((Integer) animation.getAnimatedValue());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_log:
//                DisplayHelper displayHelper = new DisplayHelper(this);
//                float width = (int) displayHelper.getWidth();
//                float height = displayHelper.getHeight();
//                float density = getResources().getDisplayMetrics().density;
//                float dpW = width / density;
//                float dpH = height / density;
//                Log.i(TAG, "width: " + width + " density " + density + " dp " + dpW);
//                Log.i(TAG, "height: " + height + " density " + density + " dp " + dpH);
                firstTimeDatabaseInitialize();

                if (fragmentManager.findFragmentByTag(TIMER_UI_FRAGMENT_TAG) != null) {
                    ((TimerTaskFragment) fragmentManager.findFragmentByTag(TIMER_TASK_FRAGMENT_TAG)).log();
                    ((TimerUIFragment) fragmentManager.findFragmentByTag(TIMER_UI_FRAGMENT_TAG)).log();
                }

                break;

            case R.id.action_delete:
                manageDeleteAction();
                break;

            case R.id.action_add_to_favorite:
                manageFavDialog(1);
                break;

            case R.id.action_add_fav:
                AlertDialog.Builder favBuilder = AlertDialogHelper.favoritesDialog(this);
                favBuilder.show();
                AlertDialogHelper.favoritesInput.setLayoutParams(AlertDialogHelper.favoritesParams);
                break;

            case R.id.action_manage_fav:
                manageFavDialog(0);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows favorites dialog and adds onItemClick listener which works depending on which menu item was pressed
     *
     * @param menuItemPressed which menu item was pressed. Enter 0 for navigating to favorites manager.
     *                        Enter 1 for adding to favorites
     */
    private void manageFavDialog(final int menuItemPressed) {
        final List<Favorite> favorites = ExercisesDataSource.getAllFavorites(this);
        AlertDialog.Builder mngFavBuilder = AlertDialogHelper.manageFavoritesDialog(this, favorites);
        final AlertDialog dialog = mngFavBuilder.create();
        AlertDialogHelper.manageFavoritesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (menuItemPressed == 0) {
                    if (snackbar != null) {
                        snackbar.dismiss();
                    }
                    Intent intent = new Intent(MainActivity.this, FavoriteActivity.class);
                    intent.putExtra(FAVORITE_PACKAGE_NAME, favorites.get(position));
                    startActivity(intent);
                } else {
                    HashMap<Long, Long> favExIdsMap = new HashMap<Long, Long>();
                    if (whatToDelete.equals(ExercisesGroup.class.toString())) {
                        List<Long> exercisesIds = new ArrayList<Long>();
                        for (int i = 0; i < deleteIdList.size(); i++) {
                            String[] selectionArgs = {String.valueOf(exercisesGroups.get(deleteIdList.get(i)).getId())};
                            exercisesIds.addAll(ExercisesDataSource.getExercisesIds(MainActivity.this, DatabaseContract.ExercisesTable.COLUMN_GROUP_ID + "=?", selectionArgs, -1));
                        }
                        for (int i = 0; i < exercisesIds.size(); i++) {
                            favExIdsMap.put(exercisesIds.get(i), favorites.get(position).getId());
                        }
                    } else {
                        for (int i = 0; i < deleteIdList.size(); i++) {
                            favExIdsMap.put(exercises.get(deleteIdList.get(i)).getId(), favorites.get(position).getId());
                        }
                    }
//                    dataSource.createFavExIds(favExIdsMap);
                    ExercisesDataSource.createFavExIds(MainActivity.this, favExIdsMap);
                    Toast.makeText(MainActivity.this, "Added " + favExIdsMap.size() + " items to " + favorites.get(position).getName() + " favorites", Toast.LENGTH_SHORT).show();
                    updateListViews(false);
                    clearMultiSelection();
                }
                dialog.dismiss();
            }
        });
        dialog.show();
        AlertDialogHelper.manageFavoritesListView.setLayoutParams(AlertDialogHelper.favoritesParams);
    }

    /**
     * Manage exercisesGroups or exercises deletion
     */
    private void manageDeleteAction() {
        if (multiSelectionFragment == null) {
            multiSelectionFragment = new MultiSelectionFragment();
            fragmentManager.beginTransaction().add(multiSelectionFragment, MULTI_SELECTION_FRAGMENT).commit();
        }
        // pass the id of items and the class name of which items to delete
        multiSelectionFragment.setDeleteIdList(deleteIdList);
        multiSelectionFragment.setWhatToDelete(whatToDelete);

        multiSelectionFragment.removeItems(exercisesGroups, exercises);
        map = multiSelectionFragment.getMap();

        snackbar = multiSelectionFragment.getSnackbar(mainLayout, exercises, exercisesGroups, map);
        snackbarCallback = multiSelectionFragment.getSnackbarCallback(this, map);
        snackbar.addCallback(snackbarCallback);

        snackbar.show();
        // update adapters of listviews
        updateListViews(true);
        clearMultiSelection();
    }

    private void updateListViews(boolean animate) {

        Fragment fragment = fragmentManager.findFragmentByTag(EXERCISE_GROUP_FRAGMENT_TAG);
        Fragment fragment1 = fragmentManager.findFragmentByTag(EXERCISES_FRAGMENT_TAG);
        if (fragment != null) {
            if (fragment.isVisible()) {
                ((ExercisesGroupsFragment) fragment).updateListView(null, animate);
            }
        }
        if (fragment1 != null) {
            if (fragment1.isVisible()) {
                ((ExercisesFragment) fragment1).updateListView(null, animate);
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
    public void onExerciseClicked(Exercise exercise) {
        EventBus.getDefault().post(new DrawerItemClickedEvent(fragmentManager, EXERCISE_DETAIL_FRAGMENT_TAG, exercise, true));
    }

    //---------------------------------------------------------------------------------------

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~
     * ExercisesGroups callbacks
     * ~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    @Override
    public void onExercisesGroupItemClicked(long exercisesGroupId) {
        String selection = DatabaseContract.ExercisesTable.COLUMN_GROUP_ID + " = ?";
        String[] selectionArgs = {String.valueOf(exercisesGroupId)};
        exercises = ExercisesDataSource.findExercises(this, selection, selectionArgs);
        EventBus.getDefault().post(new DrawerItemClickedEvent(fragmentManager, EXERCISES_FRAGMENT_TAG, exercises, true, exercisesGroupId));
    }

    @Override
    public void onFirstLoadFinished(List<ExercisesGroup> exercisesGroups) {
        this.exercisesGroups = exercisesGroups;
        TimerPreferenceManager.setiSFirstTimeLoad(false);
        Log.i(TAG, "onFirstLoadFinished: ");
    }

    @Override
    public void onSnackbarGone(boolean undo) {
//        whatToDelete = null;
        if (undo) {
            updateListViews(false);
        }
    }

    @Override
    public void onSetRandomExercise() {
        long randExerciseId = TimerPreferenceManager.getCurrentRandomExercise();
        String selection = DatabaseContract.ExercisesTable._ID + "=?";
        String[] selectionArgs = {String.valueOf(randExerciseId)};
        List<Exercise> exercises = ExercisesDataSource.findExercises(this, selection, selectionArgs);
        Exercise exercise = null;
        List<ExerciseHistory> exerciseHistoryList = null;
        if (exercises.size() > 0) {
            exercise = exercises.get(0);
            exerciseHistoryList = ExercisesDataSource.getExerciseHistory(this, exercise.getId());
        }
        TimerUIFragment fragment = (TimerUIFragment) fragmentManager.findFragmentByTag(TIMER_UI_FRAGMENT_TAG);
        if (fragment != null) {
            fragment.setExercise(exercise, exerciseHistoryList);
        }
    }

    @Override
    public void onFavoritesListRequested() {
        List<Favorite> favorites = ExercisesDataSource.getAllFavorites(this);
        TimerUIFragment fragment = (TimerUIFragment) fragmentManager.findFragmentByTag(TIMER_UI_FRAGMENT_TAG);
        if (fragment != null) {
            fragment.setFavorites(favorites);
        }
    }

    @Override
    public void onExerciseDonePressed(int howMany, long exerciseId, String exerciseName, String exerciseType, boolean needNewExercise) {

        Toast.makeText(this,
                "Done " + exerciseName + " " +
                        howMany + " " +
                        ((exerciseType.equalsIgnoreCase(getResources().getString(R.string.reps))) ? "reps" : "sec"),
                Toast.LENGTH_SHORT).show();

        ExercisesDataSource.saveExerciseHistory(this, howMany, exerciseId);
        ExerciseHistory exerciseHistory = new ExerciseHistory();
        exerciseHistory.setHowMany(howMany);
        exerciseHistory.setName(exerciseName);
        EventBus.getDefault().post(new UpdateNestedExerciseHistoryEvent(exerciseHistory));
        if (needNewExercise) {
            EventBus.getDefault().post(new RequestForNewExerciseEvent(true));
        }
    }

    @Override
    public void onListFragmentInteraction(ExerciseHistory exercise) {

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
            event.getFragmentTransaction().commit();
        }
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

    @Subscribe
    public void onTimerTypeStateChanged(TimerHandlerEvent event) {
        Fragment fragment = fragmentManager.findFragmentByTag(TIMER_UI_FRAGMENT_TAG);
        if (fragment == null) {
            EventBus.getDefault().post(new DrawerItemClickedEvent(fragmentManager, TIMER_UI_FRAGMENT_TAG, false));
            navigationView.getMenu().getItem(0).setChecked(true);
        }
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

    //**********************************************************************************************

    /**
     * Helper function to clear multi selection and invalidateOptionsMenu
     */
    private void clearMultiSelection() {
        deleteIdList.clear();
        invalidateOptionsMenu();
        if (exercisesGroups != null) {
            CheckUncheckExerciseHelper.uncheckExercisesGroups(exercisesGroups);
        }
        if (exercises != null) {
            CheckUncheckExerciseHelper.uncheckExercises(exercises);
        }
    }

}
