package com.example.android.justchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    Button updateButton;
    EditText editTextUserName, editTextUserStatus;
    CircleImageView userProfileImage;
    private static final int galleryImage = 1;
    FirebaseAuth mAuth;
    DatabaseReference mRef;
    StorageReference userProfileImageRef;
    ProgressBar loadingBar;
    Uri imageUri;
    String photoUrl;
    Toolbar settingsToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        updateButton = findViewById(R.id.buttonUpdateSettings);
        editTextUserName = findViewById(R.id.editTextUsername);
        editTextUserStatus = findViewById(R.id.editTextStatus);
        userProfileImage = findViewById(R.id.userProfileImage);
        settingsToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Settings");
        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference();
        userProfileImageRef = FirebaseStorage.getInstance().getReference("Profile Images");
        loadingBar = new ProgressBar(this);

        retrieveInfo();

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentGallery = new Intent();
                intentGallery.setAction(Intent.ACTION_GET_CONTENT);
                intentGallery.setType("image/*");
                startActivityForResult(intentGallery, galleryImage);
            }
        });
    }

    private void updateProfile() {
        String Status = editTextUserStatus.getText().toString();
        String Name = editTextUserName.getText().toString();
        mRef.child("Users").child(mAuth.getUid()).child("Name").setValue(Name);
        mRef.child("Users").child(mAuth.getUid()).child("Status").setValue(Status);
        mRef.child("Users").child(mAuth.getUid()).child("ProfileImage").setValue(photoUrl);
    }

    private void retrieveInfo() {
        mRef.child("Users").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.hasChild("Name") && snapshot.hasChild("Status")){
                    String name = snapshot.child("Name").getValue().toString();
                    String status = snapshot.child("Status").getValue().toString();
                    if(snapshot.child("ProfileImage").exists()) {
                        String image = snapshot.child("ProfileImage").getValue().toString();
                        Picasso.get().load(image).into(userProfileImage);
                    }
                    editTextUserName.setText(name);
                    editTextUserStatus.setText(status);
                }
                else{
                        Toast.makeText(getApplicationContext(),"Please Update Information", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==galleryImage && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imageUri);
                userProfileImage.setImageBitmap(bitmap);
                uploadImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadImage(){
        final String temp="Profile Images/" + System.currentTimeMillis() + "." + getFileExtension(imageUri);
        final StorageReference productImageRef = FirebaseStorage.getInstance().getReference(temp);
        if(imageUri!=null){
            loadingBar.setVisibility(View.VISIBLE);
            productImageRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadingBar.setVisibility(View.INVISIBLE);
                        }
                    },500);
                    productImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri data=uri;
                            photoUrl = data.toString();
                        }
                    });
                    Toast.makeText(getApplicationContext(),"Upload Successfull", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    loadingBar.setProgress((int) progress);
                }
            });
        }else{
            Toast.makeText(getApplicationContext(),"No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendToMainActivity() {
        Intent intent = new Intent(SettingsActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}