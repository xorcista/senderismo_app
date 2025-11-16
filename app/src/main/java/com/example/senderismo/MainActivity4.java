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

public class MainActivity4 extends AppCompatActivity {

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main4);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toolbar = findViewById(R.id.mi_toolbar);
        toolbar.setTitle("Buscar");
        setSupportActionBar(toolbar);

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

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