package com.utn.juanignacio.utnecgv10;

/**
 * Created by juani on 16.02.17.
 */

public class DigitalFilter {

    public double[] b_notch_50 = new double[]{0.984533708596897,-1.593009003579764,0.984533708596897};
    public double[] a_notch_50 = new double[]{1,-1.593009003579764,0.969067417193793};
    public double[] b_lowpass = new double[]{41,195,443,577,316,0,0,0,740,1206,99,0,0,1008,6781,11385,11385,6781,1008,0,0,99,1206,740,0,0,0,316,577,443,195,41};
    public double[] a_lowpass = new double[]{1};

    public int[] iirFilter(double[] bCoeff, double[] aCoeff, int[] samples){
        /* Metedo para implemetacion de un filtro IIR */

        double x_sum = 0;
        double y_sum = 0;
        int y_index = 0;
        int tap_index = 0;
        int int_Samples_size = samples.length;
        double y_out[] = new double[int_Samples_size];
        double sumCoefb = 0;
        double sumCoefa = 0;

        for (y_index = 0; y_index <int_Samples_size ; y_index++){
            // Suma de B entradas (actual y pasadas)
            x_sum = 0;
            for (tap_index = 0;tap_index < bCoeff.length; tap_index ++){
                if(y_index >= tap_index)
                    x_sum = x_sum + (bCoeff[tap_index]*(double)(samples[y_index-tap_index]));
            }
            y_sum = 0;
            // Suma de A salidas (pasadas)
            if (aCoeff.length > 1){
                //es IIR
                for (tap_index = 1;tap_index < aCoeff.length; tap_index++){
                    if(y_index >= tap_index)
                        y_sum = y_sum + (aCoeff[tap_index]*(y_out[y_index-tap_index]));
                }
            }
            // Diferencia entre ambas sumatorias
            y_out[y_index] = x_sum - y_sum;
        }
        // Escalo el array de salida y lo casteo a entero
        int[] int_y_out = new int[y_out.length];
        sumCoefa = sumCoeff(aCoeff);
        sumCoefb = sumCoeff(bCoeff);
        for (int i=0; i<int_y_out.length; i++)
            int_y_out[i] = (int)(((y_out[i])*(sumCoefa))/(sumCoefb));
        // retorno el array con la senial filtrada
        return int_y_out;
    }

    public double getMedian (int[] samples){
        /* Remuevo el valor medio de la funcion*/
        int i = 0;
        double sum = 0;
        double median = 0;

        for (i=0; i < samples.length; i++)
            sum = sum + (samples[i]);
        median = (sum) / (samples.length);

        return median;
    }

    public int[] removeConstant (int[] samples, double constant){

        int[] int_y_out = new int[samples.length];
        int i = 0;

        for (i=0; i < samples.length; i++)
            int_y_out[i] = (int) (samples[i] - constant);
        return int_y_out;
    }

    public double sumCoeff (double[] coeff){
        /*Sumo los coeficientes para esaclar los resultados del filtro*/
        int i = 0;
        double sum = 0;

        for(i=0; i< coeff.length; i++)
            sum = sum + coeff[i];

        return sum;
    }

}
