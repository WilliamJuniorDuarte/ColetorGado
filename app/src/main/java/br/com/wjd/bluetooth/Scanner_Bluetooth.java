package br.com.wjd.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class Scanner_Bluetooth {

    private Main_Bluetooth ma;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner scanner;
    private boolean mScanning;
    private Handler mHandler;

    private long scanPeriod;
    private int signalStrength;
    private Context context;


    public Scanner_Bluetooth(Main_Bluetooth mainBluetooth, long scanPeriod, int signalStrength, Context context) {
        ma = mainBluetooth;

        mHandler = new Handler();

        this.scanPeriod = scanPeriod;
        this.signalStrength = signalStrength;

        final BluetoothManager bluetoothManager = (BluetoothManager) ma.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        this.context = context;
    }

    public boolean isScanning() {
        return mScanning;
    }

    //Inicia a busca por dispositivos
    public void start() {

        //Verifica se o bluetooth está ativado, caso contrário exibe um alerta para o usuário ativar
        if (!Utils_Bluetooth.checkBluetooth(mBluetoothAdapter)) {
            //Exibe o dialog para habilitar o bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(ma.getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ma,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
            } else {
                ma.startActivityForResult(enableBtIntent, Main_Bluetooth.REQUEST_ENABLE_BT);
            }
        }else{
            ma.startActivityForResult(enableBtIntent, Main_Bluetooth.REQUEST_ENABLE_BT);
        }


            stop();
        } else {
            scanLeDevice(true);
        }
    }

    //Pausa a busca por dispositivos
    public void stop() {
        scanLeDevice(false);
            ma.btn_Scan.setText("Procurar Dispositivos");

        try {
            ma.dialog.dismiss();
        } catch (Exception ex) {

        }
    }

    //Busca por dispositivos bluetooth
    private void scanLeDevice(final boolean enable) {

        if (enable && !mScanning) {
            Log.d("Scanner_Bluetooth", "Iniciando a busca por dispositivos bluetooth");

            //Interrompe a busca após o tempo configurado pelo usuario
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d("Scanner_Bluetooth", "Pausando a busca por dispositivos bluetooth");

                    mScanning = false;


                    //mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (ActivityCompat.checkSelfPermission(ma.getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(ma,
                                    new String[]{Manifest.permission.BLUETOOTH_SCAN}, 1);
                        } else {
                            mBluetoothAdapter.startLeScan(mLeScanCallback);
                        }
                    }else{
                        mBluetoothAdapter.startLeScan(mLeScanCallback);
                    }

                    //Interrompe a busca por dispositivos bluetooth
                    stop();
                }
            }, scanPeriod);

            mScanning = true;

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(ma.getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ma,
                            new String[]{Manifest.permission.BLUETOOTH_SCAN}, 1);
                } else {
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                }
            }else{
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            }


            //Caso queira buscar somente por dispositivos com determinado UUID de config
            //mBluetoothAdapter.startLeScan(uuids, mLeScanCallback);
        } else {
            mScanning = false;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(ma.getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ma,
                            new String[]{Manifest.permission.BLUETOOTH_SCAN}, 1);
                } else {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }else{
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }
    }

    //Retorno da busca, exibe o dispositivo encontrado na tela
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

                    final int new_rssi = rssi;

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (ActivityCompat.checkSelfPermission(ma.getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(ma,
                                    new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
                        } else {
                            Log.e("BluetoothScanResult", "Name: " + device.getName() + ", RSSI: " + String.valueOf(rssi));
                        }
                    }else{
                        Log.e("BluetoothScanResult", "Name: " + device.getName() + ", RSSI: " + String.valueOf(rssi));
                    }

                    //Se for maior que a potência minima setada
                    if (rssi > signalStrength) {
                        String sName = "";
                        try {

                            sName = device.getName().toString().toUpperCase();
                        }
                        catch (Exception ex)
                        {
                            sName = "";
                        }
                        if(sName.toUpperCase().contains("RFID"))
                            mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ma.addDevice(device, new_rssi);
                                }
                        });
                    }
                }
            };
}