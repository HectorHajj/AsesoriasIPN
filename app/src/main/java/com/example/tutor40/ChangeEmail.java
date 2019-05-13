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
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChangeEmail extends AppCompatActivity
{
    FirebaseFirestore db;
    EditText CEN;
    EditText CER;
    EditText COP;
    Button   ACT;
    TextView RET;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cemail);

        CEN = (EditText) findViewById(R.id.ce_nuevo);
        CER = (EditText) findViewById(R.id.ce_nuevo_rep);
        COP = (EditText) findViewById(R.id.contpass);
        ACT = (Button) findViewById(R.id.act_button);
        RET = (TextView) findViewById(R.id.ret_link);

        RET.setOnClickListener(new View.OnClickListener() {
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
                            Intent intent = new Intent(ChangeEmail.this, AlumnoMain.class);
                            startActivity(intent);
                        }

                        if(documentSnapshot.getData().get("RolID").toString().equals("Ck5Tnzr0ipmAzKpQpTDX"))
                        {
                            Intent intent = new Intent(ChangeEmail.this, AsesorMain.class);
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
                if(TextUtils.isEmpty(CEN.getText().toString()))
                {
                    Toast.makeText(ChangeEmail.this, "Por favor introduzca un E-Mail nuevo", Toast.LENGTH_SHORT).show();
                }

                if(TextUtils.isEmpty(CER.getText().toString()))
                {
                    Toast.makeText(ChangeEmail.this, "Por favor introduzca la verificación del E-mail nuevo", Toast.LENGTH_SHORT).show();
                }

                if(TextUtils.isEmpty(COP.getText().toString()))
                {
                    Toast.makeText(ChangeEmail.this, "Por favor introduzca su contraseña actual, esta será la contraseña del nuevo E-mail", Toast.LENGTH_SHORT).show();
                }

                if(!TextUtils.equals(CEN.getText().toString(), CER.getText().toString()))
                {
                    Toast.makeText(ChangeEmail.this, "Los E-mails no son similares", Toast.LENGTH_SHORT).show();
                }

                if(!TextUtils.isEmpty(CEN.getText().toString()) & !TextUtils.isEmpty(CER.getText().toString()) & !TextUtils.isEmpty(COP.getText().toString()) & TextUtils.equals(CEN.getText().toString(), CER.getText().toString()))
                {
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail().toString(),COP.getText().toString());

                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                user.updateEmail(CEN.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if(task.isSuccessful())
                                        {
                                            user.sendEmailVerification();
                                            Toast.makeText(ChangeEmail.this, "E-mail actualizado, verifique su correo", Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(ChangeEmail.this, Login.class);
                                            startActivity(intent);
                                        }
                                        else
                                        {
                                            Toast.makeText(ChangeEmail.this, "Error al actualizar el E-mail", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                            else
                            {
                                Toast.makeText(ChangeEmail.this, "La contraseña actual no es la correcta", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}