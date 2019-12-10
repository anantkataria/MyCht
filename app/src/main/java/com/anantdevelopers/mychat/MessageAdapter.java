package com.anantdevelopers.mychat;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.List;

public class MessageAdapter extends ArrayAdapter<myClass> {
     public MessageAdapter(Context context, int resource, List<myClass> objects) {
          super(context, resource, objects);
     }

     @NonNull
     @Override
     public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
          if (convertView == null){
               convertView = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.item_message, parent, false);
          }

          ImageView photoImageView = convertView.findViewById(R.id.photoImageView);
          TextView messageTextView = convertView.findViewById(R.id.messageTextView);
          TextView authorTextView = convertView.findViewById(R.id.nameTextView);

          myClass myClass = getItem(position);

          boolean isPhoto = myClass.getPhotoUrl() != null;
          if (isPhoto) {
               messageTextView.setVisibility(View.GONE);
               photoImageView.setVisibility(View.VISIBLE);
               Glide.with(photoImageView.getContext()).load(myClass.getPhotoUrl()).into(photoImageView);
          }
          else {
               messageTextView.setVisibility(View.VISIBLE);
               photoImageView.setVisibility(View.GONE);
               messageTextView.setText(myClass.getText());
          }
          authorTextView.setText(myClass.getName());

          return convertView;
     }
}
