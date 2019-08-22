package com.example.socialmediaproject2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class MyPostActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView myPostList;
    FirebaseAuth mAuth;
    private DatabaseReference postRef  , likesRef ;
    private String currentUserID;

    private Boolean likesChecker= false;
    private   String UserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_post);

        initialize();

        displayMyAllPosts();
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

    private void displayMyAllPosts()
    {
        Query myPostQuery = postRef.orderByChild("uid").startAt(currentUserID)
                .endAt(currentUserID +"\uf8ff") ;

        FirebaseRecyclerAdapter<Post ,MyPostViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Post, MyPostViewHolder>
                        (
                                Post.class,
                                R.layout.all_post_layout,
                                MyPostViewHolder.class,
                                myPostQuery
                        )
                {
                    @Override
                    protected void populateViewHolder(MyPostViewHolder holder, Post model, int pos)
                    {
                        final String    postKey = getRef(pos).getKey().toString();

                        holder.setFullName(model.getFullName());
                        holder.setTime(model.getTime());
                        holder.setDate(model.getDate());
                        holder.setDescription(model.getDescription());
                        holder.setPostImage(getApplicationContext() ,model.getPostImage());
                        holder.setProfileImage(getApplicationContext() ,model.getProfileImage());

                      holder.setLikeButtonStatus(postKey);

                        holder.likePostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {

                                 likesChecker = true;

                                likesRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                    {
                                        if(likesChecker.equals(true))
                                        {
                                            if(dataSnapshot.child(postKey).hasChild(UserID))
                                            {
                                                likesRef.child(postKey).child(UserID).removeValue();
                                                likesChecker = false;
                                            }
                                            else
                                            {
                                                likesRef.child(postKey).child(UserID).setValue(true);
                                                likesChecker = false;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        });


                    }
                };

        myPostList.setAdapter(firebaseRecyclerAdapter);
    }


    public static class MyPostViewHolder extends RecyclerView.ViewHolder
    {

        View mView;
        TextView username  , timee , datee ,descrip ;
        CircleImageView imageView;
        ImageView image;

        ImageButton likePostButton  ;
        TextView numberOfLikes;
        String currentUserID;
        DatabaseReference likeRef ;
        int countLikes;

        public MyPostViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            username = (TextView)mView.findViewById(R.id.id_post_user_name);
            imageView = (CircleImageView)mView.findViewById(R.id.id_post_profile_image);
            timee = (TextView)mView.findViewById(R.id.id_post_time);
            datee = (TextView)mView.findViewById(R.id.id_post_date);
            descrip = (TextView)mView.findViewById(R.id.id_post_description);
            image = (ImageView)mView.findViewById(R.id.id_post_image);

            likePostButton = (ImageButton)mView.findViewById(R.id.id_like_button);

            numberOfLikes = (TextView)mView.findViewById(R.id.id_no_of_likes);
            likeRef =FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();


        }
        public void setFullName(String fullName)
        {
            username.setText(fullName);
        }
        public void setProfileImage(Context ctx , String profileImage)
        {
            Picasso.with(ctx).load(profileImage).into(imageView);
        }
        public void setDate(String date)
        {
            datee.setText(date);
        }
        public void setTime(String time)
        {
            timee.setText("  "+time);
        }

        public void setDescription(String description)
        {
            descrip.setText(description);
        }
        public void setPostImage(Context ctx ,String postImage)
        {
            Picasso.with(ctx).load(postImage).into(image);
        }


        public void setLikeButtonStatus(final String postKey)
        {
            likeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.child(postKey).hasChild(currentUserID))
                    {
                        likePostButton.setImageResource(R.drawable.like);

                        countLikes = (int)dataSnapshot.child(postKey).getChildrenCount();
                        numberOfLikes.setText(countLikes+ "  Likes");
                    }
                    else
                    {
                        likePostButton.setImageResource(R.drawable.dislike);

                        countLikes = (int)dataSnapshot.child(postKey).getChildrenCount();
                        numberOfLikes.setText(countLikes +"  Likes");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }




    private void initialize() {
        mToolbar = (Toolbar)findViewById(R.id.my_post_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("My Posts");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        myPostList = (RecyclerView)findViewById(R.id.my_all_post_list);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPostList.setLayoutManager(linearLayoutManager);


        mAuth = FirebaseAuth.getInstance();
        UserID =mAuth.getCurrentUser().getUid();
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        currentUserID = mAuth.getCurrentUser().getUid().toString();
    }
}
