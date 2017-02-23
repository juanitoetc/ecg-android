package com.utn.juanignacio.utnecgv10;

import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;

import android.app.Activity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ShareActionProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class ActivityPlot extends Activity implements View.OnLongClickListener{

    private static final String TAG = "com.j.PlotAct";

    /* Inicio Android Plot Variables */

    public XYPlot  plot;
    public XYPlot  backgroundPlot;

    private static final String LOG_TAG = "errorTag";
    ShareActionProvider mShareActionProvider;
    boolean pressed = false;

    ArrayList<Double> VectorXYPlot = new ArrayList<Double>();

    public int i = 0;
    public double DatosX, DatosY;
    public double MAX_Value;
    public double MIN_Value;
    public String path_img;
    public int num_samples;
    final static int duration = 3; //duracion de lo que quiero graficar
    public double MAX_Value_fixed_y = 1.5;          //mV
    public double MIN_Value_fixed_y = -1.5;         //mV
    public double NumThickLines_y = 6;              // Divisiones de lineas gruesas, exterior de los cuadrados grandes y
    public double NumFineLines_y = 5;               // Divisiones de lineas finas, interior de los cuadrados grandes y
    public double NumFineLines_x = 5;               //Divisiones de lineas finas, interior de los cuadrados grandes x
    public double timeThickLines_x = 0.2;           //sg. Lo que dura en tiempo un cuadrado grande.

    /* FinAndroid Plot Variables*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_plot);

        //Dejo esta activity unicamente en landscape porque sino se ve mal
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        /* Instancio la clase ECG*/

        //Obtengo la información de la Actividad anterior y se la asigno a la variable info.
        Intent men = getIntent();

        SamplesECG canal1 = new SamplesECG(men.getIntArrayExtra("muestras"));
        String lead = new String(men.getStringExtra("lead"));
        path_img = new String(men.getStringExtra("path"));

        /*Me quedo con los ultimos 3 segundos*/
        num_samples = (int) ((canal1.fs)*duration);
        int[] samp_plot = new int[num_samples];

        for (i=0;i<num_samples;i++)
            samp_plot[i] = canal1.Samples[(canal1.Samples.length)-(num_samples)+i];

        double[] samplesInmV = new double[num_samples];

        samplesInmV = canal1.getSamplesInmV(samp_plot);

        /* Inicio Android Plot */
        /*Busco los valores maximos y minimos dentro del array de datos para graficar correctamente*/
        MIN_Value = samplesInmV[0];
        MAX_Value = samplesInmV[0];

        for(i = 0; i< samplesInmV.length; i++)
        {
            if(samplesInmV[i] < MIN_Value)
                MIN_Value = samplesInmV[i];
            if(samplesInmV[i] > MAX_Value)
                MAX_Value = samplesInmV[i];
        }

        plot = (XYPlot) findViewById(R.id.MiPrimerXY);
        backgroundPlot = (XYPlot) findViewById(R.id.MybackgroundXY);

        plot.setOnLongClickListener(this);

        /*<background FORMAT>*/
        backgroundPlot.setBorderPaint(null);
        backgroundPlot.setPlotMargins(0, 0, 0, 0);

        backgroundPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.rgb(255, 255, 255));      //Control del color de fondo
        backgroundPlot.getGraphWidget().getBackgroundPaint().setColor(Color.rgb(255, 255, 255));
        backgroundPlot.getBackgroundPaint().setColor(Color.rgb(255,255,255));
        backgroundPlot.getGraphWidget().getDomainLabelPaint().setColor(Color.TRANSPARENT);
        backgroundPlot.getGraphWidget().getRangeLabelPaint().setColor(Color.TRANSPARENT);
        backgroundPlot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.rgb(254, 69,69));
        backgroundPlot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.rgb(254, 69,69));

        backgroundPlot.getDomainLabelWidget().setVisible(true);
        backgroundPlot.getDomainLabelWidget().getLabelPaint().setColor(Color.rgb(0, 0, 0));
        backgroundPlot.getRangeLabelWidget().setVisible(true);
        backgroundPlot.getRangeLabelWidget().getLabelPaint().setColor(Color.rgb(0, 0, 0));
        backgroundPlot.getTitleWidget().setVisible(true);
        backgroundPlot.getTitleWidget().getLabelPaint().setColor(Color.rgb(0, 0, 0));

        backgroundPlot.getGraphWidget().getRangeGridLinePaint().setColor(Color.rgb(254, 172, 172));         //Control de color de eje X
        backgroundPlot.getGraphWidget().getDomainGridLinePaint().setColor(Color.rgb(254, 172, 172));        //Control de color de eje Y

        backgroundPlot.setDomainBoundaries(0, (samplesInmV.length) / canal1.fs, BoundaryMode.FIXED);        //Determino los extremos en el eje X
        backgroundPlot.setRangeBoundaries(MIN_Value_fixed_y, MAX_Value_fixed_y, BoundaryMode.FIXED);        //Determino los extremos en el eje Y
        backgroundPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, (((samplesInmV.length) / canal1.fs) / (timeThickLines_x/NumFineLines_x)));        //Controla el incremento de lineas finas en X
        backgroundPlot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, (MAX_Value_fixed_y - MIN_Value_fixed_y) / (NumFineLines_y*NumThickLines_y));       //Controla el incremento de lineas finas en Y
        /*</background FORMAT>*/

        /*<realplot FORMAT>*/
        plot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, timeThickLines_x);                                                  //Controla el incremento de lineas gruesas en X
        plot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, (MAX_Value_fixed_y - MIN_Value_fixed_y) / NumThickLines_y );         //Controla el incremento de lineas gruesas en Y

        plot.setBorderPaint(null);
        plot.setPlotMargins(0, 0, 0, 0);

        plot.getGraphWidget().getGridBackgroundPaint().setColor(Color.TRANSPARENT);      //Control del color de fondo
        plot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
        plot.getBackgroundPaint().setColor(Color.TRANSPARENT);

        plot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.rgb(254, 69,69));
        plot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.rgb(254, 69,69));

        plot.getGraphWidget().getRangeGridLinePaint().setColor(Color.rgb(254, 69, 69));          //Control de color de eje X
        plot.getGraphWidget().getDomainGridLinePaint().setColor(Color.rgb(254, 69, 69));          //Control de color de eje Y
        plot.getGraphWidget().getDomainLabelPaint().setColor(Color.rgb(0,0,0));
        plot.getGraphWidget().getRangeLabelPaint().setColor(Color.rgb(0,0,0));

        plot.setDomainBoundaries(0, (samplesInmV.length)/canal1.fs , BoundaryMode.FIXED);           //Determino los extremos en el eje X
        plot.setRangeBoundaries(MIN_Value_fixed_y, MAX_Value_fixed_y, BoundaryMode.FIXED);          //Determino los extremos en el eje Y
        /*<realplot FORMAT>*/

        //Copio las muestras que estan en const_file_class a la instancia de la clase Vector

        for(i = 0; i < samplesInmV.length ; i++)
        {
            /*Cargo los valores del tiempo*/
            DatosX = i/(canal1.fs);                          //Ts (tiempo entre muestras)
            VectorXYPlot.add(DatosX);

            /*Cargo los valores del ECG*/
            DatosY = samplesInmV[i];
            VectorXYPlot.add(DatosY);
        }

        XYSeries series = new SimpleXYSeries(VectorXYPlot, SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED,lead);
        LineAndPointFormatter seriesFormat = new LineAndPointFormatter(Color.rgb(0, 0, 0), 0x00, 0x00, null);

        plot.clear();
        plot.addSeries(series, seriesFormat);
        /* FinAndroid Plot */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_plot, menu);

        /*Save*/
        /*Antes de terminar guardo la imagen en la SD*/

        // create bitmap screen capture
        View v2 = (RelativeLayout) findViewById(R.id.Merge);
        v2.setDrawingCacheEnabled(true);
        Bitmap combine = Bitmap.createBitmap(v2.getDrawingCache());
        v2.setDrawingCacheEnabled(false);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path_img, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        combine.compress(Bitmap.CompressFormat.PNG, 100, fos);

        try {
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
        plot.setDrawingCacheEnabled(true);
        backgroundPlot.setDrawingCacheEnabled(true);
        int width = plot.getWidth();
        int height = plot.getHeight();
        plot.measure(width, height);
        backgroundPlot.measure(width, height);
        Bitmap front = Bitmap.createBitmap(plot.getDrawingCache());
        Bitmap background = Bitmap.createBitmap(backgroundPlot.getDrawingCache());
        plot.setDrawingCacheEnabled(false);
        backgroundPlot.setDrawingCacheEnabled(false);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(Environment.getExternalStorageDirectory() + "/test.bmp", true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Bitmap combine = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas comboImage = new Canvas(combine);

        comboImage.drawBitmap(background,0f,0f,null);
        comboImage.drawBitmap(front, 0f, 0f, null);

        combine.compress(Bitmap.CompressFormat.JPEG, 100, fos);

        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        plot.setDrawingCacheEnabled(false);
        */


        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();
        mShareActionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        mShareActionProvider.setShareIntent(getDefaultSharedPreferences());

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(getDefaultSharedPreferences());
        } else {
            Log.i(LOG_TAG, "is null");
        }

        // Return true to display menu
        return true;
    }

    private Intent getDefaultSharedPreferences() {

        Intent intent_share = new Intent(Intent.ACTION_SEND);
        //intent_share.setType("text/plain");
        intent_share.setType("image/*");
        File image = new File(path_img);
        Uri uriSavedImage = Uri.fromFile(image);

        intent_share.putExtra(Intent.EXTRA_STREAM, uriSavedImage);

        return intent_share;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.e(TAG, "ORIENTATION_LANDSCAPE");
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.e(TAG, "ORIENTATION_PORTRAIT");
        }
    }

    @Override
    public boolean onLongClick(View v) {
        this.openOptionsMenu();
        return false;
    }
}
