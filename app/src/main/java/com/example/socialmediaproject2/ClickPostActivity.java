package com.example.socialmediaproject2;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ClickPostActivity extends AppCompatActivity {
    private PhotoView clickPostImage;
    private TextView  clickPostDescrip;
    private Button clickUpdatePost , clickPostDeletePost;
    private String postKey    , postUserID  , currentUserID ;
    private DatabaseReference clickPostRef ;
    private FirebaseAuth mAuth;
    private  String postDescp ;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        initialize_id();

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Posting Images");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        clickUpdatePost.setVisibility(View.INVISIBLE);
        clickPostDeletePost.setVisibility(View.INVISIBLE);

        clickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("description"))
                {
                    postDescp = dataSnapshot.child("description").getValue().toString();
                   clickPostDescrip.setText(postDescp);
                }
                if(dataSnapshot.hasChild("postImage"))
                {
                    String myImage =dataSnapshot.child("postImage").getValue().toString();
                    Picasso.with(getApplicationContext()).load(myImage).into(clickPostImage);
                }

                if(dataSnapshot.hasChild("uid"))
                {
                    postUserID = dataSnapshot.child("uid").getValue().toString();
                }

                if(postUserID.equals(currentUserID))
                {
                    clickUpdatePost.setVisibility(View.VISIBLE);
                    clickPostDeletePost.setVisibility(View.VISIBLE);

            }
        } @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ClickPostActivity.this,
                        "could upload", Toast.LENGTH_SHORT).show();

            }
        });

      // clicking on the delete post button
        clickPostDeletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   deleteThePost();
            }
        });

      // clicking on the update post
        clickUpdatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateMyPost(postDescp);
            }
        });


        updateUserStatus("online");
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

    private void updateMyPost(String postDescp) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Post");
        final EditText inputField = new EditText(getApplicationContext());
        inputField.setText(postDescp);
        builder.setView(inputField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              clickPostRef.child("description").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this,
                        "Post updated successfully", Toast.LENGTH_SHORT).show();
            }
        })
                .setNegativeButton("cancel" , null)
                .setCancelable(true);

        Dialog dialog = builder.create();
        dialog.show();
        
    }

    private void deleteThePost() {
//        Toast.makeText(this,
//                " post deleted successfully" , Toast.LENGTH_SHORT).show();
  //      clickPostRef.removeValue();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Post")
                .setMessage("Want to delete the post")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(ClickPostActivity.this,
                                "deleted successfuly", Toast.LENGTH_SHORT).show();
                        clickPostRef.removeValue();
                        sendToMainActivity();
                    }
                })
                .setNegativeButton("No",null)
                .setCancelable(true);

        AlertDialog obj = builder.create();
       obj.show();


    }

    private void initialize_id() {
        clickPostImage = (PhotoView) findViewById(R.id.id_clickPost_image_view);
        clickPostDescrip = (TextView)findViewById(R.id.id_click_post_image_descrip);
        clickUpdatePost = (Button)findViewById(R.id.id_click_post_update_btn);
        clickPostDeletePost = (Button)findViewById(R.id.id_click_post_delete_button);

        mToolbar = (Toolbar)findViewById(R.id.id_clickPost_toolbar);

        postKey = getIntent().getStringExtra("postKey").toString();

        clickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);
        mAuth = FirebaseAuth.getInstance();

        currentUserID = mAuth.getCurrentUser().getUid();
    }
    private void sendToMainActivity()
    {
        finish();
        Intent intent = new Intent(getApplicationContext() , MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }
}
