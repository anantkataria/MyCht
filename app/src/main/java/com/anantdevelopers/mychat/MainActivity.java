package com.anantdevelopers.mychat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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

     private ChildEventListener mChildEventListener;

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

                    TextKeyListener.clear(mMessageEditText.getText());
               }
          });

          //after data at given reference is added, the childeventlistener's onchildadded method will get invoked in which
          //we provide the code to show messages back to the UI using adapter/listview
          mChildEventListener = new ChildEventListener() {
               @Override
               public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    myClass myClass = dataSnapshot.getValue(myClass.class);
                    mMessageAdapter.add(myClass);
               }

               @Override
               public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
               }

               @Override
               public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
               }

               @Override
               public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
               }

               @Override
               public void onCancelled(@NonNull DatabaseError databaseError) {
               }
          };

          databaseReference.addChildEventListener(mChildEventListener);


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

     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
          MenuInflater inflater = getMenuInflater();
          inflater.inflate(R.menu.main_menu, menu);
          return true;
     }

     @Override
     public boolean onOptionsItemSelected(@NonNull MenuItem item) {
          return super.onOptionsItemSelected(item);
     }
}
