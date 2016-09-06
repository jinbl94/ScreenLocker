package com.tang.screenlocker;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class BootService extends BroadcastReceiver {

    private final static String _PACKAGENAME="com.tang.screenlocker";
    private final static String _SERVICENAME=".ScreenService";

    @Override
    public void onReceive(Context context, Intent intent) {
        //start screen on/off intent receiver
        Intent myIntent = new Intent();
        myIntent.setAction(_PACKAGENAME+_SERVICENAME);
        context.startService(myIntent);
    }
}
