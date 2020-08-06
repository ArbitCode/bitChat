package com.arbitcode.bitchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.arbitcode.bitchat.R;
import com.arbitcode.bitchat.adapters.UsersAdapter;
import com.arbitcode.bitchat.listeners.UsersListener;
import com.arbitcode.bitchat.models.User;
import com.arbitcode.bitchat.utilities.Constants;
import com.arbitcode.bitchat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements UsersListener {
    private PreferenceManager preferenceManager;
    private List<User>users;
    private UsersAdapter usersAdapter;
    private TextView textErrorMessage;
    private SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferenceManager =new PreferenceManager(getApplicationContext());

        TextView textTitle =findViewById(R.id.textTitle);
        textTitle.setText(String.format(
                "%s %s",
                preferenceManager.getString(Constants.KEY_FIRST_NAME),
                preferenceManager.getString(Constants.KEY_LAST_NAME)
        ));

        findViewById(R.id.textSignOut).setOnClickListener(view -> signOut());

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if(task.isSuccessful() && task.getResult()!=null){
                sendFCMTokenToDatabase(task.getResult().getToken());
            }
        });

        RecyclerView usersRecyclerView = findViewById(R.id.usersRecyclerView);
        textErrorMessage = findViewById(R.id.textErrorMessage);
        swipeRefreshLayout =findViewById(R.id.swipeRefreshLayout);
        users = new ArrayList<>();
        usersAdapter = new UsersAdapter(users,this);
        usersRecyclerView.setAdapter(usersAdapter);

        swipeRefreshLayout.setOnRefreshListener(this::getUsers);

        getUsers();


    }

    private void getUsers(){

        swipeRefreshLayout.setRefreshing(true);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get().addOnCompleteListener(task -> {
                    swipeRefreshLayout.setRefreshing(false);
                    String myUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult() !=null){
                        users.clear();
                        for(QueryDocumentSnapshot documentSnapshots:task.getResult()){
                            if(myUserId.equals(documentSnapshots.getId())){
                                continue;
                            }
                            User user = new User();
                            user.firstName = documentSnapshots.getString(Constants.KEY_FIRST_NAME);
                            user.lastName = documentSnapshots.getString(Constants.KEY_LAST_NAME);
                            user.email= documentSnapshots.getString(Constants.KEY_EMAIL);
                            user.token = documentSnapshots.getString(Constants.KEY_FCM_TOKEN);
                            users.add(user);

                        }
                        if(users.size()>0){
                                usersAdapter.notifyDataSetChanged();
                        }else{
                            textErrorMessage.setText(String.format("%s","No users available"));
                            textErrorMessage.setVisibility(View.VISIBLE);
                        }

                    }
                    else{
                        textErrorMessage.setText(String.format("%s","No users available"));
                        textErrorMessage.setVisibility(View.VISIBLE);
                    }

                });
    }

    private void sendFCMTokenToDatabase (String token){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_FCM_TOKEN,token)
                .addOnSuccessListener(aVoid -> Log.d("token value","Token updated successfully"))
                .addOnFailureListener(e -> Log.d("token Value","unable to send token value "+e.getMessage()));
    }

    private void signOut(){
        Toast.makeText(this,"Signing out",Toast.LENGTH_SHORT).show();
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());

        documentReference.update(updates).addOnSuccessListener(aVoid -> {
            preferenceManager.clearPreferences();
            startActivity(new Intent(getApplicationContext(),SignInActivity.class));
            finish();
        }).addOnFailureListener(e -> Toast.makeText(MainActivity.this,"unable to sign out",Toast.LENGTH_SHORT).show());

    }

    @Override
    public void initiateVideoMeeting(User user) {

        if(user.token==null || user.token.trim().isEmpty()){
            Toast.makeText(this,user.firstName+" "+user.lastName+ " is offline",Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this,"Video Meeting with "+user.firstName+" "+user.lastName,Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void initiateAudioMeeting(User user) {
        if(user.token==null || user.token.trim().isEmpty()){
            Toast.makeText(this,user.firstName+" "+user.lastName+ " is offline",Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this,"Calling "+user.firstName+" "+user.lastName,Toast.LENGTH_SHORT).show();

        }


    }
}