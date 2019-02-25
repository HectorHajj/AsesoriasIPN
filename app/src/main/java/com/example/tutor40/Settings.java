package com.example.tutor40;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
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

import java.util.HashMap;

public class Settings extends AppCompatActivity {

    private Button UpdateAccountSettings;
    private EditText userName, userStatus;
    private ImageView userProfileImage;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private static final int GalleryPick = 1;
    private StorageReference UserProfileImagesRef;
    private ProgressDialog loadingBar;  //Linea extra

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
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

        RetrieveUserInfo();

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
        loadingBar = new ProgressDialog(this);   //Linea extra
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if((requestCode == GalleryPick)&&(resultCode==RESULT_OK)&&(data!=null))
        {
            Uri ImgaeUri = data.getData();

            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1).start(this);
        }

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Ponga su imagen de perfil");   //Linea extra
                loadingBar.setMessage("Por favor espere, su imagen de perfil se esta actualizando..."); //Linea extra
                loadingBar.setCanceledOnTouchOutside(false);    //Linea extra
                loadingBar.show();  //Linea extra

                Uri resultUri = result.getUri();

                StorageReference filePath = UserProfileImagesRef.child(currentUserID + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>(){
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(SettingsActivity.this, "Imagen de perfil subida exitosamente...", Toast.LENGTH_SHORT).show();
                            final String downloadUrl = task.getResult().getDownloadUrl().toString();  //Línea extra
                            RootRef.child("Users").child(currentUserID).child("image").setValue(downloadUrl)    //Linea extra
                                    .addOnCompleteListener(new OnCompleteListener<Void>(){                      //Linea extra
                                        @Override                                                               //Linea extra
                                        public void onComplete(@NonNull Task<Void> task)                        //Linea extra
                                        {                                                                       //Linea extra
                                            if(task.isSuccessful())                                             //Linea extra
                                            {                                                                   //Linea extra
                                                Toast.makeText(SettingsActivity.this, "Imagen guadada en la base de datos exitosamente", Toast.LENGTH_SHORT).show();    //Linea extra
                                                loadingBar.dismiss();   //Linea extra
                                            }                           //Linea extra
                                            else                        //Linea extra
                                            {                           //Linea extra
                                                String message = task.getException().toString();    //Linea extra
                                                Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();  //Linea extra
                                                loadingBar.dismiss();   //Linea extra
                                            }                           //Linea extra
                                        }                               //Linea extra
                                    });                                 //Linea extra
                        }
                        else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();   //Linea extra
                        }
                    }
                });
            }
        }
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
            profileMap.put("uid", currentUserID);
            profileMap.put("name", setUserName);
            profileMap.put("status", setStatus);

            RootRef .child("Users").child(currentUserID).setValue(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>(){
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                SendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "Perfil actualizado con exito...", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void RetrieveUserInfo()
    {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener(){
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image")))
                        {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrievesStatus = dataSnapshot.child("status").getValue().toString();
                            String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrievesStatus);
                            Picasso.get().load(retrieveProfileImage).into(userProfileImage);    //Linea extra
                        }
                        else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
                        {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrievesStatus = dataSnapshot.child("status").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrievesStatus);
                        }
                        else
                        {
                            userName.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this, "Por favor actualiza tu información de perfil", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {
                    }
                });
    }
}
