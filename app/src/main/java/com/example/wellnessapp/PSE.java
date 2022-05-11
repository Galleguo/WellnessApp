package com.example.wellnessapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PSE extends AppCompatActivity {

    Spinner spEjercicio;
    String email, cemail, currentDate, club, posicion, apellido, nombre;
    private EditText txtTiempo;
    private Float tiempo;

    // Access a Cloud Firestore instance from your Activity
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fin_sesion);

        Intent caller = getIntent();
        email=caller.getStringExtra("Email");
        cemail = caller.getStringExtra("CEmail");

        setup();

    }

    private void setup() {
        Calendar date = Calendar.getInstance(); // to get the date
        currentDate = ((date.get(Calendar.DAY_OF_MONTH))+"-"+(date.get(Calendar.MONTH)+1)+"-"+(date.get(Calendar.YEAR)-2000));

        spEjercicio= (Spinner) findViewById(R.id.spejercicio);
        ArrayAdapter<CharSequence> adapterpuntuacion= ArrayAdapter.createFromResource(this,R.array.puntuacion10,android.R.layout.simple_list_item_1);
        spEjercicio.setAdapter(adapterpuntuacion);

        txtTiempo = findViewById(R.id.TxtTiempo);

        db.collection("Vinculacion")
                .document(email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            cemail = (String) task.getResult().getData().get("Cliente");

                            ////////EDITABLE
                            club = (String) task.getResult().getData().get("Club");
                            posicion = (String) task.getResult().getData().get("Posicion");
                            ////////////

                            lecturaCaracteristicas(club,posicion);

                        }
                    }
                });
    }

    private void lecturaCaracteristicas(String clubb, String posicionn) {
        db.collection("Clientes")
                .document(cemail)
                .collection("Usuarios")
                .document(clubb)
                .collection(posicionn)
                .document(email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documento = task.getResult();
                            if (documento.exists()) {
                                apellido = documento.getString("Apellido");
                                nombre = documento.getString("Nombre");
                            }
                        }
                    }
                });
    }

    public void Go_Menu(View view) {
        //Obtener puntuacion

        //Dificultad del ejercicio
        int spinner_pos_Ej = spEjercicio.getSelectedItemPosition();
        String[] puntuacionEjercicio = getResources().getStringArray(R.array.puntuacion10);
        final float puntosEjercicio = Float.valueOf(puntuacionEjercicio[spinner_pos_Ej]); // puntuacion Ejercicio

        //Tiempo
        tiempo = Float.valueOf(txtTiempo.getText().toString());

        //There must not be empty boxes
        if (puntosEjercicio==0 || tiempo==null) {
            Toast.makeText(this, "0 is not a valid number" , Toast.LENGTH_SHORT).show();
            spEjercicio.setBackgroundColor(Color.RED);
        }

        if (puntosEjercicio!=0 && tiempo!=null){

            //Guardo información
            Map<String, Object> PSE = new HashMap<>();
            PSE.put("Dificultad de ejercicio", puntosEjercicio);
            PSE.put("Tiempo",tiempo);
            PSE.put("Fecha", currentDate);
            PSE.put("Nombre",(nombre+" "+apellido));


            db.collection("Registro")
                    .document(club)
                    .collection("PSE")
                    .document(posicion)
                    .collection(email)
                    .add(PSE);

            //Calculo de carga
            /*float carga = puntosEjercicio * tiempo;
            Map<String,Object> calculado = new HashMap<>();
            calculado.put(currentDate,carga);

            db.collection("Registro")
                    .document(club)
                    .collection("PSE")
                    .document(posicion)
                    .collection(email)
                    .document("Calculado")
                    .set(calculado);*/

            // Vuelve al inicio
            Intent goMenu= new Intent(PSE.this, MenuActivity.class);
            goMenu.putExtra("Email",email);
            startActivity(goMenu);

            finish();
        }
    }

    public void Go_atras(View view) {
        Intent goMenu= new Intent(PSE.this, MenuActivity.class);
        goMenu.putExtra("Email",email);

        startActivity(goMenu);
        finish();
    }

    public void Go_pPSE(View view) {
        Intent gopPSE = new Intent(PSE.this,pPSEActivity.class);
        startActivity(gopPSE);
    }
}