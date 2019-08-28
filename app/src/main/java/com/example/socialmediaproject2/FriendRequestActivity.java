package com.example.socialmediaproject2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
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
import java.util.Iterator;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private FirebaseAuth mAuth;
    private DatabaseReference UserRef;
    String current_user_id ;
    private DatabaseReference rootRef , freindReqRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        initialize_ids();

        // do something here
        searchPeopleAndFriends();



    }



    private void searchPeopleAndFriends()
    {

        FirebaseRecyclerAdapter<FriendRequest ,FriendRequestHolder >obj =
                new FirebaseRecyclerAdapter<FriendRequest, FriendRequestHolder>
                        (
                                FriendRequest.class , R.layout.friend_request_layout ,
                                FriendRequestHolder.class , UserRef
                        )
                {
                    @Override
                    protected void populateViewHolder(final FriendRequestHolder holder,
                                                      FriendRequest model, int pos) {

                        holder.setFullName(model.getFullName());
                        holder.setProfileImage(getApplicationContext() , model.getProfileImage());

//                        freindReqRef.addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//                            {
//                               Iterator obj = dataSnapshot.getChildren().iterator();
//                               while (obj.hasNext())
//                               {
//                                   final String myID = (((DataSnapshot)obj.next()).getKey());
//
//                                   DatabaseReference dr2 =freindReqRef.child(myID).child(current_user_id);
//
//                                   dr2.addValueEventListener(new ValueEventListener() {
//                                       @Override
//                                       public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                           if(dataSnapshot.hasChild("request_type"))
//                                           {
//                                               String myType =dataSnapshot.child("request_type").getValue().toString();
//                                               if(myType.equals("received"))
//                                               {
//                                                   holder.acceptbtn.setVisibility(View.VISIBLE);
//                                               }
//                                               else{
//                                                   holder.rejectbtn.setVisibility(View.VISIBLE);
//                                               }
//                                           }
//                                       }
//
//                                       @Override
//                                       public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                                       }
//                                   });
//                               }
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                            }
//                        });

                    }
                };
        mRecyclerView.setAdapter(obj);

    }

    public static class FriendRequestHolder extends RecyclerView.ViewHolder
    {
        View mView;
        CircleImageView profileImages;
        TextView name ;
        Button acceptbtn , rejectbtn;

        public FriendRequestHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
            profileImages = (CircleImageView)mView.findViewById(R.id.id_friend_request_profile_image);
            name= (TextView)mView.findViewById(R.id.id_friend_request_user_name);
            acceptbtn = (Button)mView.findViewById(R.id.id_friend_request_accept_btn);
            rejectbtn = (Button)mView.findViewById(R.id.id_friend_request_decline_button);

            rejectbtn.setVisibility(View.INVISIBLE);
            acceptbtn.setVisibility(View.INVISIBLE);
        }

        public void setProfileImage(Context ctx ,String profileImage)
        {
            Picasso.with(ctx).load(profileImage).into(profileImages);
        }
        public void setFullName(String fullName)
        {
            name.setText(fullName);
        }


    }

    private void initialize_ids() {

        mToolbar = (Toolbar)findViewById(R.id.id_friend_request_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Find new Friends");
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

    }
}
