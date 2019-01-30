package com.example.tutor40;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class TutorMain extends AppCompatActivity {

    FirebaseAuth mAuth;

    FirebaseFirestore db;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();

        menuInflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.i("Item", item.getTitle().toString());

        if(item.getTitle().toString().equals("Perfil")){
            Intent intent = new Intent(getApplicationContext(), Perfil.class);

            startActivity(intent);
        } else if (item.getTitle().toString().equals("Salir de Cuenta")){
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(getApplicationContext(), Login.class);

            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_main);
    }

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
}
