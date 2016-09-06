package com.tang.screenlocker;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class ScreenService extends Service {

    private KeyguardManager keyguardManager = null;
    private KeyguardManager.KeyguardLock keyguardLock = null;
    Intent intentOfLockScreen;
    //declaration of broadcast receiver
    private BroadcastReceiver broadcastReceiver=new BroadcastReceiver(){
        @Override
        public void onReceive(Context context,Intent intent){
            String action = intent.getAction();
            Log.d("ScreenService", "action: " + action);
            //disable keyguard when screen turns on/off
            if(action.equals("android.intent.action.SCREEN_ON")||action.equals("android.intent.action.SCREEN_OFF")){
                //disable keyguard
                keyguardManager = (KeyguardManager) context.getSystemService(context.KEYGUARD_SERVICE);
                keyguardLock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
                keyguardLock.disableKeyguard();
                LockScreen.setLock();
                startActivity(intentOfLockScreen);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        //create intent
        intentOfLockScreen=new Intent(this,LockScreen.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //register intent filter
        IntentFilter intentFilter=new IntentFilter("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        //restart this service when destroyed
        startActivity(new Intent(this, ScreenService.class));
    }
}
