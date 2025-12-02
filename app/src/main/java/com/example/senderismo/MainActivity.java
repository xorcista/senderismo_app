package com.example.senderismo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    EditText usuario, contraseña;
    private ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("login")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document: task.getResult()){
                                Log.i("Contraseña",document.get("contraseña").toString());
                                Log.i("Usuario",document.get("usuario").toString());
                                                            }
                        }else{
                            Toast.makeText(MainActivity.this, "Error al traer los documentos: "+task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });


        usuario = findViewById(R.id.etuser);
        contraseña = findViewById(R.id.etpass);


        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        if(data !=null){
                            String usuario = data.getStringExtra("resultado");

                        }
                    }
                }
        );

    }


    public void entrar(View view) {

        String user = usuario.getText().toString();
        String pass = contraseña.getText().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> loginData = new HashMap<>();
        loginData.put("usuario", user);
        loginData.put("contraseña", pass);

        db.collection("login")
                .add(loginData)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(MainActivity.this, "Datos guardados en Firestore", Toast.LENGTH_SHORT).show()
                ).addOnFailureListener(e ->
                        Toast.makeText(MainActivity.this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
        Intent i = new Intent(MainActivity.this, MainActivity2.class);
        i.putExtra("usuario", usuario.getText().toString());
        i.putExtra( "contraseña", contraseña.getText().toString());
        launcher.launch(i);


    }
}