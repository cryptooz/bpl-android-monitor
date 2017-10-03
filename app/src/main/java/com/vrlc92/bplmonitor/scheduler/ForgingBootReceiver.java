package com.vrlc92.bplmonitor.scheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vrlc92.bplmonitor.utils.Utils;

public class ForgingBootReceiver extends BroadcastReceiver {
    private final ForgingAlarmReceiver alarm = new ForgingAlarmReceiver();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) && Utils.alarmEnabled(context)) {
            alarm.setAlarm(context);
        }
    }
}