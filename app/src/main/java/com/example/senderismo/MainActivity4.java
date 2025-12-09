package com.example.senderismo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class MainActivity4 extends AppCompatActivity implements RutasAdapter.OnRutaInteractionListener {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private RutasAdapter rutasAdapter;
    private List<Ruta> listaDeRutas;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        toolbar = findViewById(R.id.mi_toolbar);
        toolbar.setTitle("Mis Rutas");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listaDeRutas = new ArrayList<>();
        rutasAdapter = new RutasAdapter(listaDeRutas, this);
        recyclerView.setAdapter(rutasAdapter);

        cargarRutasDesdeFirestore();
    }

    @Override
    public void onRutaClick(Ruta ruta) {
        Log.d("ITEM_CLICK", "Abriendo ruta en el mapa: " + ruta.getNombreRuta());
        Intent intent = new Intent(this, MainActivity2.class);
        intent.putExtra("RUTA_SELECCIONADA", ruta);
        startActivity(intent);
    }

    @Override
    public void onFavoritoClick(Ruta ruta, int position) {
        boolean nuevoEstadoFavorito = !ruta.isFavorita();
        db.collection("rutasGuardadas").document(ruta.getDocumentId())
                .update("favorita", nuevoEstadoFavorito)
                .addOnSuccessListener(aVoid -> {
                    ruta.setFavorita(nuevoEstadoFavorito);
                    rutasAdapter.notifyItemChanged(position);
                    Toast.makeText(this, nuevoEstadoFavorito ? "Ruta añadida a favoritos" : "Ruta quitada de favoritos", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al actualizar favorito", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onEliminarClick(Ruta ruta, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Ruta")
                .setMessage("¿Estás seguro de que quieres eliminar la ruta '" + ruta.getNombreRuta() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    db.collection("rutasGuardadas").document(ruta.getDocumentId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                listaDeRutas.remove(position);
                                rutasAdapter.notifyItemRemoved(position);
                                rutasAdapter.notifyItemRangeChanged(position, listaDeRutas.size());
                                Toast.makeText(this, "Ruta eliminada", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error al eliminar la ruta", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void cargarRutasDesdeFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_LONG).show();
            return;
        }

        db.collection("rutasGuardadas")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaDeRutas.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Ruta ruta = document.toObject(Ruta.class);
                            ruta.setDocumentId(document.getId());
                            listaDeRutas.add(ruta);
                        }
                        rutasAdapter.notifyDataSetChanged();
                        Log.d("FIRESTORE", "Rutas cargadas: " + listaDeRutas.size());
                    } else {
                        Log.e("FIRESTORE_ERROR", "Error al obtener documentos: ", task.getException());
                        Toast.makeText(this, "Error al cargar las rutas.", Toast.LENGTH_SHORT).show();
                    }
                });
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
        }

        if (id == R.id.menu_perfil) {
            startActivity(new Intent(this, MainActivity3.class));
            return true;
        } else if (id == R.id.menu_planificar_ruta) {
            startActivity(new Intent(this, MainActivity2.class));
            return true;
        } else if (id == R.id.menu_mis_rutas) {
            Toast.makeText(this, "Ya estás en Mis Rutas", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_cerrar_sesion) {
            mAuth.signOut();
            Intent i = new Intent(MainActivity4.this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
