package com.example.tutor40;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;

public class Calificaciones extends AppCompatActivity {
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calificaciones);
    }

    public void Continuar(View view){
        Intent intent = new Intent(getApplicationContext(), TutorMain.class);
        startActivity(intent);
    }
    //   startActivity(intent);
    //
    //@Override
    //public void onStart() {
       // super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        //try {

          //  db = FirebaseFirestore.getInstance();

            //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            //Log.i("Login", user.getEmail());

            //Si hay un usuario logeado
            //DocumentReference docRef = db.collection("users").document(user.getUid());
            //docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
              //  @Override
               // public void onSuccess(DocumentSnapshot documentSnapshot) {
                 //   if(documentSnapshot.getData().get("RolID").toString() == "Ck5Tnzr0ipmAzKpQpTDX") {
                   //     Intent intent = new Intent(getApplicationContext(), TutorMain.class);

                     //   startActivity(intent);
                //    }
                //}
            //});

        //} catch(Exception e){
          //  e.printStackTrace();

            //Los llevamos a Login
            //Intent intent = new Intent(getApplicationContext(), Login.class);

        //    startActivity(intent);
        //}
    //}
}
