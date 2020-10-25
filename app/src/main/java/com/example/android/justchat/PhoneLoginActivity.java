 package com.example.android.justchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.gson.internal.$Gson$Preconditions;

import java.util.concurrent.TimeUnit;

 public class PhoneLoginActivity extends AppCompatActivity {

    Button buttonSendVerificationCode, buttonVerify;
    EditText editTextPhoneNumber, editTextVerificationCode;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    String mVerificationId;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    FirebaseAuth mAuth;
    ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        buttonSendVerificationCode = findViewById(R.id.send_ver_code_button);
        buttonVerify = findViewById(R.id.verify_button);
        editTextPhoneNumber = findViewById(R.id.phone_number_input);
        editTextVerificationCode = findViewById(R.id.phone_verification_code_input);
        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);

        buttonSendVerificationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String phoneNumber = editTextPhoneNumber.getText().toString();
                if(phoneNumber.isEmpty()){
                    editTextPhoneNumber.setError("Please Enter your number first");
                    editTextPhoneNumber.requestFocus();
                }
                else{
                    loadingBar.setTitle("Code Verification");
                    loadingBar.setMessage("Please wait,we arr authenticating your phone no.");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,
                            60,
                            TimeUnit.SECONDS,
                            PhoneLoginActivity.this,
                            callbacks);
                    }
                }
            }
        );

        buttonVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonSendVerificationCode.setVisibility(View.INVISIBLE);
                 editTextPhoneNumber.setVisibility(View.INVISIBLE);

                 String verificationCode = editTextVerificationCode.getText().toString();
                if (verificationCode.isEmpty()) {
                    editTextVerificationCode.setError("Please Enter the Code");
                    editTextVerificationCode.requestFocus();
                }
                else{
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }


            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                Toast.makeText(PhoneLoginActivity.this, "Invalid Phone No.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();

                buttonSendVerificationCode.setVisibility(View.VISIBLE);
                editTextPhoneNumber.setVisibility(View.VISIBLE);
                buttonVerify.setVisibility(View.INVISIBLE);
                editTextVerificationCode.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {


                mVerificationId = verificationId;
                mResendToken = token;
                loadingBar.dismiss();

                Toast.makeText(PhoneLoginActivity.this, "Code is sent.", Toast.LENGTH_SHORT).show();

                buttonSendVerificationCode.setVisibility(View.INVISIBLE);
                editTextPhoneNumber.setVisibility(View.INVISIBLE);
                buttonVerify.setVisibility(View.VISIBLE);
                editTextVerificationCode.setVisibility(View.VISIBLE);

            }
        };

        buttonVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PhoneLoginActivity.this,MainActivity.class);
            }
        });
    }

     private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
         mAuth.signInWithCredential(credential)
                 .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                     @Override
                     public void onComplete(@NonNull Task<AuthResult> task) {
                         if (task.isSuccessful()) {
                             loadingBar.dismiss();
                             Toast.makeText(PhoneLoginActivity.this, "Congrats, You are Logged in!", Toast.LENGTH_SHORT).show();

                             SendUserToMainActivity();
                         }
                         else {

                             String message=task.getException().toString();
                             Toast.makeText(PhoneLoginActivity.this, "Error: "+ message, Toast.LENGTH_SHORT).show();
                         }
                     }
                 });
    }
     private void SendUserToMainActivity() {

         Intent intent = new Intent(PhoneLoginActivity.this,MainActivity.class);
         startActivity(intent);
         finish();
     }


 }