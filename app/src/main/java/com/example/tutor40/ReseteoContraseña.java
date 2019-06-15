package com.example.tutor40;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ReseteoContraseña extends AppCompatActivity
{
    private EditText correo;
    private Button sendpassword;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contrasena_olvidada);

        mAuth = FirebaseAuth.getInstance();

        correo = (EditText) findViewById(R.id.correo);
        sendpassword = (Button) findViewById(R.id.password_button);

        sendpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String userEmail = correo.getText().toString();

                if(TextUtils.isEmpty(userEmail))
                {
                    Toast.makeText(ReseteoContraseña.this, "Por favor introduzca un E-Mail", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(ReseteoContraseña.this, "Cheque la bandeja de entrada de su correo", Toast.LENGTH_SHORT).show();

                                startActivity(new Intent(ReseteoContraseña.this, Login.class));
                            }
                            else
                            {
                                String mensaje = task.getException().getMessage();
                                Toast.makeText(ReseteoContraseña.this, "Error ocurrido: " + mensaje, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    public void Regresar(View view)
    {
        Intent back = new Intent(getApplicationContext(), Login.class);
        startActivity(back);
    }
}