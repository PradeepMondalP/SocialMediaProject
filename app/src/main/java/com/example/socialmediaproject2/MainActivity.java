package com.example.socialmediaproject2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.socialmediaproject2.latseenupdate.LastSeenUpdate;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.zolad.zoominimageview.ZoomInImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView postList;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef, postRef, likesRef;

    private CircleImageView navProfileImage;
    private TextView navProfileName;
    private String UserID  , currentUserID;
    private ImageButton addNewPostButton;
    private FloatingActionButton fab;

    private Boolean likesChecker = false;

    //other stuff
    private LastSeenUpdate lastSeenUpdate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initialization();


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                UserMenuSelector(menuItem);
                return false;
            }
        });

        addNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToPostActivity();
            }
        });

        displayAllTheUsersPost();
        displayImageAndNameOnTheNavigation();


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToFindingFriendActivity();
            }
        });
    }

    private void initialization() {

        mAuth = FirebaseAuth.getInstance();
        UserID = mAuth.getCurrentUser().getUid();
        currentUserID = UserID;
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        addNewPostButton = (ImageButton) findViewById(R.id.id_add_new_post_button);

        mToolbar = (Toolbar) findViewById(R.id.id_main_page_toolbar);
        fab = (FloatingActionButton)findViewById(R.id.id_floating_btn);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setTitle("Home");
//        getActionBar().setDisplayHomeAsUpEnabled(true);


        drawerLayout = (DrawerLayout) findViewById(R.id.id_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout,
                R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        navigationView = (NavigationView) findViewById(R.id.id_navigation_view);

        postList = (RecyclerView) findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        navProfileImage = (CircleImageView) navView.findViewById(R.id.id_nav_profile_image);
        navProfileName = (TextView) navView.findViewById(R.id.id_nav_user_profile_name);


    }

    @Override
    protected void onStart() {
        super.onStart();


        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            sendUserToLoginActivity();
        } else {
            checkUserExistence();

        }

        lastSeenUpdate = new LastSeenUpdate(currentUserID);
        lastSeenUpdate.update("online");

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        lastSeenUpdate.update("online");
    }

    @Override
    protected void onResume() {
        super.onResume();
        lastSeenUpdate.update("online");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lastSeenUpdate.update("offline");
    }

    @Override
    protected void onPause() {
        super.onPause();
        lastSeenUpdate.update("offline");
    }

    private void displayAllTheUsersPost() {
    //    updateUserStatus("online");
        Query sortPostInDescendingOrder = postRef.orderByChild("counter");
        sortPostInDescendingOrder.keepSynced(true);


        FirebaseRecyclerAdapter< Post , PostsViewHolder > firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Post, PostsViewHolder>
                        (
                                Post.class , R.layout.all_post_layout ,
                                PostsViewHolder.class , sortPostInDescendingOrder
                        )
                {
                    @Override
                    protected void populateViewHolder(PostsViewHolder holder, Post model, final int position)
                    {

                        final String    postKey = getRef(position).getKey().toString();

                        holder.setFullName(model.getFullName());
                        holder.setTime(model.getTime());
                        holder.setDate(model.getDate());
                        holder.setDescription(model.getDescription());
                        holder.setPostImage(getApplicationContext() ,model.getPostImage());
                        holder.setProfileImage(getApplicationContext() ,model.getProfileImage());

                        holder.setLikeButtonStatus(postKey);

                        holder.image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getApplicationContext() ,ClickPostActivity.class);
                                intent.putExtra("postKey" , postKey);
                                startActivity(intent);
                            }
                        });

                        holder.imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String pressedPos= getRef(position).getKey().toString();
                                postRef.child(pressedPos).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if(dataSnapshot.hasChild("uid"))
                                        {
                                            String originalPresdPos=
                                                    dataSnapshot.child("uid").getValue().toString();

                                            Intent profIntent = new Intent(getApplicationContext() , PersonProfileActivity.class);
                                                profIntent.putExtra("visitUserId" , originalPresdPos);
                                                startActivity(profIntent);

                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }
                        });


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


                        holder.commentButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                sendUserToCommentActivity(postKey);
                            }
                        });

                        showNumberOfComments(postKey  , holder);
                    }

                };
        postList.setAdapter(firebaseRecyclerAdapter);

    }


    private void showNumberOfComments(String postKey, final PostsViewHolder holder) {
        DatabaseReference dr = postRef.child(postKey).child("comment");

        dr.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {
                    int numberOfPost = (int) dataSnapshot.getChildrenCount();
                    holder.numberOfCComments.setText(numberOfPost+" "+" comments");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void displayImageAndNameOnTheNavigation() {

        final String cureent_user2 = mAuth.getCurrentUser().getUid();
        DatabaseReference dR3 = userRef.child(cureent_user2);

        dR3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("fullName") &&
                        dataSnapshot.hasChild("profileImage") )
                {
                    final  String tempFullName = dataSnapshot.child("fullName")
                            .getValue().toString();
                    navProfileName.setText(tempFullName);

                    final   String tempImage = dataSnapshot.child("profileImage")
                            .getValue().toString();

                    Picasso.with(MainActivity.this).load(tempImage)
                            .into(navProfileImage);

                    navProfileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendUserToImageViewerActivity(tempFullName , tempImage);
                        }
                    });

                }
                else {
                    Toast.makeText(MainActivity.this,
                            "couldn't load image or text",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToImageViewerActivity(String tempFullName, String tempImage) {
        Intent intent = new Intent(getApplicationContext() ,ImageViewerActivity.class);
        intent.putExtra("userName",tempFullName);
        intent.putExtra("userImage",tempImage);
        startActivity(intent);
    }





    public static class PostsViewHolder extends RecyclerView.ViewHolder

    {
        View mView;
        TextView  username , timee , datee , descrip  ;
        CircleImageView imageView;
        ZoomInImageView image;

        ImageButton likePostButton , commentButton ;
        TextView numberOfLikes  , numberOfCComments;
        String currentUserID;
        DatabaseReference likeRef ;
        int countLikes;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView ;
            numberOfCComments = (TextView)mView.findViewById(R.id.id_no_of_commnets);
            commentButton = (ImageButton)mView.findViewById(R.id.id_commnet_btn);
            username = (TextView)mView.findViewById(R.id.id_post_user_name);
            imageView = (CircleImageView)mView.findViewById(R.id.id_post_profile_image);
            timee = (TextView)mView.findViewById(R.id.id_post_time);
            datee = (TextView)mView.findViewById(R.id.id_post_date);
            descrip = (TextView)mView.findViewById(R.id.id_post_description);
            image = (ZoomInImageView) mView.findViewById(R.id.id_post_image);
            likePostButton = (ImageButton)mView.findViewById(R.id.id_like_button);
            numberOfLikes = (TextView)mView.findViewById(R.id.id_no_of_likes);

            likeRef =FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        }



        public void setFullName(String fullName)
        {
            username.setText(fullName);
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


        public void setProfileImage(final Context ctx , final String profileImage) {

            Picasso.with(ctx).load(profileImage).networkPolicy(NetworkPolicy.OFFLINE)
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(ctx).load(profileImage).into(imageView);
                        }
                    });
        }

        public void setPostImage(final Context ctx , final String postImage)
        {
            Picasso.with(ctx).load(postImage).networkPolicy(NetworkPolicy.OFFLINE)
                    .into(image, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                            Picasso.with(ctx).load(postImage).into(image);
                        }
                    });

        }

        public void setLikeButtonStatus(final String postKey) {
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

    private void UserMenuSelector(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.id_nav_profile:
                sendUserToProfileActivity();
                break;

            case R.id.id_nav_add_new_post:
                sendUserToPostActivity();
                break;

            case R.id.id_nav_home:
                Toast.makeText(this, "home", Toast.LENGTH_SHORT).show();
                break;

            case R.id.id_nav_friends:
                 sendUserToFriendsActivity();
                break;

            case R.id.id_nav_messages:
                sendUserToFriendsActivity();
                break;

            case R.id.id_nav_find_friend_request:
                sendUserToFriendRequestActivity();
                break;

            case R.id.id_nav_settings:
                sendUserToUpdateProfileSettingActivity();
                break;

            case R.id.id_nav_logout:
                mAuth.signOut();
                sendUserToLoginActivity();
                break;

            case R.id.id_rate:
                sendUserToRatingActivity();
                break;


            case R.id.id_developer:
                Toast.makeText(this, "Deep", Toast.LENGTH_SHORT).show();
                break;

        }
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this );

        builder.setTitle("Exit")
                .setMessage("want to exit..")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.super.onBackPressed();

                    }
                })
                .setNegativeButton("No", null)
                .setCancelable(true);

        builder.create().show();
    }

    private void sendUserToLoginActivity() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    private void checkUserExistence() {

        final String current_user_id = mAuth.getCurrentUser().getUid();

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if( ! dataSnapshot.hasChild(current_user_id)) {

                    sendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void sendUserToSetupActivity() {
        Intent setupIntent = new Intent(getApplicationContext() , SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void sendUserToRatingActivity() {
        Intent fintent = new Intent(getApplicationContext() ,RatingActivity.class);
        startActivity(fintent);
    }

    private void sendUserToUpdateProfileSettingActivity() {
        Intent fintent = new Intent(getApplicationContext() ,UpdateProfileSettingActivity.class);
        startActivity(fintent);
    }

    private void sendUserToPostActivity() {
        Intent fintent = new Intent(getApplicationContext() ,PostActivity.class);
        startActivity(fintent);

    }


    private void sendUserToFindingFriendActivity() {
        Intent fintent = new Intent(getApplicationContext() ,FindFriendsActivity.class);
        startActivity(fintent);

    }

    private void sendUserToProfileActivity() {
        Intent fintent = new Intent(getApplicationContext() ,ProfileActivity.class);
        startActivity(fintent);

    }

    private void sendUserToFriendsActivity() {
        Intent fintent = new Intent(getApplicationContext() ,FriendsActivity.class);
        startActivity(fintent);
    }

    private void sendUserToFriendRequestActivity() {

        Intent fintent = new Intent(getApplicationContext() ,FriendRequestActivity.class);
        startActivity(fintent);
    }

    public void sendUserToCommentActivity(String postKey)
    {
        Intent fintent = new Intent(getApplicationContext() ,CommentActivity.class);
        fintent.putExtra("postKey" , postKey);
        startActivity(fintent);
    }



}