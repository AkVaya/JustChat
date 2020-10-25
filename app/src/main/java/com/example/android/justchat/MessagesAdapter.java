package com.example.android.justchat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import javax.security.auth.callback.PasswordCallback;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder> {

    List<Messages> messagesList;
    FirebaseUser mUser;
    DatabaseReference mRef;

    public MessagesAdapter(List<Messages> messagesList) {
        this.messagesList = messagesList;
    }

    @NonNull
    @Override
    public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout, parent,false);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        return new MessagesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessagesViewHolder holder, int position) {
        Messages curr = messagesList.get(position);

        String userId = curr.getFrom();
        String type = curr.getType();

        mRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String image = snapshot.child("ProfileImage").getValue().toString();
                Picasso.get().load(image).into(holder.circleImageViewReceiver);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if(type.equals("text")){
            holder.textViewReceiver.setVisibility(View.INVISIBLE);
            holder.circleImageViewReceiver.setVisibility(View.INVISIBLE);
            holder.textViewSenderMessage.setVisibility(View.INVISIBLE);

            if(mUser.getUid().equals(userId)){
                holder.textViewSenderMessage.setVisibility(View.VISIBLE);
                holder.textViewSenderMessage.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.textViewSenderMessage.setText(curr.getMessage());
            }
            else{

                holder.circleImageViewReceiver.setVisibility(View.VISIBLE);
                holder.textViewReceiver.setVisibility(View.VISIBLE);

                holder.textViewReceiver.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.textViewReceiver.setText(curr.getMessage());
            }
        }

    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public static class MessagesViewHolder extends RecyclerView.ViewHolder {
        TextView textViewSenderMessage, textViewReceiver;
        CircleImageView circleImageViewReceiver;
        public MessagesViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewSenderMessage = itemView.findViewById(R.id.sender_message_text);
            textViewReceiver = itemView.findViewById(R.id.receiver_message_text);
            circleImageViewReceiver = itemView.findViewById(R.id.message_profile_image);
        }
    }
}
