package br.com.wjd.bluetooth;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Broadcast_GATT extends BroadcastReceiver {

    private Comunicacao_Service_Bluetooth activity;
    private boolean tela_conexao;
    String uuid, tag_received, tag_retorno;

    public Broadcast_GATT(Comunicacao_Service_Bluetooth activity, boolean activity_connection) {

        this.activity = activity;
        this.tela_conexao = activity_connection;
    }

    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device. This can be a result of read or notification operations.
    // EXTRA_UUID: UUID do bluetooth conectado
    // EXTRA_DATA: Dado recebido pelo bluetooth conectado.

    @Override
    public void onReceive(Context context, Intent intent) {


        final String action = intent.getAction();
        Bundle extras = intent.getExtras();

        /*try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        if(extras != null)
        {
            uuid = extras.getString(Service_GATT.EXTRA_UUID);
            tag_received = extras.getString(Service_GATT.EXTRA_DATA);
            if (tag_received == null)
            {
               // tag_received = intent.getExtras().getString(Service_GATT.EXTRA_DATA);
            }
        }
        else {

            try {
                uuid = intent.getExtras().getString(Service_GATT.EXTRA_UUID);
                tag_received = intent.getExtras().getString(Service_GATT.EXTRA_DATA);
            } catch (Exception ex) {
                uuid = intent.getStringExtra(Service_GATT.EXTRA_UUID);
                tag_received = intent.getStringExtra(Service_GATT.EXTRA_DATA);
            }
        }

        boolean erro = false;

        if (Service_GATT.ACTION_GATT_CONNECTED.equals(action)) {
            Log.d("Broadcast_GATT", "Server GATT conectado");
        }
        else if (Service_GATT.ACTION_GATT_DISCONNECTED.equals(action)) {
            Log.d("Broadcast_GATT", "Server GATT desconectado");

            //Se estiver na tela de configuração vamos finalizar a activity, do contrario seta a flag como falso para realizar a reconexão
            if (tela_conexao)
                activity.finish();
            else
                Global.setConn_ble(false);
        }
        else if (Service_GATT.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            Log.d("Broadcast_GATT", "Server GATT serviços disponiveis");

            Service_GATT mBTLE_Service = Global.getMBTLE_Service();

            if (mBTLE_Service != null) {

                HashMap<String, ArrayList<BluetoothGattCharacteristic>> characteristics_HashMapList = new HashMap<>();

                characteristics_HashMapList.clear();

                List<BluetoothGattService> servicesList = mBTLE_Service.getSupportedGattServices();

                for (BluetoothGattService service : servicesList) {

                    List<BluetoothGattCharacteristic> characteristicsList = service.getCharacteristics();
                    ArrayList<BluetoothGattCharacteristic> newCharacteristicsList = new ArrayList<>();

                    for (BluetoothGattCharacteristic characteristic : characteristicsList) {
                        newCharacteristicsList.add(characteristic);
                    }

                    characteristics_HashMapList.put(service.getUuid().toString(), newCharacteristicsList);
                }

                try {
                    //Recebe e seta a variavel global de leitura e escrita
                    Global.setCharacteristicRead(characteristics_HashMapList.get("00001101-0000-1000-8000-00805f9b34fb").get(0));
                    Global.setCharacteristicWrite(characteristics_HashMapList.get("00001101-0000-1000-8000-00805f9b34fb").get(1));

                    //Após se conectar com os services aguarda o dispositivo ficar disponivel para configurar automaticamente o modo leitura do bluetooth
                    while (Utils_Bluetooth.hasNotifyProperty(Global.getCharacteristicRead().getProperties()) == 0);
                    mBTLE_Service.setCharacteristicNotification(Global.getCharacteristicRead(), true);

                    erro = false;

                    //Seta a flag de conexão com o bluetooth
                    if (!tela_conexao)
                        Global.setConn_ble(true);

                    Log.d("Broadcast_GATT", "Inicia Leitura Bluetooth");
                } catch (Exception e) {
                    erro = true;

                    //Seta a flag de conexão com o bluetooth
                    if (!tela_conexao)
                        Global.setConn_ble(false);


                    Log.d("Broadcast_GATT", "Falha ao tratar os serviços do bluetooth");
                }
            }

            //Somente vamos realizar a ação abaixo caso o broadcast tenha sido criado pela tela de configuração, para finalizar o alertDialog da activity
            if (tela_conexao) {
                if (erro)
                    activity.failServices();
                 else
                    activity.updateServices();
            }
        }
        else if (Service_GATT.ACTION_DATA_AVAILABLE.equals(action)) {
            Log.d("Broadcast_GATT", "Server GATT mensagem recebida");

            Log.d("Broadcast_GATT", "UUID: " + uuid);
            Log.d("Broadcast_GATT", "Dado: " + tag_received);
        }
    }

    //Retorna a tag lida
    public String return_read() {

        //Recebe a tag para zerar a variavel recebida
        tag_retorno = tag_received;

        //Retira os espaços da tag
        if (tag_received != null && !tag_received.isEmpty()) {
            tag_retorno = tag_retorno.replaceAll(" ", "");
        }

        return tag_retorno;
    }

    //Limpa a ultima tag lida
    public void limpa_tagBtle() {
        tag_retorno = null;
        tag_received = null;
    }

}