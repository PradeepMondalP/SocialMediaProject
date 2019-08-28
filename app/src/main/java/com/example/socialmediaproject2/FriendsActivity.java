package com.example.socialmediaproject2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {
    private RecyclerView myFriendList;
    private DatabaseReference friendsRef  , userRef;
    private FirebaseAuth mAuth;
    private String online_user_id;
    private String type;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        myFriendList =(RecyclerView)findViewById(R.id.id_friends_activity_recyclerView);
        myFriendList.setHasFixedSize(true);

        initilize();


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);

        displayAllFriends();


    }


    private void initilize() {

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        friendsRef =FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    private void displayAllFriends() {

        friendsRef.keepSynced(true);
        FirebaseRecyclerAdapter<Friends ,FriendsViewHolder>recyclerAdapter =
                new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>
                        (
                                Friends.class, R.layout.all_user_display_layout,
                                FriendsViewHolder.class ,friendsRef
                        )
                {
                    @Override
                    protected void populateViewHolder(final FriendsViewHolder holder, final Friends model, int pos) {

                        holder.setDate(model.getDate());

                        final String userID = getRef(pos).getKey();


                        userRef.child(userID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if(dataSnapshot.hasChild("fullName") &&dataSnapshot.hasChild("profileImage"))
                                {
                                    final String userName=dataSnapshot.child("fullName").getValue().toString();
                                    final String profileImage = dataSnapshot.child("profileImage")
                                            .getValue().toString();

                                    if(dataSnapshot.hasChild("userState"))
                                    {
                                     type = dataSnapshot.child("userState").child("type").getValue().toString();

                                       if(type.equals("online"))
                                       {
                                           holder.onlineStatusView.setVisibility(View.VISIBLE);
                                       }
                                       else
                                       {
                                           holder.onlineStatusView.setVisibility(View.INVISIBLE);
                                       }
                                    }

                                    holder.setFullName(userName);
                                    holder.setProfileImage(getApplicationContext() ,profileImage);


                                    holder.mView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v)
                                        {
                                            CharSequence options[] = new CharSequence[]
                                                    {

                                                            userName +"'s  Profile",
                                                            "Send Message"
                                                    };
                                            AlertDialog.Builder builder= new AlertDialog.Builder
                                                    (FriendsActivity.this);
                                            builder.setTitle("Select Options");

                                            builder.setItems(options, new DialogInterface.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(DialogInterface dialog, int pos)
                                                {
                                                    if(pos==0)
                                                    {
                                                        Intent obj = new Intent(getApplicationContext()
                                                                , PersonProfileActivity.class);

                                                        obj.putExtra("visitUserId" , userID);
                                                        startActivity(obj);
                                                    }
                                                    if(pos==1)
                                                    {
                                                        Intent obj2 = new Intent(getApplicationContext()
                                                                , ChatActivity.class);

                                                        obj2.putExtra("visitUserId" , userID);
                                                        obj2.putExtra("userName" , userName);
                                                        startActivity(obj2);
                                                    }
                                                }

                                            });
                                            builder.show();
                                        }


                                    });

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }


                        });

                    }
                };
        myFriendList.setAdapter(recyclerAdapter);
    }


    public static class FriendsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        CircleImageView profileImage2;
        TextView fullName2 , date2;
        ImageView onlineStatusView;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
            profileImage2 =(CircleImageView) mView.findViewById(R.id.id_all_user_profile_image);
            fullName2 = (TextView) mView.findViewById(R.id.id_user_profile_name);
            date2 =(TextView)mView.findViewById(R.id.id_user_status);
            onlineStatusView  =(ImageView)mView.findViewById(R.id.id_all_user_online_icon);
        }

        public void setFullName(String fullName)
        {
            fullName2.setText(fullName);
        }

        public void setProfileImage(final Context ctx ,final String profileImage)
        {


           Picasso.with(ctx).load(profileImage).networkPolicy(NetworkPolicy.OFFLINE)
                   .into(profileImage2, new Callback() {
                       @Override
                       public void onSuccess() {

                       }

                       @Override
                       public void onError() {
                           Picasso.with(ctx).load(profileImage).into(profileImage2);
                       }
                   });

        }

        public void setDate(String date) {
            date2.setText("Friends since  "+date);
        }
    }


}
