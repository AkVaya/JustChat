package com.example.android.justchat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;

    View groupFragmentView;
    RecyclerView recyclerViewGroups;
    List<String> groups;
    DatabaseReference mRef;

    public GroupFragment() {
    }

    // TODO: Rename and change types and number of parameters
    public static GroupFragment newInstance(String param1, String param2) {
        GroupFragment fragment = new GroupFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        groupFragmentView =  inflater.inflate(R.layout.fragment_group, container, false);


        groups = new ArrayList<>();
        mRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        RetrieveGroups();
        recyclerViewGroups = groupFragmentView.findViewById(R.id.recyclerViewGroups);
        recyclerViewGroups.setHasFixedSize(true);
        recyclerViewGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        return groupFragmentView;
    }

    private void RetrieveGroups() {
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> set = new HashSet<>();
                for(DataSnapshot temp : snapshot.getChildren()){
                    String x = temp.getKey();
                    set.add(temp.getKey());
                }
                groups.clear();
                groups.addAll(set);
                recyclerViewGroupsAdapter adapter = new recyclerViewGroupsAdapter(getContext(), groups, new recyclerViewGroupsAdapter.onNoteListener() {
                    @Override
                    public void onNoteClick(int position) {
                        String currName = groups.get(position);
                        Intent intent = new Intent(getContext(), GroupChatActivity.class);
                        intent.putExtra("groupName",currName);
                        startActivity(intent);
                    }
                });
                recyclerViewGroups.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}