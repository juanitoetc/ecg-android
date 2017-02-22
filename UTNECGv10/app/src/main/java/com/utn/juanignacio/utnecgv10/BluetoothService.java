package com.utn.juanignacio.utnecgv10;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class BluetoothService{

    private static final String TAG = "com.j.BluetoothService";
    private static final boolean DEBUG_MODE		= true;

    private final Handler handler;
    private final Context context;
    private final BluetoothAdapter bAdapter;

    public static final String NOMBRE_SEGURO = "BluetoothServiceSecure";
    public static final String NOMBRE_INSEGURO = "BluetoothServiceInsecure";
    public static UUID UUID_SEGURO; //
    public static UUID UUID_INSEGURO; //

    public static final int	ESTADO_NINGUNO				= 0;
    public static final int	ESTADO_CONECTADO			= 1;
    public static final int	ESTADO_REALIZANDO_CONEXION	= 2;
    public static final int	ESTADO_ATENDIENDO_PETICIONES= 3;

    public static final int MSG_CAMBIO_ESTADO = 10;
    public static final int MSG_LEER = 11;
    public static final int MSG_ESCRIBIR = 12;
    public static final int MSG_ATENDER_PETICIONES = 13;
    public static final int MSG_ALERTA = 14;

    private int 			estado;
    private HiloServidor	hiloServidor	= null;
    private HiloCliente		hiloCliente		= null;
    private HiloConexion	hiloConexion	= null;


    public BluetoothService(Context context, Handler handler, BluetoothAdapter adapter)
    {
        debug("BluetoothService()", "Iniciando metodo");

        this.context	= context;
        this.handler 	= handler;
        this.bAdapter 	= adapter;
        this.estado 	= ESTADO_NINGUNO;

        UUID_SEGURO = generarUUID();
        UUID_INSEGURO = generarUUID();
    }

    private synchronized void setEstado(int estado)
    {
        this.estado = estado;
        handler.obtainMessage(MSG_CAMBIO_ESTADO, estado, -1).sendToTarget();
    }

    public synchronized int getEstado()
    {
        return estado;
    }

    public String getNombreDispositivo()
    {
        String nombre = "";
        if(estado == ESTADO_CONECTADO)
        {
            if(hiloConexion != null)
                nombre = hiloConexion.getName();
        }

        return nombre;
    }

    // Inicia el servicio, creando un HiloServidor que se dedicara a atender las peticiones
    // de conexion.
    public synchronized void iniciarServicio()
    {
        debug("iniciarServicio()", "Iniciando metodo");

        // Si se esta intentando realizar una conexion mediante un hilo cliente,
        // se cancela la conexion
        if(hiloCliente != null)
        {
            hiloCliente.cancelarConexion();
            hiloCliente = null;
        }

        // Si existe una conexion previa, se cancela
        if(hiloConexion != null)
        {
            hiloConexion.cancelarConexion();
            hiloConexion = null;
        }

        // Arrancamos el hilo servidor para que empiece a recibir peticiones
        // de conexion
        if(hiloServidor == null)
        {
            hiloServidor = new HiloServidor();
            hiloServidor.start();
        }

        debug("iniciarServicio()", "Finalizando metodo");
    }

    public void finalizarServicio()
    {
        debug("finalizarServicio()", "Iniciando metodo");

        if(hiloCliente != null)
            hiloCliente.cancelarConexion();
        if(hiloConexion != null)
            hiloConexion.cancelarConexion();
        if(hiloServidor != null)
            hiloServidor.cancelarConexion();

        hiloCliente = null;
        hiloConexion = null;
        hiloServidor = null;

        setEstado(ESTADO_NINGUNO);

    }

    // Instancia un hilo conector
    public synchronized void solicitarConexion(BluetoothDevice dispositivo)
    {
        debug("solicitarConexion()", "Iniciando metodo");
        // Comprobamos si existia un intento de conexion en curso.
        // Si es el caso, se cancela y se vuelve a iniciar el proceso
        if(estado == ESTADO_REALIZANDO_CONEXION)
        {
            if(hiloCliente != null)
            {
                hiloCliente.cancelarConexion();
                hiloCliente = null;
            }
        }

        // Si existia una conexion abierta, se cierra y se inicia una nueva
        if(hiloConexion != null)
        {
            hiloConexion.cancelarConexion();
            hiloConexion = null;
        }

        // Se instancia un nuevo hilo conector, encargado de solicitar una conexion
        // al servidor, que sera la otra parte.
        hiloCliente = new HiloCliente(dispositivo);
        hiloCliente.start();

        setEstado(ESTADO_REALIZANDO_CONEXION);
    }

    public synchronized void realizarConexion(BluetoothSocket socket, BluetoothDevice dispositivo)
    {
        debug("realizarConexion()", "Iniciando metodo");
        hiloConexion = new HiloConexion(socket);
        hiloConexion.start();
    }

    // Sincroniza el objeto con el hilo HiloConexion e invoca a su metodo escribir()
    // para enviar el mensaje a traves del flujo de salida del socket.
    public int enviar(byte[] buffer)
    {
        debug("enviar()", "Iniciando metodo");
        HiloConexion tmpConexion;

        synchronized(this) {
            if(estado != ESTADO_CONECTADO)
                return -1;
            tmpConexion = hiloConexion;
        }

        tmpConexion.escribir(buffer);

        return buffer.length;

    }

    // Hilo que hace las veces de servidor, encargado de escuchar conexiones entrantes y
    // crear un hilo que maneje la conexion cuando ello ocurra.
    // La otra parte debera solicitar la conexion mediante un HiloCliente.
    private class HiloServidor extends Thread
    {
        private final BluetoothServerSocket serverSocket;

        public HiloServidor()
        {
            debug("HiloServidor.new()", "Iniciando metodo");
            BluetoothServerSocket tmpServerSocket = null;

            // Creamos un socket para escuchar las peticiones de conexion
            try {
                tmpServerSocket = bAdapter.listenUsingRfcommWithServiceRecord(NOMBRE_SEGURO, UUID_SEGURO);
            } catch(IOException e) {
                Log.e(TAG, "HiloServidor(): Error al abrir el socket servidor", e);
            }

            serverSocket = tmpServerSocket;
        }

        public void run()
        {
            debug("HiloServidor.run()", "Iniciando metodo");
            BluetoothSocket socket = null;

            setName("HiloServidor");
            setEstado(ESTADO_ATENDIENDO_PETICIONES);
            // El hilo se mantendra en estado de espera ocupada aceptando conexiones
            // entrantes siempre y cuando no exista una conexion activa.
            // En el momento en el que entre una nueva conexion,
            while(estado != ESTADO_CONECTADO)
            {
                try {
                    // Cuando un cliente solicite la conexion se asignara valor al socket..
                    socket = serverSocket.accept();
                }
                catch(IOException e) {
                    Log.e(TAG, "HiloServidor.run(): Error al aceptar conexiones entrantes", e);
                    break;
                }

                // Si el socket tiene valor sera porque un cliente ha solicitado la conexion
                if(socket != null)
                {
                    // Realizamos un lock del objeto
                    synchronized(BluetoothService.this)
                    {
                        switch(estado)
                        {
                            case ESTADO_ATENDIENDO_PETICIONES:
                            case ESTADO_REALIZANDO_CONEXION:
                            {
                                debug("HiloServidor.run()", estado == ESTADO_ATENDIENDO_PETICIONES ? "Atendiendo peticiones" : "Realizando conexion");
                                // Estado esperado, se crea el hilo de conexion que recibira
                                // y enviara los mensajes
                                realizarConexion(socket, socket.getRemoteDevice());
                                break;
                            }
                            case ESTADO_NINGUNO:
                            case ESTADO_CONECTADO:
                            {
                                // No preparado o conexion ya realizada.
                                // Se cierra el nuevo socket.
                                try {
                                    debug("HiloServidor.run()", estado == ESTADO_NINGUNO ? "Ninguno" : "Conectado");
                                    socket.close();
                                }
                                catch(IOException e) {
                                    Log.e(TAG, "HiloServidor.run(): socket.close(). Error al cerrar el socket.", e);
                                }
                                break;
                            }
                            default:
                                break;
                        }
                    }
                }

            } // End while
        }

        public void cancelarConexion()
        {
            debug("HiloServidor.cancelarConexion()", "Iniciando metodo");
            try {
                serverSocket.close();
            }
            catch(IOException e) {
                Log.e(TAG, "HiloServidor.cancelarConexion(): Error al cerrar el socket", e);
            }
        }
    }

    // Hilo encargado de solicitar una conexion a un dispositivo que este corriendo un
    // HiloServidor.
    private class HiloCliente extends Thread
    {
        private BluetoothDevice dispositivo;
        private BluetoothSocket socket;

        public HiloCliente(BluetoothDevice dispositivo)
        {
            debug("HiloCliente.new()", "Iniciando metodo");
            BluetoothSocket tmpSocket = null;
            this.dispositivo = dispositivo;

            // Obtenemos un socket para el dispositivo con el que se quiere conectar
            try {
                tmpSocket = dispositivo.createRfcommSocketToServiceRecord(UUID_SEGURO);
            }
            catch(IOException e) {
                Log.e(TAG, "HiloCliente.HiloCliente(): Error al abrir el socket", e);
            }

            socket = tmpSocket;
        }

        public void run()
        {
            debug("HiloCliente.run()", "Iniciando metodo");
            setName("HiloCliente");

            if(bAdapter.isDiscovering())
                bAdapter.cancelDiscovery();

            try {
                socket.connect();
                setEstado(ESTADO_REALIZANDO_CONEXION);
            }
            catch(IOException e) {
                Log.e(TAG, "HiloCliente.run(): socket.connect(): Error realizando la conexion", e);
                try {
                    socket.close();
                }
                catch(IOException inner) {
                    Log.e(TAG, "HiloCliente.run(): Error cerrando el socket", inner);
                }
                setEstado(ESTADO_NINGUNO);
                return;         // terminates run before calling realizarConexion
            }

            // Reiniciamos el hilo cliente, ya que no lo necesitaremos mas
            synchronized(BluetoothService.this)
            {
                hiloCliente = null;
            }

            // Realizamos la conexion
            realizarConexion(socket, dispositivo);
        }

        public void cancelarConexion()
        {
            debug("cancelarConexion()", "Iniciando metodo");
            try {
                socket.close();
            }
            catch(IOException e) {
                Log.e(TAG, "HiloCliente.cancelarConexion(): Error al cerrar el socket", e);
            }
            setEstado(ESTADO_NINGUNO);
        }
    }

    // Hilo encargado de mantener la conexion y realizar las lecturas y escrituras
    // de los mensajes intercambiados entre dispositivos.
    private class HiloConexion extends Thread
    {
        private final BluetoothSocket 	socket;			// Socket
        private final InputStream		inputStream;	// Flujo de entrada (lecturas)
        private final OutputStream		outputStream;	// Flujo de salida (escrituras)

        public HiloConexion(BluetoothSocket socket)
        {
            debug("HiloConexion.new()", "Iniciando metodo");
            this.socket = socket;
            SocketHandler.setSocket(socket);

            setName(socket.getRemoteDevice().getName() + " [" + socket.getRemoteDevice().getAddress() + "]");

            // Se usan variables temporales debido a que los atributos se declaran como final
            // no seria posible asignarles valor posteriormente si fallara esta llamada
            InputStream tmpInputStream = null;
            OutputStream tmpOutputStream = null;

            // Obtenemos los flujos de entrada y salida del socket.
            try {
                tmpInputStream = socket.getInputStream();
                tmpOutputStream = socket.getOutputStream();
            }
            catch(IOException e){
                Log.e(TAG, "HiloConexion(): Error al obtener flujos de E/S", e);
            }

            inputStream = tmpInputStream;
            outputStream = tmpOutputStream;
        }

        // Metodo principal del hilo, encargado de realizar las lecturas
        public void run()
        {

            debug("HiloConexion.run()", "Iniciando metodo");
            byte[] buffer_samples_DI = new byte[500*4*4];   // 500 sps
            byte[] buffer_samples_DII = new byte[500*4*4];   // 500 sps
            byte[] buffer_recepcion = new byte [1];
            int[] muestras_DI = new int [(buffer_samples_DI.length)/4];;
            int[] muestras_DII = new int [(buffer_samples_DI.length)/4];;
            int[] muestras_DIII = new int [(buffer_samples_DI.length)/4];;
            int[] aux_muestras_DI = new int[500*4];//3 seg
            int[] aux_muestras_DII = new int[500*4];//3 seg
            int[] aux_muestras_DIII = new int[500*4];//3 seg

            byte[] buffer_samples = new byte[500*3*4];   // 3600 muestras, 4 bytes c/u
            byte[] largo_msj = new byte[5];
            int largo;
            int bytes_samples;

            char start_of_tx=0;
            int[] muestras = new int[500*3];

            setEstado(ESTADO_CONECTADO);
            // Mientras se mantenga la conexion el hilo se mantiene en espera ocupada
            // leyendo del flujo de entrada

            // Dos veces, no cambiar
            setEstado(ESTADO_CONECTADO);

            // Obtengo el handler de addPatientActivity
            Handler handler2 = HandlerAux.getHandleraux();

            while(true)
            {
                try {
                    // Leemos del flujo de entrada del socket
                    bytes_samples=0;

                    inputStream.read(buffer_recepcion,0,1);

                     if(((char) buffer_recepcion[0]) == 3) { //3 para iniciar recepcion

                        while ( bytes_samples<4)
                            bytes_samples += inputStream.read(largo_msj, bytes_samples, 4 - bytes_samples); // recibo el largo del msj q me van a mandar

                        largo=0;
                        //guardo el msj como int
                        largo |= 0x000000FF&largo_msj [0];
                        largo |= 0x0000FF00&largo_msj [1] << 8;
                        largo |= 0x00FF0000&largo_msj [2] << 16;
                        largo |= 0xFF000000&largo_msj [3] << 24;

                        bytes_samples=0;
                        while (bytes_samples != largo)
                            bytes_samples += inputStream.read(buffer_samples_DI, bytes_samples, largo - bytes_samples); // recibo el buffer de muestras DI
                         while (bytes_samples != largo)
                             bytes_samples += inputStream.read(buffer_samples_DII, bytes_samples, largo - bytes_samples); // recibo el buffer de muestras de DII

                        for (int i =0; i<3*500 ; i++)
                        {
                            //Inicializo el entero en el cual trabajo
                            muestras_DI[i]=0;
                            muestras_DII[i]= 0;
                            muestras_DIII[i]= 0;
                            //codifico cada muestra de DI como int (4 bytes)
                            muestras_DI[i] |= 0x000000FF&buffer_samples_DI[4*i];
                            muestras_DI[i] |= 0x0000FF00&buffer_samples_DI[4*i+1]<<8;
                            muestras_DI[i] |= 0x00FF0000&buffer_samples_DI[4*i+2]<<16;
                            muestras_DI[i] |= 0xFF000000&buffer_samples_DI[4*i+3]<<24;
                            //codifico cada muestra de DII como int (4 bytes)
                            muestras_DII[i] |= 0x000000FF&buffer_samples_DII[4*i];
                            muestras_DII[i] |= 0x0000FF00&buffer_samples_DII[4*i+1]<<8;
                            muestras_DII[i] |= 0x00FF0000&buffer_samples_DII[4*i+2]<<16;
                            muestras_DII[i] |= 0xFF000000&buffer_samples_DII[4*i+3]<<24;
                            //Formo DIII  como resta de dos enteros
                            muestras_DIII[i] = muestras_DII[i]-muestras_DI[i];;

                        }
                         // Genero las tres instancias de una clase de muestras inicializandolas con muestras
                         SamplesECG canal_I = new SamplesECG(muestras_DI);
                         SamplesECG canal_II = new SamplesECG(muestras_DII);
                         SamplesECG canal_III = new SamplesECG(muestras_DIII);

                         // Mensaje y bundle para comunicarme a travez de activities
                         Message m = new Message();
                         Bundle b = new Bundle();

                         b.putSerializable("canal1", canal_I);
                         b.putSerializable("canal2", canal_II);
                         b.putSerializable("canal3", canal_III);

                         b.putIntArray("muestras1", canal_I.Samples);
                         b.putIntArray("muestras2", canal_II.Samples);
                         b.putIntArray("muestras3", canal_III.Samples);

                         b.putString("key", "Muestreo exitoso. Guarde el paciente.");

                         m.what = HandlerAux.LETRA_H;
                         m.setData(b);

                         // Envio mensaje a la clase addPatient
                         handler2.sendMessage(m);

                         // handler.obtainMessage(MSG_LEER, muestras_DI.length, -1, muestras_DI).sendToTarget();
                         // Espero un poco para que el handler pueda terminar el proceso
                         // sleep(500);
                         // handler.obtainMessage(MSG_LEER, muestras_DII.length, -1, muestras_DII).sendToTarget();
                         // sleep(500);
                         // handler.obtainMessage(MSG_LEER, muestras_DIII.length, -1, muestras_DIII).sendToTarget();
                         // sleep(500);

                    }else if (((char) buffer_recepcion[0])==4) {
                         outputStream.write(4);// devuelvo el keepalive
                         }
                } catch(IOException e) {
                    Log.e(TAG, "HiloConexion.run(): Error al realizar la lectura", e);
                }
            }
        }

        public void escribir(byte[] buffer)
        {
            debug("HiloConexion.escribir()", "Iniciando metodo");
            try {
                // Escribimos en el flujo de salida del socket
                outputStream.write(buffer);

                // Enviamos la informacion a la actividad a traves del handler.
                // El metodo handleMessage sera el encargado de recibir el mensaje
                // y mostrar los datos enviados en el Toast
                handler.obtainMessage(MSG_ESCRIBIR, -1, -1, buffer).sendToTarget();
            }
            catch(IOException e) {
                Log.e(TAG, "HiloConexion.escribir(): Error al realizar la escritura", e);
            }
        }

        public void cancelarConexion()
        {
            debug("HiloConexion.cancelarConexion()", "Iniciando metodo");
            try {
                // Forzamos el cierre del socket
                socket.close();

                // Cambiamos el estado del servicio
                setEstado(ESTADO_NINGUNO);
            }
            catch(IOException e) {
                Log.e(TAG, "HiloConexion.cerrarConexion(): Error al cerrar la conexion", e);
            }
        }

    }

    public void debug(String metodo, String msg)
    {
        if(DEBUG_MODE)
            Log.d(TAG, metodo + ": " + msg);
    }

    private UUID generarUUID()
    {
        //ContentResolver appResolver = context.getApplicationContext().getContentResolver();
        /*String id = Secure.getString(appResolver, Secure.ANDROID_ID);
        final TelephonyManager tManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        final String deviceId = String.valueOf(tManager.getDeviceId());
        final String simSerialNumber = String.valueOf(tManager.getSimSerialNumber());
        final String androidId	= android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID uuid = new UUID(androidId.hashCode(), ((long)deviceId.hashCode() << 32) | simSerialNumber.hashCode());
        uuid = new UUID((long)1000, (long)23);
        */
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        return uuid;
    }
}

