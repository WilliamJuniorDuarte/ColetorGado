package br.com.wjd.fragments;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.wjd.Classes.AlertDialogCustom;
import br.com.wjd.Classes.LeituraRecebida;
import br.com.wjd.Classes.PecaClas;
import br.com.wjd.Classes.PecaTipo;
import br.com.wjd.Classes.RefCruzada;
import br.com.wjd.Classes.ServiceFunction;
import br.com.wjd.R;
import br.com.wjd.UtilLocal;
import br.com.wjd.bd.DAC;
import br.com.wjd.bd.PecaClasDao;
import br.com.wjd.bd.PecaTipoDao;
import br.com.wjd.bluetooth.Broadcast_GATT;
import br.com.wjd.bluetooth.Comunicacao_Service_Bluetooth;
import br.com.wjd.bluetooth.Global;
import br.com.wjd.bluetooth.Main_Bluetooth;
import br.com.wjd.bluetooth.Service_GATT;
import br.com.wjd.bluetooth.Utils_Bluetooth;

public class Leitura extends AppCompatActivity {

    private ImageView iniciar, stop, shared, reset, addTag;
    private TextView total;
    private TextView fvFooter;
    private EditText newTag;
    private String vc_epc_filter = "";
    private String vc_prefixo_epc_mask = "";
    List<RefCruzada> listref = new ArrayList<RefCruzada>();
    public boolean statusTraduzir = false, fimPecaChip = false;
    private boolean bLeituraHid = false;
    private boolean bEPC128Mode = false;
    public ProgressBar pbLeitura;
    String reader_mac;
    private ListView listView;
    private ListViewAdapter adapterTags;
    private List<LeituraRecebida> listaLeituras = new ArrayList<>();
    private boolean isStart = false;
    String epc;
    String rssi;
    static Map<String, Integer> scanResult6cNew = new HashMap<String, Integer>();
    public boolean botao_rfid = false;
    private boolean btle_start = false;
    private Intent mBTLE_Service_Intent;
    private Service_GATT mBTLE_Service;
    private Broadcast_GATT mGattUpdateReceiver;
    private String name, address;
    public Thread monitoraConexao;
    public boolean threadConnection = true;
    SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private List<PecaTipo> listPecaTipo = new ArrayList<>();
    private JSONObject joPecaChip;

    public String s_getdatetime() {
        return simpleFormat.format(new Date(System.currentTimeMillis()));
    }

    private ServiceConnection mBTLE_ServiceConnection;
    private Spinner sp_classe;
    private DAC dac;
    private PecaClasDao daoClas;
    private List<PecaClas> listPecaClas;
    private SharedPreferences.Editor ed;
    private SharedPreferences spViaOnda;
    private PecaClas selectedPecaClas;

    //Handler de atualização da lista e do display durante a leitura
    @SuppressLint("HandlerLeak")
    public Handler handlerAtualiza = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            String epc;
            String rssi;
            Bundle bundle = msg.getData();
            epc = bundle.getString("epc").replaceFirst("^0+", "");
            rssi = bundle.getString("rssi");

            if (epc == null || epc.length() <= 2) {
                return;
            }

            Pattern p = Pattern.compile("[^0-9]");
            Matcher m = p.matcher(epc);
            boolean b = m.find();

            if(b){
                return;
            }

            if (!vc_epc_filter.isEmpty()) {
                if (!epc.equals(vc_epc_filter))
                    return;
            }

            if (!vc_prefixo_epc_mask.isEmpty()) {
                if (!epc.startsWith(vc_prefixo_epc_mask))
                    return;
            }

            int num = getLeituresByEPC(epc);
            if (num < 0) {
                LeituraRecebida cLinha = new LeituraRecebida(epc, 1, rssi, s_getdatetime(), "");
                listaLeituras.add(cLinha);
            } else {
                for (LeituraRecebida item : listaLeituras) {
                    if (item.getsEPC().equals(epc)) {
                        item.setiLeituras(num + 1);
                        item.setsRSSIHex(rssi);
                        item.setVc_datetime(s_getdatetime());
                    }
                }
            }

            //Cria e seta o adapter com nossos dados do HASH
            createAdapter();

            //Sinaliza para o adapter que houve atualização na nossa lista
            listView.refreshDrawableState();
            adapterTags.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leitura);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getString(R.string.leitura));

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.baseline_arrow_white_24);

        //SharedPreferences
        spViaOnda = this.getSharedPreferences("wjdPreferences", MODE_PRIVATE);
        ed = spViaOnda.edit();
        vc_prefixo_epc_mask = spViaOnda.getString("prefixoepc", "");

        if(spViaOnda.getString("blename", "").equals("")){
            reader_mac = "";
        } else {
            String[] ssid = spViaOnda.getString("blename", "").split("-");
            if(ssid.length >= 3) {
                reader_mac = ssid[3].trim();
            } else {
                reader_mac = "";
            }
        }
        name = spViaOnda.getString("blename", null);
        address = spViaOnda.getString("bleaddress", null);

        bLeituraHid = spViaOnda.getBoolean("hidmode", false);
        bEPC128Mode = spViaOnda.getBoolean("epc128", false);

        if(!bLeituraHid) {
            mBTLE_ServiceConnection = new ServiceConnection() {

                @Override
                public void onServiceConnected(ComponentName className, IBinder service) {

                    Service_GATT.BTLeServiceBinder binder = (Service_GATT.BTLeServiceBinder) service;
                    mBTLE_Service = binder.getService();

                    Global.setMBTLE_Service(mBTLE_Service);

                    //Se ocorrer algum problema na conexão com o service do bluetooth vamos finalizar a activity
                    if (!bLeituraHid) {
                        if (!mBTLE_Service.connect(address)) {
                            AlertDialogCustom.showDialog(
                                Leitura.this,
                                getString(R.string.fail),
                                getString(R.string.fail_connection_bluetooth),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //Finaliza a ação do botão do leitor RFID
                                        if (botao_rfid == true) {
                                            unregisterReceiver(keyReceiver);
                                            botao_rfid = false;
                                        }

                                        threadConnection = false;
                                        Intent intent = new Intent(Leitura.this, Main_Bluetooth.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                },
                                null
                            );
                        }
                    }

                    //Verifica se a thread ainda não foi iniciada
                    if (!bLeituraHid && name != null) {
                        if (monitoraConexao.getState() == Thread.State.NEW)
                            monitoraConexao.start();


                    }

                }

                @Override
                public void onServiceDisconnected(ComponentName arg0) {
                    mBTLE_Service = null;
                    Global.setMBTLE_Service(mBTLE_Service);

                    //Seta como falso para inicializar o bluetooth novamente na próxima conexão
                    btle_start = false;
                }
            };
        }

        if (bLeituraHid) {
            UtilLocal.hideKeyboardFrom(Leitura.this, getWindow().getDecorView().findViewById(android.R.id.content));

        }

        //Configura a ação do botão do leitor RFID
        if(!bLeituraHid) {
            registerReceiver(keyReceiver, new IntentFilter("android.rfid.FUN_KEY"));
            botao_rfid = true;
        }

        //Thread para verificar a conexão com o bluetooth
        if(!bLeituraHid) {
            monitoraConexao = new Thread(new Runnable() {
                @Override
                public void run() {

                    while (threadConnection) {

                        //Caso perca a conexão realiza a reconexão automaticamente
                        if (Global.getConn_ble() == false) {

                            //Seta como falso para reiniciar os parâmetros do bluetooth ao reconectar
                            btle_start = false;

                            //Caso estava na leitura vamos finalizar a operação
                            if (isStart) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        finalizar();

                                        AlertDialogCustom.showDialog(
                                            Leitura.this,
                                            getString(R.string.fail),
                                            getString(R.string.connection_leitor_fail),
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            },
                                            null
                                        );
                                    }
                                });
                            }

                            //Verifica se houve uma perda de conexão com o bluetooth ou se o usuario desativou propositadamente
                            if (!Utils_Bluetooth.checkBluetooth(BluetoothAdapter.getDefaultAdapter())) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlertDialogCustom.showDialog(
                                            Leitura.this,
                                            getString(R.string.bluetooth),
                                            getString(R.string.bluetoooth_disable_finish_connection),
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //Finaliza a ação do botão do leitor RFID
                                                    if (botao_rfid == true) {
                                                        unregisterReceiver(keyReceiver);
                                                        botao_rfid = false;
                                                    }

                                                    threadConnection = false;
                                                    finish();
                                                }
                                            },
                                            null
                                        );
                                    }
                                });

                            } else {
                                if (mBTLE_Service != null) {
                                    //Se ocorrer algum problema na conexão com o service do bluetooth vamos finalizar a activity
                                    if (!mBTLE_Service.connect(address)) {

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                AlertDialogCustom.showDialog(
                                                    Leitura.this,
                                                    getString(R.string.fail),
                                                    getString(R.string.fail_connection_bluetooth),
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            //Finaliza a ação do botão do leitor RFID
                                                            if (botao_rfid == true) {
                                                                unregisterReceiver(keyReceiver);
                                                                botao_rfid = false;
                                                            }

                                                            threadConnection = false;
                                                            Intent intent = new Intent(Leitura.this, Main_Bluetooth.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    },
                                                    null
                                                );
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }

        newTag = findViewById(R.id.et_newTag_leitura);
        addTag = findViewById(R.id.bt_addTagManual);
        addTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!newTag.getText().toString().isEmpty()){
                    addTagManual(newTag.getText().toString());
                }
            }
        });

        //Botão Iniciar Leitura
        iniciar = findViewById(R.id.btn_leituraTag);

        //ProgressBar
        pbLeitura = (ProgressBar) findViewById(R.id.pbLeitura);

        //EditText
        total = (TextView) findViewById(R.id.tvTotal);

        //Lista de Leitura
        listView = (ListView) findViewById(R.id.listView);

        listView.setBackgroundColor(Color.parseColor("#F2f2f2"));

        listref.clear();

        if (bLeituraHid) {
            setEnable(iniciar, false);
        }

        /*-----------------PARAMETROS LEITURA----------------*/
        //1-NA | 8-EU | 10-CH
        final int banda = spViaOnda.getInt("rfbanda", 1);
        final int potencia = spViaOnda.getInt("rfpotencia", 30);
        //final boolean bTrigger = spViaOnda.getBoolean("trigger", false);
        final boolean bTrigger = true; // define bipagem pelo gatilho
        int iTrigger = 0;
        if (bTrigger)
            iTrigger = 0;
        else
            iTrigger = 1;
        final int finalITrigger = iTrigger;

        //Ação botão iniciar

        fvFooter = (TextView) findViewById(R.id.footer_1);

        iniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!listPecaClas.isEmpty()) {
                    sp_classe.setEnabled(false);
                    selectedPecaClas = new PecaClas();
                    for (int x = 0; x < listPecaClas.size(); x++) {
                        if (listPecaClas.get(x).getNomeclas().equals(sp_classe.getSelectedItem())) {
                            selectedPecaClas = listPecaClas.get(x);
                            listPecaTipo = new PecaTipoDao(dac).getListPecaTipo(" WHERE CODITIPO IN (" + selectedPecaClas.getCodistri() + ")");
                        }

                    }
                    getPecaChip();
                } else {
                    AlertDialogCustom.showDialog(
                        Leitura.this,
                        getString(R.string.fail),
                        getString(R.string.no_row_data_for_pecaclas),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        },
                        null
                    );
                }


                if (fimPecaChip) {
                    ///Inicializa o módulo de leitura
                    if (btle_start == false && mBTLE_Service != null && Global.getConn_ble() == true) {

                        if (Utils_Bluetooth.hasWriteProperty(Global.getCharacteristicWrite().getProperties()) != 0) {

                            mGattUpdateReceiver.limpa_tagBtle();

                            Global.getCharacteristicWrite().setValue("T" + finalITrigger);
                            mBTLE_Service.writeCharacteristic(Global.getCharacteristicWrite());
                        }
                    }
                    btle_start = true;
                    //Tratamento para não gerar exceção em dispositivos que não contam com o módulo leitor RFID
                    if (bLeituraHid)
                        btle_start = true;

                    if (btle_start == false) {
                        AlertDialogCustom.showDialog(
                            Leitura.this,
                            getString(R.string.fail),
                            getString(R.string.fail_initialized_reading),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            },
                            null
                        );
                    } else {

                        //Seta a variavel para inflar nosso layout correto
                        statusTraduzir = false;

                        //Recebe o HASH da nossa biblioteca
                        //dataHash = scanResult6cNew;

                        //Cria o adapter inicial para quando a ação retornar da tradução não precisar esperar a leitura de uma TAG para atualizar
                        createAdapter();
                        isStart = false;
                        if (!isStart) {

                            isStart = true;

                            new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    while (isStart) {
                                        epc = Service_GATT.Retorno;

                                        if (epc != null && !epc.isEmpty()) {
                                            epc = epc.replaceAll(" ", "");
                                        }

                                        if (epc != null && !epc.isEmpty()) {
                                            if (epc.length() > 24) {
                                                rssi = epc.substring(epc.length() - 2, epc.length());
                                                epc = epc.substring(0, epc.length() - 2);
                                            } else {
                                                rssi = "64";
                                            }
                                            Message msg = new Message();
                                            Bundle b = new Bundle();
                                            b.putString("epc", epc);
                                            b.putString("rssi", rssi);
                                            msg.setData(b);

                                            handlerAtualiza.sendMessage(msg);
                                            mGattUpdateReceiver.limpa_tagBtle();
                                            epc = "";
                                            Service_GATT.Retorno = "";
                                        }
                                    }
                                }
                            }).start();
                        }

                        //Verifica se o leitor foi iniciado corretamente
                        if (isStart) {
                            pbLeitura.setVisibility(View.VISIBLE);
                            newTag.setVisibility(View.VISIBLE);
                            addTag.setVisibility(View.VISIBLE);
                        } else {
                            AlertDialogCustom.showDialog(
                                    Leitura.this,
                                    getString(R.string.fail),
                                    getString(R.string.fail_initialized_reading),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    },
                                    null
                            );
                        }
                    }
                }
            }
        });

        dac = new DAC(Leitura.this);
        daoClas = new PecaClasDao(dac);
        listPecaClas = daoClas.getListPecaClas();

        sp_classe = findViewById(R.id.spin_classe);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(Leitura.this, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);

        for (PecaClas pecaClas : listPecaClas) {
            adapter.add(pecaClas.getNomeclas());
        }
        sp_classe.setAdapter(adapter);


        stop = findViewById(R.id.btn_stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalizar();
            }
        });

        shared = findViewById(R.id.btn_shared);
        shared.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalizar();
                if (listaLeituras.isEmpty()){
                    AlertDialogCustom.showDialog(
                            Leitura.this,
                            getString(R.string.fail),
                            getString(R.string.no_shared_list_isEmpty),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            },
                            null
                    );
                } else {
                    int erro = 0;

                    if (selectedPecaClas.getCodigene() > 0){
                        if (sendMovPecaClas()) {
                            AlertDialogCustom.showDialog(
                                    Leitura.this,
                                    getString(R.string.sucess),
                                    getString(R.string.sucess_data_transmition),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            limpaTag();
                                            sp_classe.setEnabled(true);
                                        }
                                    },
                                    null
                            );
                        } else {
                            erro++;
                        }
                    } else if (sendPecaClas()) {
                        if (sendMovPecaClas()) {
                            AlertDialogCustom.showDialog(
                                    Leitura.this,
                                    getString(R.string.sucess),
                                    getString(R.string.sucess_data_transmition),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            limpaTag();
                                            sp_classe.setEnabled(true);
                                        }
                                    },
                                    null
                            );
                        } else {
                            erro++;
                        }
                    } else {
                        erro++;
                    }


                    if (erro > 0) {
                        AlertDialogCustom.showDialog(
                                Leitura.this,
                                getString(R.string.fail),
                                getString(R.string.fail_the_comunication_with_web_service),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                },
                                null
                        );
                    }
                }
            }
        });

        reset = findViewById(R.id.btn_reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                limpaTag();
            }
        });
    }

    private boolean sendMovPecaClas() {
        JSONObject jo = new JSONObject();
        String ret = "";
        int f = 0, count = 0;

        StringBuilder  ListaChips = new StringBuilder();
        for(int x = 0; x < listaLeituras.size(); x++){

            ListaChips.append(listaLeituras.get(x).sEPC.replaceFirst("^0+", "") + ((x != listaLeituras.size() -1) ? "," : ""));
        }

        do {
            try {
                JSONObject joEnvi = new JSONObject();
                joEnvi.put("rotina", "ProcessaLeituras");
                joEnvi.put("codiclas", selectedPecaClas.getCodigene());
                joEnvi.put("listachips", ListaChips);

                ServiceFunction serviceFunction = new ServiceFunction();
                ret = serviceFunction.sendPost("http://" + spViaOnda.getString("wsip", "") + ":" + spViaOnda.getInt("wsporta", 0) + "/sag/pecamvcl/Exec_Rotina", UtilLocal.TrataJSONEnvio(joEnvi.toString()), spViaOnda.getString("token", ""));
                jo = new JSONObject(ret);
                if (UtilLocal.flags(new JSONObject().put("result", jo.toString()))) {
                    f++;
                    Log.d("pecamvcl/ProcessaLeituras", jo.toString());
                    if (jo.getString("Result").equals("true")) {
                        //selectedPecaClas.setCodigene(jo.getInt("Valor"));
                        //daoClas.updateCodiGene(selectedPecaClas.getCodigene(), selectedPecaClas.getCodiclas());
                    } else { return false; }
                }
            } catch (ServiceFunction.MinhaException e) {
            } catch (JSONException e) {
            }
            count++;
        } while (f == 0 && count < 3);

        return (f > 0);
    }

    private boolean sendPecaClas() {
        JSONObject jo = new JSONObject();
        String ret = "";
        int f = 0, count = 0;

        do {
            try {
                JSONObject joEnvi = new JSONObject();
                joEnvi.put("codiclas", "0");
                joEnvi.put("nomeclas", selectedPecaClas.getNomeclas());
                joEnvi.put("dataclas", selectedPecaClas.getDataclas());
                String[] cd = selectedPecaClas.getCodistri().split(", ");

                if (cd.length >= 1) { joEnvi.put("coditp01",cd[0]); }
                if (cd.length >= 2) { joEnvi.put("coditp02",cd[1]); }
                if (cd.length >= 3) { joEnvi.put("coditp03",cd[2]); }
                if (cd.length >= 4) { joEnvi.put("coditp04",cd[3]); }
                if (cd.length == 5) { joEnvi.put("coditp05",cd[4]); }

                ServiceFunction serviceFunction = new ServiceFunction();
                ret = serviceFunction.sendPost("http://" + spViaOnda.getString("wsip", "") + ":" + spViaOnda.getInt("wsporta", 0) + "/sag/pecaclas/sgmanutencao", UtilLocal.TrataJSONEnvio(joEnvi.toString()), spViaOnda.getString("token", ""));
                jo = new JSONObject(ret);
                if (UtilLocal.flags(new JSONObject().put("result", jo.toString()))) {
                    f++;
                    Log.d("PecaClas/sgmanutencao", jo.toString());
                    if (jo.getString("Result").equals("true")) {
                        selectedPecaClas.setCodigene(jo.getInt("Valor"));
                        daoClas.updateCodiGene(selectedPecaClas.getCodigene(), selectedPecaClas.getCodiclas());
                    } else { return false; }
                }
            } catch (ServiceFunction.MinhaException e) {
            } catch (JSONException e) {
            }
            count++;
        } while (f == 0 && count < 3);
        return (f > 0);
    }

    private void getPecaChip() {
        String[] cd = selectedPecaClas.getCodistri().split(", ");
        joPecaChip  = new JSONObject();
        for ( int x = 0; x < cd.length; x++) {
            JSONArray ja = new JSONArray();
            String ret = "";
            int f = 0, count = 0;

            do {
                try {
                    JSONObject joEnvi = new JSONObject();
                    joEnvi.put("fonte_sql", "SQL_PecaChip");
                    joEnvi.put("where", "CODITIPO = "+cd[x]);

                    ServiceFunction serviceFunction = new ServiceFunction();
                    ret = serviceFunction.sendPost("http://" + spViaOnda.getString("wsip", "") + ":" + spViaOnda.getInt("wsporta", 0) + "/sag/pecachip/Listar_FK", UtilLocal.TrataJSONEnvio(joEnvi.toString()), spViaOnda.getString("token", ""));
                    ja = new JSONArray(ret);
                    if (UtilLocal.flags(new JSONObject().put("result", ja.toString()))) {
                        f++;
                        joPecaChip.put(cd[x], ja);
                        Log.d("SQL_PecaChip", ja.toString());
                        //joPecaChip.put(cd[x], "[{\"codichip\": 81,\"valochip\": \"4195\",\"coditipo\": "+cd[x]+"},{\"codichip\": 83,\"valochip\": \"4368\",\"coditipo\": "+cd[x]+"},{\"codichip\": 82,\"valochip\": \"9\",\"coditipo\": "+cd[x]+"}]");
                    }
                } catch (ServiceFunction.MinhaException e) {
                } catch (JSONException e) {
                }
                count++;
            } while (f == 0 || count == 3);
        }
        fimPecaChip = true;
    }

    public void finalizar(){
        //Seta a flag para interromper a thread de leitura
        isStart = false;

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (btle_start == false && mBTLE_Service != null && Global.getConn_ble() == true) {

            if (Utils_Bluetooth.hasWriteProperty(Global.getCharacteristicWrite().getProperties()) != 0) {
                Global.getCharacteristicWrite().setValue("T" + false);
                mBTLE_Service.writeCharacteristic(Global.getCharacteristicWrite());
            }
        }

        pbLeitura.setVisibility(View.INVISIBLE);
        newTag.setVisibility(View.INVISIBLE);
        addTag.setVisibility(View.INVISIBLE);

        if (listaLeituras.isEmpty())
            sp_classe.setEnabled(true);

        btle_start = false;
    }

    public void createAdapter() {

        //Vamos verificar se o adapter já foi criado
        //if (adapterTags == null) {
            adapterTags = new ListViewAdapter(this, listaLeituras);
            listView.setAdapter(adapterTags);
        //}

        //Exibe o total de TAGS lidas
        total.setText(String.valueOf(adapterTags.getCount()));
    }

    public void limpaTag() {

        //Limpa o Hash
        scanResult6cNew.clear();

        //Condição necessario pois se limparmos a lista sem nenhum registro o APP crash
        if (adapterTags != null) {

            //Limpa nossa lista
            adapterTags.mList.clear();

            //Atualiza o adapter
            listView.refreshDrawableState();
            adapterTags.notifyDataSetChanged();
        }

        //Atualiza o total de TAGS lidas
        total.setText("0");

        listaLeituras.clear();
        Service_GATT.Retorno = "";

        sp_classe.setEnabled(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //Seta a flag para interromper a thread de leitura
        isStart = false;

        //Seta a flag para interromper o controle de conexão com o bluetooth
        threadConnection = false;

        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Limpa a lista de TAGS
        limpaTag();

        //Finaliza a ação do botão do leitor RFID
        if (botao_rfid == true) {
            unregisterReceiver(keyReceiver);
            botao_rfid = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Seta a flag para interromper a thread de leitura
        isStart = false;

        //Seta a flag para interromper o controle de conexão com o bluetooth
        threadConnection = false;

        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Limpa a lista de TAGS
        limpaTag();

        //Finaliza a ação do botão do leitor RFID
        if (botao_rfid == true) {
            unregisterReceiver(keyReceiver);
            botao_rfid = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(!bLeituraHid) {
            //Monitora o estado do servidor GATT
            mGattUpdateReceiver = new Broadcast_GATT(new Comunicacao_Service_Bluetooth(), false);
            registerReceiver(mGattUpdateReceiver, Utils_Bluetooth.makeGattUpdateIntentFilter());

            //Cria e inicializa o service de comunicação
            mBTLE_Service_Intent = new Intent(this, Service_GATT.class);
            bindService(mBTLE_Service_Intent, mBTLE_ServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(!bLeituraHid) {
            unregisterReceiver(mGattUpdateReceiver);
            unbindService(mBTLE_ServiceConnection);
        }
    }

    //Aqui controla a quantidade de vezes que a etiqueta foi lida
    public int getLeituresByEPC(String sEPC) {
        int iRetorno = -1;

        for (int i = 0; i < listaLeituras.size(); i++) {
            LeituraRecebida pos = (LeituraRecebida) listaLeituras.get(i);
            if (pos.sEPC.equals(sEPC)) {
                iRetorno = pos.iLeituras;
                return iRetorno;
            }
        }
        return iRetorno;
    }

    /*------------Adapter-------------*/
    public class ListViewAdapter extends BaseAdapter {

        public Context mContext;
        public List<LeituraRecebida> mList;
        public LayoutInflater layoutInflater;


        public ListViewAdapter(Context context, List<LeituraRecebida> list) {
            mContext = context;
            mList = list;
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewParent) {
            ItemView iv;

            if (view == null) {
                iv = new ItemView();
                view = layoutInflater.inflate(R.layout.column_list, null);
                iv.txtTags = (TextView) view.findViewById(R.id.columnTags);
                iv.txtLeituras = (TextView) view.findViewById(R.id.columnLeituras);
                iv.txtRssi = (TextView) view.findViewById(R.id.columnRssi);
                iv.cx01 = (TextView) view.findViewById(R.id.cx_01);
                iv.cx02 = (TextView) view.findViewById(R.id.cx_02);
                iv.cx03 = (TextView) view.findViewById(R.id.cx_03);
                iv.cx04 = (TextView) view.findViewById(R.id.cx_04);
                iv.cx05 = (TextView) view.findViewById(R.id.cx_05);
                iv.ll = (LinearLayout) view.findViewById(R.id.ll01_leitura);
                view.setTag(iv);
            } else {
                iv = (ItemView) view.getTag();
            }

            LeituraRecebida leitura = mList.get(position);

            iv.txtTags.setText(leitura.sEPC.replaceFirst("^0+", ""));
            iv.txtLeituras.setText(String.valueOf(leitura.iLeituras));
            iv.txtRssi.setText(String.valueOf(leitura.iRSSI));

            for (int x = 0; x < listPecaTipo.size(); x++) {
                switch (x){
                    case 0 : { iv.cx01.setText(listPecaTipo.get(0).getSigltipo()); break; }
                    case 1 : { iv.cx02.setText(listPecaTipo.get(1).getSigltipo()); break; }
                    case 2 : { iv.cx03.setText(listPecaTipo.get(2).getSigltipo()); break; }
                    case 3 : { iv.cx04.setText(listPecaTipo.get(3).getSigltipo()); break; }
                    case 4 : { iv.cx05.setText(listPecaTipo.get(4).getSigltipo()); break; }
                    default: break;
                }
            }

            iv.cx01.setBackgroundColor(Color.WHITE);
            iv.cx02.setBackgroundColor(Color.WHITE);
            iv.cx03.setBackgroundColor(Color.WHITE);
            iv.cx04.setBackgroundColor(Color.WHITE);
            iv.cx05.setBackgroundColor(Color.WHITE);
            iv.cx01.setBackgroundResource(R.drawable.border_background);
            iv.cx02.setBackgroundResource(R.drawable.border_background);
            iv.cx03.setBackgroundResource(R.drawable.border_background);
            iv.cx04.setBackgroundResource(R.drawable.border_background);
            iv.cx05.setBackgroundResource(R.drawable.border_background);

            try {
                for (int x = 0; x < joPecaChip.length(); x++) {
                    JSONArray jsonArray = new JSONArray(joPecaChip.get(selectedPecaClas.getCodistri().split(", ")[x]).toString());
                    for (int y = 0; y < jsonArray.length(); y++) {
                        JSONObject jo = new JSONObject(jsonArray.getJSONObject(y).toString());
                        if (jo.getString("numegado").equals(leitura.sEPC)) {
                            for (int z = 0; z < listPecaTipo.size(); z++) {
                                if (jo.getInt("coditipo") == listPecaTipo.get(z).getCoditipo()) {
                                    switch (z){
                                        case 0 : {
                                            iv.cx01.setBackgroundColor(Color.parseColor(listPecaTipo.get(z).getColotipo()));
                                            break;
                                        }
                                        case 1 : {
                                            iv.cx02.setBackgroundColor(Color.parseColor(listPecaTipo.get(z).getColotipo()));
                                            break;
                                        }
                                        case 2 : {
                                            iv.cx03.setBackgroundColor(Color.parseColor(listPecaTipo.get(z).getColotipo()));
                                            break;
                                        }
                                        case 3 : {
                                            iv.cx04.setBackgroundColor(Color.parseColor(listPecaTipo.get(z).getColotipo()));
                                            break;
                                        }
                                        case 4 : {
                                            iv.cx05.setBackgroundColor(Color.parseColor(listPecaTipo.get(z).getColotipo()));
                                            break;
                                        }
                                        default: break;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            iv.txtRssi.setVisibility(View.VISIBLE);

            iv.ll.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    String selectedTag = iv.txtTags.getText().toString();

                    AlertDialogCustom.showDialog(
                            Leitura.this,
                            getString(R.string.attention),
                            getString(R.string.delete_tag_selected),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    listaLeituras.remove(position);
                                    listView.refreshDrawableState();
                                    listView.refreshDrawableState();
                                    adapterTags.notifyDataSetChanged();
                                    adapterTags.notifyDataSetChanged();
                                    total.setText(String.valueOf(adapterTags.getCount()));
                                }
                            },
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }
                    );
                    return true;
                }
            });
            return view;
        }

        private class ItemView {
            TextView txtTags;
            TextView txtLeituras;
            TextView txtRssi;
            TextView cx01, cx02, cx03, cx04, cx05;
            LinearLayout ll;
        }
    }

    /*-----Código para controle do botão do leitor---*/

    private long startTime = 0;
    private boolean keyUpFalg = true;

    private BroadcastReceiver keyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int keyCode = intent.getIntExtra("keyCode", 0);
            boolean keyDown = intent.getBooleanExtra("keydown", false);

            if (keyUpFalg && keyDown && System.currentTimeMillis() - startTime > 500) {
                keyUpFalg = false;
                startTime = System.currentTimeMillis();

                if ((keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_F2
                        || keyCode == KeyEvent.KEYCODE_F3 || keyCode == KeyEvent.KEYCODE_F4 || keyCode == KeyEvent.KEYCODE_F5)) {

                    if (!isStart)
                        iniciar.callOnClick();
                }

                return;

            } else if (keyDown) {
                startTime = System.currentTimeMillis();
            } else {
                keyUpFalg = true;
            }
        }
    };

    //Botão Menu
    @Override
    protected void onPause() {
        super.onPause();
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Inicial.ACTIVITY_SERVICE);
        activityManager.moveTaskToFront(getTaskId(), 0);
    }

    private void setEnable(ImageView imgview, boolean Enabled) {
        imgview.clearColorFilter();

        if (Enabled) {
            imgview.setAlpha(1f);
        } else {
            imgview.setAlpha(0.1f);
        }
        imgview.setEnabled(Enabled);
    }

    private void addTagManual(String newValue){
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString("epc", newValue);
        b.putString("rssi", "0");
        msg.setData(b);
        handlerAtualiza.sendMessage(msg);
        newTag.setText("");
        listView.refreshDrawableState();
        adapterTags.notifyDataSetChanged();
        total.setText(String.valueOf(adapterTags.getCount()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}