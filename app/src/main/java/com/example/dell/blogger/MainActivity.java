package com.example.dell.blogger;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    private RecyclerView bloglist;
    private DatabaseReference mainRef;
    private DatabaseReference mainRefUsers;
    private DatabaseReference mainRefLike;
    private FirebaseAuth mainAuth;
    private boolean liked = false;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         bloglist= findViewById(R.id.bloglist);
         mainRef = FirebaseDatabase.getInstance().getReference("root");
         mainRefUsers = FirebaseDatabase.getInstance().getReference("Users");
         mainRefLike = FirebaseDatabase.getInstance().getReference("Liked");
         mainRefUsers.keepSynced(true);
         mainRef.keepSynced(true);
         mainRefLike.keepSynced(true);
         bloglist.setHasFixedSize(true);
         bloglist.setLayoutManager(new LinearLayoutManager(this));
         mainAuth = FirebaseAuth.getInstance();
         authStateListener = new FirebaseAuth.AuthStateListener() {
             @Override
             public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                 FirebaseUser user = firebaseAuth.getCurrentUser();
                 if(user == null) {
                     Intent loginIntent = new Intent(MainActivity.this,LogInActivity.class);
                     loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // user wont be able to go back
                     startActivity(loginIntent);
                 }
             }
         };
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserExist();

        mainAuth.addAuthStateListener(authStateListener);
        // UPDATED WAY TO USE FIREBASERECYCLERADAPTER
        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                .setQuery(mainRef,Users.class).setLifecycleOwner(this)
                .build();

       FirebaseRecyclerAdapter firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users,BlogViewHolder>(options) {
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

                       Intent description = new Intent(MainActivity.this,Description.class);
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
                                       if (dataSnapshot.child(post_key).hasChild(mainAuth.getCurrentUser().getUid())) {

                                           mainRefLike.child(post_key).child(mainAuth.getCurrentUser().getUid()).removeValue();
                                       } else {
                                           mainRefLike.child(post_key).child(mainAuth.getCurrentUser().getUid()).setValue(mainAuth.getCurrentUser().getEmail());
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
        bloglist.setAdapter(firebaseRecyclerAdapter);
    }

    private void checkUserExist() {
        if (mainAuth.getCurrentUser() != null) {
            final String userId = mainAuth.getCurrentUser().getUid();
            mainRefUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(userId)) {

                        Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
                        profileIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // user wont be able to go back
                        startActivity(profileIntent);

                    } else {

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
    @Override
    protected void onStop() {
        super.onStop();

        if(authStateListener != null){
            mainAuth.removeAuthStateListener(authStateListener);
        }
    }

    // MENU ITEMS
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.add){
            startActivity(new Intent(MainActivity.this,PostActivity.class));
        }else if(item.getItemId() == R.id.logOut){
            mainAuth.signOut();
            LoginManager.getInstance().logOut();

        }else if(item.getItemId() == R.id.profile){
            startActivity(new Intent(MainActivity.this,Dashboard.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void Print(String str) {

        Toast.makeText(MainActivity.this,str,Toast.LENGTH_SHORT).show();
    }
}
