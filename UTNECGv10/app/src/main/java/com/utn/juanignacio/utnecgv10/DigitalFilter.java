package com.utn.juanignacio.utnecgv10;

import java.util.Arrays;

/**
 * Created by juani on 16.02.17.
 */

public class DigitalFilter {


    public double[] b_notch_50 = new double[]{0.984533708596897,-1.593009003579764,0.984533708596897};
    public double[] a_notch_50 = new double[]{1,-1.593009003579764,0.969067417193793};
    public double[] b_lowpass = new double[]{41,195,443,577,316,0,0,0,740,1206,99,0,0,1008,6781,11385,11385,6781,1008,0,0,99,1206,740,0,0,0,316,577,443,195,41};
    public double[] a_lowpass = new double[]{1};
    public double[] b_bandpass = new double[]{0.175124050220482, 0, -0.700496200881930, 0, 1.050744301322894, 0, -0.700496200881930, 0, 0.175124050220482};
    public double[] a_bandpass = new double[]{1,-3.188390463247433,3.595316282794339,-2.344356989904013, 2.280493390848253, -1.973417539922519, 0.707374952065500, -0.147850253488695, 0.070830620991567};

    public double [] a_high = new double []{ 1.00000000000000000000000000000000, -19.91991769624890000000000000000000, 188.48164228513600000000000000000000, -1126.36354987364000000000000000000000, 4767.88932757482000000000000000000000, -15196.20557236670000000000000000000000, 37838.55577006020000000000000000000000, -75374.47458940890000000000000000000000, 121993.80594288300000000000000000000000, -162008.20173709100000000000000000000000, 177496.80398991900000000000000000000000, -160715.98331855600000000000000000000000, 120055.46021648800000000000000000000000, -73585.20453618450000000000000000000000, 36645.68307959580000000000000000000000, -14599.75252005730000000000000000000000, 4544.21177537462000000000000000000000, -1064.95938453118000000000000000000000, 176.78506476380100000000000000000000, -18.53472338868110000000000000000000, 0.92304021011003100000000000000000};
    public double [] b_high = new double []{ 0.96074981660681600000000000000000, -19.21499633213630000000000000000000, 182.54246515529500000000000000000000, -1095.25479093177000000000000000000000, 4654.83286146002000000000000000000000, -14895.46515667210000000000000000000000, 37238.66289168020000000000000000000000, -74477.32578336040000000000000000000000, 121025.65439796100000000000000000000000, -161367.53919728100000000000000000000000, 177504.29311700900000000000000000000000, -161367.53919728100000000000000000000000, 121025.65439796100000000000000000000000, -74477.32578336040000000000000000000000, 37238.66289168020000000000000000000000, -14895.46515667210000000000000000000000, 4654.83286146002000000000000000000000, -1095.25479093177000000000000000000000, 182.54246515529500000000000000000000, -19.21499633213630000000000000000000, 0.96074981660681600000000000000000};

    public double [] a_low = new double [] { 1.00000000000000000000000000000000, 2.39759887373305000000000000000000, 5.36919193965370000000000000000000, 7.52344600776591000000000000000000, 9.31987208611336000000000000000000, 8.93262545609765000000000000000000, 7.52649616788162000000000000000000, 5.23880337169268000000000000000000, 3.19833845840326000000000000000000, 1.65175583620140000000000000000000, 0.74375675393112400000000000000000, 0.28451210428888600000000000000000, 0.09370784385181280000000000000000, 0.02595463505184700000000000000000, 0.00604594219108841000000000000000, 0.00115163780293827000000000000000, 0.00017637929086426900000000000000, 0.00002074140673739660000000000000, 0.00000177057385782720000000000000, 0.00000009714949762635500000000000, 0.00000000258639259258589000000000};
    public double [] b_low = new double [] { 0.00005084367380682720000000000000, 0.00101687347613654000000000000000, 0.00966029802329717000000000000000, 0.05796178813978300000000000000000, 0.24633759959407800000000000000000, 0.78828031870104900000000000000000, 1.97070079675262000000000000000000, 3.94140159350525000000000000000000, 6.40477758944603000000000000000000, 8.53970345259470000000000000000000, 9.39367379785417000000000000000000, 8.53970345259470000000000000000000, 6.40477758944603000000000000000000, 3.94140159350525000000000000000000, 1.97070079675262000000000000000000, 0.78828031870104900000000000000000, 0.24633759959407800000000000000000, 0.05796178813978300000000000000000, 0.00966029802329717000000000000000, 0.00101687347613654000000000000000, 0.00005084367380682720000000000000};

    public int [] filter( double []a, double []b, int []x_int)
    {
        double []x= new double[x_int.length];
        double []y = new double[x.length];
        int [] y_salida = new int [x.length];
        int ord = a.length-1;
        int np = x.length -1 ;
        int i,j;

        for (i=0;i<x.length;i++)
            x[i]=x_int[i];

        y[0]=b[0]*x[0];

        for (i=1;i<ord+1;i++)
        {
            y[i]=0.0;
            for (j=0;j<i+1;j++)
                y[i]=y[i]+b[j]*x[i-j];
            for (j=0;j<i;j++)
                y[i]=y[i]-a[j+1]*y[i-j-1];
        }
        /* end of initial part */

        for (i=ord+1;i<np+1;i++)
        {
            y[i]=0.0;
            for (j=0;j<ord+1;j++)
                y[i]=y[i]+b[j]*x[i-j];
            for (j=0;j<ord;j++)
                y[i]=y[i]-a[j+1]*y[i-j-1];
        }

        for (i=0; i<y_salida.length; i++)
            y_salida[i] = (int)(y[i]);

        return y_salida;
    } /* end of filter */

    public int[] iirFilter(double[] bCoeff, double[] aCoeff, int[] samples, String type){
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
        // lo llevo a integer
        int[] int_y_out = new int[y_out.length];

        // Si a la salida tenemos una ganancia en continua hay que escalarlo
        if (type.equals("lowpass") == true) {

            sumCoefa = sumCoeff(aCoeff);
            sumCoefb = sumCoeff(bCoeff);
            for (int i=0; i<int_y_out.length; i++)
                int_y_out[i] = (int)(((y_out[i])*(sumCoefa))/(sumCoefb));
        }else{
            for (int i=0; i<int_y_out.length; i++)
                int_y_out[i] = (int)(y_out[i]);
        }

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
