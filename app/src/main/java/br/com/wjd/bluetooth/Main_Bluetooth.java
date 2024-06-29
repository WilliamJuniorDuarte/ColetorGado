package br.com.wjd.bluetooth;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.HashMap;

import br.com.wjd.R;

public class Main_Bluetooth extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public static final int REQUEST_ENABLE_BT = 1;
    public static final int BTLE_SERVICES = 2;
    public static final String PREF_NAME = "wjdPreferences";
    SharedPreferences spViaOnda;
    SharedPreferences.Editor ed;
    private HashMap<String, Device_Bluetooth> mBTDevicesHashMap;
    private ArrayList<Device_Bluetooth> mBTDevicesArrayList;
    private ListAdapter_Devices_Bluetooth adapter;
    public ListView listView;
    ProgressDialog dialog;
    public Button btn_Scan;
    private Estado_Bluetooth mBTStateUpdateReceiver;
    public Scanner_Bluetooth mBTLeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_btle);
        spViaOnda = getApplicationContext().getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        ed = spViaOnda.edit();

        //Menu Suspenso
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.baseline_arrow_white_24);
        ab.setTitle(getString(R.string.connection_bluetooth));

        //Verifica se o dispositivo atual suporta o Bluetooth BLE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast toast = Toast.makeText(Main_Bluetooth.this, getString(R.string.device_not_supported_bluetooth_ble), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
                finish();
        }

        //Scaneia os bluetooths disponiveis por 5seg e que estejam com no minimo -100 de potência do sinal RSSI
        mBTLeScanner = new Scanner_Bluetooth(this, 5000, -100, this);

        /*---Lista de dispositivos---*/
        mBTDevicesHashMap = new HashMap<>();
        mBTDevicesArrayList = new ArrayList<>();

        adapter = new ListAdapter_Devices_Bluetooth(this, R.layout.device_btle, mBTDevicesArrayList);

        listView = new ListView(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        /*---------------------------*/

        btn_Scan = (Button) findViewById(R.id.btn_scan);
        ((ScrollView) findViewById(R.id.scrollView)).addView(listView);


        btn_Scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!mBTLeScanner.isScanning()) {
                    dialog = new ProgressDialog(v.getContext());
                    dialog.setTitle("Buscando Dispositivos. Aguarde.");
                    dialog.setMessage("Aguarde o fim da pesquisa...");

                    dialog.show();

                    //Limpa a lista dos dispositivos
                    mBTDevicesArrayList.clear();
                    mBTDevicesHashMap.clear();

                    //Inicia a busca
                    mBTLeScanner.start();
                } else {
                    //Interrompe a busca por dispositivos bluetooth
                    mBTLeScanner.stop();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Verifica se o usuario aceitou ou não habilitar o bluetooth, caso recuse finaliza a aplicação
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        //Caso clique em um dispositivo antes de terminar a busca interrompe e realiza a conexão
        mBTLeScanner.stop();

        String name = mBTDevicesArrayList.get(position).getName();
        String address = mBTDevicesArrayList.get(position).getAddress();

        ed.putString("blmac", address);
        ed.apply();

        Intent intent = new Intent(this, Comunicacao_Service_Bluetooth.class);
        intent.putExtra(Comunicacao_Service_Bluetooth.EXTRA_NAME, name);
        intent.putExtra(Comunicacao_Service_Bluetooth.EXTRA_ADDRESS, address);
        startActivityForResult(intent, BTLE_SERVICES);

        //Limpa a lista dos dispositivos após a conexão
        mBTDevicesArrayList.clear();
        mBTDevicesHashMap.clear();

        adapter.notifyDataSetChanged();
    }

    //Adiciona os dispositivos bluetooths disponiveis encontrado
    public void addDevice(BluetoothDevice device, int rssi) {

        String address = device.getAddress();

        if (!mBTDevicesHashMap.containsKey(address)) {
            Device_Bluetooth btleDevice = new Device_Bluetooth(device, this.getApplicationContext(), this);
            btleDevice.setRSSI(rssi);

            mBTDevicesHashMap.put(address, btleDevice);
            mBTDevicesArrayList.add(btleDevice);
        } else {
            mBTDevicesHashMap.get(address).setRSSI(rssi);
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Monitora o estado do bluetooth
        mBTStateUpdateReceiver = new Estado_Bluetooth(getApplicationContext());
        registerReceiver(mBTStateUpdateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(mBTStateUpdateReceiver);

        //Interrompe a busca por dispositivos bluetooth
        mBTLeScanner.stop();
    }
}