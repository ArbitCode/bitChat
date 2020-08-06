package com.arbitcode.bitchat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.arbitcode.bitchat.R;
import com.arbitcode.bitchat.utilities.Constants;
import com.arbitcode.bitchat.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    private EditText inputFirstNameET, inputLastNameET, inputEmailET, inputPasswordET, inputConfirmPasswordET;
    private MaterialButton buttonSignUp;
    private ProgressBar signUpProgressBar;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        preferenceManager =new PreferenceManager(getApplicationContext());

        findViewById(R.id.imageBack).setOnClickListener(view -> onBackPressed());
        findViewById(R.id.textSignIn).setOnClickListener(view -> onBackPressed());

        inputFirstNameET = findViewById(R.id.inputFirstName);
        inputLastNameET = findViewById(R.id.inputLastName);
        inputEmailET = findViewById(R.id.inputEmail);
        inputPasswordET = findViewById(R.id.inputPassword);
        inputConfirmPasswordET = findViewById(R.id.inputConfrimPassword);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        signUpProgressBar = findViewById(R.id.signUpProgressBar);

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inputFirstNameET.getText().toString().trim().isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Enter First Name", Toast.LENGTH_SHORT).show();
                } else if (inputLastNameET.getText().toString().trim().isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Enter Last Name", Toast.LENGTH_SHORT).show();
                } else if (inputEmailET.getText().toString().trim().isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Enter Email Address", Toast.LENGTH_SHORT).show();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(inputEmailET.getText().toString()).matches()) {
                    Toast.makeText(SignUpActivity.this, "Enter Valid Email Address", Toast.LENGTH_SHORT).show();
                } else if (inputPasswordET.getText().toString().trim().isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                } else if (inputConfirmPasswordET.getText().toString().trim().isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Confirm Your Password", Toast.LENGTH_SHORT).show();
                } else if (!inputPasswordET.getText().toString().equals(inputConfirmPasswordET.getText().toString())) {
                    Toast.makeText(SignUpActivity.this, "Password & Confirm Password must be same", Toast.LENGTH_SHORT).show();
                } else {
                        signUp();
                }
            }
        });

    }
    private void signUp(){

        buttonSignUp.setVisibility(View.INVISIBLE);
        signUpProgressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore database = FirebaseFirestore.getInstance();

        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_FIRST_NAME,inputFirstNameET.getText().toString());
        user.put(Constants.KEY_LAST_NAME,inputLastNameET.getText().toString());
        user.put(Constants.KEY_EMAIL,inputEmailET.getText().toString());
        user.put(Constants.KEY_PASSWORD,inputConfirmPasswordET.getText().toString());


        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                        preferenceManager.putString(Constants.KEY_FIRST_NAME,inputFirstNameET.getText().toString());
                        preferenceManager.putString(Constants.KEY_LAST_NAME,inputLastNameET.getText().toString());
                        preferenceManager.putString(Constants.KEY_EMAIL,inputEmailET.getText().toString());
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        signUpProgressBar.setVisibility(View.INVISIBLE);
                        buttonSignUp.setVisibility(View.VISIBLE);
                        Toast.makeText(SignUpActivity.this,"Error: "+e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });





    }
}