package com.dmiesoft.fitpomodoro.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import com.dmiesoft.fitpomodoro.database.ExercisesDataSource;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;
import com.dmiesoft.fitpomodoro.utils.helpers.BitmapHelper;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executor;

public class MultiSelectionFragment extends Fragment{

    private static final String TAG = "MSF";

    private List<Integer> deleteIdList;
    private String whatToDelete;
    private TreeMap<Integer, ?> map;
    private MultiSelectionFragmentListener mListener;

    public MultiSelectionFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MultiSelectionFragmentListener) {
            mListener = (MultiSelectionFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement MultiSelectionFragmentListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setDeleteIdList(List<Integer> deleteIdList) {
        this.deleteIdList = deleteIdList;
    }

    public void setWhatToDelete(String whatToDelete) {
        this.whatToDelete = whatToDelete;
    }

    public void setMap(TreeMap<Integer, ?> map) {
        this.map = map;
    }

    public TreeMap<Integer, ?> getMap() {
        return map;
    }

    @NonNull
    public Snackbar getSnackbar(CoordinatorLayout mainLayout, final List<Exercise> exercises, final List<ExercisesGroup> exercisesGroups, final TreeMap<Integer, ?> map) {
        Snackbar snackbar = Snackbar.make(mainLayout, "Deleted " + map.size() + " items ", Snackbar.LENGTH_LONG)
                .setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "onClick: " + map);
                        for (Object o : map.entrySet()) {
                            Map.Entry pair = (Map.Entry) o;
                            if (whatToDelete.equals(ExercisesGroup.class.toString())) {
                                exercisesGroups.add((int) pair.getKey(), (ExercisesGroup) pair.getValue());
                            } else {
                                exercises.add((int) pair.getKey(), (Exercise) pair.getValue());
                            }
                        }
                        map.clear();
                        mListener.onSnackbarGone(true);
                    }
                });
        return snackbar;
    }

    public Snackbar.Callback getSnackbarCallback(final ExercisesDataSource dataSource, final Context context, final TreeMap<Integer, ?> map) {
        return new Snackbar.Callback() {

            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                    removeItemsFromDatabase(dataSource, context, map);
                    map.clear();
                    mListener.onSnackbarGone(false);
                }
            }
        };
    }


    /** Removes items from database. After this method, the removal is irreversible.
     * @param dataSource
     * @param context
     * @param map
     */
    private void removeItemsFromDatabase(ExercisesDataSource dataSource, Context context, TreeMap<Integer, ?> map) {
        if (whatToDelete == null) {
            return;
        }
        File[] allImages = new File(context.getFilesDir(), "images").listFiles();
        for (Object o : map.values()) {
            if (whatToDelete.equals(ExercisesGroup.class.toString())) {
                ExercisesGroup eG = ((ExercisesGroup)o);
                File file = null;
                if (eG.getImage() != null) {
                    file = BitmapHelper.getFileFromImages(eG.getImage(), context);
                }
                for (int i = 0; i < allImages.length; i++) {
                    String imgWithExerciseId = allImages[i].getName();
                    if (imgWithExerciseId.contains("@" + eG.getId() +"@")){
                        allImages[i].delete();
                    }
                }
                if (file != null) {
                    file.delete();
                }
                dataSource.deleteExercisesGroup(eG.getId());

            } else {
                Exercise e = ((Exercise)o);
                File file = BitmapHelper.getFileFromImages(e.getImage(), context);
                if (file != null) {
                    file.delete();
                }
                dataSource.deleteExercise(e.getId());
            }
        }
    }

    /**
     * Remove items from either exerciseGroup or exercise list and set a map object which contains
     * the removed exercisesGroup or exercise position in list and the object itself.
     * @param exercisesGroups
     * @param exercises
     */
    public void removeItems(List<ExercisesGroup> exercisesGroups, List<Exercise> exercises) {
        TreeMap<Integer, ExercisesGroup> eGMap = null;
        TreeMap<Integer, Exercise> eMap = null;
        if (whatToDelete.equals(ExercisesGroup.class.toString())) {
            eGMap = new TreeMap<>();
        } else {
            eMap = new TreeMap<>();
        }
        int currExId;
        for (int i = 0; deleteIdList.size() > i; i++) {
            currExId = deleteIdList.get(i);
            if (whatToDelete.equals(ExercisesGroup.class.toString())) {
                ExercisesGroup eG = exercisesGroups.get(currExId);
                eG.setChecked(false);
                if (eGMap != null) {
                    eGMap.put(currExId, eG);
                }
            } else {
                Exercise e = exercises.get(currExId);
                e.setChecked(false);
                if (eMap != null) {
                    eMap.put(currExId, e);
                }
            }
        }
        for (int i = 0; deleteIdList.size() > i; i++) {
            currExId = deleteIdList.get(i);
                        /*
                         * Logic:
                         * if currExId (id that is going to be removed) is less than
                         * the next id, then decrement the next id which is going to be removed
                         * because after removing exercisesGroup all id's that are higher
                         * is going to decrease by 1
                         * else do nothing
                         *
                         * after loop remove exercisesGroup
                         */
            for (int j = i + 1; deleteIdList.size() > j; j++) {
                int exGrIdToDecrement = deleteIdList.get(j);
                if (currExId < exGrIdToDecrement) {
                    deleteIdList.set(j, exGrIdToDecrement - 1);
                }
            }
            if (whatToDelete.equals(ExercisesGroup.class.toString())) {
                exercisesGroups.remove(currExId);
            } else {
                exercises.remove(currExId);
            }
        }
        if (whatToDelete.equals(ExercisesGroup.class.toString())) {
            setMap(eGMap);
        } else {
            setMap(eMap);
        }
    }

    public interface MultiSelectionFragmentListener{
        void onSnackbarGone(boolean undo);
    }
}
