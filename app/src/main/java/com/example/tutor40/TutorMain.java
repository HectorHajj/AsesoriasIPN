package com.example.tutor40;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class TutorMain extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseFirestore db;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
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
}
