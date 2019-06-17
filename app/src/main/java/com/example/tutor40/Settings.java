package com.example.tutor40;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class Settings extends AppCompatActivity
{
    private Button UpdateAccountSettings;
    private EditText userName, userStatus;
    private ImageView userProfileImage;
    private String currentUserID;
    private static final int GalleryPick = 1;

    private FirebaseAuth mAuth;
    private StorageReference UserProfileImagesRef;
    FirebaseFirestore db;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        currentUserID = mAuth.getCurrentUser().getUid();
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        InitializeFieds();

        userName.setVisibility(View.INVISIBLE);

        UpdateAccountSettings.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                UpdateSettings();
            }
        });

        userProfileImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });
    }

    private void InitializeFieds()
    {
        UpdateAccountSettings = (Button) findViewById(R.id.update_settings_button);
        userName = (EditText) findViewById(R.id.set_user_name);
        userStatus = (EditText) findViewById(R.id.set_profile_status);
        userProfileImage = (ImageView) findViewById(R.id.set_profile_image);
        loadingBar = new ProgressDialog(this);
    }

    private void UpdateSettings()
    {
        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();

        if(TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(this, "Por favor escribe tu nombre de usuario primero...", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(setStatus))
        {
            Toast.makeText(this, "Por favor escribe tu estatus...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            HashMap<String, String> profileMap = new HashMap<>();
            profileMap.put("name", setUserName);
            profileMap.put("status", setStatus);

            db.collection("users").document(currentUserID)
                    .set(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>(){
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                SendUserToMainActivity();
                                Toast.makeText(Settings.this, "Perfil actualizado con exito...", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(Settings.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(Settings.this, AsesorMain.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


}
