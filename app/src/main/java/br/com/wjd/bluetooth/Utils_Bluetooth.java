package br.com.wjd.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.IntentFilter;

public class Utils_Bluetooth {

    public static boolean checkBluetooth(BluetoothAdapter bluetoothAdapter) {

        //Verifica se o bluetooth está habilitado
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled())
            return false;
        else
            return true;
    }

    public static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(Service_GATT.ACTION_GATT_CONNECTED);
        intentFilter.addAction(Service_GATT.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(Service_GATT.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(Service_GATT.ACTION_DATA_AVAILABLE);

        return intentFilter;
    }

    public static String hexToString(byte[] data) {
        final StringBuilder sb = new StringBuilder(data.length);

        for (byte byteChar : data) {
            sb.append(String.format("%02X ", byteChar));
        }

        return sb.toString();
    }

    public static int hasWriteProperty(int property) {
        return property & BluetoothGattCharacteristic.PROPERTY_WRITE;
    }

    public static int hasNotifyProperty(int property) {
        return property & BluetoothGattCharacteristic.PROPERTY_NOTIFY;
    }

}
