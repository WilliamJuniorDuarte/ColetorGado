package br.com.wjd.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.warkiz.tickseekbar.TickSeekBar;

import br.com.wjd.R;

public class Config extends Fragment {

    private TextInputEditText ip, porta;
    private TickSeekBar potencia;
    private Button bt_save;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_config, container, false);

        //SharedPreferences
        final SharedPreferences spViaOnda = getActivity().getSharedPreferences("wjdPreferences", MODE_PRIVATE);
        final SharedPreferences.Editor ed = spViaOnda.edit();

        ip = v.findViewById(R.id.actext_ip_config);
        porta = v.findViewById(R.id.actext_porta_config);
        potencia = v.findViewById(R.id.spin_rfpotencia_config);
        ip.setText(spViaOnda.getString("wsip", ""));
        porta.setText(String.valueOf(spViaOnda.getInt("wsporta", 0)));
        potencia.setProgress(spViaOnda.getInt("rfpotencia", 5));

        bt_save = v.findViewById(R.id.btn_save_config);
        bt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ed.putInt("wsenable", 1);
                ed.putString("wsip", ip.getText().toString());
                ed.putInt("wsporta", porta.getText().length()>0 ? Integer.parseInt(porta.getText().toString()) : 0);
                ed.putInt("rfpotencia", (int)potencia.getProgress());
                ed.apply();

                new AlertDialog.Builder(v.getContext())
                        .setTitle(v.getResources().getString(R.string.sucess))
                        .setMessage(v.getResources().getString(R.string.sucess_save_config))
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                getActivity().finish();
                            }
                        })
                        .show();
            }
        });
        return v;
    }
}
