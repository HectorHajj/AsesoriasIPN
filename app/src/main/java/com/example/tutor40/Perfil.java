package com.example.tutor40;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Perfil extends AppCompatActivity {

    FirebaseFirestore db;

    EditText Nombre;
    EditText ApellidoPaterno;
    EditText ApellidoMaterno;

    public void cambiarImagen(View view){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        Nombre = findViewById(R.id.editTextNombre);
        ApellidoPaterno = findViewById(R.id.editTextApellidoPaterno);
        ApellidoMaterno = findViewById(R.id.editTextApellidoMaterno);

        db = FirebaseFirestore.getInstance();

        FirebaseUser loggedUser = FirebaseAuth.getInstance().getCurrentUser();

        //Verificar Rol de usuario y mandarlo a pantalla correspondiente
        DocumentReference user = db.collection("users").document(loggedUser.getUid());
        user.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Nombre.setText(documentSnapshot.getData().get("Nombre").toString());
                ApellidoPaterno.setText(documentSnapshot.getData().get("ApellidoPaterno").toString());
                ApellidoMaterno.setText(documentSnapshot.getData().get("ApellidoMaterno").toString());
            }
        });
    }
}
