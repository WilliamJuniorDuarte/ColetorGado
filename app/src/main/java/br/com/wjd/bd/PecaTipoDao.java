package br.com.wjd.bd;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import br.com.wjd.Classes.PecaTipo;

public class PecaTipoDao {

    private DAC dac;
    public PecaTipoDao(DAC dac) {
        this.dac = dac;
    }

    public Boolean insertPecaTipo(PecaTipo pecatipo){
        SQLiteDatabase db = dac.getWritableDatabase();
        Boolean ret;

        ContentValues contentValues = new ContentValues();
        contentValues.put("coditipo", pecatipo.getCoditipo());
        contentValues.put("nometipo", pecatipo.getNametipo());
        contentValues.put("sigltipo", pecatipo.getSigltipo());
        contentValues.put("colotipo", pecatipo.getColotipo());
        ret = (db.insert("pecatipo", null, contentValues) > 0);
        db.close();
        return ret;
    }

    public Boolean deleteAll(){
        SQLiteDatabase db = dac.getWritableDatabase();
        Boolean ret = false;
        ret = (db.delete("pecatipo", " (select coditipo from pecatipo)", null) > 0);
        db.close();
        return ret;
    }

    @SuppressLint("Range")
    public List<PecaTipo> getListPecaTipo( String where) {
        List<PecaTipo> listPecaTipo = new ArrayList<>();
        SQLiteDatabase db = dac.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM pecatipo"+ where, null, null);

        if (cursor.moveToFirst()){
            do {
                PecaTipo pecatipo = new PecaTipo();
                pecatipo.setCoditipo(cursor.getInt(cursor.getColumnIndex("coditipo")));
                pecatipo.setNametipo(cursor.getString(cursor.getColumnIndex("nometipo")));
                pecatipo.setSigltipo(cursor.getString(cursor.getColumnIndex("sigltipo")));
                pecatipo.setColotipo(cursor.getString(cursor.getColumnIndex("colotipo")));
                listPecaTipo.add(pecatipo);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return listPecaTipo;
    }
}
