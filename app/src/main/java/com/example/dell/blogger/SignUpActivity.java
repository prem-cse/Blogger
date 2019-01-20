package com.example.dell.blogger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private EditText email; //#ffcb2b
    private EditText password;
    private Button signUp;
    private ProgressDialog progressDialog;
    private DatabaseReference mref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        signUp = findViewById(R.id.signup);
        progressDialog = new ProgressDialog(this);
        mref = FirebaseDatabase.getInstance().getReference("Users");

        auth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                    finish();
                }

            }
        };

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSignUp();
            }
        });
    }

        private void doSignUp() {
            final String Email = email.getText().toString().trim();
            String Password = password.getText().toString().trim();
            // Cases for Validation
            if(Email.isEmpty()){
                email.setError("Email is required");
                email.requestFocus();
                return;
            }
            if(!Patterns.EMAIL_ADDRESS.matcher(Email).matches()){
                email.setError("Invalid Email");
                email.requestFocus();
                return;
            }
            if(Password.isEmpty()){
                password.setError("Password is required");
                password.requestFocus();
                return;
            }
            if(Password.length()<8){
                password.setError("Minimum length should be 8");
                password.requestFocus();
                return;
            }
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
            auth.createUserWithEmailAndPassword(Email,Password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    progressDialog.dismiss();

                    if(task.isSuccessful()){
                        String userId = auth.getCurrentUser().getUid();
                        DatabaseReference current_user = mref.child(userId);
                        current_user.child("Email").setValue(Email);
                        current_user.child("Image").setValue("default");

                        Print("Successfully Registered");
                        startActivity(new Intent(SignUpActivity.this,ProfileActivity.class));

                    }else{

                        if(task.getException() instanceof FirebaseAuthUserCollisionException)
                            Print("User already Exists");
                        else Print(task.getException().getMessage());

                    }
                }
            });
        }
        @Override
        public void onStart() {
            super.onStart();
            auth.addAuthStateListener(authStateListener);
        }
        private void Print(String str) {
        Toast.makeText(SignUpActivity.this,str,Toast.LENGTH_SHORT).show();
        }

    }

