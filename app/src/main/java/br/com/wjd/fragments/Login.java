package br.com.wjd.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import br.com.wjd.Classes.ServiceFunction;
import br.com.wjd.R;
import br.com.wjd.UtilLocal;

public class Login extends AppCompatActivity {

    private SharedPreferences spViaOnda;
    private SharedPreferences.Editor ed;
    private boolean ipOk;
    private ImageButton settings, passwordVisibility;
    private TextInputEditText username, password;
    private Button confirm;
    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        spViaOnda = this.getSharedPreferences("wjdPreferences", MODE_PRIVATE);
        ed = spViaOnda.edit();

        ipOk = !spViaOnda.getString("wsip", "").isEmpty();

        settings = findViewById(R.id.bt_settings_login);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Configuracao.class));
            }
        });

        passwordVisibility = findViewById(R.id.iv_visiblepassword_login);
        passwordVisibility.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        break;
                }
                password.setSelection(password.getText().length());
                return true;
            }
        });

        confirm = findViewById(R.id.bt_confirm_login);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validaCampos(view)){
                    if (ipOk){
                        int f = 0, count = 0;
                        do {
                            try {
                                JSONObject joEnv = new JSONObject();
                                joEnv.put("jwtusername", username.getText());
                                joEnv.put("jwtpassword", password.getText());

                                ServiceFunction serviceFunction = new ServiceFunction();
                                String ret = serviceFunction.sendPost("http://" + spViaOnda.getString("wsip", "") + ":" + spViaOnda.getInt("wsporta", 8080) + "/sag/login", UtilLocal.TrataJSONEnvio(joEnv.toString()), "");
                                if (UtilLocal.flags(new JSONObject(ret))){
                                    JSONObject joRet = new JSONObject(ret);
                                    ed.putString("token", joRet.getString("token"));
                                    ed.putString("username", joRet.getJSONObject("refreshToken").getString("userId"));
                                    ed.apply();
                                    Toast.makeText(Login.this, getString(R.string.welcome)+" "+joRet.getJSONObject("refreshToken").getString("userId")+"!", Toast.LENGTH_SHORT).show();
                                    f++;
                                }
                            } catch (ServiceFunction.MinhaException e) {
                            } catch (JSONException e) {
                            }
                            count++;
                        } while (f == 0 && count < 3);

                        if (f > 0) {
                            startActivity(new Intent(Login.this, Inicial.class));
                            finish();
                        }
                        else {
                            new AlertDialog.Builder(Login.this).setTitle(R.string.fail).
                                    setMessage(R.string.fail_the_comunication_with_web_service)
                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .show();
                        }
                    } else {
                        new AlertDialog.Builder(Login.this).setTitle(R.string.attention).
                                setMessage(R.string.info_webservice_not_found)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .show();
                    }
                } else {
                    new AlertDialog.Builder(Login.this).setTitle(R.string.fail).
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

        username = findViewById(R.id.actext_username_login);
        password = findViewById(R.id.actext_password_login);
    }

    private boolean validaCampos(View view){
        message = "";

        if (username.getText().toString().isEmpty()){ message = getResources().getString(R.string.check_username); username.setError(message); }
        if (message.isEmpty() && password.getText().toString().isEmpty()){ message = getResources().getString(R.string.check_password); password.setError(message); }

        return message.isEmpty();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        spViaOnda = this.getSharedPreferences("wjdPreferences", MODE_PRIVATE);
        ed = spViaOnda.edit();
        ipOk = !spViaOnda.getString("wsip", "").isEmpty();
    }
}
