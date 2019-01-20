package com.example.dell.blogger;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class BlogViewHolder extends RecyclerView.ViewHolder{

    private TextView post_title;
    private TextView post_desc;
    private ImageView post_image;
    private View v;
    public BlogViewHolder(View itemView) {
        super(itemView);
         v = itemView;
    }

     public void setPost_title(String title) {
       post_title = v.findViewById(R.id.post_title);
       post_title.setText(title);
     }

     public void setPost_desc(String desc) {
        post_desc = v.findViewById(R.id.post_desc);
        post_desc.setText(desc);
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