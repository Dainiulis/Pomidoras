package com.dmiesoft.fitpomodoro.utils.helpers;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.database.ExercisesDataSource;
import com.dmiesoft.fitpomodoro.model.Favorite;
import com.dmiesoft.fitpomodoro.ui.activities.MainActivity;

import java.util.List;

public class AlertDialogHelper {

    public static EditText favoritesInput;
    public static FrameLayout.LayoutParams favoritesParams;
    public static ListView manageFavoritesListView;

    public static Dialog dialogToGetPermissionForDbGenerate(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("You don't have any exercises, would you like to generate some?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Jei prireiks, tai arba padaryt firstTimeDatabaseInitialize() public arba padaryt callback
//                firstTimeDatabaseInitialize();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNeutralButton("Don't ask", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        return dialog;
    }

    public static AlertDialog.Builder favoritesDialog(final Context context, final ExercisesDataSource dataSource) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final List<Favorite> favorites = dataSource.getAllFavorites();
        favoritesInput = new EditText(context);
        favoritesInput.setInputType(InputType.TYPE_CLASS_TEXT);

        favoritesParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        int margins = (int) context.getResources().getDimension(R.dimen.dialog_text_view_margins);
        favoritesParams.setMargins(margins, margins / 2, margins, margins / 2);

        builder.setView(favoritesInput);
        builder.setTitle("Add favorites category");
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String favName = AlertDialogHelper.favoritesInput.getText().toString().trim();
                boolean exists = false;
                for (Favorite fav : favorites) {
                    if (fav.getName().toLowerCase().equals(favName.toLowerCase())) {
                        exists = true;
                    }
                }
                if (exists) {
                    Toast.makeText(context, "Favorite with the same name already exists", Toast.LENGTH_SHORT).show();
                } else {
                    dataSource.createFavorite(favName.trim());
                    dialog.dismiss();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder;
    }

    public static AlertDialog.Builder manageFavoritesDialog(Context context, List<Favorite> favorites) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        manageFavoritesListView = new ListView(context);

        favoritesParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        int margins = (int) context.getResources().getDimension(R.dimen.dialog_text_view_margins);
        favoritesParams.setMargins(margins, margins / 2, margins, margins / 2);

        builder.setView(manageFavoritesListView);
        ArrayAdapter<Favorite> adapter = new ArrayAdapter<Favorite>(context, android.R.layout.simple_list_item_1, android.R.id.text1, favorites);
        manageFavoritesListView.setAdapter(adapter);
        builder.setTitle("Favorites");
        return builder;
    }

}
