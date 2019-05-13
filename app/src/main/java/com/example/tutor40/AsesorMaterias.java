package com.example.tutor40;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class AsesorMaterias extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    ArrayList<String> materiasPreferidasList = new ArrayList<>();
    SharedPreferences sharedPreferences;
    FirebaseFirestore db;

    TableLayout CBM;
    TableLayout CI;
    TableLayout DI;
    TableLayout CSH;
    TableLayout OC;

    public void crearCheckboxes(final String rama)
    {
        db.collection("Materias")
                .whereEqualTo("Rama", rama)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            for(QueryDocumentSnapshot document : task.getResult())
                            {
                                //Dependiendo de la rama a la que pertenezca la materia, se añade a su tabla correspondiente:
                                CheckBox checkBox = new CheckBox(getApplicationContext());
                                checkBox.setText(document.getData().get("Nombre").toString());

                                //Si el nombre esta en la lista de materias preferidas, inicializar checkbox con On:
                                if(materiasPreferidasList.contains(document.getData().get("Nombre").toString()))
                                {
                                    checkBox.setChecked(true);
                                }

                                checkBox.setGravity(1);
                                checkBox.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                checkBox.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        CheckBox vi = (CheckBox) v;

                                        if(vi.isChecked())
                                        {
                                            //Agregar a preferencias si no estaba:
                                            if(!materiasPreferidasList.contains(vi.getText().toString()))
                                            {
                                                materiasPreferidasList.add(vi.getText().toString());
                                            }
                                        }
                                        else
                                        {
                                            //Quitar de preferencias:
                                            if(materiasPreferidasList.contains(vi.getText().toString()))
                                            {
                                                materiasPreferidasList.remove(vi.getText().toString());
                                            }
                                        }

                                        //Guardar las preferencias de materias del usuario:
                                        try
                                        {
                                            sharedPreferences.edit().putString("materiasPreferidas", ObjectSerializer.serialize(materiasPreferidasList)).apply();
                                        }
                                        catch(Exception e)
                                        {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                                //Se crea un nuevo contenedor:
                                TableRow currentRow = new TableRow(getApplicationContext());

                                switch(rama)
                                {
                                    case "CBM":
                                        CBM.addView(currentRow);
                                        break;
                                    case "CI":
                                        CI.addView(currentRow);
                                        break;
                                    case "DI":
                                        DI.addView(currentRow);
                                        break;
                                    case "CSH":
                                        CSH.addView(currentRow);
                                        break;
                                    case "OC":
                                        OC.addView(currentRow);
                                        break;
                                    default:
                                        break;
                                }
                                currentRow.addView(checkBox);
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
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_materias);

        //Menus y Toolbars:
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Se inicializa los checkboxes dependiendo de si estan en preferencias:
        sharedPreferences = getSharedPreferences("com.example.tutor40", MODE_PRIVATE);

        try
        {
            materiasPreferidasList = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("materiasPreferidas", ObjectSerializer.serialize(new ArrayList<String>())));
            Log.i("Materias Preferidas inicializadas desde SharedPreferences", materiasPreferidasList.toString());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        //Materias:
        CBM = findViewById(R.id.tableLayout_CBM);
        CI = findViewById(R.id.tableLayout_CI);
        DI = findViewById(R.id.tableLayout_DI);
        CSH = findViewById(R.id.tableLayout_CSH);
        OC = findViewById(R.id.tableLayout_OC);

        //Crear TableRows de acuerdo a materias en el server:
        db = FirebaseFirestore.getInstance();

        crearCheckboxes("CBM");
        crearCheckboxes("CI");
        crearCheckboxes("DI");
        crearCheckboxes("CSH");
        crearCheckboxes("OC");
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
            Intent intent = new Intent(AsesorMaterias.this, Perfil.class);
            startActivity(intent);
        }
        else if(id == R.id.logout)
        {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(AsesorMaterias.this, Login.class);
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
                                Toast.makeText(AsesorMaterias.this, "Cuenta eliminada", Toast.LENGTH_SHORT).show();

                                FirebaseAuth.getInstance().signOut();

                                Intent intent = new Intent(AsesorMaterias.this, Login.class);
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
            Intent intent = new Intent(AsesorMaterias.this, ChangePassword.class);
            startActivity(intent);
        }
        else if(id == R.id.email)
        {
            Intent intent = new Intent(AsesorMaterias.this, ChangeEmail.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.regresar)
        {
            Intent intent = new Intent(AsesorMaterias.this, AsesorMain.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}