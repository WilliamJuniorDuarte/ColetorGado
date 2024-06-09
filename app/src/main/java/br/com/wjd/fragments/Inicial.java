package br.com.wjd.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import br.com.wjd.R;
import br.com.wjd.bluetooth.Main_Bluetooth;

public class Inicial extends AppCompatActivity {

    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicial);

        toolbar = (Toolbar) findViewById(R.id.toolbar_inicial);
        toolbar.setTitle(getString(R.string.menu_starting));
        setSupportActionBar(toolbar);

        Button leitura = (Button) findViewById(R.id.btn_leitura_inicial);
        leitura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Inicial.this, Leitura.class));
            }
        });

        Button bluetooth = (Button) findViewById(R.id.btn_bluetooth_inicial);
        bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Inicial.this, Main_Bluetooth.class));
            }
        });
        Button classe = (Button) findViewById(R.id.btn_addclass_inicial);
        classe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Inicial.this, ListPecaClas.class));
            }
        });
        Button config = (Button) findViewById(R.id.btn_config_inicial);
        config.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Inicial.this, Configuracao.class));
            }
        });


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Set the color of overflow icon (three dots) to white
        Drawable overflowIcon = toolbar.getOverflowIcon();
        if (overflowIcon != null) {
            overflowIcon.setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile:
                return true;
            case R.id.about:
                return true;
            case R.id.logout:
                new AlertDialog.Builder(Inicial.this)
                        .setTitle(R.string.attention)
                        .setMessage(R.string.logout_confirmation)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                logout();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logout(){
        SharedPreferences spViaOnda = this.getSharedPreferences("wjdPreferences", MODE_PRIVATE);
        SharedPreferences.Editor ed = spViaOnda.edit();

        ed.remove("token");
        ed.apply();
        Intent intent = new Intent(Inicial.this, Splash.class);
        startActivity(intent);
        finish();
    }
}