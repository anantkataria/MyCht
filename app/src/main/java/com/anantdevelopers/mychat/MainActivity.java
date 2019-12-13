package com.anantdevelopers.mychat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
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
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

     //for use in log messages
     private static final String TAG = "MainActivity";

     //when user is not signed in, username is anonymous
     public static final String ANONYMOUS = "anonymous";
     public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
     public static final int RC_SIGN_IN = 1;
     private static final int RC_PHOTO_PICKER = 2;

     //initializing the components that are available in activity_main.xml
     private EditText mMessageEditText;
     private Button mSendButton;
     private ImageButton mPhotoPickerButton;

     //for accessing realtime database
     private FirebaseDatabase firebaseDatabase;
     private DatabaseReference databaseReference;

     //needed for authentication of the user
     private FirebaseAuth mFirebaseAuth;
     private FirebaseAuth.AuthStateListener mAuthStateListener;

     //for storage in firebase
     private FirebaseStorage mFirebaseStorage;
     private StorageReference mChatPhotosStorageReference;

     //to show messages using listview/custom adapter technique
     private ListView mMessageListView;
     private MessageAdapter mMessageAdapter;

     //when messages are loading or things are initializing, a way to show that some
     //background processes are running
     private ProgressBar mProgressBar;

     private String mUsername;

     //if something changes in the database at the given reference, then childeventlistener
     //is invoked at that reference
     private ChildEventListener mChildEventListener;

     @Override
     protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_main);

          mUsername = ANONYMOUS;

          //attaching the initialized components with that in design side (activity_main.xml)
          mMessageEditText = findViewById(R.id.messageEditText);
          mSendButton = findViewById(R.id.sendButton);
          mPhotoPickerButton = findViewById(R.id.photoPickerButton);

          firebaseDatabase = FirebaseDatabase.getInstance();
          databaseReference = firebaseDatabase.getReference().child("messages");
          mFirebaseStorage = FirebaseStorage.getInstance();
          mChatPhotosStorageReference = mFirebaseStorage.getReference().child("chat_photos");

          //initializing auth object
          mFirebaseAuth = FirebaseAuth.getInstance();

          mMessageListView = findViewById(R.id.messageListView);
          List<myClass> myClasses = new ArrayList<>();
          mMessageAdapter = new MessageAdapter(this, R.layout.item_message, myClasses);
          mMessageListView.setAdapter(mMessageAdapter);

          mProgressBar = findViewById(R.id.progressBar);
          mProgressBar.setVisibility(ProgressBar.INVISIBLE);

          mSendButton.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                    myClass myClass = new myClass(mMessageEditText.getText().toString(), mUsername, null);
                    String unique_id = databaseReference.push().getKey();
                    databaseReference.child(unique_id).setValue(myClass);

                    TextKeyListener.clear(mMessageEditText.getText());
               }
          });


          mAuthStateListener = new FirebaseAuth.AuthStateListener() {
               @Override
               public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null){
                         // user is signed in
                         onSignedInInitialize(user.getDisplayName());
                    }
                    else {
                         // user is signed out
                         onSignedOutCleanup();
                         startActivityForResult(
                                 AuthUI.getInstance()
                                         .createSignInIntentBuilder()
                                         .setIsSmartLockEnabled(false)
                                         .setAvailableProviders(Arrays.asList(
                                                 new AuthUI.IdpConfig.GoogleBuilder().build(),
                                                 new AuthUI.IdpConfig.EmailBuilder().build())).build(), RC_SIGN_IN);
                    }
               }
          };


          //when photo icon is clicked the following code will get executed
          mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/jpeg");
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                    startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
               }
          });

          //when the text is changed in the edittext the following code will get executed
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
     protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
          super.onActivityResult(requestCode, resultCode, data);

          if (requestCode == RC_SIGN_IN){
               if(resultCode == RESULT_OK){
                    Toast.makeText(this, "signed in!", Toast.LENGTH_SHORT).show();
               }
               else if (resultCode == RESULT_CANCELED){
                    Toast.makeText(this, "sign in canceled!", Toast.LENGTH_SHORT).show();
                    finish();
               }
          }
          else if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK){
               Uri selectedImageUri = data.getData();

               //this is the storagereference where the photo will get saved
               final StorageReference photoRef = mChatPhotosStorageReference.child(selectedImageUri.getLastPathSegment());

               //uploading the selected image in the firebase storage
               //and retrieving the image url to store it in the realtime database
               UploadTask uploadTask = photoRef.putFile(selectedImageUri);


               //this is currently the best practise to upload imageurl into the firebase database
               Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                         if (!task.isSuccessful()) {
                              throw task.getException();
                         }

                         // Continue with the task to get the download URL
                         return photoRef.getDownloadUrl();
                    }
               }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                         if (task.isSuccessful()) {
                              Uri downloadUri = task.getResult();
                              myClass myClass = new myClass(null, mUsername, downloadUri.toString());
                              databaseReference.push().setValue(myClass);
                         } else {
                              // Handle failures
                              Toast.makeText(MainActivity.this, "Something wrong wile uploading image url in the realtime database", Toast.LENGTH_SHORT).show();
                         }
                    }
               });
          }
     }

     //You use onCreateOptionsMenu() to specify the options menu for an activity.
     // In this method, you can inflate your menu resource (defined in XML)
     // into the Menu provided in the callback.
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
          MenuInflater inflater = getMenuInflater();
          inflater.inflate(R.menu.main_menu, menu);
          return true;
     }

     @Override
     public boolean onOptionsItemSelected(@NonNull MenuItem item) {
          switch (item.getItemId()) {
               case R.id.sign_out_menu:
                    //sign out
                    AuthUI.getInstance().signOut(this);
                    return true;
               default:
                    return super.onOptionsItemSelected(item);
          }
     }

     @Override
     protected void onResume() {
          super.onResume();
          mFirebaseAuth.addAuthStateListener(mAuthStateListener);
     }

     @Override
     protected void onPause() {
          super.onPause();
          if (mAuthStateListener != null) {
               mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
          }
          detachDatabaseReadListener();
          mMessageAdapter.clear();

     }

     private void onSignedOutCleanup() {
          mUsername = ANONYMOUS;
          mMessageAdapter.clear();
          detachDatabaseReadListener();
     }

     private void onSignedInInitialize(String displayName) {
          mUsername = displayName;
          attachDatabaseReadListener();
     }

     private void attachDatabaseReadListener() {
          if (mChildEventListener == null) {
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
               //attaching above childeventlistener at a particular reference
               databaseReference.addChildEventListener(mChildEventListener);
          }
     }

     private void detachDatabaseReadListener() {
          if (mChildEventListener != null) {
               databaseReference.removeEventListener(mChildEventListener);
               mChildEventListener = null;
          }
     }

}

