package com.example.android.justchat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder> {
    Context mCTx;
    List<Contacts> contactsList;
    final String UID = "UID", NAME = "NAME", IMAGE = "IMAGE";
    public DatabaseReference mRefContacts, mRefChats;
    public FirebaseUser mUser;

    public ContactsAdapter(Context mCTx, List<Contacts> contactsList) {
        this.mCTx = mCTx;
        this.contactsList = contactsList;
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCTx).inflate(R.layout.users_display_layout ,null);
        ContactsViewHolder contactsViewHolder = new ContactsViewHolder(view);
        return contactsViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position) {
        final Contacts curr = contactsList.get(position);
        holder.userName.setText(curr.getName());
        holder.userStatus.setText(curr.getStatus());
        Picasso.get().load(curr.image).into(holder.profileImage);

        if(curr.getCheck1()) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mCTx, ProfileActivity.class);
                    intent.putExtra(UID, curr.getRef());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mCTx.startActivity(intent);
                }
            });
        }
        if(curr.getCheck2()){
            mUser = FirebaseAuth.getInstance().getCurrentUser();
            mRefContacts = FirebaseDatabase.getInstance().getReference().child("Contacts");
            mRefChats = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
            holder.buttonCancelRequest.setVisibility(View.VISIBLE);
            holder.buttonAcceptRequest.setVisibility(View.VISIBLE);
            holder.buttonAcceptRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRefContacts.child(mUser.getUid()).child(curr.getRef()).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mRefContacts.child(curr.getRef()).child(mUser.getUid()).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                                mRefChats.child(mUser.getUid()).child(curr.getRef()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        mRefChats.child(curr.getRef()).child(mUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    Toast.makeText(mCTx, "New Contact Saved",Toast.LENGTH_SHORT).show();
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
                    });
                }
            });
            holder.buttonCancelRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRefChats.child(mUser.getUid()).child(curr.getRef()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mRefChats.child(curr.getRef()).child(mUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(mCTx, "Request Denied",Toast.LENGTH_SHORT).show();
                                            contactsList.remove(curr);
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            });
        }
        if(curr.getCheck3()){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mCTx,ChatActivity.class);
                    intent.putExtra(UID,curr.getRef());
                    intent.putExtra(NAME,curr.getName());
                    intent.putExtra(IMAGE,curr.getImage());
                    mCTx.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder{
        TextView userName, userStatus;
        CircleImageView profileImage;
        Button buttonAcceptRequest, buttonCancelRequest;
        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            buttonAcceptRequest = itemView.findViewById(R.id.request_accept_btn);
            buttonCancelRequest = itemView.findViewById(R.id.request_cancel_btn);
        }
    }
}
