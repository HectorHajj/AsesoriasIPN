package com.example.tutor40;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;

public class Calificaciones extends AppCompatActivity {
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calificaciones);
    }

    public void Continuar(View view){
        Intent intent = new Intent(getApplicationContext(), AsesorMain.class);
        startActivity(intent);
    }
}
