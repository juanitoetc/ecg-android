package com.utn.juanignacio.utnecgv10;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class pacientMenuActivity extends ActionBarActivity implements View.OnClickListener {

    /*Declaro privados todos los botones*/
    private Button bt_newPatient;
    private Button bt_modifyPatient;
    private Button bt_deletePatient;
    private Button bt_config;
    private Button bt_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pacient_menu);

        configurarBotones(); /*Agrego los findViewbyId y los setOnClickListener*/
    }

    private void configurarBotones() {

        bt_newPatient = (Button) findViewById(R.id.btnAddPacient);
        bt_modifyPatient = (Button) findViewById(R.id.btnFindPacient);
        bt_deletePatient = (Button) findViewById(R.id.btnDeletePacient);
        bt_config = (Button) findViewById(R.id.btnConfig);
        bt_back = (Button) findViewById(R.id.btnBack);

        bt_newPatient.setOnClickListener(this);
        bt_modifyPatient.setOnClickListener(this);
        bt_deletePatient.setOnClickListener(this);
        bt_config.setOnClickListener(this);
        bt_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAddPacient:{
                Intent addPacientIntent = new Intent(this, addPacientActivity.class);
                startActivity(addPacientIntent);
                break;
            }

            case R.id.btnFindPacient:{

                /*Notar que los casos btnFindPacient y btnDeletePacient van al mismo menu solo que al hacer el
                onclick al listview cambia la accion, manejo dicha accion con un boleano*/

                Intent viewPacientIntent = new Intent(this, viewPacientActivity.class);
                viewPacientIntent.putExtra("actionToDo_delete", false);
                startActivity(viewPacientIntent);
                break;
            }

            case R.id.btnDeletePacient:{

                /*Notar que los casos btnFindPacient y btnDeletePacient van al mismo menu solo que al hacer el
                onclick al listview cambia la accion, manejo dicha accion con un boleano*/

                Intent viewPacientIntent = new Intent(this, viewPacientActivity.class);
                viewPacientIntent.putExtra("actionToDo_delete", true);
                startActivity(viewPacientIntent);
                break;
            }

            case R.id.btnConfig:{
                Toast.makeText(this, "Configurar", Toast.LENGTH_SHORT).show();
                break;
            }

            case R.id.btnBack: {
                finish();
                break;
            }
        }
    }

}
