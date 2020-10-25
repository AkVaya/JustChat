   package com.example.android.justchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

   public class ChatActivity extends AppCompatActivity {

    String userId, userName, userImage;
    TextView textViewUserName, textViewUserLastSeen;
    CircleImageView circleImageViewUserImage;
    Toolbar toolbarChat;
    ImageButton buttonSendMessage;
    EditText editTextMessage;
    FirebaseUser mUser;
    DatabaseReference mRef;
    List<Messages> messagesList;
    RecyclerView recyclerViewMessages;
    MessagesAdapter messagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        userId = getIntent().getExtras().getString("UID");
        userName = getIntent().getExtras().getString("NAME");
        userImage = getIntent().getExtras().getString("IMAGE");
        toolbarChat = findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbarChat);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference();
        messagesList = new ArrayList<>();
        recyclerViewMessages = findViewById(R.id.recyclerViewPrivateChat);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        messagesAdapter = new MessagesAdapter(messagesList);
        recyclerViewMessages.setAdapter(messagesAdapter);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        textViewUserLastSeen = findViewById(R.id.custom_user_last_seen);
        textViewUserName = findViewById(R.id.custom_profile_name);
        circleImageViewUserImage = findViewById(R.id.custom_profile_image);
        textViewUserName.setText(userName);
        Picasso.get().load(userImage).into(circleImageViewUserImage);

        buttonSendMessage = findViewById(R.id.send_message_btn);
        editTextMessage = findViewById(R.id.input_message);

        buttonSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });
    }

       @Override
       protected void onDestroy() {
           super.onDestroy();
           messagesList.clear();
       }

       @Override
       protected void onPause() {
           super.onPause();
           messagesList.clear();
       }

       @Override
       protected void onStart() {
           super.onStart();
           mRef.child("Messages").child(mUser.getUid()).child(userId).addChildEventListener(new ChildEventListener() {
               @Override
               public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                   Messages temp = snapshot.getValue(Messages.class);
                   messagesList.add(temp);
                   messagesAdapter.notifyDataSetChanged();
                   recyclerViewMessages.smoothScrollToPosition(messagesAdapter.getItemCount());
               }

               @Override
               public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

               }

               @Override
               public void onChildRemoved(@NonNull DataSnapshot snapshot) {

               }

               @Override
               public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

               }

               @Override
               public void onCancelled(@NonNull DatabaseError error) {

               }
           });
       }

       private void SendMessage() {
        String currMessage = editTextMessage.getText().toString();
        if(currMessage.isEmpty()){
            editTextMessage.setError("Please Enter a message first");
            editTextMessage.requestFocus();
        }
        else{

            final String messageKey = mRef.child("Messages").child(mUser.getUid()).child(userId).push().getKey();

            final Map messageText = new HashMap();
            messageText.put("message", currMessage);
            messageText.put("type", "text");
            messageText.put("from", mUser.getUid());

            mRef.child("Messages").child(mUser.getUid()).child(userId).child(messageKey).setValue(messageText).addOnCompleteListener(new OnCompleteListener() {

                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        mRef.child("Messages").child(userId).child(mUser.getUid()).child(messageKey).setValue(messageText).addOnCompleteListener(new OnCompleteListener() {

                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ChatActivity.this, "Message Sent.", Toast.LENGTH_SHORT).show();
                            } else {
                                String message = task.getException().getMessage();
                                Toast.makeText(ChatActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                            editTextMessage.setText("");
                        }
                    });

                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(ChatActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
       }
   }