package br.com.wjd.bd;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import javax.annotation.Nullable;

public class DAC extends SQLiteOpenHelper {

    public DAC(@Nullable Context context){
        super(context, "coletor", null, 4);
    }

    @Override
    public void onCreate (SQLiteDatabase db){
        String SQL_PecaClas = "CREATE TABLE pecaclas (" +
                " codiclas INTEGER PRIMARY KEY AUTOINCREMENT," +
                " nomeclas VARCHAR," +
                " dataclas DATE," +
                " codistri VARCHAR," +
                " expoclas INTEGER," +
                " codigene INTEGER);";
        db.execSQL(SQL_PecaClas);

        String SQL_PecaTipo = "CREATE TABLE pecatipo (" +
                " coditipo INTEGER," +
                " nometipo VARCHAR," +
                " colotipo VARCHAR," +
                " sigltipo VARCHAR);";
        db.execSQL(SQL_PecaTipo);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        String SQL_PecaClas = "DROP TABLE pecaclas";
        db.execSQL(SQL_PecaClas);
        String SQL_PecaTipo = "DROP TABLE pecatipo";
        db.execSQL(SQL_PecaTipo);
        onCreate(db);
    }
}
