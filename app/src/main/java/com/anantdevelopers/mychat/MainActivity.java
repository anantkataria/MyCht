package com.anantdevelopers.mychat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

     private static final String TAG = "MainActivity";

     public static final String ANONYMOUS = "anonymous";
     public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;

     private EditText mMessageEditText;
     private Button mSendButton;
     private ImageButton mPhotoPickerButton;

     private FirebaseDatabase firebaseDatabase;
     private DatabaseReference databaseReference;

     private ListView mMessageListView;
     private MessageAdapter mMessageAdapter;
     private ProgressBar mProgressBar;

     private String mUsername;

     @Override
     protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_main);

          mUsername = ANONYMOUS;

          mProgressBar = findViewById(R.id.progressBar);
          mProgressBar.setVisibility(ProgressBar.INVISIBLE);

          mMessageListView = findViewById(R.id.messageListView);
          List<myClass> myClasses = new ArrayList<>();
          mMessageAdapter = new MessageAdapter(this, R.layout.item_message, myClasses);
          mMessageListView.setAdapter(mMessageAdapter);

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

          mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                    // TODO: Fire an intent to show an image picker
               }
          });

          mMessageEditText.addTextChangedListener(new TextWatcher() {
               @Override
               public void beforeTextChanged(CharSequence s, int start, int count, int after) {
               }

               @Override
               public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(s.toString().trim().length() > 0) {
                         mSendButton.setEnabled(true);
                    }else {
                         mSendButton.setEnabled(false);
                    }
               }

               @Override
               public void afterTextChanged(Editable s) {
               }
          });





     }
}
