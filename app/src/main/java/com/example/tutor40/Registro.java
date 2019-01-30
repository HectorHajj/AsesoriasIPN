package com.example.tutor40;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Registro extends AppCompatActivity {

    EditText Email;
    EditText Password;
    EditText Nombre;
    EditText ApellidoPaterno;
    EditText ApellidoMaterno;

    Switch alumnoTutor;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    public void registrarNuevoUsuario(String userId, String Nombre, String ApellidoPaterno, String ApellidoMaterno, Boolean Rol) {
        Map<String, Object> user = new HashMap<>();

        if(Rol){
            //Si Rol es true entonces es un Alumno
            user.put("Nombre", Nombre);
            user.put("ApellidoPaterno", ApellidoPaterno);
            user.put("ApellidoMaterno", ApellidoMaterno);
            user.put("RolID", "I60WiSHvFyzJqUT0IU20");
        } else{
            //Si Rol es true entonces es un Tutor
            user.put("Nombre", Nombre);
            user.put("ApellidoPaterno", ApellidoPaterno);
            user.put("ApellidoMaterno", ApellidoMaterno);
            user.put("RolID", "Ck5Tnzr0ipmAzKpQpTDX");
        }

        // Add a new document with a generated ID
        db.collection("users").document(userId).set(user);


        if(user.get("RolID").toString() == "Ck5Tnzr0ipmAzKpQpTDX") {
            Intent intent = new Intent(getApplicationContext(), TutorMain.class);

            startActivity(intent);
        } else if (user.get("RolID").toString() == "I60WiSHvFyzJqUT0IU20"){
            Intent intent = new Intent(getApplicationContext(), AlumnoMain.class);

            startActivity(intent);
        }

    }

    public void registrar(View view){

        //TODO: Deberiamos poner aqui validaciones de que no halla nada vacio y asi


        //Metodo para crear nuevo usuario
        mAuth.createUserWithEmailAndPassword(Email.getText().toString(), Password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getInstance().getCurrentUser();

                            registrarNuevoUsuario(user.getUid(), Nombre.getText().toString(), ApellidoPaterno.getText().toString(), ApellidoMaterno.getText().toString(), alumnoTutor.isChecked());

                        } else {
                            // If sign in fails, display a message to the user.
                                Log.i("Login", "la cagamos");
                        }

                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        Email = findViewById(R.id.editTextEmail);
        Password = findViewById(R.id.editTextPassword);
        Nombre = findViewById(R.id.editTextNombre);
        ApellidoPaterno = findViewById(R.id.editTextApellidoPaterno);
        ApellidoMaterno = findViewById(R.id.editTextApellidoMaterno);

        alumnoTutor = findViewById(R.id.switch3);

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();
    }
}
