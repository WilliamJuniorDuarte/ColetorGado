package br.com.wjd.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.wjd.Classes.PecaClas;
import br.com.wjd.R;
import br.com.wjd.adapters.ItemPecaClasAdapter;
import br.com.wjd.adapters.RecyclerItemClickListener;
import br.com.wjd.adapters.RecyclerViewSeparator;
import br.com.wjd.bd.DAC;
import br.com.wjd.bd.PecaClasDao;

public class ListPecaClas extends AppCompatActivity {

    private RecyclerView rv;
    private Button btn_add;
    private List<PecaClas> listPecaclas;
    private PecaClasDao dao;
    private DAC dac;
    private ItemPecaClasAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_pecaclas);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_listPecaClas);
        toolbar.setTitle(getResources().getString(R.string.btclass));
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.baseline_arrow_white_24);

        dac = new DAC(ListPecaClas.this);
        dao = new PecaClasDao(dac);
        listPecaclas = dao.getListPecaClas();

        rv = findViewById(R.id.rv_list_pecaclas);
        rv.addItemDecoration(new RecyclerViewSeparator(ListPecaClas.this));
        rv.setLayoutManager(new LinearLayoutManager(ListPecaClas.this));
        loadRecyclerView();

        rv.addOnItemTouchListener(new RecyclerItemClickListener(ListPecaClas.this, rv, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (listPecaclas.get(position).getCodiclas() > 0) {
                            Bundle bundle = new Bundle();
                            bundle.putInt("codiclas", listPecaclas.get(position).getCodiclas());
                            Intent intent = new Intent(ListPecaClas.this, AddClass.class);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        new AlertDialog.Builder(ListPecaClas.this).setTitle(R.string.attention).
                                setMessage(R.string.delete_onlongitem_pressed)
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (dao.deletePecaClas(listPecaclas.get(position).getCodiclas())){
                                            loadRecyclerView();
                                            new AlertDialog.Builder(ListPecaClas.this).setTitle(R.string.sucess).
                                                    setMessage(R.string.sucess_delete)
                                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                        }
                                                    })
                                                    .show();
                                        } else {
                                            new AlertDialog.Builder(ListPecaClas.this).setTitle(R.string.fail).
                                                    setMessage(R.string.fail_delete)
                                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                        }
                                                    })
                                                    .show();
                                        }
                                    }
                                })
                                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                })
                                .show();
                    }
                }));

        btn_add = findViewById(R.id.btn_add_listpecaclas);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListPecaClas.this, AddClass.class);
                startActivity(intent);
            }
        });
    }

    private void loadRecyclerView(){
        listPecaclas = dao.getListPecaClas();
        adapter = new ItemPecaClasAdapter(listPecaclas);
        rv.setAdapter(adapter);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        loadRecyclerView();
    }
}
