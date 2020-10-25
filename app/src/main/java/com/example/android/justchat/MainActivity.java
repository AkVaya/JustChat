package com.example.android.justchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    Toolbar mToolbar;
    ViewPager mViewpager;
    TabLayout mtabLayout;
    TabsAccessorAdapter mtabsAccessorAdapter;
    DatabaseReference mRef;

    FirebaseUser mUser;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("JustChat");
        mtabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());

        mViewpager = findViewById(R.id.main_tabs_pager);
        mViewpager.setAdapter(mtabsAccessorAdapter);
        mtabLayout = findViewById(R.id.main_tabs);
        mtabLayout.setupWithViewPager(mViewpager);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mUser == null){
            SendToLogin();
        }
        else{
            verifyUserExistence();
        }
    }

    private void verifyUserExistence() {
        String currentId = mAuth.getCurrentUser().getUid();
        mRef.child("Users").child(currentId).child("Uid").setValue(currentId);
        mRef.child("Users").child(currentId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("Name").exists()){
                }
                else{
                    SendToSettings();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.main_logout_option :
                mAuth.signOut();
                SendToLogin();
                break;

            case R.id.main_settings_option :
                SendToSettings();
                break;

            case R.id.main_create_group_option :
                createNewGroup();
                break;
            case R.id.main_find_friends_option :
                sendToFindFriendsActivity();
                break;
        }
        return true;
    }


    private void createNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name :");
        final EditText editTextGroupName = new EditText(MainActivity.this);
        editTextGroupName.setHint("Group name");
        builder.setView(editTextGroupName);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = editTextGroupName.getText().toString();
                if(groupName.isEmpty()){
                    editTextGroupName.setError("Group Name can't be empty");
                    editTextGroupName.requestFocus();
                }
                else
                    CreateNewGroup(groupName);

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void CreateNewGroup(String groupName) {
        mRef.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"Group Created Successfully",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void SendToLogin() {
        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void SendToSettings() {
        Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(intent);
    }


    private void sendToFindFriendsActivity() {
        Intent intent = new Intent(MainActivity.this,FindFriendsActivity .class);
        startActivity(intent);
    }
}