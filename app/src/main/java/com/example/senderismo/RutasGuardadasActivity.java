package com.example.senderismo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

public class RutasGuardadasActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private RutasAdapter adapter;
    private List<Ruta> listaRutas;
    private ProgressBar progressBar;
    private TextView tvNoRutas;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rutas_guardadas);

        // Configurar la barra de herramientas
        toolbar = findViewById(R.id.mi_toolbar_rutas);
        toolbar.setTitle("Mis Rutas");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recyclerViewRutas);
        progressBar = findViewById(R.id.progressBarRutas);
        tvNoRutas = findViewById(R.id.tvNoRutas);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listaRutas = new ArrayList<>();
        adapter = new RutasAdapter(listaRutas);
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
            return;
        }

        // Consulta a Firestore para obtener las rutas del usuario actual, ordenadas por fecha
        db.collection("rutasGuardadas")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        listaRutas.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Ruta ruta = document.toObject(Ruta.class);
                            ruta.setId(document.getId());
                            listaRutas.add(ruta);
                        }

                        if (listaRutas.isEmpty()) {
                            tvNoRutas.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged(); // Notifica al adaptador que los datos cambiaron
                        }
                    } else {
                        Toast.makeText(this, "Error al cargar las rutas.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
