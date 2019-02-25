package com.example.tutor40;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class GroupChat extends AppCompatActivity
{
    private Toolbar mToolbar;
    private ImageButton SendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages, Temporizador;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserID, currentUserName, PeticionID, TutorID, AlumnoID;
    private CountDownTimer Reloj;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        //Traer el id de la peticion
        Intent intent = getIntent();
        PeticionID = intent.getStringExtra("PeticionID");

        Log.i("PeticionID", PeticionID);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        currentUserID = mAuth.getCurrentUser().getUid();

        InitializeFields();
        GetUserInfo();
        DisplayMessages();



        SendMessageButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                SaveMessageInfoToDatabase();

                userMessageInput.setText("");

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

        db.collection("Chats").document(PeticionID).collection("Mensajes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                DisplayMessages();
            }
        });

        Contador();

        db.collection("Peticiones").document(PeticionID)
        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
        @Override
        public void onSuccess(DocumentSnapshot documentSnapshot) {
            Log.i("Peticion", PeticionID);
            TutorID = documentSnapshot.getData().get("TutorID").toString();
            AlumnoID = documentSnapshot.getData().get("AlumnoID").toString();
        }
    });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    private void InitializeFields() {
        mToolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        //TODO Nombre del otro usuario
        getSupportActionBar().setTitle("Algo informativo");

        SendMessageButton = findViewById(R.id.send_message_button);
        userMessageInput = findViewById(R.id.input_group_message);
        displayTextMessages = findViewById(R.id.group_chat_text_display);
        mScrollView = findViewById(R.id.my_scroll_view);
        Temporizador = findViewById(R.id.tiempo);
    }

    private void GetUserInfo() {
        DocumentReference docRef = db.collection("users").document(currentUserID);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                currentUserName = documentSnapshot.getData().get("Nombre").toString() + " " + documentSnapshot.getData().get("ApellidoPaterno").toString() + " " + documentSnapshot.getData().get("ApellidoMaterno").toString();

            }
        });
    }

    private void SaveMessageInfoToDatabase() {
        String message = userMessageInput.getText().toString();

        if(TextUtils.isEmpty(message))
        {
            Toast.makeText(this, "Por favor escriba un mensaje primero", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Mensajes mensaje = new Mensajes();

            mensaje.UserID = currentUserID;

            mensaje.Nombre = currentUserName;

            mensaje.Mensaje = message;

            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            mensaje.Fecha = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            mensaje.Tiempo = currentTimeFormat.format(calForTime.getTime());

            db.collection("Chats").document(PeticionID).collection("Mensajes")
                    .add(mensaje)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {

                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            DisplayMessages();
                        }
                    });
        }
    }

    private void DisplayMessages() {
        try {
            db.collection("Chats").document(PeticionID).collection("Mensajes")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            displayTextMessages.setText("");

                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String chatDate = document.getData().get("Fecha").toString();
                                    String chatMessage = document.getData().get("Mensaje").toString();
                                    String chatName = document.getData().get("Nombre").toString();
                                    String chatTime = document.getData().get("Tiempo").toString();

                                    displayTextMessages.append(chatName + ":\n" + chatMessage + "\n" + chatTime + "     " + chatDate + "\n\n\n");
                                    mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                                }
                            } else {
                                Log.i("Error en Query", "Error getting documents: ", task.getException());
                            }
                        }
                    });
        } catch (Exception e){
            //No hay mensajes
        }
    }

    public void Contador(){

        Reloj = new CountDownTimer(60000,1000) {
            @Override
            public void onTick(long milisegundosRestantes) {
                //12:00
                Log.i("milisegundos",String.valueOf(milisegundosRestantes));
                long segundos = milisegundosRestantes /1000 % 60;
                long minutos = milisegundosRestantes /60000;
                Temporizador.setText(String.valueOf(minutos)+ ":" + String.valueOf(segundos));

            }
            @Override
            public void onFinish() {

                new AlertDialog.Builder(GroupChat.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Se termino el tiempo de la sesión desea continuar con otra ?")
                        .setPositiveButton("Aceptar",new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Reloj.cancel();
                                Contador();
                            }
                        })
                        .setNegativeButton("Rechazar",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Reloj.cancel();
                        //PeticionID = intent.getStringExtra("PeticionID");
                        db.collection("Peticiones").document(PeticionID)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Intent intent = new Intent(getApplicationContext(), Calificaciones.class);

                                        intent.putExtra("TutorID",TutorID);
                                        intent.putExtra("AlumnoID",AlumnoID);

                                        startActivity(intent);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                    }
                }).show();


            }
        }.start();
    }


    @Override
    public void onBackPressed() {
        //Si se presiona el botón de retroceso

        super.onBackPressed();
    }
}