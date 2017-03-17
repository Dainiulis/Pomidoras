package com.dmiesoft.fitpomodoro.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.database.ExercisesDataSource;
import com.dmiesoft.fitpomodoro.events.UnfavoriteObjects;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.model.Favorite;
import com.dmiesoft.fitpomodoro.utils.adapters.ExercisesListAdapter;
import com.dmiesoft.fitpomodoro.utils.helpers.ObjectsHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity {

    private static final String TAG = "FAct";
    private static final String UNFAVORITE_LIST = "UNFAVORITE_LIST";
    private Favorite favorite;
    private ExercisesDataSource dataSource;
    private List<Exercise> exercises;
    private ExercisesListAdapter adapter;
    private ListView listView;
    private List<Integer> unfavoriteIdList;
    private Menu menu;
    private Toolbar toolbar;
    private ValueAnimator animToolbarColor, tempBtnAnimatorAppear, tempBtnAnimatorDisappear;
    private MenuItem unfavoriteItem;
    private ImageView tempMenuUnFavBtn;
    private boolean isUnfavToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getData();
        listView = (ListView) findViewById(R.id.list_fav);
        adapter = new ExercisesListAdapter(this, R.layout.list_exercises, exercises);
        listView.setAdapter(adapter);

        if (savedInstanceState != null) {
            unfavoriteIdList = savedInstanceState.getIntegerArrayList(UNFAVORITE_LIST);
            isUnfavToolbar = unfavoriteIdList.size() != 0;
        }

        getSupportActionBar().setTitle(favorite.getName());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putIntegerArrayList(UNFAVORITE_LIST, (ArrayList<Integer>) unfavoriteIdList);
        super.onSaveInstanceState(outState);
    }

    private void getData() {
        unfavoriteIdList = new ArrayList<>();
        dataSource = new ExercisesDataSource(this);
        dataSource.open();
        favorite = getIntent().getParcelableExtra(MainActivity.FAVORITE_PACKAGE_NAME);
        exercises = dataSource.findFavoriteExercises(favorite.getId());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.favorites_menu, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        unfavoriteItem = menu.findItem(R.id.action_unfavorite);
        if (unfavoriteIdList.size() > 0) {
            getSupportActionBar().setTitle("" + unfavoriteIdList.size());
            unfavoriteItem.setVisible(true);
            if (!isUnfavToolbar) {
                initTempMenuButton();
                tempBtnAnimatorAppear.start();
                initToolbarAnimation();
                animToolbarColor.start();
            } else {
                toolbar.setBackgroundColor(Color.parseColor(MainActivity.DELETE_BACKGROUND_COLOR));
            }
            isUnfavToolbar = true;
        } else {
            if (isUnfavToolbar) {
                initTempMenuButton();
                initToolbarAnimation();
                tempBtnAnimatorDisappear.start();
                animToolbarColor.reverse();
            } else {
                unfavoriteItem.setVisible(false);
            }
            isUnfavToolbar = false;
            getSupportActionBar().setTitle(favorite.getName());
        }

        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Copied from MainActivity
     *
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

        tempMenuUnFavBtn = new ImageView(this);
        tempMenuUnFavBtn.setImageResource(R.drawable.ic_star);
        tempMenuUnFavBtn.setPadding(pLR, pTB, pLR, pTB);

        unfavoriteItem.setActionView(tempMenuUnFavBtn);

        tempBtnAnimatorAppear = ValueAnimator.ofFloat(0, 1);
        tempBtnAnimatorAppear.setDuration(500);
        tempBtnAnimatorAppear.setInterpolator(new LinearInterpolator());
        tempBtnAnimatorAppear.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                tempMenuUnFavBtn.setAlpha((Float) animation.getAnimatedValue());
            }
        });
        tempBtnAnimatorAppear.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                unfavoriteItem.setActionView(null);
            }
        });
        tempBtnAnimatorDisappear = ValueAnimator.ofFloat(1, 0);
        tempBtnAnimatorDisappear.setDuration(300);
        tempBtnAnimatorDisappear.setInterpolator(new LinearInterpolator());
        tempBtnAnimatorDisappear.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                tempMenuUnFavBtn.setAlpha((Float) animation.getAnimatedValue());
            }
        });
        tempBtnAnimatorDisappear.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                unfavoriteItem.setActionView(null);
                unfavoriteItem.setVisible(false);
                if (unfavoriteIdList.size() > 0) {
                    unfavoriteItem.setVisible(true);
                }
            }
        });
    }

    /**
     * Copied from MainActivity
     *
     * Initializes toolbar animations to let them use whenever they are required
     * burgerAnim is for animating drawer button to arrow
     * <p>
     * animToolbarColor is for animating toolbar color to transition for smooth experience
     */
    private void initToolbarAnimation() {
        animToolbarColor = ObjectAnimator.ofObject(new ArgbEvaluator(),
                getResources().getColor(R.color.colorPrimary),
                Color.parseColor(MainActivity.DELETE_BACKGROUND_COLOR));
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
            case android.R.id.home:
                if (unfavoriteIdList.size() > 0) {
                    clearMultiSelection();
                    adapter.clearViewsToAnimate();
                    adapter.notifyDataSetChanged();
                } else {
                    onBackPressed();
                }
                return true;
            case R.id.action_unfavorite:
                Toast.makeText(this, "Delete favorites " + unfavoriteIdList.size(), Toast.LENGTH_SHORT).show();
                clearMultiSelection();
                adapter.notifyDataSetChanged();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void clearMultiSelection() {
        unfavoriteIdList.clear();
        invalidateOptionsMenu();
        if (exercises != null) {
            ObjectsHelper.uncheckExercises(exercises);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        dataSource.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        dataSource.open();
    }

    @Subscribe
    public void onUnfavoriteClicked(UnfavoriteObjects event) {
        Integer unfavId = (int) event.getId();
        if (unfavoriteIdList.contains(unfavId)) {
            unfavoriteIdList.remove(unfavId);
        } else {
            unfavoriteIdList.add(unfavId);
        }
        invalidateOptionsMenu();
    }
}
