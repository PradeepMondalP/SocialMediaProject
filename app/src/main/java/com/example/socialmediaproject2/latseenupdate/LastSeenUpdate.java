package com.example.socialmediaproject2.latseenupdate;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class LastSeenUpdate {
    private DatabaseReference rootRef , userRef  , userStateRef;
    private FirebaseAuth mAuth;
    private String currentUserId , saveCurrentDate ,saveCurrentTime  , state;

    public LastSeenUpdate(String currentUserId)
    {
        this.currentUserId = currentUserId;
    }

    public void update(String state)
    {
         // all initialization stuff
        rootRef = FirebaseDatabase.getInstance().getReference();
        userRef = rootRef.child("Users");
        mAuth = FirebaseAuth.getInstance();
        userStateRef = userRef.child(currentUserId).child("userStateRef");

        SimpleDateFormat date = new SimpleDateFormat("MMM dd,yyyy");
        saveCurrentDate = date.format(Calendar.getInstance().getTime());

        SimpleDateFormat time = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = time.format(Calendar.getInstance().getTime());

        Map map = new HashMap();
        map.put("time" , saveCurrentTime);
        map.put("date" , saveCurrentDate);
        map.put("type" , state);

        userStateRef.setValue(map);

    }
}
