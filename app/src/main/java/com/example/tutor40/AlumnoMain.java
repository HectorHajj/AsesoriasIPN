package com.example.tutor40;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;

public class AlumnoMain extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    //Firebase
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    EventListener<DocumentSnapshot> yolo;
    ListenerRegistration registration;

    //Controles
    Spinner spinnerMaterias;
    TextView pregunta;
    ProgressDialog loadingBar;

    //Variables
    ArrayList<String> spinnerArray =  new ArrayList<String>();
    String currentUser;
    Boolean Aceptado = false;
    String peticionActual;

    public void enviarPregunta(View view){
        //Tomar valor actual de pregunta y de materia y enviarla
        if(TextUtils.isEmpty(pregunta.getText().toString()))
        {
            Toast.makeText(this, "Ingrese primero su pregunta por favor", Toast.LENGTH_LONG).show();
        }
        else
        {
            loadingBar.show(this, "Buscando asesores", "Por favor espere...", true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    //TODO:
                    if(peticionActual.isEmpty())
                    {
                        loadingBar.dismiss();
                    }
                    else
                    {
                        loadingBar.dismiss();

                        db.collection("Peticiones").document(peticionActual)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.i("Cancelado","Se borro peticion");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.i("Cancelado","No se borro peticion");
                                    }
                                });
                    }
                }
            });

            Peticiones peticion = new Peticiones();

            peticion.Pregunta = pregunta.getText().toString();
            peticion.Materia = spinnerMaterias.getSelectedItem().toString();
            peticion.FechaCreacion = new Date();
            peticion.AlumnoID = currentUser;

             //Agregar la peticion a la base de datos
            db.collection("Peticiones")
                    .add(peticion)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(final DocumentReference documentReference) {

                            peticionActual = documentReference.getId();

                            final DocumentReference docRef = db.collection("Peticiones").document(documentReference.getId());
                            yolo = new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                                    if (e != null) {
                                        Log.w("Null event", "Listen failed.", e);
                                        return;
                                    }

                                    if (snapshot != null && snapshot.exists()) {
                                        Log.i("Heard event", "Current data: " + snapshot.getData());

                                        //Si se asigna un Asesor
                                        if(snapshot.getData().get("AsesorID") != null){
                                            loadingBar.dismiss();

                                            registration.remove();

                                            Intent intent = new Intent(getApplicationContext(), Chat.class);

                                            intent.putExtra("PeticionID", documentReference.getId());
                                            intent.putExtra("RolID", getIntent().getStringExtra("RolID"));

                                            startActivity(intent);
                                        }
                                    } else {
                                        Log.d("Heard event with null data", "Current data: null");
                                    }
                                }
                            };

                            registration = docRef.addSnapshotListener(yolo);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("Undone", "Error adding document", e);
                        }
                    });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alumno_main);

        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //Navigation Menu
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getInstance().getCurrentUser().getUid();

        //Controles
        pregunta = findViewById(R.id.editTextPregunta);
        loadingBar = new ProgressDialog(this);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMaterias = findViewById(R.id.spinnerMaterias);
        spinnerMaterias.setAdapter(adapter);

        //Acciones de spinner
        spinnerMaterias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                ((TextView) view).setTextColor(Color.BLACK);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });

        //Introducir materias en el spinner
        db.collection("Materias").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            for(QueryDocumentSnapshot document : task.getResult())
                            {
                                spinnerArray.add(document.getData().get("Nombre").toString());
                                adapter.notifyDataSetChanged();
                            }
                        }
                        else
                        {
                            Log.i("Mala tuya", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if(id == R.id.perfil)
        {
            Intent intent = new Intent(AlumnoMain.this, Perfil.class);
            startActivity(intent);
        }
        else if(id == R.id.logout)
        {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(AlumnoMain.this, Login.class);
            startActivity(intent);
        }
        else if(id == R.id.delete)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("¿Esta seguro de que desea eliminar su cuenta?");
            builder.setCancelable(false);

            builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    FirebaseFirestore db;
                    db = FirebaseFirestore.getInstance();

                    db.collection("users").document(user.getUid().toString()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                        }
                    });

                    user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(AlumnoMain.this, "Cuenta eliminada", Toast.LENGTH_SHORT).show();

                                FirebaseAuth.getInstance().signOut();

                                Intent intent = new Intent(AlumnoMain.this, Login.class);
                                startActivity(intent);
                            }
                        }
                    });
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.cancel();
                }
            });

            AlertDialog ad = builder.create();
            ad.setTitle("Eliminar cuenta");
            ad.show();
        }
        else if(id == R.id.cpass)
        {
            Intent intent = new Intent(AlumnoMain.this, ChangePassword.class);
            startActivity(intent);
        }
        else if(id == R.id.email)
        {
            Intent intent = new Intent(AlumnoMain.this, ChangeEmail.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if(id == R.id.ranking)
        {
        //Deben poner hacia que ventana ira ranking, de momento esta hacia login.
            Intent intent = new Intent(AlumnoMain.this, Login.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        try
        {
            db = FirebaseFirestore.getInstance();

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            Log.i("Login", user.getEmail());

            //Si hay un usuario logeado
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot)
                {
                    if(documentSnapshot.getData().get("RolID").toString() == "Ck5Tnzr0ipmAzKpQpTDX")
                    {
                        Intent intent = new Intent(AlumnoMain.this, AsesorMain.class);
                        startActivity(intent);
                    }
                }
            });

        }
        catch(Exception e)
        {
            e.printStackTrace();

            //Los llevamos a Login
            Intent intent = new Intent(AlumnoMain.this, Login.class);
            startActivity(intent);
        }
    }
}