package com.example.socialmediaproject2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RatingActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private Button submitButton;
    private FirebaseAuth mAuth;
    private String currentUser  , userName;
    private DatabaseReference userRef , ratingRef;
    private Toolbar mToolbar ;
    private TextView tv;
    private float ratingValue ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);


        initialize_id();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                submitRatingToDataBase();
            }
        });
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


    private void submitRatingToDataBase()
    {
        ratingValue = ratingBar.getRating();

        if(ratingValue<2)
            tv.setText("rating ="+ratingValue +"\n is that worse ?");
        else
        if(ratingValue<=3 && ratingValue>=2)
            tv.setText("rating ="+ratingValue +"\n  we will try to be better");
        else
        if(ratingValue>3 && ratingValue<=4)
            tv.setText("rating ="+ratingValue +"\n  You had a good time");
        else
        if(ratingValue>4)
            tv.setText("rating ="+ratingValue +"\n  Thank you");

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("fullName"))
                {
                    userName= dataSnapshot.child("fullName").getValue().toString();

                    HashMap map = new HashMap();
                    map.put("fullName" , userName);
                    map.put("rating" , ratingValue);

                    ratingRef.updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task)
                        {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(RatingActivity.this,
                                        "Thank you for rating us" ,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initialize_id()
    {
        mToolbar = (Toolbar)findViewById(R.id.id_nav_bar_ratingActivity);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Posting Images");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ratingBar = (RatingBar)findViewById(R.id.id_ratingBar);
        submitButton = (Button)findViewById(R.id.id_rating_button);
        mAuth =FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser().getUid().toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser);
        ratingRef = FirebaseDatabase.getInstance().getReference().child("MyRatings").child(currentUser);
        tv =(TextView)findViewById(R.id.id_rating_display);

    }
}
