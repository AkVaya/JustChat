package com.example.android.justchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    String userId,currentStatus;

    CircleImageView userProfileImage;
    TextView userProfileName, userProfileStatus;
    Button buttonSendMessageRequest, buttonDeclineMessageRequest;
    DatabaseReference mRefPerson, mRefChatReq, mRefContacts, mRefNotifications ;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userId = getIntent().getExtras().getString("UID");
        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_user_name);
        userProfileStatus = findViewById(R.id.visit_profile_status);
        buttonSendMessageRequest = findViewById(R.id.send_message_request_button);
        buttonDeclineMessageRequest = findViewById(R.id.decline_message_request_button);
        mRefPerson = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mRefChatReq = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        mRefContacts = FirebaseDatabase.getInstance().getReference().child("Contacts");
        mRefNotifications = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mAuth = FirebaseAuth.getInstance();
        currentStatus = "new";

        RetrieveData();

    }

    private void RetrieveData() {
        mRefPerson.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String image = snapshot.child("ProfileImage").getValue().toString();
                String name = snapshot.child("Name").getValue().toString();
                String status = snapshot.child("Status").getValue().toString();
                Picasso.get().load(image).into(userProfileImage);
                userProfileName.setText(name);
                userProfileStatus.setText(status);
                
                ManageChatRequests();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ManageChatRequests() {
        mRefChatReq.child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(userId)){
                    String requestType = snapshot.child(userId).child("request_type").getValue().toString();

                    if(requestType.equals("sent")){
                        buttonSendMessageRequest.setEnabled(true);
                        currentStatus = "sent";
                        buttonSendMessageRequest.setText("Cancel Chat Request");
                    }
                    else if(requestType.equals("received")){
                        currentStatus = "request_received";
                        buttonSendMessageRequest.setText("Accept Chat Request");
                        buttonDeclineMessageRequest.setVisibility(View.VISIBLE);
                        buttonDeclineMessageRequest.setEnabled(true);

                        buttonDeclineMessageRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelChatRequest();
                                buttonDeclineMessageRequest.setEnabled(false);
                                buttonDeclineMessageRequest.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                    else{
                        mRefContacts.child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.hasChild(userId)){
                                    currentStatus = "friends";
                                    buttonSendMessageRequest.setText("Remove Contact");
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if(!mAuth.getUid().equals(userId)){
            buttonSendMessageRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonSendMessageRequest.setEnabled(false);
                    if(currentStatus.equals("new")){
                        SendChatRequest();
                    }
                    if(currentStatus.equals("request_sent")){
                        CancelChatRequest();
                    }
                    if(currentStatus.equals("request_received")){
                        AcceptChatRequest();
                    }
                    if(currentStatus.equals("friends")){
                        RemoveFriend();
                    }
                }
            });
        }
        else{
            buttonSendMessageRequest.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveFriend() {
        mRefContacts.child(mAuth.getUid()).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    mRefContacts.child(userId).child(mAuth.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                currentStatus = "request_sent";
                                buttonSendMessageRequest.setText(" Send Chat Request");
                                buttonSendMessageRequest.setEnabled(true);
                            }
                        }
                    });
                }
            }
        });
    }

    private void AcceptChatRequest() {
        mRefContacts.child(mAuth.getUid()).child(userId).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    mRefContacts.child(userId).child(mAuth.getUid()).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mRefChatReq.child(mAuth.getUid()).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            mRefChatReq.child(userId).child(mAuth.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    buttonSendMessageRequest.setEnabled(true);
                                                    currentStatus = "friends";
                                                    buttonSendMessageRequest.setText("Remove Contact");
                                                    buttonDeclineMessageRequest.setVisibility(View.INVISIBLE);
                                                    buttonDeclineMessageRequest.setEnabled(false);
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    private void CancelChatRequest() {
        mRefChatReq.child(mAuth.getUid()).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    mRefChatReq.child(userId).child(mAuth.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                currentStatus = "request_sent";
                                buttonSendMessageRequest.setText(" Send Chat Request");
                                buttonSendMessageRequest.setEnabled(true);
                            }
                        }
                    });
                }
            }
        });
    }

    private void SendChatRequest() {
        mRefChatReq.child(mAuth.getUid()).child(userId).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    mRefChatReq.child(userId).child(mAuth.getUid()).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                HashMap<String, String> chatNotifications = new HashMap<>();
                                chatNotifications.put("from", mAuth.getUid());
                                chatNotifications.put("type", "request");
                                mRefNotifications.child(userId).push().setValue(chatNotifications).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            buttonSendMessageRequest.setEnabled(true);
                                            currentStatus = "request_sent";
                                            buttonSendMessageRequest.setText("Cancel Request");
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }
}