package com.example.senderismo;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity4 extends AppCompatActivity {

    Toolbar toolbar;

    private RecyclerView recyclerView;
    private RutasAdapter rutasAdapter;
    private List<Ruta> listaRutas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toolbar = findViewById(R.id.mi_toolbar);
        toolbar.setTitle("Buscar Rutas");
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);

        crearDatosDeEjemplo();

        rutasAdapter = new RutasAdapter(listaRutas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(rutasAdapter);
    }

    private void crearDatosDeEjemplo() {
        listaRutas = new ArrayList<>();
        listaRutas.add(new Ruta("Laguna de Pucaccocha", "Media", "Trekking hacia una hermosa laguna de aguas turquesas cerca de Huánuco.", R.drawable.ruta));
        listaRutas.add(new Ruta("Zona arqueológica de Kotosh", "Fácil", "Caminata corta para visitar el sitio arqueológico de las Manos Cruzadas.", R.drawable.visita));
        listaRutas.add(new Ruta("Ciclismo a Tomayquichua", "Fácil", "Ruta de ciclismo por carretera hacia el pueblo de Micaela Villegas 'La Perricholi'.", R.drawable.ciclismo));
        listaRutas.add(new Ruta("Trekking a la Bella Durmiente", "Difícil", "Ruta exigente hacia la famosa montaña con forma de mujer dormida en Tingo María.", R.drawable.trekking));
        listaRutas.add(new Ruta("Visita a la Cueva de las Lechuzas", "Fácil", "Paseo ecoturístico para observar guácharos (aves nocturnas) en su hábitat natural.", R.drawable.lechuzas));
        listaRutas.add(new Ruta("Ruta a las Aguas Sulfurosas de Jacintillo", "Media", "Caminata para llegar a pozas naturales de aguas termales con propiedades curativas.", R.drawable.aguas));
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
            Intent i = new Intent(MainActivity4.this, MainActivity3.class);
            startActivity(i);
        }
        if(id == R.id.item2){
            Toast.makeText(this, "Ya esta aqui", Toast.LENGTH_SHORT).show();
        }
        if(id == R.id.item3){
            Intent i = new Intent(MainActivity4.this, MainActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    public void atras1 (View view) {
        Intent i = new Intent(MainActivity4.this, MainActivity2.class);
        startActivity(i);
    }
}
