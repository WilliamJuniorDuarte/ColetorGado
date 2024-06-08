package br.com.wjd.Classes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PecaTipo {
    private int coditipo;
    private String nametipo;
    private String colotipo;
    private String sigltipo;

    public PecaTipo() {
    }

    public int getCoditipo() {
        return coditipo;
    }

    public void setCoditipo(int coditipo) {
        this.coditipo = coditipo;
    }

    public String getNametipo() {
        return nametipo;
    }

    public void setNametipo(String nametipo) {
        this.nametipo = nametipo;
    }

    public String getColotipo() {
        return colotipo;
    }

    public void setColotipo(String colotipo) {
        this.colotipo = colotipo;
    }

    public String getSigltipo() {
        return sigltipo;
    }

    public void setSigltipo(String sigltipo) {
        this.sigltipo = sigltipo;
    }

    public List<PecaTipo> convertJsonToList(JSONArray ja){
        List<PecaTipo> list = new ArrayList<>();
        for (int x = 0; x < ja.length(); x++){
            PecaTipo pecaTipo = new PecaTipo();
            try {
                JSONObject jo = new JSONObject(ja.get(x).toString());
                pecaTipo.setCoditipo(Integer.parseInt(jo.getString("coditipo")));
                pecaTipo.setNametipo(jo.getString(("nometipo")));
                pecaTipo.setSigltipo(jo.getString(("sigltipo")));
                pecaTipo.setColotipo(jo.getString("colotipo"));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            list.add(pecaTipo);
        }
        return list;
    }
}
