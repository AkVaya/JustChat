package com.example.android.justchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {

    Toolbar mToolbar;
    ImageButton sendMessageButton;
    EditText userMessageInput;
    ScrollView mScrollView;
    TextView messages;
    String currGroupName, currUserId, currUserName, currTime, currDate;
    FirebaseAuth mAuth;
    DatabaseReference mRef , groupRef, groupMessageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        mToolbar = findViewById(R.id.group_chat_bar_layout);
        sendMessageButton = findViewById(R.id.send_message_button);
        mScrollView = findViewById(R.id.scroll_view);
        userMessageInput = findViewById(R.id.input_group_message);
        messages = findViewById(R.id.group_chat_text_display);
        mAuth = FirebaseAuth.getInstance();
        currUserId = mAuth.getCurrentUser().getUid();
        mRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currUserId);

        currGroupName = getIntent().getExtras().getString("groupName");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currGroupName);
        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currGroupName);

        getUserInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
                userMessageInput.setText("");
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        groupRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    DisplayMessages(snapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    DisplayMessages(snapshot);
                }
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


    private void getUserInfo() {
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    currUserName = snapshot.child("Name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage() {
        String message = userMessageInput.getText().toString();
        String messageKey = groupRef.push().getKey();
        if(message.isEmpty()){
            userMessageInput.setError("Message can't be Empty");
            userMessageInput.requestFocus();
        }
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
        currDate = currentDateFormat.format(calForDate.getTime());


        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
        currTime = currentTimeFormat.format(calForTime.getTime());

        HashMap<String,Object> groupChats = new HashMap<>();
        groupRef.updateChildren(groupChats);
        groupMessageRef = groupRef.child(messageKey);

        HashMap<String,Object> messageInfoMap = new HashMap<>();
        messageInfoMap.put("name", currUserName);
        messageInfoMap.put("message", message);
        messageInfoMap.put("date", currDate);
        messageInfoMap.put("time", currTime);
        groupMessageRef.updateChildren(messageInfoMap);
    }

    private void DisplayMessages(DataSnapshot snapshot) {

        String chatDate = snapshot.child("date").getValue().toString();
        String chatMessage = snapshot.child("message").getValue().toString();
        String chatName =  snapshot.child("name").getValue().toString();
        String chatTime =  snapshot.child("time").getValue().toString();
        messages.append(chatName+":\n"+ chatMessage +"\n"+ chatTime+"  "+chatDate+"\n\n\n");
        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

    }
}