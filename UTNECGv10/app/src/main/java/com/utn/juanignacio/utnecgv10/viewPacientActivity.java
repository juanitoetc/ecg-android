package com.utn.juanignacio.utnecgv10;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;


public class viewPacientActivity extends ActionBarActivity {

    DataBaseManager manager2;
    Cursor cursor2;
    ListView lista2;
    SimpleCursorAdapter adapter2;
    boolean bool_result = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pacient);

        /*Recupero el boleano para la accion del onclick*/
        Intent bool_IntentRestore = getIntent();
        bool_result = bool_IntentRestore.getBooleanExtra("actionToDo_delete", false);

        manager2 = new DataBaseManager(this);
        lista2 = (ListView) findViewById(R.id.pacientListView);

        String[] from = new String[]{manager2.CN_LAST_NAME, manager2.CN_DOCUMENT};
        int[] to = new int[] {android.R.id.text1, android.R.id.text2};

        cursor2 = manager2.cargarCursorContactos();
        adapter2 = new SimpleCursorAdapter(this, android.R.layout.two_line_list_item, cursor2, from, to,0);
        lista2.setAdapter(adapter2);

        lista2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final Cursor rowClicked = (Cursor) lista2.getItemAtPosition(position);
                final String id_clicked = rowClicked.getString(0);

                if(bool_result == false){

                    /*false -> no quiero eliminar el registro. Quiero verlo*/
                    Toast.makeText(viewPacientActivity.this, "Abriendo el estudio de "
                            + rowClicked.getString(1) + " " + rowClicked.getString(2)
                            , Toast.LENGTH_SHORT).show();

                    Intent seePacientIntent = new Intent(viewPacientActivity.this, seePacientActivity.class);
                    seePacientIntent.putExtra("id_search", rowClicked.getString(0));
                    startActivity(seePacientIntent);
                }
                else
                {
                    /*boleano = true, quiero eliminar el registro*/
                    final AlertDialog.Builder dialogAlert = new AlertDialog.Builder(viewPacientActivity.this);
                    dialogAlert.setTitle("Confirmacion de accion Eliminar");
                    dialogAlert.setMessage("Esta seguro de eliminar el registro seleccionado?");

                    dialogAlert.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            /*Se setea si se ha decidio SI eliminar dicho registro*/
                            manager2.deletePacient(id_clicked);
                            Toast.makeText(viewPacientActivity.this, "Estudio eliminado...", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                    dialogAlert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            /*Se setea si se ha decidio NO eliminar dicho registro*/
                            finish();
                            /*Se vuelve atrás*/
                        }
                    });
                    dialogAlert.show();
                }


            }
        });

    }

}
