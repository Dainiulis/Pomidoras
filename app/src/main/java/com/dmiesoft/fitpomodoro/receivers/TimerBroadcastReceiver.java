package com.dmiesoft.fitpomodoro.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.dmiesoft.fitpomodoro.services.TimerService;
import com.dmiesoft.fitpomodoro.ui.activities.MainActivity;
import com.dmiesoft.fitpomodoro.ui.fragments.TimerTaskFragment;
import com.dmiesoft.fitpomodoro.utils.helpers.NotificationHelper;

public class TimerBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "TBR";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "onReceive: ");
        if (action.equalsIgnoreCase(NotificationHelper.ACTION_STOP_TIMER)) {
            TimerService.stopTimerService(context);
        } else if (action.equalsIgnoreCase(NotificationHelper.ACTION_OPEN_TIMER_FRAG)) {
            Intent intent1 = new Intent(context, MainActivity.class);
            context.getApplicationContext().startActivity(intent1);
        }
    }
}
