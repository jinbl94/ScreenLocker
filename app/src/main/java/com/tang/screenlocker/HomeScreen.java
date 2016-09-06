package com.tang.screenlocker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.List;

public class HomeScreen extends Activity {

    private final static String _HOMECHOICE="HOMECHOICE";
    private final static String _PACKAGENAME="PACKAGEANME";
    private final static String _ACTIVITYNAME="ACTIVITYNAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LockScreen.checkLock()){
            //screen locked
            Log.d("HomeScreen","lock");
            startActivity(new Intent(this, LockScreen.class));
            this.finish();
        }else {
            //return to home screen
            HomeChoice homeChoice = new HomeChoice(this);
            //start screen service
            startService(new Intent(HomeScreen.this, ScreenService.class));
            try {
                homeChoice.originalHome();
            } catch (Exception e) {
                homeChoice.chooseBackHome();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //block key down events
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public class HomeChoice{
        Context context;
        Intent intent;
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;
        List<String> pkgNames,actNames;

        public HomeChoice(Context context) {
            this.context=context;
            intent=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
            sharedPreferences = context.getSharedPreferences(_HOMECHOICE, MODE_PRIVATE);
            editor=sharedPreferences.edit();
            pkgNames=new ArrayList<>();
            actNames=new ArrayList<>();
        }

        public void chooseBackHome() {
            List<ResolveInfo> resolveInfos = context.getPackageManager()
                    .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (int i = 0; i < resolveInfos.size(); i++) {
                String name = resolveInfos.get(i).activityInfo.packageName;
                if (name.equals(context.getPackageName()))
                    continue;
                pkgNames.add(name);
                name = resolveInfos.get(i).activityInfo.name;
                actNames.add(name);
            }

            //save package names to array
            String[] list = new String[pkgNames.size()];
            for (int i = 0; i < list.length; i++) {
                list[i] = pkgNames.get(i);
            }

            //make a alert dialog
            new AlertDialog.Builder(context)
                    .setTitle(R.string.chooseHome)
                    .setCancelable(false)
                    .setSingleChoiceItems(list, 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editor.putString(_PACKAGENAME, pkgNames.get(which));
                            editor.putString(_ACTIVITYNAME, actNames.get(which));
                            editor.commit();
                            originalHome();
                            dialog.dismiss();
                        }
                    }).show();
        }

        public void originalHome() {
            String pkgName = sharedPreferences.getString(_PACKAGENAME, null);
            String actName = sharedPreferences.getString(_ACTIVITYNAME, null);
            Log.d("HomeScreen","pkgName: "+pkgName+" actName: "+actName);
            ComponentName componentName = new ComponentName(pkgName, actName);
            Intent intent = new Intent();
            intent.setComponent(componentName);
            context.startActivity(intent);
            ((Activity) context).finish();
        }
    }
}
