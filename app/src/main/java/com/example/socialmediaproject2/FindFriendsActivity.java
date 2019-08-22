package com.example.socialmediaproject2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText friendsName;
    private ImageButton searchButton;
    private RecyclerView friendList;
    private DatabaseReference userRef ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);


        mToolbar = (Toolbar)findViewById(R.id.id_find_friend_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Find Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        friendList = (RecyclerView)findViewById(R.id.id_find_friend_result_list);

        friendList.setHasFixedSize(true);
        friendList.setLayoutManager(new LinearLayoutManager(this));

        friendsName = (EditText)findViewById(R.id.id_find_friend_search_box_input);
        searchButton = (ImageButton)findViewById(R.id.id_find_friend_search_button);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");


        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchBoxInput = friendsName.getText().toString().trim();

                searchPeopleAndFriend();
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


    private void searchPeopleAndFriend() {

        Toast.makeText(this, "searching..", Toast.LENGTH_LONG).show();

        FirebaseRecyclerAdapter<FindFriends ,FindFriendHolder> recyclerAdapter
                = new FirebaseRecyclerAdapter<FindFriends, FindFriendHolder>
                (
                        FindFriends.class , R.layout.all_user_display_layout,
                        FindFriendHolder.class , userRef
                )
        {
            @Override
            protected void populateViewHolder(FindFriendHolder holder, FindFriends model , final int pos) {


                holder.setFullName(model.getFullName ()) ;
                holder.setStatus(model.getStatus());

                holder.setProfileImage(getApplicationContext(), model.getProfileImage());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_User_id = getRef(pos).getKey().toString();

                        Intent profIntent = new Intent(getApplicationContext() , PersonProfileActivity.class);
                        profIntent.putExtra("visitUserId" , visit_User_id);
                        startActivity(profIntent);
                    }
                });
            }

        };
               friendList.setAdapter(recyclerAdapter);

    }

    public static class FindFriendHolder extends RecyclerView.ViewHolder
    {

        View mView;
        CircleImageView profileImagee ;
        TextView userNamee , statuss;
        public FindFriendHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
            profileImagee = (CircleImageView)mView.findViewById(R.id.id_all_user_profile_image);
            userNamee = (TextView)mView.findViewById(R.id.id_user_profile_name);
            statuss = (TextView)mView.findViewById(R.id.id_user_status);
        }

        public void setStatus(String status)
        {
            statuss.setText(status);
        }
        public void setFullName(String fullName)
        {
            userNamee.setText(fullName);
        }
        public void setProfileImage(Context ctx , String profileImage)
        {
            Picasso.with(ctx).load(profileImage).placeholder(R.drawable.profile).into(profileImagee);
        }
    }
}
