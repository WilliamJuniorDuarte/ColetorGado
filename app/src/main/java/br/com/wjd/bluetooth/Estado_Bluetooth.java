package br.com.wjd.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Estado_Bluetooth extends BroadcastReceiver {

    Context activityContext;

    public Estado_Bluetooth(Context activityContext) {
        this.activityContext = activityContext;
    }

    //Método executado apenas quando o estado inicial do bluetooth foi alterado
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    Log.d("Estado_bluetooth", "Bluetooth Desligado");
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Log.d("Estado_bluetooth", "Bluetooth Desativando");
                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.d("Estado_bluetooth", "Bluetooth Ligado");
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Log.d("Estado_bluetooth", "Bluetooth Ativando");
                    break;
            }
        }
    }
}
