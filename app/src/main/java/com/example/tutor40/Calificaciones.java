package com.example.tutor40;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Calificaciones extends AppCompatActivity {
    FirebaseAuth mAuth;
    String CurrentUserRole,UserID;
    RatingBar R1;
    float Calificacion;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calificaciones);

        CurrentUserRole = getIntent().getStringExtra("RolID");
        UserID = getIntent().getStringExtra("UserID");
        R1 = (RatingBar) findViewById (R.id. ratingBar );  // iniciar una barra de calificación
    }



    public void Continuar(View view){
        Calificacion = R1.getRating();
        if (Calificacion <=0.0f){
            Toast.makeText(this, "PORFAVOR ASIGNA UNA CALIFICACION", Toast.LENGTH_SHORT).show();
        }
        else
            if (CurrentUserRole.equals("Ck5Tnzr0ipmAzKpQpTDX")) {
                DocumentReference docRef2 = db.collection("Calificaciones").document(UserID);
                docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                float Cal = (float) document.getData().get("Calificacion");
                                int NumCal = (int) document.getData().get("NumeroCalificaciones");
                                float Promedio = (NumCal*Cal+Calificacion)/(NumCal+1);
                                Calificacion = Promedio;
                                NumCal = NumCal + 1;

                                final Map<String, Object> data = new HashMap<>();
                                data.put("Calificacion",Promedio);

                                DocumentReference docRef3 = db.collection("Calificaciones").document(UserID);
                                docRef3.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {
                                            Intent intent = new Intent(getApplicationContext(), AsesorMain.class);
                                            startActivity(intent);

                                        }
                                    }
                                });

                            } else {
                                final Map<String, Object> data = new HashMap<>();
                                data.put("Calificacion", Calificacion);
                                data.put("NumeroCalificaciones",1);

                                final DocumentReference docRef = db.collection("Calificaciones").document(UserID);
                                docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        Intent intent = new Intent(getApplicationContext(), AsesorMain.class);
                                        startActivity(intent);
                                    }
                                });
                                    }
                        }
                    }
                });


            }
            else if(CurrentUserRole.equals("I60WiSHvFyzJqUT0IU20")){
                DocumentReference docRef2 = db.collection("Calificaciones").document(UserID);
                docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                float Cal = (float) document.getData().get("Calificacion");
                                int NumCal = (int) document.getData().get("NumeroCalificaciones");
                                float Promedio = (NumCal*Cal+Calificacion)/(NumCal+1);
                                Calificacion = Promedio;
                                NumCal = NumCal + 1;

                                final Map<String, Object> data = new HashMap<>();
                                data.put("Calificacion",Promedio);

                                DocumentReference docRef3 = db.collection("Calificaciones").document(UserID);
                                docRef3.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {
                                            Intent intent = new Intent(getApplicationContext(), AlumnoMain.class);
                                            startActivity(intent);
                                        }
                                    }
                                });

                            } else {
                                final Map<String, Object> data = new HashMap<>();
                                data.put("Calificacion", Calificacion);
                                data.put("NumeroCalificaciones",1);

                                final DocumentReference docRef = db.collection("Calificaciones").document(UserID);
                                docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        Intent intent = new Intent(getApplicationContext(), AlumnoMain.class);
                                        startActivity(intent);
                                    }
                                });
                            }
                        }
                    }
                });
        }
    }
}