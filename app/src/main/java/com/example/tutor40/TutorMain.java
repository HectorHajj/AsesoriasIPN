package com.example.tutor40;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class TutorMain extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    SharedPreferences sharedPreferences;
    FirebaseFirestore db;

    ImageButton detenerse;
    Button conectarse;
    TextView textViewEstado;

    ArrayList<String> materiasPreferidasList = new ArrayList<>();
    ArrayList<Peticiones> peticiones = new ArrayList<>();

    GetPeticionesTask getPeticiones;

    boolean disponible = false;

    public void Conectar(View view){

        //Disponibilidad es true
        disponible = true;

        //Mostrar boton de detenerse
        detenerse.setVisibility(View.VISIBLE);

        //Ocultar boton de conectarse
        conectarse.setVisibility(View.INVISIBLE);

        //Cambiar texto a Buscando Peticiones de Tutoria...
        textViewEstado.setText("Buscando Peticiones de Tutoria...");

        //Buscar peticiones con materias preferidas de usuario Tutor (Si NO tiene materias preferidas alertarlo y mandarlo a pantalla de materias)
        //Si materiasPreferidas no esta vacia proseguir
        if(actualizarMateriasPreferidas()){

            //Comenzar una busqueda asincrona
            getPeticiones = new GetPeticionesTask();

            try {
                    getPeticiones.execute(materiasPreferidasList).get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void Desconectar(View view){
        //Disponibilidad es true
        disponible = false;

        //Mostrar boton de detenerse
        detenerse.setVisibility(View.INVISIBLE);

        //Ocultar boton de conectarse
        conectarse.setVisibility(View.VISIBLE);

        //Cambiar texto a Buscando Peticiones de Tutoria...
        textViewEstado.setText("Estas Desconectado...");

        //Intentar interrumpir thread
        if(getPeticiones != null){
            getPeticiones.cancel(true);
        }
    }

    public boolean actualizarMateriasPreferidas(){
        try {
            materiasPreferidasList.clear();

            materiasPreferidasList = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("materiasPreferidas", ObjectSerializer.serialize(new ArrayList<String>())));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Si materiasPreferidasList esta vacia, alertar al usuario y recomendarle seleccionar algunas en actividad Materias
        if(materiasPreferidasList.isEmpty()){
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("No tiene Materias Preferidas")
                    .setMessage("Para poder contestar preguntas, tiene que elegir las materias de su preferencia.")
                    .setPositiveButton("Ir a Materias", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(getApplicationContext(), TutorMaterias.class);

                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Ir despues", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Desconectar(detenerse);
                        }
                    }).show();
            //Si llega aqui, eligio no hacer nada
            return false;
        }
        //Si llega aqui materias no esta vacia
        return true;
    }

    public class GetPeticionesTask extends AsyncTask<ArrayList<String>, Void, Void> {
        @Override
        protected Void doInBackground(ArrayList<String>... listas) {

            //Encuentra las peticiones de acuerdo a materias elegidas por usuario
            peticiones.clear();

            final int[] numeroQueriesTerminados = {0};

            for (final ArrayList<String> lista : listas) {
                for (final String materia : lista) {
                    db.collection("Peticiones")
                            .whereEqualTo("Materia", materia)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {

                                            final Peticiones peticion = new Peticiones();

                                            peticion.AlumnoID = document.getData().get("AlumnoID").toString();
                                            peticion.PreguntaID = document.getData().get("PreguntaID").toString();
                                            peticion.Materia = document.getData().get("Materia").toString();
                                            peticion.Pregunta = document.getData().get("Pregunta").toString();
                                            peticion.FechaCreacion = (Date) document.getData().get("FechaCreacion");

                                            //Pesar peticion
                                            /* Peso por tiempo */
                                            long diferenciaSegundos;
                                            Date newDate = new Date();

                                            diferenciaSegundos = (newDate.getTime() - peticion.FechaCreacion.getTime()) / 1000;

                                            peticion.Peso = (int) diferenciaSegundos;

                                            peticiones.add(peticion);
                                        }

                                        numeroQueriesTerminados[0] += 1;

                                        //Llamar un callback que se actualice de acuerdo al numero de llamadas a la base (numero de materias)
                                        //Despues de llegar a la ultima llamada, procesar peticiones

                                        //Si la materia es la [ultima de la lista
                                        if(lista.size() == numeroQueriesTerminados[0]){
                                            if(!peticiones.isEmpty()){
                                                //Elegir la peticion con mas peso e iniciar chat
                                                int posicion = 0;
                                                long pesoAnterior = 0;
                                                for (Peticiones peticion : peticiones) {
                                                    if(peticion.Peso > pesoAnterior){
                                                        pesoAnterior = peticion.Peso;
                                                        posicion = peticiones.indexOf(peticion);
                                                    }
                                                }

                                                new AlertDialog.Builder(TutorMain.this)
                                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                                        .setTitle("Pregunta encontrada!")
                                                        .setMessage(peticiones.get(posicion).Pregunta)
                                                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                Intent intent = new Intent(getApplicationContext(), TutorMaterias.class);

                                                                startActivity(intent);
                                                            }
                                                        })
                                                        .setNegativeButton("Rechazar", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                //Si se rechaza pregunta, pesar de nuevo preguntas
                                                                getPeticiones = new GetPeticionesTask();

                                                                try {
                                                                    getPeticiones.execute(materiasPreferidasList).get();
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        }).show();
                                            }
                                        }

                                    } else {
                                        Log.i("Error en Query", "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            Log.i("On Post Execute",peticiones.toString());


            super.onPostExecute(aVoid);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        try {

            db = FirebaseFirestore.getInstance();

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            Log.i("Login", user.getEmail());

            //Si hay un usuario logeado
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.getData().get("RolID").toString() == "I60WiSHvFyzJqUT0IU20") {
                        Intent intent = new Intent(getApplicationContext(), AlumnoMain.class);

                        startActivity(intent);
                    }
                }
            });
        } catch(Exception e){
            e.printStackTrace();

            //Los llevamos a Login
            Intent intent = new Intent(getApplicationContext(), Login.class);

            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        detenerse = findViewById(R.id.buttonDetenerse);
        conectarse = findViewById(R.id.buttonConectarse);
        textViewEstado = findViewById(R.id.textViewEstado);

        sharedPreferences = getSharedPreferences("com.example.tutor40", MODE_PRIVATE);

        actualizarMateriasPreferidas();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.perfil){
            Intent intent = new Intent(getApplicationContext(), Perfil.class);

            startActivity(intent);
        } else if (id == R.id.logout){
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(getApplicationContext(), Login.class);

            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.materias) {
            Intent intent = new Intent(getApplicationContext(), TutorMaterias.class);

            startActivity(intent);
        } else if (id == R.id.perfil) {
            Intent intent = new Intent(getApplicationContext(), Perfil.class);

            startActivity(intent);
        } else if (id == R.id.ranking) {
            Intent intent = new Intent(getApplicationContext(), Perfil.class);

            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onRestart() {

        //Si se regresa a view, desconectar al usuario y actualizar sus preferencias
        actualizarMateriasPreferidas();

        Desconectar(detenerse);

        super.onRestart();
    }
}
