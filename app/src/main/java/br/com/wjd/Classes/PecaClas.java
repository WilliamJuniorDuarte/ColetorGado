package br.com.wjd.Classes;

public class PecaClas {
    private int codiclas;
    private String nomeclas;
    private String dataclas;
    private String codistri;
    private int codigene;

    public PecaClas(int codiclas, String nomeclas, String dataclas, String codistri, int codigene) {
        this.codiclas = codiclas;
        this.nomeclas = nomeclas;
        this.dataclas = dataclas;
        this.codistri = codistri;
        this.codigene = codigene;
    }

    public PecaClas() {
    }

    public int getCodigene() {
        return codigene;
    }

    public void setCodigene(int codigene) {
        this.codigene = codigene;
    }

    public int getCodiclas() {
        return codiclas;
    }

    public void setCodiclas(int codiclas) {
        this.codiclas = codiclas;
    }

    public String getNomeclas() {
        return nomeclas;
    }

    public void setNomeclas(String nomeclas) {
        this.nomeclas = nomeclas;
    }

    public String getDataclas() {
        return dataclas;
    }

    public void setDataclas(String dataclas) {
        this.dataclas = dataclas;
    }

    public String getCodistri() {
        return codistri;
    }

    public void setCodistri(String codistri) {
        this.codistri = codistri;
    }
}
