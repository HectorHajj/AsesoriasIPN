package com.example.tutor40;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.RatingBar;

import com.google.firebase.auth.FirebaseAuth;

public class Calificaciones extends AppCompatActivity {
    FirebaseAuth mAuth;
    RatingBar R1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calificaciones);
        RatingBar R1 = (RatingBar) findViewById (R.id. ratingBar );  // iniciar una barra de calificación
    }

    private void getRating() {
    }

    public void Continuar(View view){
        getRating();

        Intent intent = new Intent(getApplicationContext(), TutorMain.class);
        startActivity(intent);
    }
}