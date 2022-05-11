package com.example.wellnessapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class MenuActivity extends AppCompatActivity {

    private TextView Apellido, Nombre, Posicion, Club, AlturaPeso, Aviso;
    private FirebaseAuth firebaseAuth;
    private String posicion, club;
    private boolean bandera_aviso = false;
    FirebaseFirestore db,fs;
    String email, currentDate, cemail;
    private ProgressDialog progressDialog;
    private ImageView FotoPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        progressDialog = new ProgressDialog(MenuActivity.this);
        progressDialog.setMessage("Cargando...");
        progressDialog.show();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        //Inicializo los edit text
        Apellido = findViewById(R.id.apellidoTxt);
        Nombre = findViewById(R.id.nombreTxt);
        Club = findViewById(R.id.clubTxt);
        Posicion = findViewById(R.id.posicionTxt);
        AlturaPeso = findViewById(R.id.TxtAlturaPeso);
        FotoPerfil = findViewById(R.id.profile_image);

        Aviso = findViewById(R.id.TxtAviso);

        Calendar date = Calendar.getInstance(); // to get the date
        currentDate = ((date.get(Calendar.DAY_OF_MONTH))+"-"+(date.get(Calendar.MONTH)+1)+"-"+(date.get(Calendar.YEAR)-2000));


        //inicializamos el objeto firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Intent caller = getIntent();
        email=caller.getStringExtra("Email");

        setup();

    }

    private void setup() {

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

                            actualizarTarjetas(club, posicion);

                        }
                    }
                });

    }

    private void actualizarTarjetas(String clab, String posicionn) {

        Log.d("Setup","Correo del cliente "+cemail);
        db.collection("Clientes")
                .document(cemail)
                .collection("Usuarios")
                .document(clab)
                .collection(posicionn)
                .document(email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documento = task.getResult();
                            if (documento.exists()) {
                                posicion = documento.getString("Posicion");
                                club = documento.getString("Club");
                                Apellido.setText(documento.getString("Apellido"));
                                Nombre.setText(documento.getString("Nombre"));
                                Club.setText(club);
                                Posicion.setText(posicion);

                                Long altura = (Long) documento.getData().get("Altura");
                                Long peso = (Long) documento.getData().get("Peso");

                                AlturaPeso.setText("Altura: "+altura.toString()+" [cm]. Peso: "+peso.toString()+" [kg]");

                                final String imgUrl = documento.getString("UrlPerfil");
                                /////////////////
                                Log.d("Reg. Wellnes", "Info personal "+documento.getData());

                                try {
                                    fs = FirebaseFirestore.getInstance();
                                    Log.d("Reg. Wellnes", "Entró en try");

                                    fs.collection("Registro")
                                            .document(club)
                                            .collection("Wellnes")
                                            .document(posicion)
                                            .collection(email)
                                            .document(currentDate)
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    Log.d("Reg. Wellnes", "Se completó la lectura");
                                                    if (task.isSuccessful()) {
                                                        DocumentSnapshot documento = task.getResult();
                                                        if (documento.exists()){
                                                            Aviso.setVisibility(View.INVISIBLE);
                                                            bandera_aviso = true;
                                                        } else {
                                                            bandera_aviso = false;
                                                        }
                                                    }
                                                    Log.d("Reg. Wellnes", "No se ha encontrado registro 1");

                                                    Log.d("Tarjeta","La Url de la tarjeta es "+imgUrl);

                                                    if (imgUrl!=""){
                                                        Log.d("ActualizarImg", "Entró");
                                                        cargarFotoPerfil(imgUrl);
                                                    }
                                                    progressDialog.dismiss();
                                                }
                                            });
                                } catch (NullPointerException direccionE){
                                    bandera_aviso = false;
                                    Log.d("Reg. Wellnes", "No se ha encontrado registro 2");
                                }
                            }
                        }

                    }
                });

    }

    private void cargarFotoPerfil(String imgUrl) {
        Glide.with(MenuActivity.this)
                .load(imgUrl)
                .into(FotoPerfil);
    }


                /*.addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        Log.d("Reg. Wellnes", "Se completó la lectura");
                        if (task.isSuccessful()) {
                            DocumentSnapshot documento = task.getResult();
                            if (documento.exists()){
                                Aviso.setVisibility(View.INVISIBLE);
                                bandera_aviso = true;
                            } else {
                                bandera_aviso = false;
                            }
                        }
                        Log.d("Reg. Wellnes", "No se ha encontrado registro 1");
                    }
                });
                /*.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Reg. Wellnes", "No se ha encontrado registro 2");
                    }
                });*/




    public void Go_PSE(View view) {
        if (bandera_aviso){
            Intent goPSE= new Intent(MenuActivity.this, PSE.class);
            goPSE.putExtra("Email",email);
            goPSE.putExtra("CEmail",cemail);
            startActivity(goPSE);
        } else {
                Toast.makeText(this, "Completar el Wellnes del día", Toast.LENGTH_SHORT).show();
            }
    }

    public void Go_wellness(View view) {
        if (!bandera_aviso){
            Intent goWellnes= new Intent(MenuActivity.this, WellnessActivity.class);
            goWellnes.putExtra("Email",email);
            goWellnes.putExtra("CEmail",cemail);
            startActivity(goWellnes);
        } else {
                Toast.makeText(this, "Wellnes diario completado", Toast.LENGTH_SHORT).show();
            }
    }

    public void go_Main(View view) {
        Intent goMenuUI= new Intent(MenuActivity.this, HomeActivity.class);
        startActivity(goMenuUI);
        firebaseAuth.signOut();
        finish();
    }

    public void Go_editarTarjeta(View view) {
        Intent goEditUI= new Intent(MenuActivity.this, EditarActivity.class);
        goEditUI.putExtra("Email",email);
        goEditUI.putExtra("CEmail",cemail);
        startActivity(goEditUI);
        finish();
    }
}