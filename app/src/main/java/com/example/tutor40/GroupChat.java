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
import android.view.Menu;
import android.view.MenuItem;
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
    private String currentUserID, currentUserRole, currentUserName, PeticionID, TutorID, AlumnoID;
    private CountDownTimer Reloj;
    private Integer CantidadExtensiones = 3, ExtensionesUsadas = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        //Traer el id de la peticion
        Intent intent = getIntent();
        PeticionID = intent.getStringExtra("PeticionID");

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

        //Comienza el temporizador del chat
        reiniciarTemporizador();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    private void InitializeFields() {
        View v = getLayoutInflater().inflate(R.layout.app_bar_layout,null);
        mToolbar = (Toolbar) v.findViewById(R.id.toolbar);

        db.collection("Peticiones").document(PeticionID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        TutorID = documentSnapshot.getData().get("TutorID").toString();
                        AlumnoID = documentSnapshot.getData().get("AlumnoID").toString();

                        if(currentUserID == TutorID){
                            db.collection("users").document(AlumnoID)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            mToolbar.setTitle("Chat con:" + documentSnapshot.getData().get("Nombre").toString() + " " + documentSnapshot.getData().get("ApellidoPaterno").toString() + " " + documentSnapshot.getData().get("ApellidoMaterno").toString());
                                        }
                            });
                        }
                        else {
                            db.collection("users").document(TutorID)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            mToolbar.setTitle("Chat con:" + documentSnapshot.getData().get("Nombre").toString() + " " + documentSnapshot.getData().get("ApellidoPaterno").toString() + " " + documentSnapshot.getData().get("ApellidoMaterno").toString());
                                        }
                                    });
                        }
                    }
                });

        SendMessageButton = findViewById(R.id.send_message_button);
        userMessageInput = findViewById(R.id.input_group_message);
        displayTextMessages = findViewById(R.id.group_chat_text_display);
        mScrollView = findViewById(R.id.my_scroll_view);
        Temporizador = findViewById(R.id.tiempo);
    }

    private void GetUserInfo()
    {
        DocumentReference docRef = db.collection("users").document(currentUserID);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                currentUserName = documentSnapshot.getData().get("Nombre").toString() + " " + documentSnapshot.getData().get("ApellidoPaterno").toString() + " " + documentSnapshot.getData().get("ApellidoMaterno").toString();
                currentUserRole = documentSnapshot.getData().get("RolID").toString();
            }
        });
    }

    private void SaveMessageInfoToDatabase()
    {
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

    private void DisplayMessages()
    {
        try
        {
            db.collection("Chats").document(PeticionID).collection("Mensajes")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            displayTextMessages.setText("");

                            if(task.isSuccessful())
                            {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String chatDate = document.getData().get("Fecha").toString();
                                    String chatMessage = document.getData().get("Mensaje").toString();
                                    String chatName = document.getData().get("Nombre").toString();
                                    String chatTime = document.getData().get("Tiempo").toString();

                                    displayTextMessages.append(chatName + ":\n" + chatMessage + "\n" + chatTime + "     " + chatDate + "\n\n\n");
                                    mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                                }
                            }
                            else
                            {
                                Log.i("Error en Query", "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }
        catch(Exception e)
        {
            //No hay mensajes
        }
    }

    public void reiniciarTemporizador()
    {
        if(ExtensionesUsadas < CantidadExtensiones)
        {
            ExtensionesUsadas += ExtensionesUsadas;

            Reloj = new CountDownTimer(60000,1000)
            {
                @Override
                public void onTick(long milisegundosRestantes)
                {
                    long segundos = milisegundosRestantes / 1000 % 60;
                    long minutos = milisegundosRestantes / 60000;
                    String segundosEdit;

                    if(segundos < 10)
                    {
                        segundosEdit = "0" + String.valueOf(segundos);
                    }
                    else
                    {
                        segundosEdit = String.valueOf(segundos);
                    }

                    Temporizador.setText(String.valueOf(minutos)+ ":" + String.valueOf(segundosEdit));
                }

                @Override
                public void onFinish()
                {
                    new AlertDialog.Builder(GroupChat.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Se termino el tiempo de la sesión, ¿Desea reiniciar el Temporizador?")
                            .setPositiveButton("Aceptar",new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Reloj.cancel();
                                    reiniciarTemporizador();
                                }
                            })
                            .setNegativeButton("Rechazar",new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    db.collection("Peticiones").document(PeticionID)
                                            .delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    //Borrar chat tambien
                                                    db.collection("Chats").document(PeticionID)
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
        else
        {
            new AlertDialog.Builder(GroupChat.this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Han utilizado todas sus extensiones. Se terminara el chat al acabar el tiempo restante.")
                    .setPositiveButton("Aceptar", null).show();
        }
    }


    @Override
    public void onBackPressed() {
        //Si se presiona el botón de retroceso

        super.onBackPressed();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(mAuth.getCurrentUser().getUid() == "Ck5Tnzr0ipmAzKpQpTDX"){
            Log.i("It's Happening!","Tutor");
            getMenuInflater().inflate(R.menu.chat_menu_tutor, menu);
        } else if ( mAuth.getCurrentUser().getUid() == "I60WiSHvFyzJqUT0IU20"){
            Log.i("It's Happening!","Alumno");
            getMenuInflater().inflate(R.menu.chat_menu_alumno, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.extender){
            reiniciarTemporizador();
        }else if (id == R.id.solicitarExtension) {
            //TODO: Crear mensaje que alerte al tutor de el tiempo restante
        }else if (id == R.id.terminar){
            new AlertDialog.Builder(GroupChat.this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("¿En verdad deseas terminar el Chat?")
                    .setMessage("Asegurate de que la duda halla quedado resuelta.")
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            db.collection("Peticiones").document(PeticionID)
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //Borrar chat tambien
                                            db.collection("Chats").document(PeticionID)
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
                                                            Log.i("Error", "Error borrando Chat");
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.i("Error", "Error borrando Peticion");
                                        }
                                    });
                        }
                    })
                    .setNegativeButton("Rechazar", null).show();
        }
        return super.onOptionsItemSelected(item);
    }
}