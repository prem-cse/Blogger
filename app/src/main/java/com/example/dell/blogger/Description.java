package com.example.dell.blogger;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class Description extends AppCompatActivity {

    private String mpost_key = null;
    private DatabaseReference mainRef;
    private FirebaseAuth mainAuth;
    private ImageView simage;
    private TextView stitle;
    private TextView sdesc;
    private Button sremove;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);

        simage = findViewById(R.id.simage);
        stitle = findViewById(R.id.stitle);
        sdesc = findViewById(R.id.sdesc);
        sremove = findViewById(R.id.sremove);
        mainAuth = FirebaseAuth.getInstance();
        mpost_key = getIntent().getExtras().getString("post_key");
        mainRef = FirebaseDatabase.getInstance().getReference("root");
        mainRef.keepSynced(true);
        mainRef.child(mpost_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {

                String post_title = dataSnapshot.child("Title").getValue(String.class);
                String post_desc = dataSnapshot.child("Desc").getValue(String.class);
                String post_image = dataSnapshot.child("Image").getValue(String.class);
                String post_uid = dataSnapshot.child("Uid").getValue(String.class);

                stitle.setText(post_title);
                sdesc.setText(post_desc);
                Picasso.with(Description.this).load(post_image).fit().into(simage);

                if(mainAuth.getCurrentUser().getUid().equals(post_uid)){
                    sremove.setVisibility(View.VISIBLE);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        sremove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                print("Deleted");
                mainRef.child(mpost_key).removeValue();
                startActivity(new Intent(Description.this,MainActivity.class));
            }
        });

    }
    private void print(String str) {
        Toast.makeText(Description.this,str,Toast.LENGTH_SHORT).show();
    }
}
