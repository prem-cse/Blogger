package com.example.dell.blogger;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class BlogViewHolder extends RecyclerView.ViewHolder{

    private TextView post_title;
    private TextView post_desc;
    private ImageView post_image;
    private TextView user;
    public ImageButton like;
    DatabaseReference refLike;
    FirebaseAuth authLike;

    private View v;
    public BlogViewHolder(View itemView) {
        super(itemView);
         v = itemView;

         like = v.findViewById(R.id.like);
         authLike = FirebaseAuth.getInstance();
         refLike = FirebaseDatabase.getInstance().getReference("Liked");
         refLike.keepSynced(true);
        // like.setOnClickListener();
    }

    public void set_Liked(final String post_key){

        refLike.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(post_key).hasChild(authLike.getCurrentUser().getUid())){

                    // change image
                    like.setImageResource(R.drawable.green_like);
                }else {
                    like.setImageResource(R.drawable.grey_like);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

     public void setPost_title(String title) {
       post_title = v.findViewById(R.id.post_title);
       post_title.setText(title);
     }

     public void setPost_desc(String desc) {
        post_desc = v.findViewById(R.id.post_desc);
        post_desc.setText(desc);
     }

    public void setUsername(String Username) {
        user = v.findViewById(R.id.user);
        user.setText(Username);

    }

    public void setPost_image(final Context context, final String image) {
         post_image = v.findViewById(R.id.post_image);
       /*  Picasso.with(context).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(post_image, new Callback() {
             @Override
             public void onSuccess() {
                 // success
             }

             @Override
             public void onError() {
                 Picasso.with(context).load(image).fit().into(post_image);
             }
         });*/
         Picasso.with(context).load(image).fit().into(post_image);
     }

 }