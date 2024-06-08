package br.com.wjd.Classes;

public class LeituraRecebida {
    public String   sEPC;
    public int      iLeituras;
    public String   sRSSIHex;
    public int      iRSSI;
    public String   vc_datetime;
    public String   Observacao;

    public LeituraRecebida (String EPC, int iLeituras, String RSSI_Hex, String pc_datetime, String sObservacao)
    {
        this.sEPC = EPC;
        this.iLeituras = iLeituras;
        this.sRSSIHex = RSSI_Hex;
        this.iRSSI = Integer.parseInt(RSSI_Hex, 16);
        this.vc_datetime = pc_datetime;
        this.Observacao = sObservacao;
    }

    public LeituraRecebida() { }

    public String getsEPC() { return this.sEPC; }
    public int getLeituras() { return this.iLeituras; }
    public String getsRSSIHex() { return this.sRSSIHex; }
    public int getiRSSI() { return this.iRSSI; }

    public void setsEPC(String sEPC) {
        this.sEPC = sEPC;
    }

    public void setiLeituras(int iLeituras) {
        this.iLeituras = iLeituras;
    }

    public void setsRSSIHex(String sRSSIHex) {
        this.sRSSIHex = sRSSIHex;
        this.iRSSI = Integer.parseInt(sRSSIHex, 16);
    }

    public String getVc_datetime() {
        return vc_datetime;
    }

    public void setVc_datetime(String vc_datetime) {
        this.vc_datetime = vc_datetime;
    }
}
