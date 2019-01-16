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

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
    private DatabaseReference mref;
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
        mref = FirebaseDatabase.getInstance().getReference("root");

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
                        startActivity(new Intent(PostActivity.this,MainActivity.class));

                        path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                //ADD TO DATABASE
                                DatabaseReference newPost = mref.push();
                                newPost.child("Title").setValue(mtitle);
                                newPost.child("Desc").setValue(mdesc);
                                newPost.child("Image").setValue(uri.toString());

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

    private void print(String str) {
        Toast.makeText(PostActivity.this,str,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
            progressDialog.setMessage("Please Wait...");
            progressDialog.show();
            // SETTING IMAGE URI
             imageUri = data.getData();
           // selectImg.setImageURI(imageUri);

            // PICASSO IS BEST
            Picasso.with(PostActivity.this).load(imageUri).fit().centerCrop().into(selectImg);
            progressDialog.dismiss();
        }
    }
}
