package com.example.tutor40;

import android.content.Intent;
import android.provider.ContactsContract;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Perfil extends AppCompatActivity
{
    FirebaseFirestore db;

    EditText Nombre;
    EditText ApellidoPaterno;
    EditText ApellidoMaterno;
    Button Actualizar;
    TextView Cancelar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        Nombre = (EditText) findViewById(R.id.editTextNombre);
        ApellidoPaterno = (EditText) findViewById(R.id.editTextApellidoPaterno);
        ApellidoMaterno = (EditText) findViewById(R.id.editTextApellidoMaterno);
        Actualizar = (Button) findViewById(R.id.actualizar_button);
        Cancelar = (TextView) findViewById(R.id.cancel_link);

        //Regresa al activity correspondiente tras cliackear en cancelar:
        Cancelar.setOnClickListener(new View.OnClickListener() {
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
                            Intent intent = new Intent(Perfil.this, AlumnoMain.class);
                            startActivity(intent);
                        }

                        if(documentSnapshot.getData().get("RolID").toString().equals("Ck5Tnzr0ipmAzKpQpTDX"))
                        {
                            Intent intent = new Intent(Perfil.this, AsesorMain.class);
                            startActivity(intent);
                        }
                    }
                });
            }
        });

        //Actualizar datos:
        Actualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(TextUtils.isEmpty(Nombre.getText().toString()))
                {
                    Toast.makeText(Perfil.this, "Por favor introduzca su nombre", Toast.LENGTH_SHORT).show();
                }

                if(TextUtils.isEmpty(ApellidoPaterno.getText().toString()))
                {
                    Toast.makeText(Perfil.this, "Por favor introduzca su apellido paterno", Toast.LENGTH_SHORT).show();
                }

                if(TextUtils.isEmpty(ApellidoMaterno.getText().toString()))
                {
                    Toast.makeText(Perfil.this, "Por favor introduzca su apellido materno", Toast.LENGTH_SHORT).show();
                }

                if(!TextUtils.isEmpty(Nombre.getText().toString()) & !TextUtils.isEmpty(ApellidoPaterno.getText().toString()) & !TextUtils.isEmpty(ApellidoMaterno.getText().toString()))
                {
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    db = FirebaseFirestore.getInstance();
                    final DocumentReference document = db.collection("users").document(user.getUid());

                    document.update("Nombre", Nombre.getText().toString(), "ApellidoPaterno", ApellidoPaterno.getText().toString(), "ApellidoMaterno", ApellidoMaterno.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(Perfil.this, "Actualización exitosa", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        Toast.makeText(Perfil.this, "Actualización erronea", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        //Muestra los datos:
        db = FirebaseFirestore.getInstance();
        FirebaseUser loggedUser = FirebaseAuth.getInstance().getCurrentUser();

        DocumentReference user = db.collection("users").document(loggedUser.getUid());
        user.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                Nombre.setText(documentSnapshot.getData().get("Nombre").toString());
                ApellidoPaterno.setText(documentSnapshot.getData().get("ApellidoPaterno").toString());
                ApellidoMaterno.setText(documentSnapshot.getData().get("ApellidoMaterno").toString());
            }
        });
    }
}