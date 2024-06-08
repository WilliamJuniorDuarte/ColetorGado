package br.com.wjd.bluetooth;

import android.app.Application;
import android.bluetooth.BluetoothGattCharacteristic;

public class Global extends Application {

    private static BluetoothGattCharacteristic characteristicRead;
    private static BluetoothGattCharacteristic characteristicWrite;
    private static Service_GATT mBTLE_Service;
    private static boolean conn_ble = false;
    private static String ble_pareado = "";

    public static BluetoothGattCharacteristic getCharacteristicRead() {
        return characteristicRead;
    }

    public static void setCharacteristicRead(BluetoothGattCharacteristic aCharacterisctR) {
        characteristicRead = aCharacterisctR;
    }

    public static BluetoothGattCharacteristic getCharacteristicWrite() {
        return characteristicWrite;
    }

    public static void setCharacteristicWrite(BluetoothGattCharacteristic aCharacterisctW) {
        characteristicWrite = aCharacterisctW;
    }

    public static Service_GATT getMBTLE_Service() {
        return mBTLE_Service;
    }

    public static void setMBTLE_Service(Service_GATT amBTLE_ServiceW) {
        mBTLE_Service = amBTLE_ServiceW;
    }

    public static boolean getConn_ble() {
        return conn_ble;
    }

    public static void setConn_ble(boolean aconn_bleW) {
        conn_ble = aconn_bleW;
    }
}
