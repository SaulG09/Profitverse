package com.kekas.fitverse;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Gyms extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private GridLayout gridLayout;
    String idGym;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gyms);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        gridLayout = findViewById(R.id.gridGymsCoach);

        // Cargar las rutinas en el GridLayout
        cargarRutinas(currentUser.getUid());
    }

    private void cargarRutinas(String userId) {
        Map<String, Object> entrenamiento = new HashMap<>();
        entrenamiento.put("id_coach", userId);
        entrenamiento.put("nombre", "Rutina de Resistencia");
        entrenamiento.put("descripcion", "45 minutos");
        entrenamiento.put("enfoque", "Alta");
        entrenamiento.put("foto", "\"https://firebasestorage.googleapis.com/v0/b/profitverse-bbb73.appspot.com/o/fotos%2Ffoto_1730931375751.jpg?alt=media&token=d4414c3d-a023-46d5-90a3-1734c17a63f4\"\n" +
                "(cadena)\n" +
                "\n");

        // Agregar el nuevo documento
        /*db.collection("Coach").document(userId).collection("Rutinas")
                .add(entrenamiento)
                .addOnSuccessListener(documentReference -> {
                    // Éxito al agregar el documento
                    String idDocumento = documentReference.getId();
                    System.out.println("Documento creado con ID: " + idDocumento);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "No se agrego", Toast.LENGTH_SHORT).show();

                    // Error al agregar el documento
                    e.printStackTrace();
                });
*/
        // Supongamos que "idGym" es el ID del documento que estás buscando en la subcolección "gyms"
        db.collection("Cargo")
                .whereEqualTo("id_coach", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            // Crear y configurar cada card

                            db.collectionGroup("Gyms")
                                    .get()
                                    .addOnCompleteListener(task5 -> {
                                        if (task5.isSuccessful()) {
                                            boolean gymFound = false;
                                            for (QueryDocumentSnapshot document4 : task5.getResult()) {
                                                if (document.getString("id_gym").equals(document4.getId())) {
                                                    // Documento encontrado
                                                    agregarRutinaAlGrid(document4, document.getString("entrada"), document.getString("salida"));
                                                    gymFound = true;
                                                    break;
                                                }
                                            }
                                            if (!gymFound) {
                                                Log.d("No encontrado", "No se encontró ningún documento con el ID especificado.");
                                            }
                                        } else {
                                            Log.e("Error", "Error al buscar en la colección group: ", task.getException());
                                        }
                                    })
                                    .addOnFailureListener(e -> e.printStackTrace());

                        }
                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());
// Realizar una consulta en el grupo de colecciones "gyms" para el documentId



    }

    private void agregarRutinaAlGrid(DocumentSnapshot document, String en, String sa) {
        // Inflar el layout del item_rutina
        MaterialCardView cardView = (MaterialCardView) getLayoutInflater().inflate(R.layout.user_card, gridLayout, false);

        // Referencias a los elementos dentro de la tarjeta
        TextView nombreRutina = cardView.findViewById(R.id.nombreGym);
        TextView descripcion = cardView.findViewById(R.id.ubicacionGym);
        TextView horario = cardView.findViewById(R.id.horarioGym);


        LinearLayout fotoRutina = cardView.findViewById(R.id.fotoGymCoach);
        LatLng latLng = new LatLng(document.getDouble("latitud"), document.getDouble("longitud"));

        // Asignar valores obtenidos de Firestore
        nombreRutina.setText(document.getString("nombre"));
        horario.setText(en + " - " + sa);

        descripcion.setText(getLocationName(latLng.latitude, latLng.longitude));

        // Cargar la imagen de la rutina usando Glide
        String fotoUrl = document.getString("foto");
        if (fotoUrl != null) {
            Glide.with(this)
                    .load(fotoUrl)
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            fotoRutina.setBackground(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        }

        // Añadir la tarjeta al GridLayout
        gridLayout.addView(cardView);
    }

    private String getLocationName(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses == null || addresses.isEmpty()) {
                return "Ubicación desconocida";
            }
            Address addressData = addresses.get(0);
            String address = addressData.getAddressLine(0);
            return address;
        } catch (IOException e) {
            e.printStackTrace();
            return "Ubicación desconocida";
        }
    }
}
