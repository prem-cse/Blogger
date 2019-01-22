package com.example.dell.blogger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class PostActivity extends AppCompatActivity {

    private ImageButton selectImg;
    private EditText title;
    private EditText desc;
    private Button post;
    private ProgressDialog progressDialog;
    private static final int GALLERY_REQUEST = 101;
    private  Uri imageUri = null;
    private StorageReference storageReference;
    private DatabaseReference postRef;
    private DatabaseReference postRefUsers;
    private FirebaseAuth postAuth;
   // private FirebaseUser postUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        selectImg = findViewById(R.id.imageButton);
        title = findViewById(R.id.title);
        desc = findViewById(R.id.desc);
        post = findViewById(R.id.post);
        progressDialog = new ProgressDialog(this);
        storageReference = FirebaseStorage.getInstance().getReference();
        postAuth = FirebaseAuth.getInstance();
       // postUser = postAuth.getCurrentUser();
        postRef = FirebaseDatabase.getInstance().getReference("root");
        postRefUsers = FirebaseDatabase.getInstance().getReference("Users").child(postAuth.getCurrentUser().getUid());



        selectImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // GALLERY IMAGE PICKER
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,GALLERY_REQUEST);
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String mtitle = title.getText().toString().trim();
                final String mdesc = desc.getText().toString().trim();

                // VALIDATIONS
                if (mtitle.isEmpty()) {
                    title.setError("Title is required");
                    title.requestFocus();
                    return;
                } else if (mdesc.isEmpty()) {
                    desc.setError("Description is required");
                    desc.requestFocus();
                    return;
                } else if (imageUri == null) {
                    print("Upload an image");
                }else{

                    progressDialog.setMessage("Posting...");
                    progressDialog.show();

                // ELSE STORE
                final StorageReference path = storageReference.child("Blog_images").child(imageUri.getLastPathSegment());
               path.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        progressDialog.dismiss();
                        print("Posted");
                        path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                //ADD TO DATABASE
                                final  Uri downloadUri = uri;
                                final DatabaseReference newPost = postRef.push();

                                newPost.child("Title").setValue(CamleCase(mtitle));
                                newPost.child("Desc").setValue(mdesc);
                                newPost.child("Image").setValue(downloadUri.toString());
                                newPost.child("Uid").setValue(postAuth.getCurrentUser().getUid());
                               postRefUsers.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        newPost.child("username").setValue(dataSnapshot.child("name").getValue(String.class)).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                    startActivity(new Intent(PostActivity.this,MainActivity.class));


                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        print(databaseError.getMessage());
                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                print(e.getMessage());
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        print(e.getMessage());
                    }
                });
            }

            }
        });
    }

    private Object CamleCase(String mtitle) {
       String str = mtitle.substring(0,1).toUpperCase().concat(mtitle.substring(1));
       return str;

    }

    private void print(String str) {
        Toast.makeText(PostActivity.this,str,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){

            // SETTING IMAGE URI
             imageUri = data.getData();
           // selectImg.setImageURI(imageUri);

            // PICASSO IS BEST
            Picasso.with(PostActivity.this).load(imageUri).fit().centerCrop().into(selectImg);

        }
    }
}
