package com.example.tutor40;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Registro extends AppCompatActivity
{
    EditText Email;
    EditText Password, Password2;
    EditText Nombre;
    EditText ApellidoPaterno;
    EditText ApellidoMaterno;

    Switch alumnoTutor;
    ProgressDialog loadingBar;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    public void registrarNuevoUsuario(String userId, String Nombre, String ApellidoPaterno, String ApellidoMaterno, Boolean Rol)
    {
        Map<String, Object> user = new HashMap<>();

        if(Rol)
        {
            //Si Rol es true entonces es un alumno
            user.put("Nombre", Nombre);
            user.put("ApellidoPaterno", ApellidoPaterno);
            user.put("ApellidoMaterno", ApellidoMaterno);
            user.put("RolID", "I60WiSHvFyzJqUT0IU20");
        }
        else
        {
            //Si Rol es false entonces es un asesor
            user.put("Nombre", Nombre);
            user.put("ApellidoPaterno", ApellidoPaterno);
            user.put("ApellidoMaterno", ApellidoMaterno);
            user.put("RolID", "Ck5Tnzr0ipmAzKpQpTDX");
        }

        // Add a new document with a generated ID
        db.collection("users").document(userId).set(user);

        if(user.get("RolID").toString() == "Ck5Tnzr0ipmAzKpQpTDX")
        {
            Toast.makeText(Registro.this, "Cuenta de asesor creada, verifique su correo", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
        }
        else if(user.get("RolID").toString() == "I60WiSHvFyzJqUT0IU20")
        {
            Toast.makeText(Registro.this, "Cuenta de alumno creada, verifique su correo", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
        }
    }

    public void registrar(View view)
    {
        if(TextUtils.isEmpty(Email.getText().toString()))
        {
            Toast.makeText(Registro.this, "Por favor introduzca un E-Mail", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(Password.getText().toString()))
        {
            Toast.makeText(Registro.this, "Por favor introduzca una contraseña", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(Password2.getText().toString()))
        {
            Toast.makeText(Registro.this, "Por favor introduzca la verificación de la contraseña", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(Nombre.getText().toString()))
        {
            Toast.makeText(Registro.this, "Por favor introduzca su nombre", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(ApellidoPaterno.getText().toString()))
        {
            Toast.makeText(Registro.this, "Por favor introduzca su apellido paterno", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(ApellidoMaterno.getText().toString()))
        {
            Toast.makeText(Registro.this, "Por favor introduzca su apellido materno", Toast.LENGTH_SHORT).show();
        }

        if(!TextUtils.equals(Password.getText().toString(), Password2.getText().toString()))
        {
            Toast.makeText(Registro.this, "Las contraseñas no son similares", Toast.LENGTH_SHORT).show();
        }

        if(!TextUtils.isEmpty(Email.getText().toString()) & !TextUtils.isEmpty(Password.getText().toString()) & !TextUtils.isEmpty(Password2.getText().toString()) & !TextUtils.isEmpty(Nombre.getText().toString()) & !TextUtils.isEmpty(ApellidoPaterno.getText().toString()) & !TextUtils.isEmpty(ApellidoMaterno.getText().toString()) & TextUtils.equals(Password.getText().toString(), Password2.getText().toString()))
        {
            loadingBar.setTitle("Creando una cuenta nueva");
            loadingBar.setMessage("Por favor espere mientras se crea su cuenta");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            //Metodo para crear nuevo usuario
            mAuth.createUserWithEmailAndPassword(Email.getText().toString(), Password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if(task.isSuccessful())
                            {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getInstance().getCurrentUser();
                                user.sendEmailVerification();
                                registrarNuevoUsuario(user.getUid(), Nombre.getText().toString(), ApellidoPaterno.getText().toString(), ApellidoMaterno.getText().toString(), alumnoTutor.isChecked());
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(Registro.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        Email = findViewById(R.id.editTextEmail);
        Password = findViewById(R.id.editTextPassword);
        Password2 = findViewById(R.id.editTextPassword2);
        Nombre = findViewById(R.id.editTextNombre);
        ApellidoPaterno = findViewById(R.id.editTextApellidoPaterno);
        ApellidoMaterno = findViewById(R.id.editTextApellidoMaterno);

        alumnoTutor = findViewById(R.id.switchTutorAlumno);

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();

        loadingBar = new ProgressDialog(this);
    }

    public void sendUserToLoginActivity(View view)
    {
        Intent loginIntent = new Intent(getApplicationContext(), Login.class);
        startActivity(loginIntent);
    }
}