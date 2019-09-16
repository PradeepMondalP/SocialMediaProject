package com.example.socialmediaproject2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

//import com.example.socialmediaproject2.latseenupdate.LastSeenUpdate;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ImageViewerActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    String userName , userImage ;
    private PhotoView profileImage;
    private FirebaseAuth mAuth;
    private String current_user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        userName = getIntent().getStringExtra("userName");
        userImage= getIntent().getStringExtra("userImage");
        profileImage = (PhotoView) findViewById(R.id.id_image);
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid().toString();

        mToolbar = (Toolbar)findViewById(R.id.id_clickPost_toolbar2);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(userName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Picasso.with(getApplicationContext()).load(userImage)
                .placeholder(R.drawable.profile).into(profileImage);



    }



}
