package com.example.android.justchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    Button createAccountButton;
    EditText userEmail,userPassword;
    TextView alreadyHaveAccount;
    FirebaseAuth mAuth;
    ProgressDialog loadingBar;
    DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        createAccountButton = findViewById(R.id.register_button);
        userEmail = findViewById(R.id.register_email);
        userPassword = findViewById(R.id.register_password);
        alreadyHaveAccount = findViewById(R.id.already_have_account);
        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);
        mRef = FirebaseDatabase.getInstance().getReference();

        alreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterUser();
            }
        });
    }

    public void SendUserToLoginActivity() {

        Intent intent = new Intent((RegisterActivity.this),LoginActivity.class);
        startActivity(intent);
    }

    public void RegisterUser() {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            userEmail.setError("Email Can't be Empty");
            userEmail.requestFocus();
        }
        if(password.length()<6){
            userPassword.setError("Password needs to be of atleast 6 digits");
            userPassword.requestFocus();
        }

        else {
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Plesae wait while we are creating new account for you");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        String currentUserID = mAuth.getCurrentUser().getUid();
                        mRef.child("Users").child(currentUserID).setValue("");

                        sendUserToMainActivity();
                        Toast.makeText(RegisterActivity.this, "Account Created!", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                    else {
                        Toast.makeText(RegisterActivity.this, "Error: "+ task.getException().toString(), Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}