package com.utn.juanignacio.utnecgv10;

/**
 * Created by juani on 16.02.17.
 */

public class DigitalFilter {

    public double[] b_notch_50 = new double[]{0.984533708596897,-1.593009003579764,0.984533708596897};
    public double[] a_notch_50 = new double[]{1,-1.593009003579764,0.969067417193793};

    public int[] iirFilter(int[] samples){
        /* Metedo para implemetacion de un filtro IIR */

        double x_sum = 0;
        double y_sum = 0;
        int y_index = 0;
        int tap_index = 0;
        int int_Samples_size = samples.length;
        double y_out[] = new double[int_Samples_size];

        for (y_index = 0; y_index <int_Samples_size ; y_index++){
            // Suma de B entradas (actual y pasadas)
            x_sum = 0;
            for (tap_index = 0;tap_index < b_notch_50.length; tap_index ++){
                if(y_index >= tap_index)
                    x_sum = x_sum + (b_notch_50[tap_index]*(double)(samples[y_index-tap_index]));
            }
            y_sum = 0;
            // Suma de A salidas (pasadas)
            for (tap_index = 1;tap_index < a_notch_50.length; tap_index++){
                if(y_index >= tap_index)
                    y_sum = y_sum + (a_notch_50[tap_index]*(y_out[y_index-tap_index]));
            }
            // Diferencia entre ambas sumatorias
            y_out[y_index] = x_sum - y_sum;
        }
        // Convierto el array a entero
        int[] int_y_out = new int[y_out.length];
        for (int i=0; i<int_y_out.length; i++)
            int_y_out[i] = (int)y_out[i];
        // retorno el array con la senial filtrada
        return int_y_out;
    }

}
