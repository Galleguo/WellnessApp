package com.example.wellnessapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Registrar extends AppCompatActivity {

    //defining view objects
    private EditText CEmail, PClient;
    private EditText TextEmail, Nombre, Apellido;
    private EditText TextPassword1;
    private EditText TextPassword2;
    private ProgressDialog progressDialog;
    LinearLayout regLayout, botLayout;
    private Button botRegistrar;
    private TextView titulo_cliente;
    private String cemail;
    private Spinner spclub, spdeporte, spposicion;
    private String[] opposicion;
    private String posicion, club;


    //Declaramos un objeto firebaseAuth
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);

        //inicializamos el objeto firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

        //Referenciamos los views
        setup();


        progressDialog = new ProgressDialog(Registrar.this);
        progressDialog.setMessage("Realizando registro en linea...");


    }

    private void setup() {

        TextEmail = findViewById(R.id.TxtEmail);
        TextPassword1 = findViewById(R.id.TxtPassword1);
        TextPassword2 = findViewById(R.id.TxtPassword2);
        Apellido = findViewById(R.id.TxtApellido);
        Nombre = findViewById(R.id.TxtNombre);
        CEmail = findViewById(R.id.TxtAcceso);
        PClient = findViewById(R.id.PassCliente);

        regLayout = findViewById(R.id.reg_layout);
        botLayout = findViewById(R.id.validate_layout);
        botRegistrar = findViewById(R.id.botonRegistrar);
        titulo_cliente = findViewById(R.id.log_cliente);

    }

    public void registrar(View view) {
        //Invocamos al método:
        registrarUsuario();
    }

    private void registrarUsuario(){


        //Obtenemos el email y la contraseña desde las cajas de texto
        final String email = TextEmail.getText().toString().trim();
        final String password  = TextPassword1.getText().toString().trim();
        String password2 = TextPassword2.getText().toString().trim();

        int spinner_pos_p = spposicion.getSelectedItemPosition();
        posicion = opposicion[spinner_pos_p]; // selección de la posicion

        int spinner_pos_c = spclub.getSelectedItemPosition();
        String[] Club = getResources().getStringArray(R.array.listaclub);
        club = String.valueOf(Club[spinner_pos_c]); // selección del club

        if(!password.isEmpty() && !password2.isEmpty() && password.equals(password2) && !email.isEmpty() && !Nombre.getText().toString().trim().isEmpty() && !posicion.equals("-") && !club.equals("Club")){

            progressDialog.setMessage("Realizando registro en linea...");
            progressDialog.show();

        //creating a new user
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            firebaseAuth.setLanguageCode("es");//para que el correo llegue en español
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            user.sendEmailVerification();

                            Log.d("Registro","se hizo correctamente");

                            registrarInformacion(email, password);

                            Toast.makeText(Registrar.this,"Se ha enviado un correo de verificación a: "+ TextEmail.getText(),Toast.LENGTH_LONG).show();

                            finish();


                        }else{
                            Toast.makeText(Registrar.this,"El usuario ya se encuentra registrado",Toast.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
                    }
                });

        } else {
            if (!password.equals(password2)){
                Toast.makeText(this, "Las contraseñas deben coincidir", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Falta completar datos", Toast.LENGTH_SHORT).show();
            }

        }

    }

    private void registrarInformacion(final String usuario, String contrasena) {

        if (!club.equals("Club") || !posicion.equals("-")){

            final Map<String, Object> registro = new HashMap<>();
            registro.put("Apellido", Apellido.getText().toString().trim());
            registro.put("Nombre", Nombre.getText().toString().trim());
            registro.put("Club", club);
            registro.put("Posicion", posicion);
            registro.put("Altura",0);
            registro.put("Peso",0);
            registro.put("UrlPerfil",null);

            firebaseAuth.signInWithEmailAndPassword(usuario, contrasena)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){


                                final FirebaseFirestore fs = FirebaseFirestore.getInstance();

                                //DECREMENTO CONTADOR DE CLIENTE
                                fs.collection("Clientes")
                                        .document(cemail)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()){
                                                    Log.d("Registro", "Accedió al cliente");
                                                    DocumentSnapshot documento = task.getResult();
                                                    if (documento.exists()){

                                                        Long usuarios = (Long) documento.getData().get("Users");

                                                        Log.d("Registro", "Usuarios disponibles restantes "+usuarios.intValue());

                                                        //GENERO ACTUALIZACION DE CANTIDAD DE USUARIOS
                                                        Map<String, Object> actualizacion = new HashMap<>();
                                                        actualizacion.put("Users", (usuarios.intValue() - 1) );

                                                        //VINCULO CLIENTE Y USUARIO
                                                        Map<String, Object> puntero_client_usuario = new HashMap<>();
                                                        puntero_client_usuario.put("Cliente", cemail );

                                                        /////////////EDITABLE
                                                        puntero_client_usuario.put("Club", club );
                                                        puntero_client_usuario.put("Posicion", posicion );
                                                        ///////////////

                                                        fs.collection("Vinculacion")
                                                                .document(usuario)
                                                                .set(puntero_client_usuario);


                                                        Log.d("Registro","Información introducida en "+cemail+" de "+usuario+" --> "+registro);

                                                        //ACTUALIZO CANTIDAD DE USUARIO
                                                        fs.collection("Clientes")
                                                                .document(cemail)
                                                                .set(actualizacion); //DECREMENTO CONTADOR DE USUARIOS

                                                        //GENERO REGISTRO DE USUARIO
                                                        fs.collection("Clientes")
                                                                .document(cemail)
                                                                .collection("Usuarios")
                                                                .document(club)
                                                                .collection(posicion)
                                                                .document(usuario)
                                                                .set(registro);

                                                    }
                                                }
                                            }
                                        });



                                firebaseAuth.signOut();
                            }

                        }
                    });
        } else {
            Toast.makeText(this, "Revisar registro", Toast.LENGTH_SHORT).show();
        }

    }

    public void Go_atras(View view) {
        finish();
    }

    public void validate_user(View view) {
        progressDialog.setMessage("Validando...");
        progressDialog.show();
        cemail = CEmail.getText().toString().trim();
        String cpassword = PClient.getText().toString().trim();

        firebaseAuth.signInWithEmailAndPassword(cemail, cpassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Log.d("LogCliente", "Cliente existente");

                            final FirebaseFirestore fs = FirebaseFirestore.getInstance();
                            fs.collection("Clientes")
                                    .document(cemail)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()){
                                                Log.d("LogCliente", "Accedió al cliente");
                                                DocumentSnapshot documento = task.getResult();
                                                if (documento.exists()){
                                                    Long usuarios = (Long) documento.getData().get("Users");
                                                    Log.d("Registro", "Usuarios disponibles restantes "+usuarios.intValue());

                                                    if (usuarios.intValue()>0) {

                                                        Toast.makeText(Registrar.this, "Cliente válido", Toast.LENGTH_SHORT).show();
                                                        setRegistro();

                                                    } else {

                                                        Toast.makeText(Registrar.this, "Se superó el número de registros", Toast.LENGTH_SHORT).show();

                                                    }
                                                }
                                            }
                                            progressDialog.dismiss();
                                        }
                                    });

                        } else {
                            Log.d("LogCliente", "Cliente inexistente");
                            Toast.makeText(Registrar.this, "Email y/o Password incorrecto", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setRegistro() {

        botLayout.setVisibility(View.INVISIBLE);
        titulo_cliente.setVisibility(View.INVISIBLE);
        regLayout.setVisibility(View.VISIBLE);
        botRegistrar.setVisibility(View.VISIBLE);

        CEmail.setEnabled(false);
        PClient.setEnabled(false);

        setSpinner();

        firebaseAuth.signOut();

    }

    private void setSpinner() {
        //////////////////////////////////////
        //////////////////////////////////////////////////////
        ///////////////////////EDITABLE///////////////////////////////
        spclub = findViewById(R.id.spClub);
        ArrayAdapter<CharSequence> adapterclub= ArrayAdapter.createFromResource(Registrar.this, R.array.listaclub,android.R.layout.simple_list_item_1);
        spclub.setAdapter(adapterclub);

        spdeporte= findViewById(R.id.spDeporte);
        String[] opdeporte = {"Deporte","Fútbol","Voley","Baloncesto"};
        ArrayAdapter <String> adaptersport = new ArrayAdapter<>(Registrar.this, android.R.layout.simple_list_item_1, opdeporte);
        spdeporte.setAdapter(adaptersport);
        spdeporte.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position>0){
                    if (position == 1){//Futbol
                        opposicion = new String[]{"Arquero-a", "Defensor-a", "Mediocampista", "Delantero-a"};
                    }
                    if (position == 2){//Voley
                        opposicion = new String[]{"Armador-a", "Central", "Punta", "Opuesto","Libero"};
                    }
                    if (position == 3){//Basquet
                        opposicion = new String[]{"Alero", "Pivot", "Base"};
                    }
                } else {
                    opposicion = new String[] {"-"};
                }

                spposicion = findViewById(R.id.spPosicion);
                ArrayAdapter <String> adapterposicion = new ArrayAdapter<>(Registrar.this, android.R.layout.simple_list_item_1, opposicion);
                spposicion.setAdapter(adapterposicion);

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //////////////////////////////////////
        //////////////////////////////////////////////////////
        ///////////////////////EDITABLE///////////////////////////////
    }
}