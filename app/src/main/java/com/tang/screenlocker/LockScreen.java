package com.tang.screenlocker;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LockScreen extends Activity{

    public static boolean lock=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //set windows params
        getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        super.onCreate(savedInstanceState);

		//get suer setting and implement lock screen
        switch (Settings.getLockType()){
            case 1:
                Log.d("LockScreen","Password Lock");
                setContentView(R.layout.activity_password_lock_screen);
                new PasswordLock();
                break;
            default:
                Log.d("LockScreen", "Slid Lock");
                setContentView(R.layout.activity_slid_lock_screen);
                new SlidLock();
                break;
        }

        //set background
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        Bitmap wallPaper = ((BitmapDrawable) wallpaperManager.getDrawable()).getBitmap();
        Bitmap backGround = Bitmap.createBitmap(wallPaper, 0, 0, metrics.widthPixels, metrics.heightPixels);
        getWindow().setBackgroundDrawable(new BitmapDrawable(getResources(), backGround));

        //set lock
        lock=true;

        //start screen service
        startService(new Intent(this, ScreenService.class));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d("LockScreen", "onResume");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (!hasFocus) {
            Log.d("LockScreen","Focus lost");
			ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
			am.moveTaskToFront(getTaskId(), ActivityManager.MOVE_TASK_WITH_HOME );
			sendBroadcast( new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS) );
        }
    }

    private void unlock(){
        //set the stat as locked
        lock=false;
        startActivity(new Intent(this, HomeScreen.class));
        this.finish();
    }

    public static boolean checkLock(){
        return lock;
    }

    public static void setLock(){
        lock=true;
    }

    private void refreshTime(TextView textView){
        SimpleDateFormat dateFormat=new SimpleDateFormat("HH:mm");
        Date date=new Date(System.currentTimeMillis());
        textView.setText(dateFormat.format(date));
    }

    //Password Lock
    class PasswordLock{
        private TextView textView;
        private EditText password;
        private Button buttonUnlock;
        private String savedPassword;
        PasswordLock(){
            textView=(TextView)findViewById(R.id.text_area);
            password=(EditText)findViewById(R.id.PasswordText);
            buttonUnlock=(Button)findViewById(R.id.ButtonUnlock);
            buttonUnlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshTime(textView);
                    String passwordInput=password.getText().toString();
                    if (savedPassword.equals(passwordInput)){
                        unlock();
                    }else {
                        Toast.makeText(getApplicationContext(),R.string.PasswordWrong,Toast.LENGTH_SHORT).show();
                    }
                }
            });
            savedPassword=Settings.getSavedPassword();
        }
    }

    //SlidLock
    class SlidLock {
        private StringBuilder builder = new StringBuilder();
        private TextView textView;
        private TextView slid_start;
        private ViewGroup slid_bar;
        private int [] mLocation=new int[2];
        private int Xstart;

        SlidLock(){
            textView=(TextView)findViewById(R.id.text_area);
            slid_bar = (ViewGroup) findViewById(R.id.slid_bar);
            slid_start=(TextView)findViewById(R.id.slid_start);
            slid_start.getLocationOnScreen(mLocation);
            slid_start.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    builder.setLength(0);
                    switch (event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            builder.append("Action_down, ");
                            Xstart=(int)event.getRawX();
                            break;
                        case MotionEvent.ACTION_UP:
                            builder.append("Action_up, ");
                            slid_start.layout(mLocation[0], mLocation[1],
                                    mLocation[0] + slid_start.getMeasuredWidth(), mLocation[1] + slid_start.getMeasuredHeight());
                            refreshTime(textView);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            builder.append("Action_move, ");
                            slid_start.layout(mLocation[0] + (int) event.getRawX() - Xstart, mLocation[1],
                                    mLocation[0] + (int) event.getRawX() - Xstart + slid_start.getMeasuredWidth(),
                                    mLocation[1] + slid_start.getMeasuredHeight());
                            if (slidDestinationHit((int) event.getRawX())) {
                                unlock();
                            }
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            builder.append("Action_cancel, ");
                            break;
                        default:
                            builder.append("Default, ");
                            break;
                    }
                    builder.append(event.getRawX() + "," + event.getRawY());
                    String text=builder.toString();
                    Log.d("TouchTest", text);
                    return true;
                }
            });
        }

        private boolean slidDestinationHit(int x){
            if (x>=(slid_bar.getMeasuredWidth()-slid_start.getMeasuredWidth()/2))
                return true;
            return false;
        }
    }
}
