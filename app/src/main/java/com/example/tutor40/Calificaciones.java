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
        Log.i("UserID AQUIIIIIIIIII",UserID);
        R1 = (RatingBar) findViewById (R.id. ratingBar );  // iniciar una barra de calificación
        db = FirebaseFirestore.getInstance();
    }



    public void Continuar(View view){
        Calificacion = R1.getRating();
        if (Calificacion <=0.0f){
            Toast.makeText(this, "EVALUA LA SESION", Toast.LENGTH_SHORT).show();
        }
        else
            if (CurrentUserRole.equals("Ck5Tnzr0ipmAzKpQpTDX")) {
                Intent intent = new Intent(getApplicationContext(), AsesorMain.class);
                  startActivity(intent);
            }

            else if(CurrentUserRole.equals("I60WiSHvFyzJqUT0IU20")) {
                db.collection("Calificaciones").document(UserID)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.getResult().exists()) {
                                    float Cal = Float.parseFloat(task.getResult().getData().get("Calificacion").toString());
                                    int NumCal = Integer.parseInt(task.getResult().getData().get("NumeroCalificaciones").toString());
                                    float Promedio = (NumCal * Cal + Calificacion) / (NumCal + 1);
                                    Calificacion = Promedio;
                                    NumCal = NumCal + 1;

                                    final Map<String, Object> data = new HashMap<>();
                                    data.put("Calificacion", Promedio);
                                    data.put("NumeroCalificaciones", NumCal);

                                    db.collection("Calificaciones").document(UserID)
                                            .set(data)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Intent intent = new Intent(getApplicationContext(), AlumnoMain.class);
                                                        startActivity(intent);
                                                    }
                                                }
                                            });
                                } else {
                                    Map<String, Object> dat = new HashMap<>();
                                    dat.put("Calificacion", Calificacion);
                                    dat.put("NumeroCalificaciones", 1);

                                    db.collection("Calificaciones").document(UserID)
                                            .set(dat)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Intent intent = new Intent(getApplicationContext(), AlumnoMain.class);
                                                    startActivity(intent);
                                                }
                                            });
                                }
                            }
                        });
            }
    }
}
//    //db.collection("Calificaciones").document(UserID)
//                     //   .get()
//                      //  .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                       //     @Override
//                         //   public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                            //    if(task.getResult().exists()){
//                              //      float Cal = Float.parseFloat(task.getResult().getData().get("Calificacion").toString());
//                              //      int NumCal = Integer.parseInt(task.getResult().getData().get("NumeroCalificaciones").toString());
//                               //     float Promedio = (NumCal*Cal+Calificacion)/(NumCal+1);
//                               //     Calificacion = Promedio;
//                                //    NumCal = NumCal + 1;
//
//                                  //  final Map<String, Object> data = new HashMap<>();
//                                  //  data.put("Calificacion",Promedio);
//                                  //  data.put("NumeroCalificaciones", NumCal);
//
//                                    //db.collection("Calificaciones").document(UserID)
//                                    //        .set(data)
//                                     //       .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                     //           @Override
//                                      //          public void onComplete(@NonNull Task<Void> task) {
///
//                                                    if (task.isSuccessful()) {
//                                                        Intent intent = new Intent(getApplicationContext(), AsesorMain.class);
//                                                        startActivity(intent);
//                                                    }
//                                                }
//                                            });
//                                } else {
//                                    Map<String, Object> data = new HashMap<>();
//                                    data.put("Calificacion", Calificacion);
//                                    data.put("NumeroCalificaciones",1);
//
//                                    db.collection("Calificaciones").document(UserID)
//                                            .set(data)
//                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                @Override
//                                                public void onSuccess(Void aVoid) {
//                                                    Intent intent = new Intent(getApplicationContext(), AsesorMain.class);
//                                                    startActivity(intent);
//                                                }
//                                            });
//                                }
//                            }
//                        });
//            }