package com.tang.screenlocker;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Settings extends Activity {

    private static SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static int lockType;
    private static String savedPassword;
    private final static String _LOCKTYPE="LOCKTYPE";
    private final static String _LOCKPASSWORD="LOCKPASSWORD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        sharedPreferences=this.getSharedPreferences(_LOCKTYPE, MODE_PRIVATE);
        editor=sharedPreferences.edit();
    }

    @Override
    protected void onDestroy(){
        Log.d("Settings","onDestroy");
        super.onDestroy();
        startActivity(new Intent(this, ScreenService.class));
    }

    public void setSlid(View view){
        editor.putInt(_LOCKTYPE, LockType.SLIDLOCK);
        editor.commit();
        startActivity(new Intent(Settings.this, LockScreen.class));
    }

    public void setPswd(View view){
        LayoutInflater inflater=LayoutInflater.from(Settings.this);
        final View alerView=inflater.inflate(R.layout.alert_dialog,null);
        final String passwordOld=sharedPreferences.getString(_LOCKPASSWORD,"");
        AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(Settings.this);
        alertDialogBuilder.setView(alerView);
        alertDialogBuilder.setTitle(R.string.SetPassword);
        final EditText newPassword=(EditText)alerView.findViewById(R.id.NewPasswordText);
        final EditText confirmPassword=(EditText)alerView.findViewById(R.id.ConfirmPasswordText);
        if (passwordOld.equals("")){
            confirmPassword.setText("");
            confirmPassword.setVisibility(View.INVISIBLE);
        }
        alertDialogBuilder.setPositiveButton(R.string.Confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String passwordNew = newPassword.getText().toString();
                String passwordConfirm = confirmPassword.getText().toString();
                if (passwordOld.equals(passwordConfirm)) {
                    if (passwordNew.equals("")) {
                        Toast.makeText(getApplicationContext(), R.string.PswdEmpt, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("Settings","Set Password");
                        editor.putInt(_LOCKTYPE, LockType.PSWDLOCK);
                        editor.putString(_LOCKPASSWORD, passwordNew);
                        editor.commit();
                        startActivity(new Intent(Settings.this, LockScreen.class));
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.OdPswdWrong, Toast.LENGTH_SHORT).show();
                }
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.Cancel, null);
        alertDialogBuilder.create().show();
    }

    public static int getLockType(){
        lockType=sharedPreferences.getInt(_LOCKTYPE,0);
        return lockType;
    }

    public static String getSavedPassword(){
        savedPassword=sharedPreferences.getString(_LOCKPASSWORD,"");
        return savedPassword;
    }
}
