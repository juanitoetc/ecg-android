package com.utn.juanignacio.utnecgv10;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by JuanIgnacio on 18/01/2016.
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "pacientesDB.sqlite"; /* Nombre de la base de datos como global */
    private static final int DB_SCHEME_VERSION = 3; /* Version de la estructura de la base de datos. No de SQLite */

    public DbHelper(Context context) {

        super(context, DB_NAME, null, DB_SCHEME_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(DataBaseManager.CREATE_TABLE); /*Todavia no se crea la base de datos*/

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        /*Si hay un cambio en la version del esquema de la base de datos se llamará a este metodo*/
        /*Destruye la base de datos anterior y la construye denuevo*/
        /*OJO NO GUARDA LOS DATOS ANTERIORES*/

        db.execSQL("DROP TABLE tablaPacientes;");
        onCreate(db);

    }
}
