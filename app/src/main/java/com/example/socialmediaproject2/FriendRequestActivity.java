package com.example.socialmediaproject2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//import com.example.socialmediaproject2.latseenupdate.LastSeenUpdate;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private FirebaseAuth mAuth;
    private DatabaseReference UserRef;
    String current_user_id ;
    private DatabaseReference rootRef , freindReqRef , myFriendsRef;


    // for storing dta from database to display in frnd req list
    String userName , userProfileImage , userStatus ;

    // others
    private ProgressDialog mDialog;
    private String saveCurrentDate;
  //  private LastSeenUpdate lastSeenUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        initialize_ids();

        displayAllRequests();

    }


    private void displayAllRequests() {

     final    DatabaseReference tempRef = freindReqRef.child(current_user_id);
     tempRef.keepSynced(true);

        FirebaseRecyclerAdapter<FindFriends ,FindFriendRequestHolder > obj
                = new FirebaseRecyclerAdapter<FindFriends, FindFriendRequestHolder>
                        (
                                 FindFriends.class , R.layout.friend_request_layout ,
                                FindFriendRequestHolder.class ,tempRef
                        )
                {
            @Override
            protected void populateViewHolder(final FindFriendRequestHolder holder, FindFriends model, int pos) {

                final String list_user_ID = getRef(pos).getKey();
                DatabaseReference temp = tempRef.child(list_user_ID);

                // getting the details from the firebase to display in the friend request list...
                DatabaseReference hmm = UserRef.child(list_user_ID);
                hmm.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            userName = dataSnapshot.child("userName").getValue().toString();
                            userProfileImage = dataSnapshot.child("profileImage").getValue().toString();
                            userStatus = dataSnapshot.child("status").getValue().toString();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                System.out.println("id are" + list_user_ID);

                temp.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists())
                        {
                            final  String type = dataSnapshot.child("request_type").getValue().toString();
                            Toast.makeText(FriendRequestActivity.this,
                                    type, Toast.LENGTH_SHORT).show();

                            if(type.equals("sent"))
                            {
                                 holder.userName.setText(userName );
                                 holder.acceptBtn.setVisibility(View.VISIBLE);
                                 holder.acceptBtn.setText("Cancel request");

                                 Picasso.with(getApplicationContext()).load(userProfileImage)
                                         .placeholder(R.drawable.profile).into(holder.profileImage2);

                                holder.acceptBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        cancelRequests(current_user_id , list_user_ID);
                                        Toast.makeText(FriendRequestActivity.this,
                                                "cancelled request successfully", Toast.LENGTH_SHORT).show();

                                    }
                                });
                            }
                            else if(type.equals("received"))
                            {
                                holder.userName.setText(userName + "");
                                holder.acceptBtn.setVisibility(View.VISIBLE);
                                holder.rejectBtn.setVisibility(View.VISIBLE);

                                holder.acceptBtn.setText("Accept request");
                                holder.rejectBtn.setText("Reject request");

                                Picasso.with(getApplicationContext()).load(userProfileImage)
                                        .placeholder(R.drawable.profile).into(holder.profileImage2);

                                holder.rejectBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        cancelRequests(current_user_id , list_user_ID);
                                        Toast.makeText(FriendRequestActivity.this,
                                                "cancelled request successfully", Toast.LENGTH_SHORT).show();

                                    }
                                });

                                holder.acceptBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        acceptFriendRequest(current_user_id , list_user_ID);
                                    }
                                });
                            }


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        };
        mRecyclerView.setAdapter(obj);
        obj.startListening();
    }

    private void acceptFriendRequest(String current_user_id, String list_user_id) {

        cancelRequests(current_user_id , list_user_id);
        DatabaseReference senderSideFriend = myFriendsRef.child(current_user_id).child(list_user_id).child("date");
        final DatabaseReference receiverSideFriend = myFriendsRef.child(list_user_id).child(current_user_id).child("date");


        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate =currentDate.format(callForDate.getTime());

        senderSideFriend.setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful())
                {
                    receiverSideFriend.setValue(saveCurrentDate);
                    Toast.makeText(FriendRequestActivity.this,
                            "requests accepted", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void cancelRequests(String current_user_id, String list_user_id) {

        DatabaseReference senderSide = freindReqRef.child(current_user_id).child(list_user_id);
        final DatabaseReference receiverSide = freindReqRef.child(list_user_id).child(current_user_id);

        senderSide.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                receiverSide.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        { //
                        }
                        else
                        {
                            Toast.makeText(FriendRequestActivity.this,
                                    "couldn't cancel request", Toast.LENGTH_SHORT).show();


                        }
                    }
                });
            }
        });

    }

    public static class FindFriendRequestHolder extends RecyclerView.ViewHolder
    {

        private View mView;
        private CircleImageView profileImage2;
        private Button acceptBtn , rejectBtn;
        private TextView userName;

        public FindFriendRequestHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            profileImage2 = (CircleImageView)itemView.findViewById(R.id.id_friend_request_profile_image);
            acceptBtn = (Button)mView.findViewById(R.id.id_friend_request_accept_btn);
            rejectBtn = (Button)mView.findViewById(R.id.id_friend_request_decline_button);
            userName = (TextView)mView.findViewById(R.id.id_friend_request_user_name);
        }

        public void setProfileImage(Context ctx ,String profileImage)
        {
            Picasso.with(ctx).load(profileImage).placeholder(R.drawable.profile).into(profileImage2);
        }
        public void setFullName(String fullName)
        {
            userName.setText(fullName);
        }

    }


    private void initialize_ids() {

        mToolbar = (Toolbar)findViewById(R.id.id_friend_request_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Requests");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView = (RecyclerView)findViewById(R.id.id_friend_request_recycler_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        rootRef =FirebaseDatabase.getInstance().getReference();
        freindReqRef = rootRef.child("FriendRequests");

        mDialog = new ProgressDialog(getApplicationContext());
        myFriendsRef = rootRef.child("Friends");

    }
}
