package br.com.wjd.fragments;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import androidx.appcompat.app.AppCompatActivity;

import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import br.com.wjd.R;
import br.com.wjd.bd.DAC;

public class Splash extends AppCompatActivity {

    private DAC dac;
    public int first = 0;
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 567;
    private boolean accept = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //SharedPreferences
        final SharedPreferences spViaOnda = this.getSharedPreferences("wjdPreferences", MODE_PRIVATE);
        final SharedPreferences.Editor ed = spViaOnda.edit();

        first = spViaOnda.getInt("bdfisrt", 0);

        while(!accept){
            requestpermissions();
        }

        // Inicializa Banco
        dac = new DAC(Splash.this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        //Não está logado, chama o login
        if (spViaOnda.getString("token", "").isEmpty()){
            startActivity(new Intent(Splash.this, Login.class));
            finish();
        } else {
            starLoader(findViewById(android.R.id.content));

            Thread timerThread = new Thread() {
                public void run() {
                    try {
                        sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        //Intent intent = new Intent(Splash.this, Main_Bluetooth.class);
                        Intent intent = new Intent(Splash.this, Inicial.class);
                        startActivity(intent);
                        finish();

                    }
                }
            };
            timerThread.start();
        }
    }

    public void starLoader(View view) {
        Runnable runnable = new Runnable() {
            public void run() {
                final ImageView iv = findViewById(R.id.iv_refresh_splash);
                final Animation an = AnimationUtils.loadAnimation(getBaseContext(), R.anim.rotate);
                an.setRepeatMode(0);

                iv.startAnimation(an);
            }
        };
        Thread mythread = new Thread(runnable);
        mythread.start();
    }

    //Botão Menu
    @Override
    protected void onPause() {
        super.onPause();
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Inicial.ACTIVITY_SERVICE);
        activityManager.moveTaskToFront(getTaskId(), 0);
    }

    //Botão Voltar
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    private void requestpermissions(){
        if ((checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        || (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        || (checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
        || (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
        ){
            String[] permissions = {
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
            };
            requestPermissions(permissions,
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);

        } else accept = true;
    }
}