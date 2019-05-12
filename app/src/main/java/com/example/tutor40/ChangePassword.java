package com.example.tutor40;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

public class ChangePassword extends AppCompatActivity
{
    FirebaseFirestore db;
    EditText CA;
    EditText CN;
    EditText CNR;
    Button ACT;
    TextView REG;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cpassword);

        CA  = (EditText) findViewById(R.id.cp_actual);
        CN  = (EditText) findViewById(R.id.cp_nueva);
        CNR = (EditText) findViewById(R.id.cp_nueva_rep);
        ACT = (Button) findViewById(R.id.a_button);
        REG = (TextView) findViewById(R.id.r_link);

        REG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                db = FirebaseFirestore.getInstance();
                final DocumentReference document = db.collection("users").document(user.getUid());

                document.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot)
                    {
                        if(documentSnapshot.getData().get("RolID").toString().equals("I60WiSHvFyzJqUT0IU20"))
                        {
                            Intent intent = new Intent(ChangePassword.this, AlumnoMain.class);
                            startActivity(intent);
                        }

                        if(documentSnapshot.getData().get("RolID").toString().equals("Ck5Tnzr0ipmAzKpQpTDX"))
                        {
                            Intent intent = new Intent(ChangePassword.this, AsesorMain.class);
                            startActivity(intent);
                        }
                    }
                });
            }
        });

        ACT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(TextUtils.isEmpty(CA.getText().toString()))
                {
                    Toast.makeText(ChangePassword.this, "Por favor introduzca su contraseña actual", Toast.LENGTH_SHORT).show();
                }

                if(TextUtils.isEmpty(CN.getText().toString()))
                {
                    Toast.makeText(ChangePassword.this, "Por favor introduzca su contraseña nueva", Toast.LENGTH_SHORT).show();
                }

                if(TextUtils.isEmpty(CNR.getText().toString()))
                {
                    Toast.makeText(ChangePassword.this, "Por favor introduzca la verificación de la contraseña", Toast.LENGTH_SHORT).show();
                }

                if(!TextUtils.equals(CN.getText().toString(), CNR.getText().toString()))
                {
                    Toast.makeText(ChangePassword.this, "Las contraseñas no son similares", Toast.LENGTH_SHORT).show();
                }

                if(!TextUtils.isEmpty(CA.getText().toString()) & !TextUtils.isEmpty(CN.getText().toString()) & !TextUtils.isEmpty(CNR.getText().toString()) & TextUtils.equals(CN.getText().toString(),CNR.getText().toString()))
                {
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail().toString(), CA.getText().toString());

                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                user.updatePassword(CN.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if(task.isSuccessful())
                                        {
                                            Toast.makeText(ChangePassword.this, "Contraseña actualizada, vuelva a iniciar sesión", Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(ChangePassword.this, Login.class);
                                            startActivity(intent);
                                        }
                                        else
                                        {
                                            Toast.makeText(ChangePassword.this, "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                            else
                            {
                                Toast.makeText(ChangePassword.this, "La contraseña actual no es la correcta", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}