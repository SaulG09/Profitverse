package com.kekas.fitverse;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Dueno extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    FirebaseUser currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dueno);

        // Inicializar FirebaseAuth y Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Configurar GoogleSignInClient
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("60072103296-bu6ekf4lgv2ar3dn7tl8sm4et4imrfm1.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        String idUser = currentUser.getUid();

        LinearLayout ly = findViewById(R.id.fotoDuenoMenu);
        TextView tv = findViewById(R.id.nombreDueno);

        MaterialCardView gyms = findViewById(R.id.administrarGym);
        gyms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dueno.this,Gimnasios.class);
                intent.putExtra("USER_ID", idUser);
                intent.putExtra("TIPO", "Dueno");
                startActivity(intent);
            }
        });


        MaterialCardView info = findViewById(R.id.botonInformacionDueno);


        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dueno.this,InformacionDueno.class);
                intent.putExtra("USER_ID", idUser);
                intent.putExtra("TIPO", "Dueno");
                startActivity(intent);
            }
        });
        // Listener para cerrar sesión
        MaterialCardView salir = findViewById(R.id.botonCerrarSesionDueno);
        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cerrar sesión de Firebase
                mAuth.signOut();

                // Cerrar sesión de Google
                mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                    // Redirigir a MainActivity después de cerrar sesión
                    Intent intent = new Intent(Dueno.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); // Finaliza esta actividad para evitar regresar con el botón de retroceso
                });
            }
        });


        MaterialCardView editar = findViewById(R.id.editarInfoDueno);
        editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cerrar sesión de Firebase
                Intent intent = new Intent(Dueno.this, DatosDueno.class);
                intent.putExtra("USER_ID", idUser);
                intent.putExtra("TIPO", "Dueno");
                startActivity(intent);
                finish();
            }
        });
        // Cargar nombre y foto de usuario si el ID es válido
        if (idUser != null) {
            obtenerNombreUsuario(idUser, tv, ly);
        }
    }

    private void obtenerNombreUsuario(String userId, TextView nombreEditText, LinearLayout cr) {
        db.collection("Dueno").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String nombre = document.getString("nombre");
                            String foto = document.getString("foto");
                            if (nombre != null) {
                                String nombres[] = nombre.split(" ");
                                nombreEditText.setText("Bienvenido " + nombres[0]);
                            }
                            if (foto != null) {
                                Glide.with(this)
                                        .load(foto)
                                        .into(new CustomTarget<Drawable>() {
                                            @Override
                                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                                cr.setBackground(resource);
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {}
                                        });
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }
}