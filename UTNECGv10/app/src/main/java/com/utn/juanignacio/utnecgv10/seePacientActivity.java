package com.utn.juanignacio.utnecgv10;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class seePacientActivity extends ActionBarActivity implements View.OnClickListener{

    DataBaseManager manager3;
    /*Declaro privados todos los textviews*/

    private TextView txtv_restLastName;
    private TextView txtv_restFirstName;
    private TextView txtv_restDocum;
    private TextView txtv_restPhone;
    private TextView txtv_restObserv;
    private TextView txtv_restDate;

    /*Declaro los botones*/
    private Button bt_openLeadI;
    private Button bt_openLeadII;
    private Button bt_openLeadIII;
    private Button bt_openLeadAll;

    /*Cursor*/
    public Cursor restoreCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_pacient);

        SimpleDateFormat sdf = new SimpleDateFormat("dd,MM,yyyy HH:mm:ss");
        Date finalDate;

        manager3 = new DataBaseManager(this);

        configurarlayout();
        configurarBotones();

        Intent intent_restore = getIntent();

        String result = intent_restore.getStringExtra("id_search");

        restoreCursor = manager3.readCursorContactos(result);

        if (restoreCursor.moveToFirst()) {
            do {
                /*Seteo en los textview los valores del paciente*/
                txtv_restLastName.setText(restoreCursor.getString(1));
                txtv_restFirstName.setText(restoreCursor.getString(2));
                txtv_restDocum.setText(restoreCursor.getString(3));
                txtv_restPhone.setText(restoreCursor.getString(4));
                txtv_restObserv.setText(restoreCursor.getString(5));
                long timeStamp = Long.parseLong(restoreCursor.getString(10));
                txtv_restDate.setText(getDate(timeStamp));

                /**/

            } while (restoreCursor.moveToNext());
        }
    }

    private void configurarBotones() {
        /*los id de los botones son id_btnLeadI*/

        bt_openLeadI = (Button) findViewById(R.id.id_btnLeadI);
        bt_openLeadII = (Button) findViewById(R.id.id_btnLeadII);
        bt_openLeadIII = (Button) findViewById(R.id.id_btnLeadIII);
        bt_openLeadAll = (Button) findViewById(R.id.id_btnAllLeads);

        bt_openLeadI.setOnClickListener(this);
        bt_openLeadII.setOnClickListener(this);
        bt_openLeadIII.setOnClickListener(this);
        bt_openLeadAll.setOnClickListener(this);
    }

    private String getDate(long timeStamp) {
        try{
            DateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        }
        catch(Exception ex)
        {
            return "xx";
        }
    }

    private void configurarlayout() {

        txtv_restLastName = (TextView) findViewById(R.id.restLastName);
        txtv_restFirstName = (TextView) findViewById(R.id.restFirstName);
        txtv_restDocum = (TextView) findViewById(R.id.restDoc);
        txtv_restPhone = (TextView) findViewById(R.id.restPhone);
        txtv_restObserv = (TextView) findViewById(R.id.restObserv);
        txtv_restDate = (TextView) findViewById(R.id.restDate);
    }

    @Override
    public void onClick(View v) {

        /*All the leads of the pacient, and different studios share the same folder.
        To reconize the folder, use "PACIENTLASTNAME DOCUMENT ID"
        To reconize the lead and the study (date of ecg) use "PACIENTLASTNAME_LEAD_TIMESTAMP"*/
        /*Variables and declarations*/
        int[] muestras2;
        SamplesECG sampECG_Leads = new SamplesECG();

        restoreCursor.moveToFirst();
        String str_path = restoreCursor.getString(9);

        switch (v.getId()) {
            case R.id.id_btnLeadI:{

                if (restoreCursor.moveToFirst()) {
                    do {
                        /*Abro el archivo, por ejemplo /storage/sdcard0/ECGPro/ROBLEDO 35804885/ROBLEDO_I_14537494*/
                        File file = new File(str_path+"/"+restoreCursor.getString(1)+"_I_"+restoreCursor.getString(10)+".png");
                        /*Muestro el Path... quiero ver si se grabo correctamente*/
                        Toast.makeText(this, "Abriendo ruta: " + str_path+"/"+restoreCursor.getString(1)+"_I_"+restoreCursor.getString(10) , Toast.LENGTH_SHORT).show();
                        /*Preguntar si existe la imagen.*/
                        if(file.exists()) {
                            /* Si existe, abrir directamente la imagen.*/
                            /* Recordar eliminar la carpeta con las iamgenes si se elimina al paciente*/
                            Toast.makeText(this, "Abriendo...", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent();
                            i.setAction(android.content.Intent.ACTION_VIEW);
                            i.setDataAndType(Uri.fromFile(file), "image/png");
                            startActivity(i);
                        }
                        else {
                            /*Si no existe, crearla instanciando la clase ActivityPlot con el constructor
                            del vector de enteros y guardandola correctamente.*/
                            Toast.makeText(this, "Generando...", Toast.LENGTH_SHORT).show();
                            /**/
                            String[] strM_Samples = sampECG_Leads.getSamplesInMatrix(restoreCursor.getString(6));
                            muestras2 = sampECG_Leads.getSamplesInIntArray(strM_Samples);
                            /*Intent for start plot activity*/
                            Intent intent_ActivityPlot = new Intent(getApplicationContext(), ActivityPlot.class);
                            /*Extras to the intent, the tag of the putExtra is in the main*/
                            //intent_ActivityPlot.putExtra(MainActivity.DATA_SENT, muestras2);
                            intent_ActivityPlot.putExtra("muestras", muestras2);
                            intent_ActivityPlot.putExtra("lead", "LEAD I");
                            intent_ActivityPlot.putExtra("path", str_path+"/"+restoreCursor.getString(1)+"_I_"+restoreCursor.getString(10)+".png");
                            startActivity(intent_ActivityPlot);

                            //mensaje = new String(buffer, 0, msg.arg1);
                            //tvMensaje.setText(mensaje);
                        }

                    } while (restoreCursor.moveToNext());
                    break;
                }
            }
            case R.id.id_btnLeadII:{

                if (restoreCursor.moveToFirst()) {
                    do {
                        /*Abro el archivo, por ejemplo /storage/sdcard0/ECGPro/ROBLEDO 35804885/ROBLEDO_I_14537494*/
                        File file = new File(str_path+"/"+restoreCursor.getString(1)+"_II_"+restoreCursor.getString(10)+".png");
                        /*Muestro el Path... quiero ver si se grabo correctamente*/
                        Toast.makeText(this, "Abriendo ruta: " + str_path+"/"+restoreCursor.getString(1)+"_II_"+restoreCursor.getString(10) , Toast.LENGTH_SHORT).show();
                        /*Preguntar si existe la imagen.*/
                        if(file.exists()) {
                            /* Si existe, abrir directamente la imagen.*/
                            /* Recordar eliminar la carpeta con las iamgenes si se elimina al paciente*/
                            Toast.makeText(this, "Abriendo...", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent();
                            i.setAction(android.content.Intent.ACTION_VIEW);
                            i.setDataAndType(Uri.fromFile(file), "image/png");
                            startActivity(i);
                        }
                        else {
                            /*Si no existe, crearla instanciando la clase ActivityPlot con el constructor
                            del vector de enteros y guardandola correctamente.*/
                            Toast.makeText(this, "Generando...", Toast.LENGTH_SHORT).show();
                            /**/
                            String[] strM_Samples = sampECG_Leads.getSamplesInMatrix(restoreCursor.getString(7));
                            muestras2 = sampECG_Leads.getSamplesInIntArray(strM_Samples);
                            /*Intent for start plot activity*/
                            Intent intent_ActivityPlot = new Intent(getApplicationContext(), ActivityPlot.class);
                            /*Extras to the intent, the tag of the putExtra is in the main*/
                            //intent_ActivityPlot.putExtra(MainActivity.DATA_SENT, muestras2);
                            intent_ActivityPlot.putExtra("muestras", muestras2);
                            intent_ActivityPlot.putExtra("lead", "LEAD II");
                            intent_ActivityPlot.putExtra("path", str_path+"/"+restoreCursor.getString(1)+"_II_"+restoreCursor.getString(10)+".png");
                            startActivity(intent_ActivityPlot);

                            //mensaje = new String(buffer, 0, msg.arg1);
                            //tvMensaje.setText(mensaje);
                        }

                    } while (restoreCursor.moveToNext());
                    break;
                }
            }
            case R.id.id_btnLeadIII:{

                if (restoreCursor.moveToFirst()) {
                    do {
                        /*Abro el archivo, por ejemplo /storage/sdcard0/ECGPro/ROBLEDO 35804885/ROBLEDO_I_14537494*/
                        File file = new File(str_path+"/"+restoreCursor.getString(1)+"_III_"+restoreCursor.getString(10)+".png");
                        /*Muestro el Path... quiero ver si se grabo correctamente*/
                        Toast.makeText(this, "Abriendo ruta: " + str_path+"/"+restoreCursor.getString(1)+"_III_"+restoreCursor.getString(10) , Toast.LENGTH_SHORT).show();
                        /*Preguntar si existe la imagen.*/
                        if(file.exists()) {
                            /* Si existe, abrir directamente la imagen.*/
                            /* Recordar eliminar la carpeta con las iamgenes si se elimina al paciente*/
                            Toast.makeText(this, "Abriendo...", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent();
                            i.setAction(android.content.Intent.ACTION_VIEW);
                            i.setDataAndType(Uri.fromFile(file), "image/png");
                            startActivity(i);
                        }
                        else {
                            /*Si no existe, crearla instanciando la clase ActivityPlot con el constructor
                            del vector de enteros y guardandola correctamente.*/
                            Toast.makeText(this, "Generando...", Toast.LENGTH_SHORT).show();
                            /**/
                            String[] strM_Samples = sampECG_Leads.getSamplesInMatrix(restoreCursor.getString(8));
                            muestras2 = sampECG_Leads.getSamplesInIntArray(strM_Samples);
                            /*Intent for start plot activity*/
                            Intent intent_ActivityPlot = new Intent(getApplicationContext(), ActivityPlot.class);
                            /*Extras to the intent, the tag of the putExtra is in the main*/
                            //intent_ActivityPlot.putExtra(MainActivity.DATA_SENT, muestras2);
                            intent_ActivityPlot.putExtra("muestras", muestras2);
                            intent_ActivityPlot.putExtra("lead", "LEAD III");
                            intent_ActivityPlot.putExtra("path", str_path+"/"+restoreCursor.getString(1)+"_III_"+restoreCursor.getString(10)+".png");
                            startActivity(intent_ActivityPlot);

                            //mensaje = new String(buffer, 0, msg.arg1);
                            //tvMensaje.setText(mensaje);
                        }
                    } while (restoreCursor.moveToNext());
                    break;
                }
            }
    }
    }
}
