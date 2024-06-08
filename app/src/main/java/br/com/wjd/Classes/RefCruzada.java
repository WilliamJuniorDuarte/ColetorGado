package br.com.wjd.Classes;

public class RefCruzada {

    String epc_from;
    String epc_to;
    String company_id;

    public RefCruzada() {
    }

    public RefCruzada(String epc_from, String epc_to, String company_id) {
        this.epc_from = epc_from;
        this.epc_to = epc_to;
        this.company_id = company_id;
    }

    public String getEpc_from() {
        return epc_from;
    }

    public void setEpc_from(String epc_from) {
        this.epc_from = epc_from;
    }

    public String getEpc_to() {
        return epc_to;
    }

    public void setEpc_to(String epc_to) {
        this.epc_to = epc_to;
    }

    public String getCompany_id() {
        return company_id;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }
}


