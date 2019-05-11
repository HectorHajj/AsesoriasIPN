package com.example.tutor40;

import android.app.ProgressDialog;
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
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AsesorMain extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    SharedPreferences sharedPreferences;

    //Firebase
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    //Controles
    ImageButton detenerse;
    Button conectarse;
    TextView textViewEstado;
    ProgressDialog loadingBar;


    //Variables
    ArrayList<String> materiasPreferidasList = new ArrayList<>();
    ArrayList<Peticiones> peticiones = new ArrayList<>();
    ArrayList<String> peticionesIgnoradasList = new ArrayList<>();
    String currentUser;
    String AsesorID;

    //Tasks
    GetPeticionesTask getPeticiones;

    //Banderas
    boolean disponible = false;

    public void Conectar(View view)
    {
        //Disponibilidad es true:
        disponible = true;

        //Mostrar boton de detenerse:
        detenerse.setVisibility(View.VISIBLE);

        //Ocultar boton de conectarse:
        conectarse.setVisibility(View.INVISIBLE);

        //Cambiar texto a Buscando Peticiones de Tutoria...
        textViewEstado.setText("Buscando peticiones de asesoria...");

        //Buscar peticiones con materias preferidas de usuario Tutor (Si NO tiene materias preferidas alertarlo y mandarlo a pantalla de materias)
        //Si materiasPreferidas no esta vacia proseguir
        if(actualizarMateriasPreferidas())
        {
            //Comenzar una busqueda asincrona
            getPeticiones = new GetPeticionesTask();

            loadingBar.show(this, "Buscando preguntas", "Por favor espere...", true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    try
                    {
                        if(getPeticiones.getStatus() == AsyncTask.Status.RUNNING)
                        {
                            getPeticiones.cancel(true);
                        }

                        Desconectar(detenerse);
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });

            //Realizar iterativamente busqueda y pesado de peticiones
            /*while (disponible == true) {
                if(getPeticiones.getStatus() != AsyncTask.Status.RUNNING){
                    try {
                        getPeticiones.execute(materiasPreferidasList).get();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }*/

            try {
                getPeticiones.execute(materiasPreferidasList).get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void Desconectar(View view)
    {
        //Disponibilidad es true:
        disponible = false;

        //Ocultar boton de detenerse:
        detenerse.setVisibility(View.INVISIBLE);

        //Mostrar boton de conectarse:
        conectarse.setVisibility(View.VISIBLE);

        //Cambiar texto a Buscando Peticiones de asesoría...
        textViewEstado.setText("Estas desconectado...");

        //Intentar interrumpir thread:
        if(getPeticiones != null)
        {
            getPeticiones.cancel(true);
        }
    }

    public boolean actualizarMateriasPreferidas()
    {
        try
        {
            materiasPreferidasList.clear();

            materiasPreferidasList = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("materiasPreferidas", ObjectSerializer.serialize(new ArrayList<String>())));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        //Si materiasPreferidasList esta vacia, alertar al usuario y recomendarle seleccionar algunas en actividad Materias
        if(materiasPreferidasList.isEmpty())
        {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("No tiene materias preferidas")
                    .setMessage("Para poder contestar preguntas, tiene que elegir las materias de su preferencia.")
                    .setPositiveButton("Ir a materias", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            Intent intent = new Intent(getApplicationContext(), AsesorMaterias.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Despues", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            Desconectar(detenerse);
                        }
                    }).show();
            //Si llega aqui, eligio no hacer nada
            return false;
        }
        //Si llega aqui materias no esta vacia
        return true;
    }

    public void GuardarEstado(View view)
    {
    }

    public class GetPeticionesTask extends AsyncTask<ArrayList<String>, Void, Void>
    {
        @Override
        protected Void doInBackground(ArrayList<String>... listas)
        {
            if(disponible)
            {
                //Encuentra las peticiones de acuerdo a materias elegidas por usuario
                peticiones.clear();

                final int[] numeroQueriesTerminados = {0};

                for(final ArrayList<String> lista : listas)
                {
                    for(final String materia : lista)
                    {
                        db.collection("Peticiones")
                                .whereEqualTo("Materia", materia)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if(task.isSuccessful())
                                        {
                                            for(QueryDocumentSnapshot document : task.getResult())
                                            {
                                                if(!peticionesIgnoradasList.contains(document.getId()))
                                                {
                                                    final Peticiones peticion = new Peticiones();

                                                    peticion.PeticionID = document.getId();
                                                    peticion.AlumnoID = document.getData().get("AlumnoID").toString();
                                                    //peticion.PreguntaID = document.getData().get("PreguntaID").toString();
                                                    peticion.Materia = document.getData().get("Materia").toString();
                                                    peticion.Pregunta = document.getData().get("Pregunta").toString();

                                                    Timestamp fechaCreacion = (Timestamp) document.getData().get("FechaCreacion");
                                                    peticion.FechaCreacion = fechaCreacion.toDate();

                                                    //Pesar peticion
                                                    /* Peso por tiempo */
                                                    long diferenciaSegundos;
                                                    Date newDate = new Date();

                                                    diferenciaSegundos = (newDate.getTime() - peticion.FechaCreacion.getTime()) / 1000;

                                                    peticion.Peso = (int) diferenciaSegundos;

                                                    peticiones.add(peticion);
                                                }
                                            }

                                            numeroQueriesTerminados[0] += 1;

                                            //Llamar un callback que se actualice de acuerdo al numero de llamadas a la base (numero de materias)
                                            //Despues de llegar a la ultima llamada, procesar peticiones

                                            //Si la materia es la ultima de la lista
                                            if(lista.size() == numeroQueriesTerminados[0])
                                            {
                                                if(!peticiones.isEmpty())
                                                {
                                                    //Elegir la peticion con mas peso e iniciar chat
                                                    int posicion = 0;
                                                    long pesoAnterior = 0;

                                                    for(Peticiones peticion : peticiones)
                                                    {
                                                        if(peticion.Peso > pesoAnterior)
                                                        {
                                                            pesoAnterior = peticion.Peso;
                                                            posicion = peticiones.indexOf(peticion);
                                                        }
                                                    }

                                                    final Peticiones peticionEscogida = peticiones.get(posicion);

                                                    new AlertDialog.Builder(AsesorMain.this)
                                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                                            .setTitle("Pregunta encontrada")
                                                            .setMessage(peticiones.get(posicion).Pregunta)
                                                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which)
                                                                {
                                                                    //Modificar peticion

                                                                    peticionEscogida.AsesorID = AsesorID;

                                                                    db.collection("Peticiones").document(peticionEscogida.PeticionID)
                                                                            .set(peticionEscogida, SetOptions.merge());

                                                                    Map<String, Object> chat = new HashMap<>();
                                                                    chat.put("AsesorID", AsesorID);
                                                                    chat.put("AlumnoID", peticionEscogida.AlumnoID);
                                                                    chat.put("FechaCreacion", new Date());

                                                                    //Creación del chat
                                                                    db.collection("Chats").document(peticionEscogida.PeticionID)
                                                                            .set(chat)
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    Intent intent = new Intent(getApplicationContext(), Chat.class);

                                                                                    intent.putExtra("RolID", getIntent().getStringExtra("RolID"));
                                                                                    intent.putExtra("PeticionID", peticionEscogida.PeticionID);

                                                                                    startActivity(intent);
                                                                                }
                                                                            });
                                                                }
                                                            })

                                                            //Si rechazas peticion, se quita de lista de posibles peticiones a resolver
                                                            .setNegativeButton("Rechazar", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which)
                                                                {
                                                                    try
                                                                    {
                                                                        //Agregar pregunta rechazada a lista de preguntas a ignorar
                                                                        peticionesIgnoradasList.add(peticionEscogida.PeticionID);

                                                                        //Si se rechaza pregunta, pesar de nuevo preguntas
                                                                        getPeticiones = new GetPeticionesTask();

                                                                        getPeticiones.execute(materiasPreferidasList).get();
                                                                    }

                                                                    catch(Exception e)
                                                                    {
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                            }).show();
                                                }
                                            }

                                        }
                                        else
                                        {
                                            Log.i("Error en Query", "Error getting documents: ", task.getException());
                                        }
                                    }
                                });
                    }
                }
            }
            else
            {
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            Log.i("On Post Execute",peticiones.toString());

            super.onPostExecute(aVoid);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_main);

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
        AsesorID = mAuth.getInstance().getCurrentUser().getUid();

        //Controles
        detenerse = findViewById(R.id.buttonDetenerse);
        conectarse = findViewById(R.id.buttonConectarse);
        textViewEstado = findViewById(R.id.textViewEstado);
        loadingBar = new ProgressDialog(this);


        //Shared Preferences
        sharedPreferences = getSharedPreferences("com.example.tutor40", MODE_PRIVATE);

        actualizarMateriasPreferidas();
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
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if(id == R.id.perfil)
        {
            Intent intent = new Intent(getApplicationContext(), Perfil.class);

            startActivity(intent);
        }
        else if(id == R.id.logout)
        {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(getApplicationContext(), Login.class);

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
                                Toast.makeText(AsesorMain.this, "Cuenta eliminada", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), Login.class));
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if(id == R.id.materias)
        {
            Intent intent = new Intent(getApplicationContext(), AsesorMaterias.class);
            startActivity(intent);
        }
        else if(id == R.id.perfil)
        {
            Intent intent = new Intent(getApplicationContext(), Perfil.class);
            startActivity(intent);
        }
       // else if(id == R.id.ranking)
        //{
         //   Intent intent = new Intent(getApplicationContext(), Perfil.class);
          //  startActivity(intent);
        //}

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onRestart()
    {
        //Si se regresa a view se actualizan sus preferencias
        actualizarMateriasPreferidas();
        //Conectar(detenerse);
        //Desconectar(detenerse);
        super.onRestart();
    }
}