package com.dmiesoft.fitpomodoro.utils;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;

public class DatabaseQueryHandler extends AsyncQueryHandler {

    public DatabaseQueryHandler(ContentResolver cr) {
        super(cr);
    }
}
