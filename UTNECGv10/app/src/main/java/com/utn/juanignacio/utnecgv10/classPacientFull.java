package com.utn.juanignacio.utnecgv10;

import android.os.Environment;

import java.io.File;

/**
 * Created by JuanIgnacio on 23/01/2016.
 */

public final class classPacientFull {

    public String str_cPacientLastName;
    public String str_cPacientFirstName;
    public int int_cPacientDocum;
    public int int_cPacientPhone;
    public String str_cObserv;
    public String str_cLeadI;
    public String str_cPath;
    public String str_cTimeStamp;
    public SamplesECG samp_cLeadI;

    /* Constructor por defecto*/
    public classPacientFull() {

        /*Valores de prueba*/
        str_cPacientLastName = "Armstrong";
        str_cPacientFirstName = "Neil";
        int_cPacientDocum = 98765432;
        int_cPacientPhone = 2023580001;
        str_cObserv = "That's one small step for a man, one giant leap for mankind.";
        str_cLeadI = "";
        str_cPath = ""; /*Existira una carpeta de paciente Prueba Apellido Prueba Nombre y guardare las señales seno, cuadrada y triangular en cada Lead*/
        str_cTimeStamp = "-14182940000"; /*Moon landing - Fecha de prueba: aterrizaje en la luna*/

        samp_cLeadI = new SamplesECG(); /*Inicializo */
        str_cLeadI = samp_cLeadI.getSamplesInString(samp_cLeadI.Samples);
        /*Creo directorio del paciente*/
        String str_Directory = createDirectory(str_cPacientLastName, int_cPacientDocum );
        str_cPath = str_Directory;

    }

    /* Constructor sobrecargado */
    public classPacientFull(String apellido, String nombre, int documento, int telefono, String observacion)
    {
        str_cPacientLastName = apellido;
        str_cPacientFirstName = nombre;
        int_cPacientDocum = documento;
        int_cPacientPhone = telefono;
        str_cObserv = observacion;
    }

    public classPacientFull(String apellido, String nombre, int documento, int telefono, String observacion, SamplesECG muestras)
    {
        /*Cargo datos del paciente*/
        str_cPacientLastName = apellido;
        str_cPacientFirstName = nombre;
        int_cPacientDocum = documento;
        int_cPacientPhone = telefono;
        str_cObserv = observacion;
        /*Cargo datos del ECG*/
        samp_cLeadI = muestras;
        str_cLeadI = muestras.getSamplesInString(muestras.Samples);
        /*Creo directorio del paciente*/
        String str_Directory = createDirectory(apellido, documento);
        str_cPath = str_Directory;
    }

    private String createDirectory(String apellido, int documento) {

        boolean success = true;

        File folder = new File(Environment.getExternalStorageDirectory() + "/ECGPro/" + apellido.toUpperCase() + " " + String.valueOf(documento));
        if(!(folder.exists() && folder.isDirectory()))
        {
            /*Si no existe o si existe pero no es un directorio tengo que crearla*/
            success = folder.mkdirs();
            if(success){
                return Environment.getExternalStorageDirectory() + "/ECGPro/" + apellido.toUpperCase() + " " + String.valueOf(documento);
            }else{
                return "Error";
            }
        }

        return Environment.getExternalStorageDirectory() + "/ECGPro/" + apellido.toUpperCase() + " " + String.valueOf(documento);
    }

    public classPacientFull(String apellido, String nombre, int documento, String observacion)
    {
        str_cPacientLastName = apellido;
        str_cPacientFirstName = nombre;
        int_cPacientDocum = documento;
        str_cObserv = observacion;
    }

    public classPacientFull(String apellido, String nombre, int documento, int telefono)
    {
        str_cPacientLastName = apellido;
        str_cPacientFirstName = nombre;
        int_cPacientDocum = documento;
        int_cPacientPhone = telefono;
    }

    /*Solo los 3 que son NotNull*/
    public classPacientFull(String apellido, String nombre, int documento)
    {
        str_cPacientLastName = apellido;
        str_cPacientFirstName = nombre;
        int_cPacientDocum = documento;
    }

    public classPacientFull(String apellido, String nombre, String documento, String telefono, String observacion, SamplesECG muestras, boolean version_final)
    {
        /*Cargo datos del paciente inclusive los vacios*/

        /*Apellido*/
        if (apellido.matches("") || apellido == null || apellido.isEmpty())
            str_cPacientLastName = "";
        else
            str_cPacientLastName = apellido;
        /*nombre*/
        if (nombre.matches("") || nombre == null || nombre.isEmpty())
            str_cPacientFirstName = "";
        else
            str_cPacientFirstName = nombre;
        /*Apellido*/
        if (documento.matches("") ||  documento == null || documento.isEmpty())
            int_cPacientDocum = 0;
        else
            int_cPacientDocum = addPacientActivity.str2int(documento);
        if (!(telefono.matches("")) && !(telefono == null) && !(telefono.isEmpty()))
            int_cPacientPhone = addPacientActivity.str2int(telefono);
        if (!(observacion.matches("")) && !(observacion == null) && !(observacion.isEmpty()))
            str_cObserv = observacion;

        /*Cargo datos del ECG*/
        samp_cLeadI = muestras;
        str_cLeadI = muestras.getSamplesInString(muestras.Samples);
        /*Creo directorio del paciente*/
        String str_Directory = createDirectory(apellido, addPacientActivity.str2int(documento));
        str_cPath = str_Directory;
    }

}