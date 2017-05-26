package com.dmiesoft.fitpomodoro.utils;

import android.content.Context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by daini on 2017-05-25.
 */

public class LogToFile {

    private File file;
    private BufferedWriter bufferedWriter;

    public LogToFile(Context context, String logFile) {
        file = new File(context.getExternalFilesDir(null), logFile);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        appendLog("....................Starting new Timer LOG...........................");
    }

    public void appendLog(String text) {
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(file, true));

            bufferedWriter
                    .append(DateFormat.getDateTimeInstance().format(new Date()))
                    .append(" - ")
                    .append(text);

            bufferedWriter.newLine();
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeLog() {
        try {
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
