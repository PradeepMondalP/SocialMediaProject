package com.example.socialmediaproject2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton selectPostingImage;
    private EditText postDescrip;
    private Button updatePostButton;
    private static final int Gallery_Pick=1;
    private Uri imageUri;
    private String desCription;
    private ProgressDialog mDialog;
    private String downloadURL;
    private String saveCurrentDate , saveCurrentTime , postRandomName;

    private StorageReference postImagesRef;
    private DatabaseReference userRef , postRef;
    private FirebaseAuth mAuth;

    private String userProfileFullName ;
    private String userProfileImageURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        initialize();

        selectPostingImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });



        updatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateThePostToTheDatabase();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        updateUserStatus("online");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateUserStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUserStatus("offline");
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserStatus("online");
    }

    public void updateUserStatus(String state)
    {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("User");
        String UserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        try
        {
            String saveCurrentDate , saveCurrentTime;

            Calendar callForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, YYYY");
            saveCurrentDate = currentDate.format(callForDate.getTime());

            Calendar callForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
            saveCurrentTime = currentTime.format(callForTime.getTime());


            Map currentStateMap = new HashMap();

            currentStateMap.put("time" ,saveCurrentDate);
            currentStateMap.put("date" ,saveCurrentTime);
            currentStateMap.put("type" ,state);

            DatabaseReference userRef2 = userRef.child(UserID).child("userState");
            userRef2.updateChildren(currentStateMap);

        }
        catch(Exception e){
            Toast.makeText(this,
                    "error in updateStatus of MainActivity",
                    Toast.LENGTH_SHORT).show();

        }

    }

    private void updateThePostToTheDatabase() {

        desCription = postDescrip.getText().toString().trim();
        if(imageUri==null)
        {
            Toast.makeText(this,
                    "you must choose a image", Toast.LENGTH_SHORT).show();
        }
        else
        if(TextUtils.isEmpty(desCription))
        {
            postDescrip.setError("write something about the post");
            return;
        }
        else
        {
            mDialog.setTitle("Add new Post..");
            mDialog.setMessage("Please wait while updating you post");
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();
            storingImageToFirebase();
        }
    }

    private void openGallery() {

        Intent gallery_intent = new Intent();
        gallery_intent.setType("image/*");
        gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(gallery_intent ,Gallery_Pick );
    }



    private void storingImageToFirebase() {

        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate =currentDate.format(callForDate.getTime());

        Calendar callForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime =currentTime.format(callForDate.getTime());

        postRandomName = saveCurrentDate+saveCurrentTime;

        final StorageReference filePath = postImagesRef.child("Post Images")
                .child(imageUri.getLastPathSegment() +postRandomName +".jpg");


        filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if(task.isSuccessful())
                {
                    Toast.makeText(PostActivity.this,
                            "Posting Image uploaded successfully", Toast.LENGTH_SHORT).show();
                    mDialog.dismiss();

                    postDescrip.setText("");

                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            downloadURL = uri.toString();

                            savingInformationToDatabase();
                        }
                    });

                }
                else
                {
                    Toast.makeText(PostActivity.this,
                            "couldn't upload your posting image", Toast.LENGTH_SHORT).show();
                    mDialog.dismiss();
                }
            }
        });
    }

    private void savingInformationToDatabase() {

        mAuth =FirebaseAuth.getInstance();
        final String current_userID = mAuth.getCurrentUser().getUid();

        DatabaseReference dR2= userRef.child(current_userID);

        dR2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("fullName"))
                {
                    userProfileFullName = dataSnapshot.child("fullName")
                            .getValue().toString();
                }
                if(dataSnapshot.hasChild("profileImage"))
                {
                    userProfileImageURL = dataSnapshot.child("profileImage")
                            .getValue().toString();
                }

                HashMap postsMap = new HashMap();
                postsMap.put("uid" , current_userID);
                postsMap.put("date" , saveCurrentDate);
                postsMap.put("time" , saveCurrentTime);
                postsMap.put("description" , desCription );
                postsMap.put("postImage" , downloadURL);
                postsMap.put("profileImage" , userProfileImageURL);
                postsMap.put("fullName" , userProfileFullName);

                postRef.child( current_userID + " " + postRandomName).
                        updateChildren(postsMap).
                        addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {

                                if(task.isSuccessful())
                                {

                                    mDialog.dismiss();
                                    Intent intent = new Intent(getApplicationContext() , MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                    Toast.makeText(PostActivity.this,
                                            " new post is uploaded successfully", Toast.LENGTH_SHORT).show();

                                }
                                else
                                {
                                    Toast.makeText(PostActivity.this,
                                            "new post couldn't upload", Toast.LENGTH_SHORT).show();
                                    mDialog.dismiss();
                                }
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Gallery_Pick && resultCode==RESULT_OK
                && data!=null &&  data.getData()!=null  )
        {
            imageUri = data.getData();

            selectPostingImage.setImageURI(imageUri);
        }


    }

    private void sendUserToMainActivity() {
        startActivity(new Intent(getApplicationContext() , MainActivity.class));
        finish();
    }

    private void initialize() {


        mToolbar = (Toolbar)findViewById(R.id.id_update_post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("update post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        selectPostingImage = (ImageButton)findViewById(R.id.id_posting_image);
        postDescrip = (EditText) findViewById(R.id.id_posting_image_descrip);
        updatePostButton = (Button)findViewById(R.id.id_post_the_image_button);

        postImagesRef = FirebaseStorage.getInstance().getReference();
        mDialog = new ProgressDialog(this);

        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        postRef= FirebaseDatabase.getInstance().getReference().child("Posts");
    }
}
