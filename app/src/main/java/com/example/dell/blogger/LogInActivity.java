package com.example.dell.blogger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
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

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;


import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LogInActivity extends AppCompatActivity {
    private EditText email;
    private EditText password;
    private Button login;
    private Button SignUp;
    private Button fb;
    private FirebaseAuth logInAuth;
    private DatabaseReference logInRef;
    private ProgressDialog progressDialog;
    private Button google;
    private FirebaseAuth.AuthStateListener authStateListener;
    private static final String GTAG = "GoogleActivity";
    private static final String FTAG = "FacebookLogin";
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        google = findViewById(R.id.google);
        fb = findViewById(R.id.fb);


        logInAuth = FirebaseAuth.getInstance();
        logInRef = FirebaseDatabase.getInstance().getReference("Users");
        progressDialog = new ProgressDialog(this);
        logInRef.keepSynced(true);
        SignUp = findViewById(R.id.signup);
        mCallbackManager = CallbackManager.Factory.create();

        // state listener added
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()!=null) {

                    Toast.makeText(LogInActivity.this, "Signed in as: "+firebaseAuth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                    checkUserExists();


                }
            }
        };

        //FACEBOOK LOGIN

        final LoginButton loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(FTAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(FTAG, "facebook:onCancel");
                print("Cancelled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(FTAG, "facebook:onError", error);
                print(error.getMessage());
            }
        });

        // END FACEBOOK LOGIN

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignIn();
            }
        });
        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LogInActivity.this,SignUpActivity.class));
            }
        });
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.performClick();
            }
        });


        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                print(e.getMessage());
            }
        }
    }


        private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(GTAG, "firebaseAuthWithGoogle:" + acct.getId());

        progressDialog.setMessage("Loading...");
        progressDialog.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        logInAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Log.d(GTAG, "signInWithCredential:success");
                            print("Signed in as: "+acct.getEmail());
                            checkUserExists();
                        } else {
                            print(task.getException().getMessage());
                            Log.w(GTAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }
        private void SignIn(){

            String Email = email.getText().toString();
            String Password = password.getText().toString();
            // Validations
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
            progressDialog.setMessage("Logging In...");
            progressDialog.show();
            logInAuth.signInWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                  progressDialog.dismiss();
                    if (!task.isSuccessful()) {
                        print(task.getException().getMessage());
                    }else {
                        // CHECK USER EXISTS FOR FIRST TIME LOGIN
                       //checkUserExists();
                       // print("Successfully Logged In !!!");

                    }
                }
            });
        }
        private void print(String str) {
            Toast.makeText(LogInActivity.this, str, Toast.LENGTH_SHORT).show();
        }

        private void checkUserExists(){
        if(logInAuth.getCurrentUser()!=null) {
            final String userId = logInAuth.getCurrentUser().getUid();
            logInRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(userId)) {

                        Intent mainIntent = new Intent(LogInActivity.this, MainActivity.class);
                        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // user wont be able to go back
                        startActivity(mainIntent);
                    } else {
                        Intent profileIntent = new Intent(LogInActivity.this, ProfileActivity.class);
                        profileIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // user wont be able to go back
                        startActivity(profileIntent);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        }
    @Override
    protected void onStart() {
        super.onStart();
        logInAuth.addAuthStateListener(authStateListener);
        // IMP....
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(authStateListener != null){
            logInAuth.removeAuthStateListener(authStateListener);
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(FTAG, "handleFacebookAccessToken:" + token);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        logInAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            print(task.getException().getMessage());
                        }
                        progressDialog.dismiss();
                    }
                });
    }
}

