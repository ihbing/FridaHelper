package com.igio90.fridainjector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;

public class DataBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String data = intent.getStringExtra("data");
        EventBus.getDefault().post(new FridaMessage(10001,data));
    }
}
