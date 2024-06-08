package br.com.wjd.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.com.wjd.Classes.PecaClas;
import br.com.wjd.Classes.PecaTipo;
import br.com.wjd.Classes.ServiceFunction;
import br.com.wjd.R;
import br.com.wjd.UtilLocal;
import br.com.wjd.adapters.ItemPecaTipoAdapter;
import br.com.wjd.adapters.RecyclerViewSeparator;
import br.com.wjd.bd.DAC;
import br.com.wjd.bd.PecaClasDao;
import br.com.wjd.bd.PecaTipoDao;

public class AddClass extends AppCompatActivity {

    private RecyclerView rv;
    private Button btn_confirm;
    private TextInputEditText name;
    private AppCompatImageButton btn_date;
    private static EditText et_data;
    private static Date date;
    private String message;
    private DAC dac;
    private PecaClasDao dao;
    private PecaClas pecaClas;
    private List<PecaTipo> listPecaTipo = new ArrayList<>();
    private ItemPecaTipoAdapter adapter;
    private PecaTipoDao daoTipo;
    private int codiclas = 0;
    private SharedPreferences spViaOnda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addclass);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        spViaOnda = this.getSharedPreferences("wjdPreferences", MODE_PRIVATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_addclass);
        toolbar.setTitle(getResources().getString(R.string.add_class));
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        et_data = findViewById(R.id.et_date_addclass);
        btn_date = findViewById(R.id.ib_date_addclass);
        et_data.setText(UtilLocal.formatDate(UtilLocal.getDataAtual()));
        rv = findViewById(R.id.rv_addclass);
        name = findViewById(R.id.actext_name_addclass);
        dac = new DAC(AddClass.this);
        dao = new PecaClasDao(dac);
        daoTipo = new PecaTipoDao(dac);
        pecaClas = new PecaClas();

        btn_confirm = findViewById(R.id.btn_confirm_addclass);
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (valida()){
                    savePecaClas();
                } else {
                    new AlertDialog.Builder(AddClass.this).setTitle(R.string.fail).
                            setMessage(message)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();
                }
            }
        });

        btn_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialog = new DatePickerFragment();
                dialog.show(getSupportFragmentManager(), "Date");
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            codiclas = extras.getInt("codiclas");
            loadEdit();
        }

        loadRecyclerView();
    }

    private void savePecaClas() {
        pecaClas.setNomeclas(name.getText().toString());
        pecaClas.setDataclas(et_data.getText().toString());
        pecaClas.setCodistri(adapter.getcodigo(listPecaTipo).toString().replace("[", "").replace("]",""));
        if (codiclas > 0) {
            if (dao.updatePecaClas(pecaClas)) {
                new AlertDialog.Builder(AddClass.this).setTitle(R.string.sucess).
                        setMessage(R.string.sucess_update)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show();
            } else {
                new AlertDialog.Builder(AddClass.this).setTitle(R.string.fail).
                        setMessage(R.string.fail_update)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        } else {
            if (dao.insertPecaClas(pecaClas)) {
                new AlertDialog.Builder(AddClass.this).setTitle(R.string.sucess).
                        setMessage(R.string.sucess_insert)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show();
            } else {
                new AlertDialog.Builder(AddClass.this).setTitle(R.string.fail).
                        setMessage(R.string.fail_insert)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        }
    }

    private void loadEdit() {
        pecaClas = dao.getpecaclas(codiclas);
        et_data.setText(pecaClas.getDataclas());
        name.setText(pecaClas.getNomeclas());
    }

    private void loadRecyclerView(){
        getPecaTipo();
        listPecaTipo = daoTipo.getListPecaTipo("");
        rv.addItemDecoration(new RecyclerViewSeparator(AddClass.this));
        rv.setLayoutManager(new LinearLayoutManager(AddClass.this));
        adapter = new ItemPecaTipoAdapter(listPecaTipo);
        adapter.selectCodiTipos( pecaClas.getCodistri() != null ? pecaClas.getCodistri()+"," : "");
        rv.setAdapter(adapter);
    }

    private void getPecaTipo(){
        JSONArray ja = new JSONArray();
        String ret = "";
        int f = 0, count = 0;
        JsonObject joEnvi = new JsonObject();
        joEnvi.addProperty("fonte_sql", "SQL_PECaTipo");
        do {
            try {
                ServiceFunction serviceFunction = new ServiceFunction();
                ret = serviceFunction.sendPost("http://"+spViaOnda.getString("wsip", "")+":"+spViaOnda.getInt("wsporta", 0)+"/sag/pecatipo/Listar_FK", UtilLocal.TrataJSONEnvio(joEnvi.toString()), spViaOnda.getString("token", ""));
                ja = new JSONArray(ret);
                if (UtilLocal.flags(new JSONObject().put("result", ja.toString()))) {
                    f++;
                    Log.d("SQL_PECaTipo", ja.toString());
                }
            } catch (ServiceFunction.MinhaException e) {
            } catch (JSONException e) {
            }
            count++;
        } while (f == 0 && count < 3);

        if (f > 0) {
            PecaTipo pecaTipo = new PecaTipo();
            insertPecaTipoBd(pecaTipo.convertJsonToList(ja));
        } else {
            new AlertDialog.Builder(AddClass.this)
                    .setTitle(R.string.fail)
                    .setMessage(R.string.fail_the_comunication_with_web_service)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .show();
        }
    }

    private void insertPecaTipoBd(List<PecaTipo> list){
        boolean erroInsert = false;

        daoTipo.deleteAll();

        for (int x = 0; x < list.size(); x++){
            if (!daoTipo.insertPecaTipo(list.get(x))){
                erroInsert = true;
            }
        }

        if (erroInsert){
            new AlertDialog.Builder(AddClass.this).setTitle(R.string.sucess).
                    setMessage(R.string.fail_insert)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        }
    }

    private Boolean valida() {
        message = "";
        if (name.getText().toString().isEmpty()){ message = getResources().getString(R.string.check_name); name.setError(message); }
        //if (message.isEmpty() &&)
        return message.isEmpty();
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            return new DatePickerDialog( getActivity(), this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH) );
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            et_data.setText((day < 10 ? "0" + day : day) + "/" + ((month + 1) < 10 ? "0" + (month + 1) : (month + 1)) + "/" + year);
            try {date = UtilLocal.parseStringToDate(et_data.getText().toString());} catch (Exception ex) {date = UtilLocal.getDataAtual(); et_data.setText(UtilLocal.formatDate(date));}
        }
    }
}
