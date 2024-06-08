package br.com.wjd.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

//Classe para representar um dispositivo bluetooth encontrado
public class Device_Bluetooth {

    private BluetoothDevice bluetoothDevice;
    private int rssi;
    private Context context;
    private Main_Bluetooth ma;

    public Device_Bluetooth(BluetoothDevice bluetoothDevice, Context context, Main_Bluetooth mainBluetooth) {
        this.bluetoothDevice = bluetoothDevice;
        this.context = context;
        this.ma = mainBluetooth;
    }

    public String getAddress() {
        return bluetoothDevice.getAddress();
    }


    public String getName() {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ma,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
        }
        return

                bluetoothDevice.getName();
    }

    public void setRSSI(int rssi) {
        this.rssi = rssi;
    }

    public int getRSSI() {
        return rssi;
    }
}

