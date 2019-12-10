package com.anantdevelopers.mychat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

     private EditText mMessageEditText;
     private Button mSendButton;
     private ImageButton mPhotoPickerButton;

     private FirebaseDatabase firebaseDatabase;
     private DatabaseReference databaseReference;

     @Override
     protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_main);

          firebaseDatabase = FirebaseDatabase.getInstance();
          databaseReference = firebaseDatabase.getReference().child("messages");

          mMessageEditText = findViewById(R.id.messageEditText);
          mSendButton = findViewById(R.id.sendButton);
          mPhotoPickerButton = findViewById(R.id.photoPickerButton);

          mSendButton.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                    myClass myClass = new myClass(mMessageEditText.getText().toString(), "anonymous", null);
                    String unique_id = databaseReference.push().getKey();
                    databaseReference.child(unique_id).setValue(myClass);
               }
          });



     }
}
