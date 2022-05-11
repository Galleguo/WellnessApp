package com.example.wellnessapp;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    //defining view objects
    private EditText TextEmail;
    private EditText TextPassword;
    private ProgressDialog progressDialog;

    //Declaramos un objeto firebaseAuth
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //inicializamos el objeto firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

        //Referenciamos los views
        TextEmail = (EditText) findViewById(R.id.TxtEmail);
        TextPassword = (EditText) findViewById(R.id.TxtPassword);


        progressDialog = new ProgressDialog(HomeActivity.this);

        chequearEstado();
    }

    private void chequearEstado() {

        FirebaseUser userr = firebaseAuth.getCurrentUser();
        Log.d("UsuarioHi","El usuario que ingreso es "+userr);

        if (userr!=null){
            String correo_u = userr.getEmail();
            Log.d("UsuarioHi","El usuario que ingreso es "+correo_u);

            Toast.makeText(HomeActivity.this,"Bienvenido/a",Toast.LENGTH_LONG).show();
            Intent goMenuUI= new Intent(HomeActivity.this, MenuActivity.class);
            goMenuUI.putExtra("Email",correo_u);

            startActivity(goMenuUI);
            finish();
        }
    }


    public void ingresar(View view) {
        logearUsuario();
    }

    private void logearUsuario() {

        //Obtenemos el email y la contraseña desde las cajas de texto
        final String email = TextEmail.getText().toString().trim();
        String password  = TextPassword.getText().toString().trim();

        //Verificamos que las cajas de texto no esten vacías
        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){

        progressDialog.setMessage("Accediendo...");
        progressDialog.show();

        //Consultamos si el usuario existe
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //checking if success
                        if(task.isSuccessful()){

                            FirebaseUser user = firebaseAuth.getCurrentUser();

                            //if (user.isEmailVerified()){
                                Toast.makeText(HomeActivity.this,"Bienvenido/a",Toast.LENGTH_LONG).show();
                                Intent goMenuUI= new Intent(HomeActivity.this, MenuActivity.class);
                                goMenuUI.putExtra("Email",TextEmail.getText().toString().trim());

                                startActivity(goMenuUI);
                                progressDialog.dismiss();
                               finish();
                            /*} else {
                                Toast.makeText(HomeActivity.this, "Usuario no verificado", Toast.LENGTH_SHORT).show();
                                firebaseAuth.signOut();
                                progressDialog.dismiss();
                            }*/


                        }else{
                            Toast.makeText(HomeActivity.this,"No se pudo acceder ",Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    }
                });
        } else {
            Toast.makeText(HomeActivity.this, "Completar Email y/o Password", Toast.LENGTH_SHORT).show();
        }
    }

    public void goRegistro(View view) {
        Intent goRegistroUI= new Intent(HomeActivity.this, Registrar.class);
        startActivity(goRegistroUI);
    }

    public void go_Recuperar(View view) {
        Intent goRecuperarUI= new Intent(HomeActivity.this, RecuperarCuenta.class);
        startActivity(goRecuperarUI);
    }
}