package com.example.senderismo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity2 extends AppCompatActivity implements OnMapReadyCallback {

    private Toolbar toolbar;
    private TextView tvSaludo;
    private Button btnBuscarRuta, btnGuardarRuta;
    private LinearLayout routeDetailsLayout;
    private TextView tvDuracion, tvDistancia;
    private ImageButton btnModoCaminar, btnModoBicicleta;
    private MapView mapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng origenLatLng;
    private LatLng destinoLatLng;
    private Marker markerDestino;
    private Polyline polylineRuta;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private PlacesClient placesClient;
    private DirectionsApiService directionsApiService;
    private String polylineRutaCodificada;
    private String modoTransporte = "walking";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        toolbar = findViewById(R.id.mi_toolbar);
        toolbar.setTitle("Planificar Ruta");
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        tvSaludo = findViewById(R.id.etlogin);
        btnBuscarRuta = findViewById(R.id.btnBuscarRuta);
        btnGuardarRuta = findViewById(R.id.btnGuardarRuta);
        routeDetailsLayout = findViewById(R.id.routeDetailsLayout);
        tvDuracion = findViewById(R.id.tvDuracion);
        tvDistancia = findViewById(R.id.tvDistancia);
        btnModoCaminar = findViewById(R.id.btnModoCaminar);
        btnModoBicicleta = findViewById(R.id.btnModoBicicleta);

        cargarDatosUsuario();

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }
        placesClient = Places.createClient(this);
        configurarAutocomplete();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        directionsApiService = retrofit.create(DirectionsApiService.class);

        btnBuscarRuta.setOnClickListener(v -> {
            if (origenLatLng != null && destinoLatLng != null) {
                buscarYDibujarRuta();
            } else {
                Toast.makeText(this, "Por favor, selecciona un destino", Toast.LENGTH_SHORT).show();
            }
        });

        btnGuardarRuta.setOnClickListener(v -> {
            if (destinoLatLng != null && polylineRutaCodificada != null && !polylineRutaCodificada.isEmpty()) {
                mostrarDialogoGuardarRuta();
            } else {
                Toast.makeText(this, "Primero debes buscar una ruta para poder guardarla", Toast.LENGTH_LONG).show();
            }
        });

        btnModoCaminar.setBackgroundColor(ContextCompat.getColor(this, R.color.selected_button_background));

        btnModoCaminar.setOnClickListener(v -> {
            modoTransporte = "walking";
            Toast.makeText(this, "Modo Caminar seleccionado", Toast.LENGTH_SHORT).show();
            btnModoCaminar.setBackgroundColor(ContextCompat.getColor(this, R.color.selected_button_background));
            btnModoBicicleta.setBackgroundColor(Color.TRANSPARENT);
        });

        btnModoBicicleta.setOnClickListener(v -> {
            modoTransporte = "bicycling";
            Toast.makeText(this, "Modo Bicicleta seleccionado", Toast.LENGTH_SHORT).show();
            btnModoBicicleta.setBackgroundColor(ContextCompat.getColor(this, R.color.selected_button_background));
            btnModoCaminar.setBackgroundColor(Color.TRANSPARENT);
        });
    }

    private void buscarYDibujarRuta() {
        if (polylineRuta != null) polylineRuta.remove();

        routeDetailsLayout.setVisibility(View.GONE);
        btnGuardarRuta.setVisibility(View.GONE);

        String originStr = origenLatLng.latitude + "," + origenLatLng.longitude;
        String destinationStr = destinoLatLng.latitude + "," + destinoLatLng.longitude;
        String apiKey = getString(R.string.google_maps_key);

        String modoSeleccionadoTexto = modoTransporte.equals("walking") ? "a pie" : "en bicicleta";
        Toast.makeText(this, "Mostrando ruta " + modoSeleccionadoTexto + "...", Toast.LENGTH_SHORT).show();

        directionsApiService.getDirections(originStr, destinationStr, modoTransporte, apiKey).enqueue(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<DirectionsResponse> call, @NonNull Response<DirectionsResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().getRoutes().isEmpty()) {
                    Log.d("DIRECTIONS_API", "Ruta encontrada con el modo: " + modoTransporte);
                    dibujarRutaYMostrarDetalles(response.body());

                } else if (modoTransporte.equals("bicycling")) {
                    Log.d("DIRECTIONS_API", "Ruta en bicicleta");
                    buscarRutaAlternativa("driving");

                } else {
                    Log.d("DIRECTIONS_API", "No se encontró ninguna ruta para el modo: " + modoTransporte);
                    Toast.makeText(MainActivity2.this, "No se pudo encontrar una ruta a pie", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<DirectionsResponse> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity2.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void dibujarRutaYMostrarDetalles(DirectionsResponse body) {
        if (polylineRuta != null) {
            polylineRuta.remove();
        }
        DirectionsResponse.Route route = body.getRoutes().get(0);
        polylineRutaCodificada = route.getOverviewPolyline().getPoints();

        if (!route.getLegs().isEmpty()) {
            DirectionsResponse.Leg leg = route.getLegs().get(0);
            String distancia = leg.getDistance().getText();
            String duracion = leg.getDuration().getText();

            tvDistancia.setText("Distancia: " + distancia);
            tvDuracion.setText("Duración: " + duracion);
            routeDetailsLayout.setVisibility(View.VISIBLE);
        }

        List<LatLng> points = decodePoly(polylineRutaCodificada);
        if (!points.isEmpty()) {
            PolylineOptions polylineOptions = new PolylineOptions().addAll(points).color(0xFF007BFF).width(12);
            polylineRuta = googleMap.addPolyline(polylineOptions);
            btnGuardarRuta.setVisibility(View.VISIBLE);
        }
    }

    private void buscarRutaAlternativa(String modoAlternativo) {
        String originStr = origenLatLng.latitude + "," + origenLatLng.longitude;
        String destinationStr = destinoLatLng.latitude + "," + destinoLatLng.longitude;
        String apiKey = getString(R.string.google_maps_key);

        directionsApiService.getDirections(originStr, destinationStr, modoAlternativo, apiKey).enqueue(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<DirectionsResponse> call, @NonNull Response<DirectionsResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().getRoutes().isEmpty()) {
                    Log.d("DIRECTIONS_API", "Ruta alternativa encontrada con modo: " + modoAlternativo);
                    dibujarRutaYMostrarDetalles(response.body());
                } else {
                    Log.d("DIRECTIONS_API", "El reintento con modo " + modoAlternativo + " también falló.");
                    Toast.makeText(MainActivity2.this, "No se pudo encontrar ninguna ruta alternativa", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<DirectionsResponse> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity2.this, "Error de red en el reintento: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            obtenerMiUbicacion();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private void configurarAutocomplete() {
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));
            autocompleteFragment.setHint("Elige un destino");
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    destinoLatLng = place.getLatLng();
                    if (googleMap != null && destinoLatLng != null) {
                        if (markerDestino != null) markerDestino.remove();
                        markerDestino = googleMap.addMarker(new MarkerOptions().position(destinoLatLng).title(place.getName()));
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinoLatLng, 15f));
                    }
                }

                @Override
                public void onError(@NonNull Status status) {
                    Toast.makeText(MainActivity2.this, "Error al buscar lugar", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void mostrarDialogoGuardarRuta() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_guardar_ruta, null);
        final EditText etNombreRuta = dialogView.findViewById(R.id.etNombreRuta);
        final EditText etDescripcionRuta = dialogView.findViewById(R.id.etDescripcionRuta);
        final Spinner spinnerDificultad = dialogView.findViewById(R.id.spinnerDificultad);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.niveles_dificultad, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDificultad.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombreRuta = etNombreRuta.getText().toString().trim();
                    if (TextUtils.isEmpty(nombreRuta)) {
                        Toast.makeText(this, "El nombre de la ruta es obligatorio", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String descripcion = etDescripcionRuta.getText().toString().trim();
                    String dificultad = spinnerDificultad.getSelectedItem().toString();
                    guardarRutaEnFirestore(nombreRuta, descripcion, dificultad);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarRutaEnFirestore(String nombre, String descripcion, String dificultad) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        Map<String, Object> rutaData = new HashMap<>();
        rutaData.put("userId", currentUser.getUid());
        rutaData.put("nombreRuta", nombre);
        rutaData.put("descripcion", descripcion);
        rutaData.put("dificultad", dificultad);
        rutaData.put("origenLat", origenLatLng.latitude);
        rutaData.put("origenLng", origenLatLng.longitude);
        rutaData.put("destinoLat", destinoLatLng.latitude);
        rutaData.put("destinoLng", destinoLatLng.longitude);
        rutaData.put("polyline", polylineRutaCodificada);
        rutaData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("rutasGuardadas").add(rutaData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(MainActivity2.this, "Ruta guardada con éxito", Toast.LENGTH_SHORT).show();
                    if (polylineRuta != null) polylineRuta.remove();
                    if (markerDestino != null) markerDestino.remove();
                    btnGuardarRuta.setVisibility(View.GONE);
                    routeDetailsLayout.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity2.this, "Error al guardar la ruta", Toast.LENGTH_SHORT).show());
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length(), lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            lat += ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            lng += ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            poly.add(new LatLng(((double) lat / 1E5), ((double) lng / 1E5)));
        }
        return poly;
    }

    private void cargarDatosUsuario() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            DocumentReference docRef = db.collection("users").document(currentUser.getUid());
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String nombre = documentSnapshot.getString("nombre");
                    tvSaludo.setText("Bienvenido, " + nombre);
                }
            });
        } else {
            irAlLogin();
        }
    }

    private void obtenerMiUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    origenLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origenLatLng, 15f));
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onMapReady(googleMap);
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
        }
    }

    private void irAlLogin() {
        Intent i = new Intent(MainActivity2.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
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
        if (id == R.id.item1) {
            startActivity(new Intent(this, MainActivity3.class));
            return true;
        } else if (id == R.id.item2) {
            startActivity(new Intent(this, MainActivity4.class));
            return true;
        } else if (id == R.id.item3) {
            mAuth.signOut();
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
            irAlLogin();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override
    protected void onPause() { super.onPause(); if (mapView != null) mapView.onPause(); }
    @Override
    protected void onDestroy() { super.onDestroy(); if (mapView != null) mapView.onDestroy(); }
    @Override
    public void onLowMemory() { super.onLowMemory(); if (mapView != null) mapView.onLowMemory(); }
}
