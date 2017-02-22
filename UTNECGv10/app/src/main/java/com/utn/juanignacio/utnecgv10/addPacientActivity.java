package com.utn.juanignacio.utnecgv10;

import android.bluetooth.BluetoothSocket;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
//import java.util.logging.Handler;
import android.os.Handler;


public class addPacientActivity extends ActionBarActivity implements View.OnClickListener {

    EditText editT_var_lastName;
    EditText editT_var_firstName;
    EditText editT_var_document;
    EditText editT_var_phone;
    EditText editT_var_observ;

    Button btn_Guardar;
    Button btn_TomarECG;

    DataBaseManager manager;

    classPacientFull classPacient_paciente;
    Bundle datos;
    Handler mHandler_return;

    boolean bool_Receive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pacient);

        btn_Guardar = (Button) findViewById(R.id.btn_saveRegister);
        btn_TomarECG = (Button) findViewById(R.id.btn_makeECG);
        btn_Guardar.setOnClickListener(this);
        btn_TomarECG.setOnClickListener(this);

        manager = new DataBaseManager(this); /*Dentro crea el objeto helper y el db*/

        /*Genero el Handler para poder comunicar los Threads con la activity principal*/
        final Handler mHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg){

                switch(msg.what){
                    case HandlerAux.LETRA_H:

                        datos = msg.getData();
                        bool_Receive = true;
                        Toast.makeText(getApplicationContext(), datos.getString("key"),Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

        /*Singleton del Handler*/
        HandlerAux.setHandleraux(mHandler);

        /* Handler propio de esta actividad. Reconoce si se esta conectado al bluetoth o si se perdio la conexion*/
        mHandler_return = new Handler()
        {
            @Override
            public void handleMessage(Message msg){

                /* Recibo la cantidad de bytes enviados o bien -1 si hubo desconexion */
                Bundle datos_return = msg.getData();

                switch(datos_return.getInt("isConnected")){

                    case -1:
                        Toast.makeText(getApplicationContext(),"No hay conexion bluetooth",Toast.LENGTH_LONG).show();
                        Cargar(false);
                        break;
                    default:
                        Toast.makeText(getApplicationContext(),"Hay conexion bluetooth",Toast.LENGTH_LONG).show();
                        Cargar(true);
                        break;

                }
            }
        };
    }

    public static int str2int(String cadena) {
        if (cadena.matches("")) {
            return 0;
        } else {
            return Integer.parseInt(cadena); //Lo parseo a entero para trabajarlo
        }
    }

    public void Cargar(boolean isConectionActive) {

        int documento;
        String nombre;
        String apellido;
        int phone;
        String observ;
        int [] muestras1 = new int[500*3];
        SamplesECG canal1;
        SamplesECG canal2;
        SamplesECG canal3;
        //int[] muestras2 = new int[500*3];
        int[] muestras2;
        DigitalFilter samplesFiltered = new DigitalFilter();

        editT_var_lastName = (EditText) findViewById(R.id.editT_lastName);
        editT_var_firstName = (EditText) findViewById(R.id.editT_firstName);
        editT_var_document = (EditText) findViewById(R.id.editT_document);
        editT_var_phone = (EditText) findViewById(R.id.editT_phone);
        editT_var_observ = (EditText) findViewById(R.id.editT_observ);


        /* Compruebo que hay conexion establecida con el Bluetooth*/
        BluetoothSocket socket_test = SocketHandler.getSocket();        // Recupero el socket

        if(bool_Receive == false && isConectionActive == false)
        {
            /*No se realizo el estudio del ECG y ademas no hay conexion Bt.*/
            Toast.makeText(getApplicationContext(), "Atencion: ECG: No realizado. BT: No conectado. Guardando datos paciente.",Toast.LENGTH_LONG).show();
            canal1 = new SamplesECG();
            canal2 = new SamplesECG();
            canal3 = new SamplesECG();
        }
        else if (bool_Receive == false && isConectionActive == true){
            /*  No se realizo el estudio del ECG y pero hax conexion Bt.
                Recomendar conectar Bt para guardar el ECG del paciente */
            Toast.makeText(getApplicationContext(), "Atencion: ECG: No realizado. BT: Conectado. Guardando datos paciente.",Toast.LENGTH_LONG).show();
            canal1 = new SamplesECG();
            canal2 = new SamplesECG();
            canal3 = new SamplesECG();
        }
        else if (bool_Receive == true && isConectionActive == false) {
            /*  Se realizo previamente el estudio del ECG y pero Antes de guardarlo se desconecto. Avisar para los proximos estudios */
            bool_Receive = false; /*Libero el flag que indica si se realizo ya el estudio y no se guardo*/
            Toast.makeText(getApplicationContext(), "Atencion: ECG: Correcto. BT: Desconectado. Guardando datos paciente y ECG.",Toast.LENGTH_LONG).show();
            canal1 = (SamplesECG) datos.getSerializable("canal1");
            canal2 = (SamplesECG) datos.getSerializable("canal2");
            canal3 = (SamplesECG) datos.getSerializable("canal3");
        }
        else {
            /*Se hizo previamente el estudio ECG y sigue habiendo BT */
            bool_Receive = false; /*Libero el flag que indica si se realizo ya el estudio y no se guardo*/
            Toast.makeText(getApplicationContext(), "ECG: Correcto. BT: Conectado. Guardando datos paciente y ECG.", Toast.LENGTH_LONG).show();
            canal1 = (SamplesECG) datos.getSerializable("canal1");
            canal2 = (SamplesECG) datos.getSerializable("canal2");
            canal3 = (SamplesECG) datos.getSerializable("canal3");
        }

        /* Aca hago el analisis con los filtros digitales para el canal 1*/
        canal1.Samples = samplesFiltered.removeConstant(canal1.Samples, samplesFiltered.getMedian(canal1.Samples)); //remuevo valor medio
        canal1.Samples = samplesFiltered.iirFilter(samplesFiltered.b_notch_50, samplesFiltered.a_notch_50, canal1.Samples, "notch"); //filtro notch
        canal1.Samples = samplesFiltered.iirFilter(samplesFiltered.b_bandpass, samplesFiltered.a_bandpass, canal1.Samples, "bandpass"); //filtro pasa banda

        /* Aca hago el analisis con los filtros digitales para el canal 2*/
        canal2.Samples = samplesFiltered.removeConstant(canal1.Samples, samplesFiltered.getMedian(canal1.Samples)); //remuevo valor medio
        canal2.Samples = samplesFiltered.iirFilter(samplesFiltered.b_notch_50, samplesFiltered.a_notch_50, canal1.Samples, "notch"); //filtro notch
        canal2.Samples = samplesFiltered.iirFilter(samplesFiltered.b_bandpass, samplesFiltered.a_bandpass, canal1.Samples, "bandpass"); //filtro pasa banda

        /* Aca hago el analisis con los filtros digitales para el canal 3*/
        canal3.Samples = samplesFiltered.removeConstant(canal1.Samples, samplesFiltered.getMedian(canal1.Samples)); //remuevo valor medio
        canal3.Samples = samplesFiltered.iirFilter(samplesFiltered.b_notch_50, samplesFiltered.a_notch_50, canal1.Samples, "notch"); //filtro notch
        canal3.Samples = samplesFiltered.iirFilter(samplesFiltered.b_bandpass, samplesFiltered.a_bandpass, canal1.Samples, "bandpass"); //filtro pasa banda

        /* TODO: ESTA PARTE DEL CODIGO ESTA IMPLEMENTADA DOS VECES. ELIMINAR LA PRIMERA VERSION. */

        // canal1 = (SamplesECG) datos.getSerializable("serial");

            /* TODO: ME PARECE QUE ES ESTE EL QUE NO VA PERO LO DEJO COMENTADO POR SI ERA UN TEMA DE LA BASE DE DATOS

            muestras1 = datos.getIntArray("muestras");
            String str_samples = Arrays.toString(muestras1);
            String str_samples2 = canal1.getSamplesInString(canal1.Samples);

            String[] strM_Samples = canal1.getSamplesInMatrix(str_samples2);

            // Separo en una matriz de strings los valores individuales de las muestras
            String s[] = str_samples.split(",");
            // La primera y la ultima muestra tienen los caracteres [], hay que sacarlos
            int[] recup = new int[s.length];

            for(int curr = 0; curr<s.length; curr++)
            {
                //Elimino el primer simbolo extraño que es [
                if(curr == 0) {
                    recup[0] = Integer.parseInt(s[0].substring(1));
                }
                else if (curr == (s.length)-1){
                    //Elimino el ultimo simbolo extraño ]
                    recup[curr] = Integer.parseInt(s[curr].substring(1,s[curr].length()-1));
                }
                else {
                    //Atencion, al hacer el plit tambien me guarda los espacios
                    recup[curr] = Integer.parseInt(s[curr].substring(1, s[curr].length()));
                }
            }
            */

        apellido = editT_var_lastName.getText().toString(); //Obtengo los valores de adentro del edittext
        nombre = editT_var_firstName.getText().toString(); //Obtengo los valores de adentro del edittext
        documento = str2int(editT_var_document.getText().toString()); //Lo parseo a entero para trabajarlo
        phone = str2int(editT_var_phone.getText().toString()); //Lo parseo a entero para trabajarlo
        observ = editT_var_observ.getText().toString(); //

        boolean versionFinal =  true;

        if (apellido.matches("") || nombre.matches("") || editT_var_document.getText().toString().matches("")) {
            /*Falta alguno de los campos obligatorios, poner un toast*/
            /*Ahora lo voy a cargar automaticamente con un constructor.*/
            /*TODO: EN LA VERSION FINAL NO DEJAR GUARDAR A MENOS QUE ESTEN TODOS LOS CAMPOS*/
            classPacientFull classPacient_paciente = new classPacientFull();
            manager.insertPacient(classPacient_paciente);

        } else {
            /* La separacion de campos completos o no se hace en el constructor. Se mandan todos los campos */
            classPacientFull classPacient_paciente = new classPacientFull(editT_var_lastName.getText().toString(), editT_var_firstName.getText().toString(), editT_var_document.getText().toString(), editT_var_phone.getText().toString(), editT_var_observ.getText().toString(), canal1, canal2, canal3);
            manager.insertPacient(classPacient_paciente);
        }
    }

    @Override
    public void onClick(View v) {

        /*Va a haber 2 botones, el de adjuntar el ECG y el de Guardar CONTACTO*/

        switch (v.getId()) {
            case R.id.btn_saveRegister: {
                /*Guardar contacto*/
                /* Recupero socket */
                BluetoothSocket socket = SocketHandler.getSocket();
                /* Me fijo si la conexion esta activa. Recibo cantidad de bytes enviados en el handler */
                esBluetoothConectado(socket);
                /* Ver accion lanzada desde el handler*/
                break;
            }
            case R.id.btn_makeECG: {

                /*Tomar Muestras*/
                Toast.makeText(this, "Realizando ECG... no desconecte el cable ni el bluetooth", Toast.LENGTH_LONG).show();
                BluetoothSocket socket = SocketHandler.getSocket();

                // Hilo encargado de mantener la conexion y realizar las lecturas y escrituras
                // de los mensajes intercambiados entre dispositivos.
                realizarConexion(socket);

                Toast.makeText(this, "Finalizando ECG...", Toast.LENGTH_LONG).show();
                break;
            }
            default: {
                break;
            }
        }
    }

    private void esBluetoothConectado(BluetoothSocket socket) {
        HiloConexion2 hiloConexion = new HiloConexion2(socket, mHandler_return);
        hiloConexion.start();
    }

    public synchronized void realizarConexion(BluetoothSocket socket) {

        HiloConexion2 hiloConexion = new HiloConexion2(socket);
        hiloConexion.start();
    }

}

class HiloConexion2 extends Thread
{
    private final InputStream		inputStream;	// Flujo de entrada (lecturas)
    private static final String TAG = "com.j.BluetoothService";
    private final OutputStream		outputStream;	// Flujo de salida (escrituras)
    int value_rtn = 0;          // Usado para ver el valor de retorno

    public HiloConexion2(BluetoothSocket socket) {
        // Se usan variables temporales debido a que los atributos se declaran como final
        // no seria posible asignarles valor posteriormente si fallara esta llamada
        InputStream tmpInputStream = null;
        OutputStream tmpOutputStream = null;

        // Obtenemos los flujos de entrada y salida del socket.
        try {
            tmpInputStream = socket.getInputStream();
            tmpOutputStream = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "HiloConexion(): Error al obtener flujos de E/S", e);
        }

        inputStream = tmpInputStream;
        outputStream = tmpOutputStream;

        /*inicar muestreo*/
        String str = "H";
        value_rtn = escribir(str);
    }

    public HiloConexion2(BluetoothSocket socket, Handler mHandler_return) {

        /*Hago un test a la conexion a ver si esta activa o devuelve error. si es asi, tengo que avisar.*/

        InputStream tmpInputStream = null;
        OutputStream tmpOutputStream = null;

        // Obtenemos los flujos de entrada y salida del socket.
        try {
            tmpInputStream = socket.getInputStream();
            tmpOutputStream = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "HiloConexion(): Error al obtener flujos de E/S", e);
        }

        inputStream = tmpInputStream;
        outputStream = tmpOutputStream;

        String str = "Z";   // Esta vez envio otro caracter y espero que no me responda, o que me

        value_rtn = escribir(str);

        Message msg = new Message();
        Bundle datos = new Bundle();

        /*Envio por Handler el valor retornado*/
        datos.putInt("isConnected", value_rtn);

        msg.setData(datos);
        mHandler_return.sendMessage(msg);
    }


    public int escribir(String string )
    {
        byte[] buffer = string.getBytes();
        try {
            /* Escribimos en el flujo de salida del socket */
            outputStream.write(buffer);
        }
        catch(IOException e) {
            Log.e(TAG, "HiloConexion.escribir(): Error al realizar la escritura", e);
            cancelarConexion();
            return -1;
        }
        return buffer.length;
    }
    public void cancelarConexion()
    {
        //  TODO: Forzar el cierre del socket. Supongo que haciendo un set a null. Ver bien.
    }
}

