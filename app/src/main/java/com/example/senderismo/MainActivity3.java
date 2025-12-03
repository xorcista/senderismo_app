package com.example.senderismo;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// --- INICIO DE IMPORTS AÑADIDOS ---
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity3 extends AppCompatActivity {

    Toolbar toolbar;

    TextView textViewNombrePerfil, textViewEmailPerfil;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main3);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toolbar = findViewById(R.id.mi_toolbar);
        toolbar.setTitle("Mi Perfil");
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        textViewNombrePerfil = findViewById(R.id.textViewNombrePerfil);
        textViewEmailPerfil = findViewById(R.id.textViewEmailPerfil);

        cargarDatosUsuario();
    }

    private void cargarDatosUsuario() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference docRef = db.collection("users").document(userId);

            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String nombre = documentSnapshot.getString("nombre");
                    String apellido = documentSnapshot.getString("apellido");
                    String email = documentSnapshot.getString("email");

                    textViewNombrePerfil.setText(nombre + " " + apellido);
                    textViewEmailPerfil.setText(email);
                } else {
                    Toast.makeText(MainActivity3.this, "No se encontraron datos de perfil.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(MainActivity3.this, "Error al cargar datos.", Toast.LENGTH_SHORT).show();
            });
        } else {
            irAlLogin();
        }
    }

    public void cerrarSesion(View view) {
        mAuth.signOut();
        Toast.makeText(this, "Sesión cerrada.", Toast.LENGTH_SHORT).show();
        irAlLogin();
    }

    private void irAlLogin() {
        Intent intent = new Intent(MainActivity3.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.item1){
            Toast.makeText(this, "Ya estas aqui", Toast.LENGTH_SHORT).show();
        }
        if(id == R.id.item2){
            Intent i = new Intent(MainActivity3.this, MainActivity4.class);
            startActivity(i);
        }
        if(id == R.id.item3){
            mAuth.signOut();
            irAlLogin();
        }
        return super.onOptionsItemSelected(item);
    }

    public void atras (View view) {
        super.onBackPressed();
    }
}
