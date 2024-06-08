package br.com.wjd.bd;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import br.com.wjd.Classes.PecaClas;

public class PecaClasDao {

    private DAC dac;
    public PecaClasDao(DAC dac) {
        this.dac = dac;
    }


    public Boolean insertPecaClas(PecaClas pecaclas){
        SQLiteDatabase db = dac.getWritableDatabase();
        Boolean ret = false;

        ContentValues contentValues = new ContentValues();
        contentValues.put("nomeclas", pecaclas.getNomeclas());
        contentValues.put("dataclas", pecaclas.getDataclas());
        contentValues.put("codistri", pecaclas.getCodistri());
        ret = (db.insert("pecaclas", null, contentValues) > 0);
        db.close();
        return ret;
    }

    public void updateCodiGene(int codigene, int codiclas){
        SQLiteDatabase db = dac.getWritableDatabase();
        db.execSQL("UPDATE PECACLAS SET CODIGENE = "+codigene+" WHERE CODICLAS = "+codiclas);
    }

    public Boolean updatePecaClas(PecaClas pecaclas){
        SQLiteDatabase db = dac.getWritableDatabase();
        Boolean ret = false;

        ContentValues contentValues = new ContentValues();
        contentValues.put("nomeclas", pecaclas.getNomeclas());
        contentValues.put("dataclas", pecaclas.getDataclas());
        contentValues.put("codistri", pecaclas.getCodistri());
        ret = (db.update("pecaclas", contentValues, " codiclas = "+pecaclas.getCodiclas(), null) > 0);
        db.close();
        return ret;
    }

    public Boolean deletePecaClas(int codiclas){
        SQLiteDatabase db = dac.getWritableDatabase();
        Boolean ret = false;
        ret = (db.delete("pecaclas", " codiclas = "+codiclas, null) > 0);
        db.close();
        return ret;
    }

    @SuppressLint("Range")
    public List<PecaClas> getListPecaClas() {
        List<PecaClas> listPecaClas = new ArrayList<>();
        SQLiteDatabase db = dac.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM pecaclas", null, null);

        if (cursor.moveToFirst()){
            do {
                PecaClas pecaClas = new PecaClas();
                pecaClas.setCodiclas(cursor.getInt(cursor.getColumnIndex("codiclas")));
                pecaClas.setNomeclas(cursor.getString(cursor.getColumnIndex("nomeclas")));
                pecaClas.setDataclas(cursor.getString(cursor.getColumnIndex("dataclas")));
                pecaClas.setCodistri(cursor.getString(cursor.getColumnIndex("codistri")));
                pecaClas.setCodigene(cursor.getInt(cursor.getColumnIndex("codigene")));
                listPecaClas.add(pecaClas);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return listPecaClas;
    }

    @SuppressLint("Range")
    public PecaClas getpecaclas(int codiclas) {
        PecaClas pecaClas = new PecaClas();
        SQLiteDatabase db = dac.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM pecaclas where codiclas = "+codiclas, null, null);

        if (cursor.moveToFirst()){
            do {
                pecaClas.setCodiclas(cursor.getInt(cursor.getColumnIndex("codiclas")));
                pecaClas.setNomeclas(cursor.getString(cursor.getColumnIndex("nomeclas")));
                pecaClas.setDataclas(cursor.getString(cursor.getColumnIndex("dataclas")));
                pecaClas.setCodistri(cursor.getString(cursor.getColumnIndex("codistri")));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return pecaClas;
    }
}
