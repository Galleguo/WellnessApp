package com.example.wellnessapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class RecuperarCuenta extends AppCompatActivity {

    private EditText txtEmail;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_cuenta);

        mAuth = FirebaseAuth.getInstance();
        txtEmail = findViewById(R.id.TxtRecuperar);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Procesando");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    public void Go_atras(View view) {
        finish();
    }

    public void Recuperar(View view) {
        progressDialog.show();

        String email = txtEmail.getText().toString().trim();
        if (!email.isEmpty()){
            recuperar_cuenta(email);
        } else {
            Toast.makeText(this, "Por favor, introducir un correo", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    private void recuperar_cuenta(final String email) {

        mAuth.setLanguageCode("es");//para que el correo llegue en español
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(RecuperarCuenta.this, "Se ha enviado un correo de reestablecimiento a: "+email, Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                    finish();
                } else {
                    Toast.makeText(RecuperarCuenta.this, "No se ha podido enviar el correo", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }

            }
        });

    }
}