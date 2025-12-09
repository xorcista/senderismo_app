package com.example.senderismo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainActivity3 extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView imageViewPerfil;
    private TextView textViewNombrePerfil, textViewEmailPerfil;
    private ProgressBar progressBarPerfil;
    private Button btnCerrarSesion, btnAtras;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageReference;
    private Uri imagenUri;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    abrirGaleria();
                } else {
                    Toast.makeText(this, "Permiso denegado para acceder a la galería.", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    imagenUri = result.getData().getData();
                    imageViewPerfil.setImageURI(imagenUri);
                    subirImagenAFirebase();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        toolbar = findViewById(R.id.mi_toolbar);
        imageViewPerfil = findViewById(R.id.imageViewPerfil);
        textViewNombrePerfil = findViewById(R.id.textViewNombrePerfil);
        textViewEmailPerfil = findViewById(R.id.textViewEmailPerfil);
        progressBarPerfil = findViewById(R.id.progressBarPerfil);
        btnCerrarSesion = findViewById(R.id.btn_cerrar_sesion);
        btnAtras = findViewById(R.id.btn_atras);
        toolbar.setTitle("Mi Perfil");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        cargarDatosUsuario();

        imageViewPerfil.setOnClickListener(v -> pedirPermisoYAbriGaleria());
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
        btnAtras.setOnClickListener(v -> onBackPressed());
    }

    private void pedirPermisoYAbriGaleria() {
        String permiso = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permiso) == PackageManager.PERMISSION_GRANTED) {
            abrirGaleria();
        } else {
            requestPermissionLauncher.launch(permiso);
        }
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void subirImagenAFirebase() {
        if (imagenUri == null) return;
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Error: No hay usuario autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBarPerfil.setVisibility(View.VISIBLE);
        final StorageReference fileRef = storageReference.child("profile_images").child(user.getUid() + ".jpg");

        fileRef.putFile(imagenUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    guardarUrlEnFirestore(imageUrl);
                }))
                .addOnFailureListener(e -> {
                    progressBarPerfil.setVisibility(View.GONE);
                    Toast.makeText(MainActivity3.this, "Error al subir la imagen.", Toast.LENGTH_SHORT).show();
                    Log.e("STORAGE_ERROR", "Fallo al subir archivo a Firebase Storage", e);
                });
    }

    private void guardarUrlEnFirestore(String imageUrl) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid())
                .update("profileImageUrl", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    progressBarPerfil.setVisibility(View.GONE);
                    Toast.makeText(MainActivity3.this, "Foto de perfil actualizada.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBarPerfil.setVisibility(View.GONE);
                    Toast.makeText(MainActivity3.this, "Error al guardar la URL de la imagen.", Toast.LENGTH_SHORT).show();
                    Log.e("FIRESTORE_ERROR", "Fallo al guardar URL: ", e);
                });
    }

    private void cargarDatosUsuario() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            progressBarPerfil.setVisibility(View.VISIBLE);
            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        progressBarPerfil.setVisibility(View.GONE);
                        if (documentSnapshot.exists()) {
                            String nombre = documentSnapshot.getString("nombre");
                            String apellido = documentSnapshot.getString("apellido");
                            String email = documentSnapshot.getString("email");
                            String imageUrl = documentSnapshot.getString("profileImageUrl");

                            textViewNombrePerfil.setText(nombre + " " + apellido);
                            textViewEmailPerfil.setText(email);

                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Glide.with(this)
                                        .load(imageUrl)
                                        .circleCrop() // Para que sea circular
                                        .placeholder(android.R.drawable.ic_menu_myplaces)
                                        .error(android.R.drawable.ic_menu_myplaces)
                                        .into(imageViewPerfil);
                            } else {
                                imageViewPerfil.setImageResource(android.R.drawable.ic_menu_myplaces);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressBarPerfil.setVisibility(View.GONE);
                        Toast.makeText(this, "Error al cargar datos de perfil.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            irAlLogin();
        }
    }

    private void cerrarSesion() {
        mAuth.signOut();
        Toast.makeText(this, "Sesión cerrada.", Toast.LENGTH_SHORT).show();
        irAlLogin();
    }

    private void irAlLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.menu_perfil) {
            Toast.makeText(this, "Ya estás en tu Perfil", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_planificar_ruta) {
            startActivity(new Intent(this, MainActivity2.class));
            return true;
        } else if (id == R.id.menu_mis_rutas) {
            startActivity(new Intent(this, MainActivity4.class));
            return true;
        } else if (id == R.id.menu_cerrar_sesion) {
            cerrarSesion();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
