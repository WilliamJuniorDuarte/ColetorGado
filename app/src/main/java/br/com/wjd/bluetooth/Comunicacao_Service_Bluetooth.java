package br.com.wjd.bluetooth;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import br.com.wjd.Classes.AlertDialogCustom;
import br.com.wjd.fragments.AddClass;
import br.com.wjd.fragments.Inicial;
import br.com.wjd.fragments.Leitura;
import br.com.wjd.R;

public class Comunicacao_Service_Bluetooth extends AppCompatActivity {

    public static final String EXTRA_NAME = ".Comunicacao_Service_Bluetooth.NAME";
    public static final String EXTRA_ADDRESS = ".Comunicacao_Service_Bluetooth.ADDRESS";
    private String name, address;
    ProgressDialog progressDialog;
    private Intent mBTLE_Service_Intent;
    private Service_GATT mBTLE_Service;
    private Broadcast_GATT mGattUpdateReceiver;
    //SharedPreferences
    public SharedPreferences spViaOnda;
    public SharedPreferences.Editor ed;
    //Parâmetros do serviço para o BindService
    private ServiceConnection mBTLE_ServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

            Service_GATT.BTLeServiceBinder binder = (Service_GATT.BTLeServiceBinder) service;
            mBTLE_Service = binder.getService();

            Global.setMBTLE_Service(mBTLE_Service);

            //Se ocorrer algum problema na conexão com o service do bluetooth vamos finalizar a activity
            if (!mBTLE_Service.connect(address))
                failServices();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBTLE_Service = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //SharedPreferences
        spViaOnda = this.getSharedPreferences("wjdPreferences", MODE_PRIVATE);
        ed = spViaOnda.edit();

        Intent intent = getIntent();

        //Nome e Endereço do dispositivo conectado
        name = intent.getStringExtra(Comunicacao_Service_Bluetooth.EXTRA_NAME);
        address = intent.getStringExtra(Comunicacao_Service_Bluetooth.EXTRA_ADDRESS);

        //Exibe o progresso de conexão
        progressDialog = new ProgressDialog(Comunicacao_Service_Bluetooth.this);

        progressDialog.show(Comunicacao_Service_Bluetooth.this,
                getString(R.string.bluetooth_ble),
                getString(R.string.connecting_reticencias),
                true,
                true,
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        progressDialog.dismiss();
                        finish();
                    }
                });
    }

    public void updateServices() {

        progressDialog.dismiss();
        AlertDialogCustom.showDialog(
                Comunicacao_Service_Bluetooth.this,
                getString(R.string.bluetooth),
                getString(R.string.bluetooth_connected),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Armazena no sharedpreferences o nome e o endereço mac do bluetooth conectado pelo usuário
                        ed.putString("blename", name);
                        ed.putString("bleaddress", address);
                        ed.apply();

                        finish();
                        startActivity(new Intent(Comunicacao_Service_Bluetooth.this, Inicial.class));
                    }
                },
                null
        );
    }

    public void failServices() {

        progressDialog.dismiss();

        AlertDialogCustom.showDialog(
            Comunicacao_Service_Bluetooth.this,
            getString(R.string.fail),
            getString(R.string.bluetooth_fail_connection),
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    finish();

                    startActivity(new Intent(Comunicacao_Service_Bluetooth.this, Leitura.class));
                }
            },
            null
        );
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Monitora o estado do servidor GATT
        mGattUpdateReceiver = new Broadcast_GATT(this, true);
        registerReceiver(mGattUpdateReceiver, Utils_Bluetooth.makeGattUpdateIntentFilter());

        //Cria e inicializa o service de comunicação
        mBTLE_Service_Intent = new Intent(this, Service_GATT.class);
        bindService(mBTLE_Service_Intent, mBTLE_ServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        super.onStop();

        unregisterReceiver(mGattUpdateReceiver);

        unbindService(mBTLE_ServiceConnection);
    }

    @Override
    public void onDestroy() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        super.onDestroy();
    }
}