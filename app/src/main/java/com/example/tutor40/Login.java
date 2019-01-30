package com.example.tutor40;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    FirebaseAuth mAuth;

    EditText Email;
    EditText Password;

    FirebaseFirestore db;

    public void login(View view){
        //TODO: hacer ela validacion con firebase de usuario y contrasenia
        mAuth.signInWithEmailAndPassword(Email.getText().toString(), Password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = mAuth.getCurrentUser();

                            //Verificar Rol de usuario y mandarlo a pantalla correspondiente
                            DocumentReference docRef = db.collection("users").document(user.getUid());
                            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(documentSnapshot.getData().get("RolID").toString().equals("Ck5Tnzr0ipmAzKpQpTDX")) {
                                        Intent intent = new Intent(getApplicationContext(), TutorMain.class);

                                        startActivity(intent);
                                    } else if(documentSnapshot.getData().get("RolID").toString().equals("I60WiSHvFyzJqUT0IU20")) {
                                        Intent intent = new Intent(getApplicationContext(), AlumnoMain.class);

                                        startActivity(intent);
                                    }
                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.i("Login", "Oye, mano, fijate que fallo tu intento");
                        }
                    }
                });
    }

    public void registrarse(View view){
        Intent intent = new Intent(getApplicationContext(), Registro.class);

        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Email = findViewById(R.id.editTextEmail);
        Password = findViewById(R.id.editTextPassword);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }
}
