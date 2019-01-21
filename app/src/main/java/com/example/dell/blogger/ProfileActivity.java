package com.example.dell.blogger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {


    private ImageButton dp;
    private EditText username;
    private Button forward;
    private Uri imageUri = null;
    private static final int GALLERY_INTENT = 123;
    private FirebaseAuth profileAuth;
    private StorageReference storageReference;
    private DatabaseReference profileRef;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dp = findViewById(R.id.dp);
        username = findViewById(R.id.username);
        forward = findViewById(R.id.forward);

        profileAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("Profile_Pic");
        profileRef = FirebaseDatabase.getInstance().getReference("Users");
        progressDialog = new ProgressDialog(this);


        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupAcc();
            }
        });

        dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_INTENT);

            }
        });

    }

    private void setupAcc() {


        final String fullname = username.getText().toString().trim();
        final String userId = profileAuth.getCurrentUser().getUid();

        if (fullname.isEmpty()) {
            username.setError("Please enter your name");
            username.requestFocus();
            return;
        }  else if (imageUri == null) {
            print("Upload an image");
        }else{
            progressDialog.setMessage("Almost Done...");
            progressDialog.show();

            final StorageReference path = storageReference.child(imageUri.getLastPathSegment());
            path.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                         String downloadurl = taskSnapshot.getDownloadUrl().toString();
                         profileRef.child(userId).child("name").setValue(cap(fullname));
                         profileRef.child(userId).child("image").setValue(downloadurl);

                         progressDialog.dismiss();
                    Intent mainIntent = new Intent(ProfileActivity.this, MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // user wont be able to go back
                    startActivity(mainIntent);
                }
            });

        }

    }

    private Object cap(String fullname) {
        String str = fullname.substring(0,1).toUpperCase().concat(fullname.substring(1));
        return str;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_INTENT && resultCode == RESULT_OK){
            imageUri = data.getData();
            Picasso.with(ProfileActivity.this).load(imageUri).fit().into(dp);
        }
    }

    private void print(String str) {
        Toast.makeText(ProfileActivity.this,str,Toast.LENGTH_SHORT).show();
    }
}
