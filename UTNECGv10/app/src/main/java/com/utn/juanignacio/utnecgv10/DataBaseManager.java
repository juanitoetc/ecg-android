package com.utn.juanignacio.utnecgv10;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by JuanIgnacio on 18/01/2016.
 */
public class DataBaseManager {

    public static final String TABLE_NAME = "tablaPacientes"; /*Nombre de la tabla*/
    public static final String CN_ID = "_id"; /*Column Name id, sera autoincremental*/
    public static final String CN_LAST_NAME = "pacientLastName"; /*Apellido de la persona*/
    public static final String CN_FIRST_NAME = "pacientFirstName"; /*Nombre de la persona*/
    public static final String CN_DOCUMENT = "pacientDocument"; /*Documento de la persona*/
    public static final String CN_PHONE = "pacientPhone"; /*Telefono de la persona*/
    public static final String CN_OBSERVATIONS = "pacientObserv"; /*Observaciones*/
    public static final String CN_SAMPLES_I ="pacientV1"; /* Guardo la señal 1*/
    public static final String CN_SAMPLES_II ="pacientV2"; /* Guardo la señal 2*/
    public static final String CN_SAMPLES_III ="pacientV3"; /* Guardo la señal 3*/
    public static final String CN_PATH ="pacientPath"; /*Guardo el Path a la imagen del paciente*/
    public static final String CN_TIMESTAMP ="pacientTimeStamp"; /*Guardo la hora en la que se hizo el estudio*/

    public static final String CREATE_TABLE = "create table " + TABLE_NAME + " ("
            + CN_ID + " integer primary key autoincrement,"
            + CN_LAST_NAME + " text not null,"
            + CN_FIRST_NAME + " text not null,"
            + CN_DOCUMENT + " integer not null,"
            + CN_PHONE + " integer,"
            + CN_OBSERVATIONS + " text,"
            + CN_SAMPLES_I + " text,"
            + CN_SAMPLES_II + " text,"
            + CN_SAMPLES_III + " text,"
            + CN_PATH + " text,"
            + CN_TIMESTAMP + " text);";

    /*Variables privadas para que los metodos esten dentro de esta clase*/
    private DbHelper helper;
    private SQLiteDatabase db;

    public DataBaseManager(Context context) {

        helper = new DbHelper(context); /*Todavia no esta creada la base de datos*/
        db = helper.getWritableDatabase(); /*Nos devuelve la base de datos creandola o abriendola*/
    }

    public ContentValues generateContentValues (String apellido, String nombre, int documento, int phone, String observ, String lead1, String lead2, String lead3 , String rec_Timestamp, String path){

        ContentValues valores = new ContentValues();
        Long tsLong = System.currentTimeMillis();
        String str_Timestamp = tsLong.toString();

        if(!(rec_Timestamp == null)){
            str_Timestamp = rec_Timestamp;
        }

        valores.put(CN_LAST_NAME, apellido);
        valores.put(CN_FIRST_NAME, nombre);
        valores.put(CN_DOCUMENT, documento);
        valores.put(CN_TIMESTAMP, str_Timestamp);
        valores.put(CN_PHONE, phone);
        valores.put(CN_OBSERVATIONS, observ);
        valores.put(CN_SAMPLES_I, lead1);
        valores.put(CN_SAMPLES_II, lead2);
        valores.put(CN_SAMPLES_III, lead3);
        valores.put(CN_PATH, path);

        return valores;
    }

    public void insertPacient(classPacientFull paciente){

        db.insert(TABLE_NAME, null, generateContentValues(paciente.str_cPacientLastName,paciente.str_cPacientFirstName, paciente.int_cPacientDocum, paciente.int_cPacientPhone, paciente.str_cObserv, paciente.str_cLeadI, paciente.str_cLeadII, paciente.str_cLeadIII, paciente.str_cTimeStamp, paciente.str_cPath));

    }

    public Cursor cargarCursorContactos(){
        /*Hay que hacer una query*/
        String[] columnas = new String[]{CN_ID,CN_LAST_NAME,CN_FIRST_NAME,CN_DOCUMENT,CN_PHONE,CN_OBSERVATIONS,CN_SAMPLES_I,CN_SAMPLES_II,CN_SAMPLES_III,CN_PATH,CN_TIMESTAMP};

        return db.query(TABLE_NAME,columnas,null,null,null,null,null);
    }

    public Cursor readCursorContactos(String id_to_restore){
        /*Hay que hacer una query*/
        /*  CN_ID = 0
            CN_LAST_NAME = 1
            CN_FIRST_NAME = 2
            CN_DOCUMENT = 3
            CN_PHONE = 4
            CN_OBSERVATIONS = 5
            CN_SAMPLES_I = 6
            CN_SAMPLES_II = 7
            CN_SAMPLES_III = 8
            CN_PATH = 9
            CN_TIMESTAMP = 10
        */

        String[] columnas = new String[]{CN_ID,CN_LAST_NAME,CN_FIRST_NAME,CN_DOCUMENT,CN_PHONE,CN_OBSERVATIONS,CN_SAMPLES_I,CN_SAMPLES_II,CN_SAMPLES_III,CN_PATH,CN_TIMESTAMP};
        return db.query(TABLE_NAME,columnas,CN_ID+"=?",new String[] {id_to_restore}, null, null, null);
    }

    public void deletePacient(String str_deleteId){

        db.delete(TABLE_NAME, CN_ID+"=?", new String[] {str_deleteId});
    }
}
