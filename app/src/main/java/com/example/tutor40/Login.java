package com.example.tutor40;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
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

public class Login extends AppCompatActivity
{
    FirebaseAuth mAuth;
    ProgressDialog loadingBar;

    EditText Email;
    EditText Password;

    FirebaseFirestore db;

    public void login(View view)
    {
        if(TextUtils.isEmpty(Email.getText().toString()))
        {
            Toast.makeText(this, "Por favor introduzca un email", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(Password.getText().toString()))
        {
            Toast.makeText(this, "Por favor introduzca una contraseña", Toast.LENGTH_SHORT).show();
        }
        else
        {
            mAuth.signInWithEmailAndPassword(Email.getText().toString(), Password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if(task.isSuccessful())
                            {
                                // Sign in success, update UI with the signed-in user's information

                                FirebaseUser user = mAuth.getCurrentUser();

                                if(user.isEmailVerified())
                                {
                                    loadingBar.setTitle("Cargando");
                                    loadingBar.setMessage("Por favor espere...");
                                    loadingBar.setCanceledOnTouchOutside(true);
                                    loadingBar.show();

                                    //Verificar Rol de usuario y mandarlo a pantalla correspondiente
                                    DocumentReference docRef = db.collection("users").document(user.getUid());
                                    docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if(documentSnapshot.getData().get("RolID").toString().equals("Ck5Tnzr0ipmAzKpQpTDX"))
                                            {
                                                loadingBar.dismiss();

                                                Intent intent = new Intent(getApplicationContext(), AsesorMain.class);

                                                intent.putExtra("RolID", "Ck5Tnzr0ipmAzKpQpTDX");

                                                startActivity(intent);
                                            }
                                            else if (documentSnapshot.getData().get("RolID").toString().equals("I60WiSHvFyzJqUT0IU20"))
                                            {
                                                loadingBar.dismiss();

                                                Intent intent = new Intent(getApplicationContext(), AlumnoMain.class);

                                                intent.putExtra("RolID", "I60WiSHvFyzJqUT0IU20");

                                                startActivity(intent);
                                            }
                                        }
                                    });
                                }
                                else
                                {
                                    Toast.makeText(Login.this, "Favor de verificar su correo primero", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(Login.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    public void irRegistrarse(View view)
    {
        Intent intent = new Intent(getApplicationContext(), Registro.class);
        startActivity(intent);
    }

    public void irOlvideContraseña(View view)
    {
        Intent contraseña = new Intent(getApplicationContext(), ReseteoContraseña.class);
        startActivity(contraseña);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loadingBar = new ProgressDialog(this);

        Email = findViewById(R.id.editTextEmail);
        Password = findViewById(R.id.editTextPassword);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }
}