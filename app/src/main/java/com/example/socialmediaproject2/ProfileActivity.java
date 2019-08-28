package com.example.socialmediaproject2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private TextView profileName , userName , status ,country , dob ,gender;
    private Button numberOfpostButton , numberOfFriendsButton;

    private FirebaseAuth mAuth;
    private DatabaseReference dRef  , current_user_ref  , friendsRef  , postRef;
    private String currentUserID;
    private int numberOfFriends = 0 , numberOfPosts =0;
    private Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        initialize();

        gettingAllTheValues();

        DatabaseReference friendsRef2=  friendsRef.child(currentUserID);
        friendsRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    numberOfFriends = (int)dataSnapshot.getChildrenCount();
                    numberOfFriendsButton.setText(numberOfFriends +" Friends");
                }
                else {
                    numberOfFriendsButton.setText("0  Friends");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // ..........for displaying number of freinds....................
        numberOfFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                sendUserToFriendsActivity();
            }
        });


        //................ for displaying number of posts .........................
        postRef.orderByChild("uid").startAt(currentUserID)
                .endAt(currentUserID +"\uf8ff")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            numberOfPosts =(int)dataSnapshot.getChildrenCount();
                            numberOfpostButton.setText(numberOfPosts + " Posts");

                        }
                        else
                        {
                            numberOfpostButton.setText( "0  Posts");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        numberOfpostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToMyPostActivity();
            }
        });


    }


    private void sendUserToMyPostActivity()
    {
        Intent intent = new Intent(getApplicationContext() ,MyPostActivity.class);
        startActivity(intent);
    }

    private void sendUserToFriendsActivity() {
        Intent intent = new Intent(getApplicationContext() ,FriendsActivity.class);
        startActivity(intent);
    }

    private void gettingAllTheValues() {

        current_user_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("countryName"))
                {
                    String cntry = dataSnapshot.child("countryName").getValue().toString();
                    country.setText("Country :  "+ cntry);
                }
                if(dataSnapshot.hasChild("profileImage"))
                {
                    String prof = dataSnapshot.child("profileImage").getValue().toString();
                    Picasso.with(getApplicationContext()).load(prof).into(profileImage);
                }

                if(dataSnapshot.hasChild("fullName"))
                {
                    String flname = dataSnapshot.child("fullName").getValue().toString();
                    profileName.setText("Name :  "+ flname);
                }

                if(dataSnapshot.hasChild("userName"))
                {
                    String usrname = dataSnapshot.child("userName").getValue().toString();
                    userName.setText("Username :  "+ usrname);
                }

                if(dataSnapshot.hasChild("status"))
                {
                    String stats = dataSnapshot.child("status").getValue().toString();
                    status.setText( stats);
                }

                if(dataSnapshot.hasChild("dob"))
                {
                    String dobb = dataSnapshot.child("dob").getValue().toString();
                    dob.setText("DOB :  " + dobb);
                }

                if(dataSnapshot.hasChild("gender"))
                {
                    String gnder = dataSnapshot.child("gender").getValue().toString();
                    gender.setText("Gender :  " + gnder);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(ProfileActivity.this,
                        "couldn't show u all the data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu , menu);
        return true;
    }

    private void initialize() {
        mToolbar = (Toolbar)findViewById(R.id.id_app_bar_prof_activiryt);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        profileImage=(CircleImageView)findViewById(R.id.id_rofile_profile_image);
        profileName = (TextView)findViewById(R.id.id_rofile_profile_name);
        userName = (TextView)findViewById(R.id.id_rofile_user_name);
        status = (TextView)findViewById(R.id.id_rofile_profile_status);
        country = (TextView)findViewById(R.id.id_rofile_country);
        dob = (TextView)findViewById(R.id.id_rofile_dob);
        gender = (TextView)findViewById(R.id.id_rofile_gender);
        numberOfpostButton=(Button)findViewById(R.id.id_no_of_posts);
        numberOfFriendsButton = (Button)findViewById(R.id.id_no_of_friends);
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        mAuth =FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        dRef = FirebaseDatabase.getInstance().getReference().child("Users");
        current_user_ref = dRef.child(currentUserID);

        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
    }
}
