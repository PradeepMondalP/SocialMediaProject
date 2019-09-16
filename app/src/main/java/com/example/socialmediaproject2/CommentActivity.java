package com.example.socialmediaproject2;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentActivity extends AppCompatActivity {

    private ImageButton postCommentButton;
    private Toolbar mToolbar;
    private EditText inputComment;
    private RecyclerView commentRecyclerView;

    private String postKey , current_USER_ID  , userProfileImage , userName  ,fullName;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef , postRef  ,commentRef;

  //  private LastSeenUpdate lastSeenUpdate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        commentRecyclerView = (RecyclerView)findViewById(R.id.id_comment_recyclerView);
        commentRecyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        commentRecyclerView.setLayoutManager(layoutManager);

        initialize_id();

        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
              final  String comment = inputComment.getText().toString().trim();
                if(TextUtils.isEmpty(comment))
                {
                    Toast.makeText(getApplicationContext(), "enter a comment",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                else
                {
                    DatabaseReference userRef2 = userRef.child(current_USER_ID);

                    userRef2.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if(dataSnapshot.hasChild("userName") &&
                                    dataSnapshot.hasChild("profileImage")
                            && dataSnapshot.hasChild("fullName"))
                            {
                                userName = dataSnapshot.child("userName").getValue().toString();
                                userProfileImage = dataSnapshot.child("profileImage")
                                        .getValue().toString();
                                fullName = dataSnapshot.child("fullName").getValue().toString();

                                Calendar callForDate = Calendar.getInstance();
                                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                                final String  saveCurrentDate =currentDate.format(callForDate.getTime());

                                Calendar callForTime = Calendar.getInstance();
                                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
                                final String  saveCurrentTime =currentTime.format(callForDate.getTime());

                                final  String randomKey = saveCurrentDate +saveCurrentTime +current_USER_ID
                                        +System.nanoTime();


                                HashMap map = new HashMap();
                                map.put("uid" , current_USER_ID);
                                map.put("comment",comment);
                                map.put("date" ,saveCurrentDate);
                                map.put("time" ,saveCurrentTime);
                                map.put("userName" , userName);
                                map.put("fullName" , fullName);
                                map.put("profileImage" , userProfileImage);


                                DatabaseReference postRef2 = postRef.child(randomKey);
                                postRef2.updateChildren(map)
                                        .addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task)
                                            {

                                                if(task.isSuccessful())
                                                {
                                                    Toast.makeText(CommentActivity.this,
                                                            "commented successfully", Toast.LENGTH_SHORT).show();
                                                    inputComment.setText("");
                                                }
                                                else
                                                {
                                                    Toast.makeText(CommentActivity.this,
                                                            "error occured in commenting..", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });


                            }
                            else
                            {

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

            }
        });
    }




    private void initialize_id() {

        mToolbar = (Toolbar)findViewById(R.id.id_app_bar_comment_act);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Commnets");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        postCommentButton = (ImageButton)findViewById(R.id.id_comment_post_image_button);

        inputComment = (EditText)findViewById(R.id.id_comment_type);
        postKey = getIntent().getStringExtra("postKey");
        mAuth = FirebaseAuth.getInstance();
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey).child("comment");
        commentRef = postRef;
        current_USER_ID = mAuth.getCurrentUser().getUid().toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @Override
    protected void onStart() {
        super.onStart();



        FirebaseRecyclerAdapter<Comments , CommentsHolder>firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Comments, CommentsHolder>
                (
                        Comments.class ,R.layout.comments_displaying_layout ,
                        CommentsHolder.class , commentRef
                )
        {
            @Override
            protected void populateViewHolder(CommentsHolder holder, Comments model, int position) {

                holder.setComment(model.getComment());
                holder.setTime(model.getTime());
                holder.setDate(model.getDate());
                holder.setProfileImage(getApplicationContext() ,model.getProfileImage());
                holder.setFullName(model.getFullName());

            }
        };

        commentRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }


    public static class CommentsHolder extends RecyclerView.ViewHolder
    {
         View mView;
         CircleImageView prfileImage;
         TextView userName  , myComments  ,date2 , time2;

        public CommentsHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
            userName = (TextView)mView.findViewById(R.id.id_comment_userName);
            prfileImage = (CircleImageView)mView.findViewById(R.id.id_comment_dp);
            myComments = (TextView)mView.findViewById(R.id.id_comment_myComment);
            date2 = (TextView)mView.findViewById(R.id.id_comment_date);
            time2 = (TextView)mView.findViewById(R.id.id_comment_time);
        }

        public void setTime(String time)
        {
            time2.setText("Time :"+time);
        }
        public void setProfileImage(Context ctx , String profileImage)
        {
            Picasso.with(ctx).load(profileImage).placeholder(R.drawable.profile).into(prfileImage);
        }
        public void setFullName(String fullName)
        {
           userName.setText(fullName);
        }
        public void setDate(String date)
        {
            date2.setText("Date :"+date);
        }
        public void setComment(String comment)
        {
            myComments.setText(comment);
        }
    }
}
