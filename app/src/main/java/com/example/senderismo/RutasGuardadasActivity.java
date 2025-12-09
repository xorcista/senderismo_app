package com.example.senderismo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
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

public class RutasGuardadasActivity extends AppCompatActivity implements RutasAdapter.OnRutaInteractionListener {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private RutasAdapter adapter;
    private List<Ruta> listaDeRutas;
    private ProgressBar progressBar;
    private TextView tvNoRutas;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rutas_guardadas);
        toolbar = findViewById(R.id.mi_toolbar_rutas);
        toolbar.setTitle("Mis Rutas");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        recyclerView = findViewById(R.id.recyclerViewRutas);
        progressBar = findViewById(R.id.progressBarRutas);
        tvNoRutas = findViewById(R.id.tvNoRutas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listaDeRutas = new ArrayList<>();
        adapter = new RutasAdapter(listaDeRutas, this);
        recyclerView.setAdapter(adapter);
        cargarRutasGuardadas();
    }

    private void cargarRutasGuardadas() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvNoRutas.setVisibility(View.GONE);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesión para ver tus rutas", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            tvNoRutas.setVisibility(View.VISIBLE);
            tvNoRutas.setText("Inicia sesión para ver tus rutas.");
            return;
        }

        db.collection("rutasGuardadas")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("favorita", Query.Direction.DESCENDING)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        listaDeRutas.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Ruta ruta = document.toObject(Ruta.class);
                            ruta.setDocumentId(document.getId());
                            listaDeRutas.add(ruta);
                        }

                        if (listaDeRutas.isEmpty()) {
                            tvNoRutas.setVisibility(View.VISIBLE);
                            tvNoRutas.setText("Aún no tienes rutas guardadas.");
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("FIRESTORE_ERROR", "Error al cargar las rutas: ", task.getException());
                        Toast.makeText(this, "Error al cargar las rutas. Revisa el Logcat.", Toast.LENGTH_LONG).show();
                    }
                });
    }
    @Override
    public void onRutaClick(Ruta ruta) {
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
                    listaDeRutas.sort((r1, r2) -> Boolean.compare(r2.isFavorita(), r1.isFavorita()));
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, nuevoEstadoFavorito ? "Añadida a favoritos" : "Quitada de favoritos", Toast.LENGTH_SHORT).show();
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
                                adapter.notifyItemRemoved(position);
                                adapter.notifyItemRangeChanged(position, listaDeRutas.size());
                                Toast.makeText(this, "Ruta eliminada", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error al eliminar la ruta", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onCompartirClick(Ruta ruta) {
        String travelMode = "walking";
        if (ruta.getTipoDeRuta() != null && ruta.getTipoDeRuta().equalsIgnoreCase("Ciclismo")) {
            travelMode = "bicycling";
        }

        String googleMapsUrl = "https://www.google.com/maps/dir/?api=1&origin=" +
                ruta.getOrigenLat() + "," + ruta.getOrigenLng() +
                "&destination=" + ruta.getDestinoLat() + "," + ruta.getDestinoLng() +
                "&travelmode=" + travelMode;

        String textoCompartir = "¡Mira esta ruta que planifiqué en Senderismo App!\n\n" +
                "Ruta: " + ruta.getNombreRuta() + "\n" +
                "Tipo: " + ruta.getTipoDeRuta() + "\n" +
                "Dificultad: " + ruta.getDificultad() + "\n\n" +
                "Puedes ver la ruta en Google Maps aquí:\n" +
                googleMapsUrl;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Ruta de Senderismo: " + ruta.getNombreRuta());
        shareIntent.putExtra(Intent.EXTRA_TEXT, textoCompartir);

        startActivity(Intent.createChooser(shareIntent, "Compartir ruta vía"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

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
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
