package com.example.wellnessapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
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

public class WellnessActivity extends AppCompatActivity {


    Spinner spqsueno, spFatiga, spMuscular, spStress, spHumor;
    String currentDate, club, posicion, email, apellido, nombre,cemail;
    String pain_site = null;
    LinearLayout layout_alarm;
    private View alarmView;
    private EditText pain_dsc;


    // Access a Cloud Firestore instance from your Activity
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_entrenamiento);

        Intent caller = getIntent();
        email=caller.getStringExtra("Email");
        cemail=caller.getStringExtra("CEmail");

        layout_alarm = findViewById(R.id.layoutDolor);
        //email="Santiago";

        Calendar date = Calendar.getInstance(); // to get the date
        currentDate = ((date.get(Calendar.DAY_OF_MONTH))+"-"+(date.get(Calendar.MONTH)+1)+"-"+(date.get(Calendar.YEAR)-2000));

        setup();

        eventoAlarma();
    }

    private void eventoAlarma() {
        final boolean[] bandera_alarma = {false};

        alarmView = getLayoutInflater().inflate(R.layout.pain_add, null, false);

        pain_dsc = alarmView.findViewById(R.id.muscular_alarm);



        spMuscular.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position>0 && position<4 && !bandera_alarma[0]){
                    layout_alarm.addView(alarmView);
                    bandera_alarma[0] = true;


                }
                if (position>3 && bandera_alarma[0]) {
                    layout_alarm.removeView(alarmView);
                    bandera_alarma[0] = false;
                    Log.d("Alarma -", pain_dsc.getText().toString().trim());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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

    private void setup() {
        spqsueno= findViewById(R.id.spQsueno);
        ArrayAdapter<CharSequence> adapterpuntuacion= ArrayAdapter.createFromResource(this,R.array.puntuacion5,android.R.layout.simple_list_item_1);
        spqsueno.setAdapter(adapterpuntuacion);

        spFatiga= findViewById(R.id.spfatiga);
        adapterpuntuacion= ArrayAdapter.createFromResource(this,R.array.puntuacion5,android.R.layout.simple_list_item_1);
        spFatiga.setAdapter(adapterpuntuacion);

        spMuscular= findViewById(R.id.spmuscular);
        adapterpuntuacion= ArrayAdapter.createFromResource(this,R.array.puntuacion5,android.R.layout.simple_list_item_1);
        spMuscular.setAdapter(adapterpuntuacion);

        spStress= findViewById(R.id.spstress);
        adapterpuntuacion= ArrayAdapter.createFromResource(this,R.array.puntuacion5,android.R.layout.simple_list_item_1);
        spStress.setAdapter(adapterpuntuacion);

        spHumor= findViewById(R.id.sphumor);
        adapterpuntuacion= ArrayAdapter.createFromResource(this,R.array.puntuacion5,android.R.layout.simple_list_item_1);
        spHumor.setAdapter(adapterpuntuacion);

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

    public void Go_postTrain(View view) {

        //Obtener puntuacion

        //Calidad del sueño

        int spinner_pos_s = spqsueno.getSelectedItemPosition();
        String[] puntuacionSueno = getResources().getStringArray(R.array.puntuacion5);
        final int puntosSueno = Integer.valueOf(puntuacionSueno[spinner_pos_s]); // puntuacion sueño

        //Fatiga General

        int spinner_pos_f = spFatiga.getSelectedItemPosition();
        String[] puntuacionFatiga = getResources().getStringArray(R.array.puntuacion5);
        final int puntosFatiga = Integer.valueOf(puntuacionFatiga[spinner_pos_f]); // puntuacion fatiga

        //Molestia muscular

        int spinner_pos_m = spMuscular.getSelectedItemPosition();
        String[] puntuacionMuscular = getResources().getStringArray(R.array.puntuacion5);
        final int puntosMuscular = Integer.valueOf(puntuacionMuscular[spinner_pos_m]); // puntuacion molestia muscular

        //Stress

        int spinner_pos_st = spStress.getSelectedItemPosition();
        String[] puntuacionStress = getResources().getStringArray(R.array.puntuacion5);
        final int puntosStress = Integer.valueOf(puntuacionStress[spinner_pos_st]); // puntuacion stress

        //Humor

        int spinner_pos_h = spHumor.getSelectedItemPosition();
        String[] puntuacionHumor = getResources().getStringArray(R.array.puntuacion5);
        int puntosHumor = Integer.valueOf(puntuacionHumor[spinner_pos_h]); // puntuacion humor

        //There must not be empty boxes
        if (puntosFatiga==0 || puntosHumor==0 || puntosMuscular==0 || puntosStress==0 || puntosSueno==0) {
            // es lo mismo de hacer if (palo.equals("")){
            Toast.makeText(this, "0 is not a valid number" , Toast.LENGTH_SHORT).show();

            if (puntosFatiga==0){
                spFatiga.setBackgroundColor(Color.RED);
            }
            if (puntosHumor==0){
                spHumor.setBackgroundColor(Color.RED);
            }
            if (puntosMuscular==0){
                spMuscular.setBackgroundColor(Color.RED);
            }
            if (puntosStress==0){
                spStress.setBackgroundColor(Color.RED);
            }
            if (puntosSueno==0){
                spqsueno.setBackgroundColor(Color.RED);
            }

            if (puntosFatiga!=0){
                spFatiga.setBackgroundColor(Color.argb(255,0, 255, 238));
            }
            if (puntosHumor!=0){
                spHumor.setBackgroundColor(Color.argb(255,0, 255, 238));
            }
            if (puntosMuscular!=0){
                spMuscular.setBackgroundColor(Color.argb(255,0, 255, 238));
            }
            if (puntosStress!=0){
                spStress.setBackgroundColor(Color.argb(255,0, 255, 238));
            }
            if (puntosSueno!=0){
                spqsueno.setBackgroundColor(Color.argb(255,0, 255, 238));
            }
        }

        //DecimalFormat formatVal = new DecimalFormat("####.##");
        if (puntosFatiga!=0 & puntosHumor!=0 & puntosMuscular!=0 & puntosStress!=0 & puntosSueno!=0){

            String PuntosSueno =Integer.toString(puntosSueno);
            String PuntosFatiga =Integer.toString(puntosFatiga);
            String PuntosMuscular =Integer.toString(puntosMuscular);
            String PuntosStress =Integer.toString(puntosStress);
            String PuntosHumor =Integer.toString(puntosHumor);

            pain_site = pain_dsc.getText().toString().trim();
            Log.d("Alarma +", pain_dsc.getText().toString().trim());

            AlertDialog.Builder alerta= new AlertDialog.Builder(WellnessActivity.this);
            Log.d("Carga", "La informacion introducida es: "+"Calidad de sueño = " +PuntosSueno+" Fatiga general= "+PuntosFatiga+" Dolor Muscular= "+PuntosMuscular+" Nivel de estres= "+PuntosStress+" Humor= "+PuntosHumor+"Lugar del dolor: "+pain_site);
            alerta.setMessage("La informacion introducida es:\n"+"\nCalidad de sueño= " +PuntosSueno+"\nFatiga general= "+PuntosFatiga+
                    "\nDolor Muscular= "+PuntosMuscular+"\nNivel de estres= "+PuntosStress+"\nHumor= "+PuntosHumor+
                    "\n\n¿Es correcto?")
                    .setCancelable(false) //para que no puedo cliqear afuera
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //Tengo que enviar la información de esta primer pantalla
                            // Create a new user with a first and last name
                            Map<String, Object> welnes = new HashMap<>();
                            welnes.put("Calidad de sueño", puntosSueno);
                            welnes.put("Fatiga general", puntosFatiga);
                            welnes.put("Dolor Muscular", puntosMuscular);
                            //if (pain_site!=null) {
                            welnes.put("Lugar del Dolor", pain_site);
                            //}
                            welnes.put("Nivel de estress", puntosStress);
                            welnes.put("Humor", puntosSueno);
                            welnes.put("Fecha", currentDate);
                            welnes.put("Nombre", (nombre+" "+apellido));


                            // Add a new document with a generated ID
                            db.collection("Registro")
                                    .document(club)
                                    .collection("Wellnes")
                                    .document(posicion)
                                    .collection(email)
                                    .document(currentDate)
                                    .set(welnes);

                            //Tengo que llevarlo a la siguiente pantalla
                            Intent goMenu= new Intent(WellnessActivity.this, MenuActivity.class);
                            goMenu.putExtra("Email",email);

                            startActivity(goMenu);
                            finish();


                        }
                    })

                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog titulo= alerta.create();
            titulo.setTitle("Verificar información");
            titulo.show();

        }


    }

    public void Go_atras(View view) {
        Intent goMenu= new Intent(WellnessActivity.this, MenuActivity.class);
        goMenu.putExtra("Email",email);

        startActivity(goMenu);
        finish();
    }

    public void Go_pWellnes(View view) {
        Intent gopWellnes = new Intent(WellnessActivity.this,pWellnesActivity.class);
        startActivity(gopWellnes);
    }
}