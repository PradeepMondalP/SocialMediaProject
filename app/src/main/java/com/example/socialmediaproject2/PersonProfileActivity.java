package com.example.socialmediaproject2;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

//import com.example.socialmediaproject2.latseenupdate.LastSeenUpdate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class PersonProfileActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private TextView profileName , userName , status ,country , dob ,gender;
    private Button sendFriendReq , cancelFriendReq;

    private FirebaseAuth mAuth;
    private String sendUserID  , receiveUserId  , current_state  , saveCurrentDate ;
    private DatabaseReference userRef , friendRequestRef  , friendsRef;
    private Toolbar mToolbar;

  //  private LastSeenUpdate lastSeenUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        initilize_id();

        gettingAllTheValues();

        cancelFriendReq.setVisibility(View.GONE);


        if(!sendUserID.equals(receiveUserId))
        {

            checkingForVerification(sendUserID , receiveUserId);

            sendFriendReq.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendFriendReq.setEnabled(false);

                    if(current_state.equals("not_friends"))
                    {
                        sendFriendRequestToAPerson();
                    }
                    if(current_state.equals("request_sent"))
                    {
                        canelTheFriendRequests();
                    }
                    if(current_state.equals("request_received"))
                    {
                        acceptFriendRequest() ;
                    }
                    if(current_state.equals("friends"))
                    {
                        unFriendExistingFriends();
                    }
                }
            });
        }
        else {
            sendFriendReq.setVisibility(View.INVISIBLE);
            cancelFriendReq.setVisibility(View.INVISIBLE);
        }



    }




    private void checkingForVerification(String sendUserID, String receiveUserId) {

        DatabaseReference tempRef = friendsRef.child(sendUserID).child(receiveUserId);

        tempRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("date"))
                {
                    current_state = "friends";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.person_profile_menu , menu);
        return true;
    }

    private void unFriendExistingFriends()
    {
        DatabaseReference friendReqRef2 = friendsRef.child(sendUserID).child(receiveUserId)
                .child("date");

        friendReqRef2.removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            DatabaseReference friendReqRef3 =friendsRef.child(receiveUserId)
                                    .child(sendUserID)
                                    .child("date");

                            friendReqRef3.removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful())
                                            {
                                                sendFriendReq.setEnabled(true);
                                                current_state = "not_friends";
                                                sendFriendReq.setText("Send Friend Request");
                                                Toast.makeText(PersonProfileActivity.this,
                                                        "Friend\t" +
                                                                "request cancelled" +
                                                                "\t successfully", Toast.LENGTH_SHORT).show();

                                                cancelFriendReq.setVisibility(View.INVISIBLE);
                                                cancelFriendReq.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptFriendRequest()
    {
        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate =currentDate.format(callForDate.getTime());

        DatabaseReference friendsRef2=friendsRef
                .child(sendUserID).child(receiveUserId).child("date");

        friendsRef2.setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            DatabaseReference friendsRef2=friendsRef
                                    .child(receiveUserId).child(sendUserID).child("date");

                            friendsRef2.setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                DatabaseReference friendReqRef2 = friendRequestRef.child(sendUserID)
                                                        .child(receiveUserId)
                                                        .child("request_type") ;

                                                friendReqRef2.removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if(task.isSuccessful())
                                                                {
                                                                    DatabaseReference friendReqRef3 =friendRequestRef
                                                                            .child(receiveUserId)
                                                                            .child(sendUserID)
                                                                            .child("request_type");

                                                                    friendReqRef3.removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        sendFriendReq.setEnabled(true);
                                                                                        current_state = "friends";
                                                                                        sendFriendReq.setText("Unfriend this person");

                                                                                        cancelFriendReq.setVisibility(View.INVISIBLE);
                                                                                        cancelFriendReq.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });

                        }
                    }
                });
    }

    private void canelTheFriendRequests()
    {
        DatabaseReference friendReqRef2 = friendRequestRef.child(sendUserID).child(receiveUserId)
                .child("request_type") ;

        friendReqRef2.removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            DatabaseReference friendReqRef3 =friendRequestRef.child(receiveUserId)
                                    .child(sendUserID)
                                    .child("request_type");

                            friendReqRef3.removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful())
                                            {
                                                sendFriendReq.setEnabled(true);
                                                current_state = "not_friends";
                                                sendFriendReq.setText("Send Friend Request");
                                                Toast.makeText(PersonProfileActivity.this,
                                                        "Friend\t" +
                                                                "request cancelled" +
                                                            "\t successfully", Toast.LENGTH_SHORT).show();

                                                cancelFriendReq.setVisibility(View.INVISIBLE);
                                                cancelFriendReq.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void sendFriendRequestToAPerson()
    {
        DatabaseReference friendReqRef2 = friendRequestRef.child(sendUserID).child(receiveUserId)
                .child("request_type") ;

        friendReqRef2.setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                             DatabaseReference friendReqRef3 =friendRequestRef.child(receiveUserId)
                                     .child(sendUserID)
                                     .child("request_type");

                             friendReqRef3.setValue("received")
                                     .addOnCompleteListener(new OnCompleteListener<Void>() {
                                         @Override
                                         public void onComplete(@NonNull Task<Void> task) {

                                             if(task.isSuccessful())
                                             {
                                                 sendFriendReq.setEnabled(true);
                                                 current_state = "request_sent";
                                                 sendFriendReq.setText("Cancel Friend Request");

                                                 cancelFriendReq.setVisibility(View.INVISIBLE);
                                                 cancelFriendReq.setEnabled(false);
                                             }
                                         }
                                     });
                        }
                    }
                });
    }

    private void gettingAllTheValues() {
        DatabaseReference userRef2 = userRef.child(receiveUserId);

        userRef2.addValueEventListener(new ValueEventListener() {
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
                    Picasso.with(getApplicationContext()).load(prof).placeholder(R.drawable.profile)
                            .into(profileImage);
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

                MaintainanceOfButtons();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(getApplicationContext(),
                        "couldn't show u all the data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void MaintainanceOfButtons()
    {
        DatabaseReference friendReqRefChek = friendRequestRef.child(sendUserID);

        friendReqRefChek.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.child(receiveUserId).hasChild("request_type"))
                {
                    String reqest_type = dataSnapshot
                            .child(receiveUserId).child("request_type").getValue().toString();

                    if(reqest_type.equals("sent"))
                    {
                        current_state ="request_sent";
                        sendFriendReq.setText("Cancel Friend Request");

                        cancelFriendReq.setVisibility(View.INVISIBLE);
                        cancelFriendReq.setEnabled(false);
                    }
                    else
                        if (reqest_type.equals("received"))
                        {
                            current_state = "request_received";
                            sendFriendReq.setText("Accept Friend Request");

                            cancelFriendReq.setVisibility(View.VISIBLE);
                            cancelFriendReq.setEnabled(true);

                            cancelFriendReq.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v)
                                {
                                    canelTheFriendRequests();
                                }
                            });
                        }

                }
                else
                {
                    DatabaseReference drf = friendsRef.child(sendUserID);
                    drf.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if(dataSnapshot.hasChild(receiveUserId))
                            {
                                current_state = "friends";
                                sendFriendReq.setText("Unfriend this Person");

                                cancelFriendReq.setVisibility(View.INVISIBLE);
                                cancelFriendReq.setEnabled(false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initilize_id() {

        mToolbar = (Toolbar)findViewById(R.id.id_app_bar_person_prof_activiryt);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        profileImage=(CircleImageView)findViewById(R.id.id_rofile_profile_image2);
        profileName = (TextView)findViewById(R.id.id_rofile_profile_name2);
        userName = (TextView)findViewById(R.id.id_rofile_user_name2);
        status = (TextView)findViewById(R.id.id_rofile_profile_status2);
        country = (TextView)findViewById(R.id.id_rofile_country2);
        dob = (TextView)findViewById(R.id.id_rofile_dob2);
        gender = (TextView)findViewById(R.id.id_rofile_gender2);

        sendFriendReq = (Button)findViewById(R.id.id_send_friend_req_btn);
        cancelFriendReq = (Button)findViewById(R.id.id_decliine_friend_req_btn);

        mAuth = FirebaseAuth.getInstance();
        receiveUserId = getIntent().getStringExtra("visitUserId");
        sendUserID = mAuth.getCurrentUser().getUid().toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        current_state = "not_friends";
        friendRequestRef =FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");

    }
}
