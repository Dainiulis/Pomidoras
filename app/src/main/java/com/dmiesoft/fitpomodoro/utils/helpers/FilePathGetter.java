package com.dmiesoft.fitpomodoro.utils.helpers;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

/**
 * Created by Dainius on 2017-02-13.
 */

public class FilePathGetter {

    private static final String TAG = "AEGD";
    private Context context;

    public FilePathGetter(Context context) {
        this.context = context;
    }

    /*
     * code used from
     * http://stackoverflow.com/questions/33208911/get-realpath-return-null-on-android-marshmallow
     */
    public String getPath(Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    String docId = DocumentsContract.getDocumentId(uri);
                    String[] split = docId.split(":");
                    String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                    // bug on nougat. there was no primary type, only different type for every phone
                    // seems to work, even with external card because if it's not primary type then it's user ID
                    // for example 891A-1CE4 or something similar
                    String path = "/storage/" + split[0] + "/" + split[1];
                    return path;
                } else if (isDownloadsDocument(uri)) {
                    String id = DocumentsContract.getDocumentId(uri);
                    Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getRealPathFromURI(contentUri);
                } else if (isMediaDocument(uri)) {
                    String docId = DocumentsContract.getDocumentId(uri);
                    String[] split = docId.split(":");
                    String type = split[0];

                    if ("image".equals(type)) {
                        Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Long.parseLong(split[1]));
                        return getRealPathFromURI(contentUri);
                    }
                }
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                if (isGooglePhotosUri(uri)) {
                    return uri.getLastPathSegment();
                }
                return getRealPathFromURI(uri);

            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        }
        //jeigu ne kitkat, kitaip visai nieko negauna
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }
            return getRealPathFromURI(uri);

        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    private String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        int column_index = 0;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}
