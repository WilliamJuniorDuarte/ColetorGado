package br.com.wjd;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UtilLocal {

    public static Context context;

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    public static Date getDataAtual() {
        return Calendar.getInstance().getTime();
    }
    public static Date parseStringToDate( String date ) throws ParseException {
        return new Date( new SimpleDateFormat("dd/MM/yyyy").parse( date ).getTime() );
    }
    public static String formatDate( Date date ) {
        return new SimpleDateFormat( "dd/MM/yyyy").format( date );
    }

    public static String TrataJSONEnvio( String value ) {
        String PARSE_BARRA = "::B::";
        String PARSE_INTERROGACAO = "::I::";
        String PARSE_PORCENTAGEM = "::P::";
        String PARSE_QUEBRA = "::Q::";

        if (value != null && value.length() > 0) {
            value = value.replace("\\/", PARSE_BARRA);
            value = value.replace("?", PARSE_INTERROGACAO);
            value = value.replace("%", PARSE_PORCENTAGEM);
            value = value.replace("\\n", PARSE_QUEBRA);
        }
        return value;
    }

    public static boolean flags(JSONObject jo) {
        boolean ret = true;
        try {
            JSONArray ja = jo.getJSONArray("result");
            jo = ja.getJSONObject(0);
            if (jo.getInt("value") == 0) {
                ret = false;
            }
        } catch (JSONException e) {
            System.out.println("Erro " + e.getMessage());
        } finally {
            return ret;
        }
    }
}

