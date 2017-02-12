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

                }
            }
        };

        /*Singleton del Handler*/
        HandlerAux.setHandleraux(mHandler);

    }

    public int str2int(String cadena) {
        if (cadena.matches("")) {
            return 0;
        } else {
            return Integer.parseInt(cadena); //Lo parseo a entero para trabajarlo
        }
    }

    public void Cargar(View view) {

        int documento;
        String nombre;
        String apellido;
        int phone;
        String observ;
        int [] muestras1 = new int[500*3];
        SamplesECG canal1;
        //int[] muestras2 = new int[500*3];
        int[] muestras2;

        editT_var_lastName = (EditText) findViewById(R.id.editT_lastName);
        editT_var_firstName = (EditText) findViewById(R.id.editT_firstName);
        editT_var_document = (EditText) findViewById(R.id.editT_document);
        editT_var_phone = (EditText) findViewById(R.id.editT_phone);
        editT_var_observ = (EditText) findViewById(R.id.editT_observ);

        /*
        apellido = editT_var_lastName.getText().toString(); //Obtengo los valores de adentro del edittext
        nombre = editT_var_firstName.getText().toString(); //Obtengo los valores de adentro del edittext
        documento = Integer.parseInt(editT_var_document.getText().toString()); //Lo parseo a entero para trabajarlo
        phone = Integer.parseInt(editT_var_phone.getText().toString()); //
        observ = editT_var_observ.getText().toString(); //
        manager.insertPacient(apellido, nombre, documento, phone, observ);
        */

        if(bool_Receive == false)
        {
            /*No se realizo el estudio del ECG.*/
            Toast.makeText(getApplicationContext(), "Guardando paciente SIN ECG...",Toast.LENGTH_LONG).show();
        }
        else{
            /*Se hizo previamente el estudio (se hizo click en ECG)*/
            bool_Receive = false; /*Libero el flag que indica si se realizo ya el estudio y no se guardo*/

            Toast.makeText(getApplicationContext(), "Guardando estudio...",Toast.LENGTH_LONG).show();

            muestras1 = datos.getIntArray("muestras");
            canal1 = (SamplesECG) datos.getSerializable("serial");

            String str_samples = Arrays.toString(muestras1);
            String str_samples2 = canal1.getSamplesInString(canal1.Samples);

            String[] strM_Samples = canal1.getSamplesInMatrix(str_samples2);

            muestras2 = canal1.getSamplesInIntArray(strM_Samples);

            /*Separo en una matriz de strings los valores individuales de las muestras*/
            String s[] = str_samples.split(",");
            /*La primera y la ultima muestra tienen los caracteres [], hay que sacarlos*/

            int[] recup = new int[s.length];

            for(int curr = 0; curr<s.length; curr++)
            {
                /*Elimino el primer simbolo extraño que es [*/
                if(curr == 0) {
                    recup[0] = Integer.parseInt(s[0].substring(1));
                }
                else if (curr == (s.length)-1){
                    /*Elimino el ultimo simbolo extraño ]*/
                    recup[curr] = Integer.parseInt(s[curr].substring(1,s[curr].length()-1));
                }
                else {
                    /*Atencion, al hacer el plit tambien me guarda los espacios*/
                    recup[curr] = Integer.parseInt(s[curr].substring(1, s[curr].length()));
                }
            }

            apellido = editT_var_lastName.getText().toString(); //Obtengo los valores de adentro del edittext
            nombre = editT_var_firstName.getText().toString(); //Obtengo los valores de adentro del edittext
            documento = str2int(editT_var_document.getText().toString()); //Lo parseo a entero para trabajarlo
            phone = str2int(editT_var_phone.getText().toString()); //Lo parseo a entero para trabajarlo
            observ = editT_var_observ.getText().toString(); //

            if (apellido.matches("") || nombre.matches("") || editT_var_document.getText().toString().matches("")) {
            /*Falta alguno de los campos obligatorios, poner un toast*/
            /*Ahora lo voy a cargar automaticamente con un constructor, en la version final no dejar continuar*/

                classPacientFull classPacient_paciente = new classPacientFull();
                manager.insertPacient(classPacient_paciente);

            } else {
            /*Los campos obligatorios estan*/
                if (editT_var_phone.getText().toString().matches("") && observ.matches("")) {
                /*Falta el telefono y las observ*/
                    classPacientFull classPacient_paciente = new classPacientFull(apellido, nombre, documento);
                    manager.insertPacient(classPacient_paciente);
                } else if (editT_var_phone.getText().toString().matches("") && !(observ.matches(""))) {
                /*Falta el telefono*/
                    classPacientFull classPacient_paciente = new classPacientFull(apellido, nombre, documento, observ);
                    manager.insertPacient(classPacient_paciente);
                } else if (!(editT_var_phone.getText().toString().matches("")) && (observ.matches(""))) {
                /*Falta las observ*/
                    classPacientFull classPacient_paciente = new classPacientFull(apellido, nombre, documento, phone);
                    manager.insertPacient(classPacient_paciente);
                } else if (!(editT_var_phone.getText().toString().matches("")) && !(observ.matches(""))) {
                /*Estan todos*/
                    classPacientFull classPacient_paciente = new classPacientFull(apellido, nombre, documento, phone, observ, canal1);
                    manager.insertPacient(classPacient_paciente);
                }
            }

        }
    }

    @Override
    public void onClick(View v) {

        /*Va a haber 2 botones, el de adjuntar el ECG y el de Guardar CONTACTO*/

        switch (v.getId()) {
            case R.id.btn_saveRegister: {
                /*Guardar contacto*/
                Cargar(v);
                Toast.makeText(this, "Paciente Guardado", Toast.LENGTH_LONG).show();
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

    public synchronized void realizarConexion(BluetoothSocket socket)
    {
        HiloConexion2 hiloConexion = new HiloConexion2(socket);
        hiloConexion.start();
    }

}

class HiloConexion2 extends Thread
{
    private final InputStream		inputStream;	// Flujo de entrada (lecturas)
    private static final String TAG = "com.j.BluetoothService";
    private final OutputStream		outputStream;	// Flujo de salida (escrituras)

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
        escribir(str);
    }

    /*public void run()
    {
        byte[] largo_msj = new byte[4];
        int bytes_samples;
        int start_of_tx;
        Bundle datos = new Bundle();

        while(true) {
            // Leemos del flujo de entrada del socket
            try {
                //Primero llega el largo del mensaje
                //inputStream.read esta sobrecargada, sin argumentos:
                //Reads a single byte from this stream and returns it as an (abstract) integer in the range from 0 to 255.
                start_of_tx = inputStream.read();  // Al principio de la trama recibo un 1 siempre (Start of Reception)
                if (start_of_tx != -1) {
                    //RECIBI EL START OF RECEIVE, ahora la trama BT me ime envía el numero de bytes que vienen en el proximo paquete
                    //Como es un entero serán 4 bytes, por lo tanto tengo que convertir esos 4 bytes en un unico integer
                    //InputStream con estos argumentos: Reads up to byteCount bytes from this stream and stores them in the byte array buffer starting at byteOffset.
                    bytes_samples = 0;
                    while (4 != bytes_samples){
                        //Me quedo esperando por que sé que largo del mensaje viene en un entero (el ragngo de un int es lo sufiente como almacenar el largo)
                        bytes_samples += inputStream.read(largo_msj, bytes_samples, 4 - bytes_samples);
                    }

                    bytes_samples=0;
                }
            }catch (IOException e) {
                e.printStackTrace();
            }

        }
    }*/

    public void escribir(String string )
    {

        byte[] buffer = string.getBytes();
        try {
            // Escribimos en el flujo de salida del socket
            outputStream.write(buffer);

             // Enviamos la informacion a la actividad a traves del handler.
             // El metodo handleMessage sera el encargado de recibir el mensaje
             // y mostrar los datos enviados en el Toast
        }

        catch(IOException e) {
             Log.e(TAG, "HiloConexion.escribir(): Error al realizar la escritura", e);
             cancelarConexion();
        }
    }
    public void cancelarConexion()
    {
            //Forzar el cierre del socket
    }
}

