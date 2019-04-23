package com.example.tutor40;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class Chat extends AppCompatActivity {
    //Firebase
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    //Controles
    Toolbar toolbar;
    ImageButton SendMessageButton, SendImageButton;
    EditText userMessageInput;
    ScrollView mScrollView;
    TextView displayTextMessages, Temporizador;

    //Variables
    String currentUserID, currentUserRole, currentUserName, PeticionID, AsesorID, AlumnoID;
    CountDownTimer Reloj;
    Integer CantidadExtensiones = 3, ExtensionesUsadas = 0;
    Date FechaCreacion;
    ArrayList<Mensajes> mensajesChat;

    private static final int GalleryPick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Firebase
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

        SendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GalleryPick);
            }
        });

        //Comienza el temporizador del chat
        reiniciarTemporizador();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {

    }

    private void InitializeFields()
    {
        //Traer el id de la peticion
        PeticionID = getIntent().getStringExtra("PeticionID");

        db.collection("Peticiones").document(PeticionID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        //Informacion de participantes
                        AsesorID = documentSnapshot.getData().get("AsesorID").toString();
                        AlumnoID = documentSnapshot.getData().get("AlumnoID").toString();

                        Timestamp fecha = (Timestamp) documentSnapshot.getData().get("FechaCreacion");
                        FechaCreacion = fecha.toDate();

                        if(getIntent().getStringExtra("Rol").equals("Ck5Tnzr0ipmAzKpQpTDX"))
                        {
                            db.collection("users").document(AlumnoID)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            setTitle("Chat con: " + documentSnapshot.getData().get("Nombre").toString() + " " + documentSnapshot.getData().get("ApellidoPaterno").toString());
                                        }
                                    });
                        }
                        else if (getIntent().getStringExtra("Rol").equals("I60WiSHvFyzJqUT0IU20"))
                        {
                            db.collection("users").document(AsesorID)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            setTitle("Chat con: " + documentSnapshot.getData().get("Nombre").toString() + " " + documentSnapshot.getData().get("ApellidoPaterno").toString());
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
        SendImageButton = (ImageButton) findViewById(R.id.send_image_button);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(getIntent().getStringExtra("Rol").equals("Ck5Tnzr0ipmAzKpQpTDX")){
            getMenuInflater().inflate(R.menu.chat_menu_asesor, menu);
        } else if (getIntent().getStringExtra("Rol").equals("I60WiSHvFyzJqUT0IU20")){
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
            new AlertDialog.Builder(Chat.this)
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

                                                            intent.putExtra("AsesorID",AsesorID);
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

    public void reiniciarTemporizador()
    {
        Log.i("Extensiones",ExtensionesUsadas.toString());
        if(ExtensionesUsadas < CantidadExtensiones)
        {
            final long tiempoTemporizador;

            if(ExtensionesUsadas == 0){
                tiempoTemporizador = 60000;
            } else {
                //Calcular tiempo restante
                if((new Date().getTime() - FechaCreacion.getTime()) <= 0){
                    tiempoTemporizador = 60000;
                } else {
                    tiempoTemporizador = ((new Date().getTime() - FechaCreacion.getTime()) + 60000) - (ExtensionesUsadas * 60000);
                }
            }

            ExtensionesUsadas = ExtensionesUsadas + 1;

            Reloj = new CountDownTimer(tiempoTemporizador,1000)
            {
                @Override
                public void onTick(long milisegundosRestantes)
                {
                    if(FechaCreacion == null){
                        db.collection("Chats").document(PeticionID)
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        Timestamp fecha = (Timestamp) documentSnapshot.getData().get("FechaCreacion");
                                        FechaCreacion = fecha.toDate();

                                        //Segundos restantes calculados desde la creación en servidor
                                        long segundos = (new Date().getTime() - FechaCreacion.getTime()) / 1000 % 60;
                                        long minutos = (new Date().getTime() - FechaCreacion.getTime()) / 60000 % 60;

                                        String segundosEdit;
                                        String minutosEdit;

                                        if (segundos < 10) {
                                            segundosEdit = "0" + String.valueOf(segundos);
                                        } else {
                                            segundosEdit = String.valueOf(segundos);
                                        }

                                        if (minutos < 10) {
                                            minutosEdit = "0" + String.valueOf(minutos);
                                        } else {
                                            minutosEdit = String.valueOf(minutos);
                                        }

                                        Temporizador.setText(String.valueOf(minutosEdit) + ":" + String.valueOf(segundosEdit));
                                    }
                                });
                    } else {
                        //Segundos restantes calculados desde la creación en servidor
                        long segundos = (new Date().getTime() - FechaCreacion.getTime()) / 1000 % 60;
                        long minutos = (new Date().getTime() - FechaCreacion.getTime()) / 60000 % 60;

                        String segundosEdit;
                        String minutosEdit;

                        if (segundos < 10) {
                            segundosEdit = "0" + String.valueOf(segundos);
                        } else {
                            segundosEdit = String.valueOf(segundos);
                        }

                        if (minutos < 10) {
                            minutosEdit = "0" + String.valueOf(minutos);
                        } else {
                            minutosEdit = String.valueOf(minutos);
                        }

                        Temporizador.setText(String.valueOf(minutosEdit) + ":" + String.valueOf(segundosEdit));
                    }
                }

                @Override
                public void onFinish()
                {
                    new AlertDialog.Builder(Chat.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Tiempo Agotado¿Desea continuar con la sesión?")
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
                                                                    if (currentUserRole.equals("Ck5Tnzr0ipmAzKpQpTDX")){
                                                                        Intent intent = new Intent(getApplicationContext(), AsesorMain.class);
                                                                        startActivity(intent);
                                                                    }
                                                                    else
                                                                    {
                                                                    Intent intent = new Intent(getApplicationContext(), Calificaciones.class);
                                                                   // if (currentUserRole.equals("Ck5Tnzr0ipmAzKpQpTDX")) {
                                                                      //  intent.putExtra("UserID",AlumnoID);
                                                                    //}
                                                                    //else
                                                                        if(currentUserRole.equals("I60WiSHvFyzJqUT0IU20")){
                                                                        intent.putExtra("UserID",AsesorID);
                                                                    }
                                                                    intent.putExtra("RolID",currentUserRole);

                                                                    startActivity(intent);
                                                                }
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
            new AlertDialog.Builder(Chat.this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Han utilizado todas sus extensiones. Se terminará el chat al acabar el tiempo restante.")
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener(){

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
                                                                    if (currentUserRole.equals("Ck5Tnzr0ipmAzKpQpTDX")) {
                                                                        intent.putExtra("UserID",AlumnoID);
                                                                    }
                                                                    else if(currentUserRole.equals("I60WiSHvFyzJqUT0IU20")){
                                                                        intent.putExtra("UserID",AsesorID);
                                                                    }
                                                                    intent.putExtra("RolID",currentUserRole);

                                                                    startActivity(intent);
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {

                                                                }
                                                            });
                                                }
                                            });
                                }
                            }).show();
        }
    }

    public void DisplayMessages()
    {
        try
        {
            db.collection("Chats").document(PeticionID).collection("Mensajes")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            displayTextMessages.setText("");
                            mensajesChat= new ArrayList<>();

                            if(task.isSuccessful())
                            {
                                for(QueryDocumentSnapshot document : task.getResult())
                                {
                                    //Extraer todos los mensajes de la coleccion
                                    Mensajes mensaje = new Mensajes();
                                    mensaje.Nombre = document.getData().get("Nombre").toString();
                                    mensaje.Mensaje = document.getData().get("Mensaje").toString();
                                    mensaje.UserID = document.getData().get("UserID").toString();

                                    Timestamp fecha = (Timestamp) document.getData().get("Fecha");
                                    mensaje.Fecha = fecha.toDate();

                                    mensajesChat.add(mensaje);
                                }
                                //Mostrar los mensajes en orden cronologico
                                Collections.sort(mensajesChat, new SortByDate());

                                for (Mensajes mensaje : mensajesChat) {
                                    displayTextMessages.append(mensaje.Nombre + ": " + mensaje.Mensaje + "\n");
                                    //mensaje.Fecha.getHours() + ":" + mensaje.Fecha.getMinutes() + ":" + mensaje.Fecha.getSeconds() + "     " + mensaje.Fecha.toString() +
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

            mensaje.Fecha = new Date();

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

    private void GetUserInfo()
    {
        DocumentReference docRef = db.collection("users").document(currentUserID);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                currentUserName = documentSnapshot.getData().get("Nombre").toString() + " ";//+ documentSnapshot.getData().get("ApellidoPaterno").toString() + " " + documentSnapshot.getData().get("ApellidoMaterno").toString()
                currentUserRole = documentSnapshot.getData().get("RolID").toString();
            }
        });
    }

}
