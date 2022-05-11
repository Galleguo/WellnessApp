package com.example.wellnessapp;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import id.zelory.compressor.Compressor;

public class EditarActivity extends AppCompatActivity {

    private EditText Apellido, Nombre, Altura, Peso;
    FirebaseFirestore db;
    private StorageReference mStorage;
    String email, cemail, posicion, club, apellido, nombre,imgUrl;
    private ProgressDialog progressDialog;

    private ImageView imagen;
    private Uri imagenUri;
    private byte [] thumb_byte;

    private int REQUEST_TOMAR_FOTO = 101;
    private int REQUEST_SELEC_IMAGEN = 200;
    private int REQUEST_EXTORAGE_CAMERA = 300;

    Bitmap thumb_bitmap = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar);

        //Inicializo los edit text
        Apellido = findViewById(R.id.TxtApellido);
        Nombre = findViewById(R.id.TxtNombre);
        Altura = findViewById(R.id.TxtAltura);
        Peso = findViewById(R.id.TxtPeso);
        imagen = findViewById(R.id.profile_image);


        //inicializamos el objeto firebaseAuth
        db = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();

        Intent caller = getIntent();
        email=caller.getStringExtra("Email");
        cemail = caller.getStringExtra("CEmail");

        Log.d("Setup","Antes de entrar en Setup con: "+cemail+" cuenta de "+email);

        progressDialog = new ProgressDialog(EditarActivity.this);
        progressDialog.setMessage("Cargando...");
        progressDialog.show();
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
                            ////////EDITABLE
                            club = (String) task.getResult().getData().get("Club");
                            posicion = (String) task.getResult().getData().get("Posicion");
                            ////////////
                            db.collection("Clientes")
                                    .document(cemail)
                                    .collection("Usuarios")
                                    .document(club)
                                    .collection(posicion)
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
                                                    imgUrl = documento.getString("UrlPerfil");

                                                    Apellido.setText(apellido);
                                                    Nombre.setText(nombre);

                                                    Long altura = (Long) documento.getData().get("Altura");
                                                    Long peso = (Long) documento.getData().get("Peso");

                                                    Log.d("Setup","Altura: "+altura+" Peso: "+peso);

                                                    Log.d("Usuario","Nombre "+nombre+" "+apellido+". Altura: "+altura+". Peso: "+peso);
                                                    Altura.setText(altura.toString());
                                                    Peso.setText(peso.toString());

                                                    Glide.with(EditarActivity.this)
                                                            .load(imgUrl)
                                                            .into(imagen);

                                                    progressDialog.dismiss();

                                                }
                                            }
                                        }
                                    });

                        }
                    }
                });
        Log.d("Setup","Entro en Setup");


    }

    public void actualizar(View view) {
        progressDialog.show();
        final Map<String, Object> registro = new HashMap<>();
        if ((!Apellido.getText().toString().isEmpty() && !Nombre.getText().toString().isEmpty() && !Altura.getText().toString().isEmpty() && !Peso.getText().toString().isEmpty()) || imagenUri!=null){
            if ((!Apellido.getText().toString().isEmpty() && !Nombre.getText().toString().isEmpty() && !Altura.getText().toString().isEmpty() && !Peso.getText().toString().isEmpty()) && imagenUri!=null){
                //Se actualiza foto si todos los cuadros estan rellenos.
                actualizarFotoPerfil(registro);
            } else {
                //Actualizo solo data personal
                registro.put("UrlPerfil",null);
                registroPersonal(registro);
            }
        }  else {
            Toast.makeText(this, "Completar información", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }

    }

    private void actualizarFotoPerfil(final Map<String, Object> registro) {

        final StorageReference filePath = mStorage.child("Fotos de perfil").child(cemail).child(email);

        if (imgUrl!="") {
            eliminarFotoAntigua(filePath,registro);
        } else {
            cargarFotoNueva(filePath, registro);
        }

    }

    private void cargarFotoNueva(final StorageReference filePath, final Map<String, Object> registro) {
        UploadTask uploadTask = filePath.putBytes(thumb_byte);

        //Genero URL de la imagen subida a storage
        Task <Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()){
                    throw Objects.requireNonNull(task.getException());
                }
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                Uri downloaduri = task.getResult();//url de la foto
                registro.put("UrlPerfil",downloaduri.toString());
                registroPersonal(registro);
                //asigno la url al map
            }
        });
    }

    private void eliminarFotoAntigua(final StorageReference filePath, final Map<String, Object> registro) {
        filePath.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Actualizar Nueva Foto de Perfil
                cargarFotoNueva(filePath,registro);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Ocurrió un erro
            }
        });
    }

    private void registroPersonal(Map<String, Object> registro) {
        registro.put("Apellido", Apellido.getText().toString().trim());
        registro.put("Nombre", Nombre.getText().toString().trim());
        registro.put("Altura",Integer.valueOf(Altura.getText().toString()));
        registro.put("Peso",Integer.valueOf(Peso.getText().toString()));
        registro.put("Posicion",posicion);
        registro.put("Club",club);


        db.collection("Clientes")
                .document(cemail)
                .collection("Usuarios")
                .document(club)
                .collection(posicion)
                .document(email)
                .set(registro);

        Log.d("Actualizar","Se realizó el registro");
        progressDialog.dismiss();

        Intent goMenu = new Intent(EditarActivity.this, MenuActivity.class);
        goMenu.putExtra("Email",email);
        startActivity(goMenu);
        finish();
    }

    public void Go_atras(View view) {
        Intent goMenu = new Intent(EditarActivity.this, MenuActivity.class);
        goMenu.putExtra("Email",email);
        startActivity(goMenu);
        finish();
    }

    public void Cargar_foto(View view) { //https://github.com/chenaoh/curso-android-codejavu/blob/master/ObtenerImagenesFotoGaleria/app/src/main/java/co/quindio/sena/obtenerimagenesfotogaleria/MainActivity.java
        cargarImagen();
    }

    private void cargarImagen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ActivityCompat.checkSelfPermission(EditarActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.checkSelfPermission(EditarActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                    CropImage.startPickImageActivity(EditarActivity.this); //Me abre un menú lindo para seleccionar la imagen
                } else {
                    // tiene camara pero no almacenaje autorizado
                    ActivityCompat.requestPermissions(EditarActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_SELEC_IMAGEN);
                }
            } else {
                if (ActivityCompat.checkSelfPermission(EditarActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                    //No tiene camara pero si almacenaje
                    ActivityCompat.requestPermissions(EditarActivity.this, new String[]{Manifest.permission.CAMERA},REQUEST_TOMAR_FOTO);
            } else {
                    //No tiene ninguna de las dos
                    ActivityCompat.requestPermissions(EditarActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA},REQUEST_EXTORAGE_CAMERA);
                }

            }
        } else {
            CropImage.startPickImageActivity(EditarActivity.this); //Me abre un menú lindo para seleccionar la imagen
        }
    }


        /*final AlertDialog.Builder alertOpciones=new AlertDialog.Builder(EditarActivity.this);
        alertOpciones.setTitle("Seleccione una Opción");
        alertOpciones.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (opciones[i].equals("Tomar Foto")){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//verifico que sea mayor a la version 23 xq en el caso que lo sea tengo que verificar los permisos
                        if (ActivityCompat.checkSelfPermission(EditarActivity.this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
                            tomarFoto();
                        } else {
                            ActivityCompat.requestPermissions(EditarActivity.this, new String[]{Manifest.permission.CAMERA},REQUEST_TOMAR_FOTO);
                        }
                    } else {
                        tomarFoto();
                    }

                } else {
                    if (opciones[i].equals("Cargar Imagen")){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                            if (ActivityCompat.checkSelfPermission(EditarActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                                seleccionarImagen();
                            } else {
                                ActivityCompat.requestPermissions(EditarActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_SELEC_IMAGEN);
                            }
                        } else {
                            seleccionarImagen();
                        }
                    }else{
                        dialogInterface.dismiss();
                    }
                }
            }
        });
        alertOpciones.show();
    }*/

    @Override//codigo para verificar que se dió el permiso de uso de cámara
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_TOMAR_FOTO){
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                CropImage.startPickImageActivity(EditarActivity.this); //Me abre un menú lindo para seleccionar la imagen
            } else {
                Toast.makeText(EditarActivity.this, "Se requiere habilitar los permisos", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_SELEC_IMAGEN){
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                CropImage.startPickImageActivity(EditarActivity.this); //Me abre un menú lindo para seleccionar la imagen
            } else {
                Toast.makeText(EditarActivity.this, "Se requiere habilitar los permisos", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_EXTORAGE_CAMERA){
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                CropImage.startPickImageActivity(EditarActivity.this); //Me abre un menú lindo para seleccionar la imagen
            } else {
                Toast.makeText(EditarActivity.this, "Se requiere habilitar los permisos", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /*public void tomarFoto() {
        Intent camaraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (camaraIntent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(camaraIntent,TOMAR_FOTO);
        }
        /*String nombreImagen = "";
        File fileImagen = new File(Environment.getExternalStorageDirectory(), RUTA_IMAGEN);
        boolean isCreada = fileImagen.exists();

        if(isCreada == false) {
            isCreada = fileImagen.mkdirs();
        }

        if(isCreada == true) {
            nombreImagen = (System.currentTimeMillis() / 1000) + ".jpg";
        }

        path = Environment.getExternalStorageDirectory()+File.separator+RUTA_IMAGEN+File.separator+nombreImagen;
        File imagen = new File(path);

        Intent intent = null;
        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String authorities = this.getPackageName()+".provider";
            Uri imageUri = FileProvider.getUriForFile(this, authorities, imagen);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imagen));
        }

        startActivityForResult(intent, TOMAR_FOTO);*/


    /*public void seleccionarImagen() {
        Intent galeria = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        if (galeria.resolveActivity(getPackageManager())!=null){
            startActivityForResult(galeria.createChooser(galeria,"Seleccione la aplicación"), SELEC_IMAGEN);
        }
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode==Activity.RESULT_OK){
            Uri uri = CropImage.getPickImageResultUri(this,data);

            //Recortar imagen
            CropImage.activity(uri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setRequestedSize(683,1024)
                    .setAspectRatio(1,1)
                    .start(EditarActivity.this);
            }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imagenUri = result.getUri();

                imagen.setImageURI(imagenUri);

                File url = new File (imagenUri.getPath());

                //Picasso.with(this).load(url).into(imagen);

                //se comprime imagen

                try {
                    thumb_bitmap = new Compressor(EditarActivity.this)
                            .setMaxWidth(1024)
                            .setMaxHeight(683)
                            .setQuality(360)
                            .compressToBitmap(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,90,byteArrayOutputStream);
                thumb_byte = new byte[0];
                thumb_byte = byteArrayOutputStream.toByteArray();

                //fin de compresor
            }
        }
    }

}