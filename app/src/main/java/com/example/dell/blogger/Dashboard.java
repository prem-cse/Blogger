package com.example.dell.blogger;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Dashboard extends AppCompatActivity {

    private DatabaseReference currRef;
    private DatabaseReference mainRefLike;
    private FirebaseAuth dashAuth;
    private FirebaseUser currUser;
    private Query query;
    private TextView curr_user;
    private boolean liked = false;
    private RecyclerView postList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        curr_user = findViewById(R.id.curr_user);
        postList = findViewById(R.id.postList);
        postList.setHasFixedSize(true);
        postList.setLayoutManager(new LinearLayoutManager(this));

        dashAuth = FirebaseAuth.getInstance();
        currUser = dashAuth.getCurrentUser();
        currRef = FirebaseDatabase.getInstance().getReference("root");
        mainRefLike = FirebaseDatabase.getInstance().getReference("Liked");
        currRef.keepSynced(true);
        mainRefLike.keepSynced(true);

        String curr_uid = currUser.getUid();
        query = currRef.orderByChild("Uid").equalTo(curr_uid);
        curr_user.setText(currUser.getEmail());





    }

    @Override
    protected void onStart() {
        super.onStart();

        // UPDATED WAY TO USE FIREBASERECYCLERADAPTER
        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(query,Users.class).setLifecycleOwner(this)
                        .build();

        FirebaseRecyclerAdapter Adapter = new FirebaseRecyclerAdapter<Users,BlogViewHolder>(options) {
            @NonNull
            @Override
            public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.blog_item, parent, false);

                return new BlogViewHolder(view);
            }
            @Override
            protected void onBindViewHolder(@NonNull BlogViewHolder holder, int position, @NonNull Users model) {

                final String post_key = getRef(position).getKey();
                holder.setPost_title(model.getTitle());
                holder.setPost_desc(model.getDesc());
                holder.setUsername(model.getUsername());
                holder.setPost_image(getApplicationContext(),model.getImage());
                holder.set_Liked(post_key);

                //WHEN USER CLICK ON CARDVIEW
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent description = new Intent(Dashboard.this,Description.class);
                        description.putExtra("post_key",post_key);
                        startActivity(description);
                    }
                });

                holder.like.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        liked = true;

                        mainRefLike.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //
                                if (liked) {
                                    if (dataSnapshot.child(post_key).hasChild(currUser.getUid())) {

                                        mainRefLike.child(post_key).child(currUser.getUid()).removeValue();
                                    } else {
                                        mainRefLike.child(post_key).child(currUser.getUid()).setValue(currUser.getEmail());
                                    }
                                    liked = false;
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
        };
        postList.setAdapter(Adapter);

    }
}
