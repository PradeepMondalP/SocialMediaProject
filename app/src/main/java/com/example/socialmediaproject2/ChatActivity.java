package com.example.socialmediaproject2;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity {
    private FloatingActionButton sendMessageButton, sendImageFileButton;
    private EditText userMessgaeInput;
    private RecyclerView chatActivityRecyclerView;
    private String messageReceiverID , messageReceiverName , messageSenderID ,   uniKey;
    private FirebaseAuth mAuth;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager ;
    private MessageAdapter messageAdapter ;
    private String UserID;

    private TextView receiverName , userLastSeen;
    CircleImageView receiverProfileImage;
    private DatabaseReference rootRef , messageRef ,userRef;

    private Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        initialize_id();



        displayReceiverInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        fetchMessages();



    }

    private void fetchMessages()
    {
        DatabaseReference rootRef2 =  rootRef.child("Messages")
                .child(messageSenderID).child(messageReceiverID);

        rootRef2.keepSynced(true);

        rootRef2.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                if(dataSnapshot.exists())
                {
                    Messages messagesObj =dataSnapshot.getValue(Messages.class);
                    messagesList.add(messagesObj);
                    messageAdapter.notifyDataSetChanged();

                    chatActivityRecyclerView.smoothScrollToPosition(chatActivityRecyclerView
                            .getAdapter().getItemCount());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void sendMessage()
        {

            final   String messageText = userMessgaeInput.getText().toString().trim();
            if(TextUtils.isEmpty(messageText))
            {
                userMessgaeInput.setError("Enter a text");
                return;
            }
            else
            {
                userMessgaeInput.setText("");
                DatabaseReference messageSenderRef = messageRef.
                        child(messageSenderID).
                        child(messageReceiverID);

                final DatabaseReference messageReceiverRef = messageRef.
                        child(messageReceiverID).
                        child(messageSenderID);
                uniKey = messageSenderRef.push().getKey();

                Calendar callForDate = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                final String  saveCurrentDate =currentDate.format(callForDate.getTime());

                Calendar callForTime = Calendar.getInstance();
                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
                final String  saveCurrentTime =currentTime.format(callForDate.getTime());


                DatabaseReference messageSenderRef2 = messageSenderRef.child(uniKey);
                Map map = new HashMap();
                map.put("date",saveCurrentDate);
                map.put("time",saveCurrentTime);
                map.put("message", messageText);
                map.put("from" ,messageSenderID);
                map.put("type","text");

                messageSenderRef2.updateChildren(map).addOnCompleteListener
                        (new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task)
                            {
                                if(task.isSuccessful())
                                {
                                    DatabaseReference messageReceiverRef2 =
                                            messageReceiverRef.child(uniKey);

                                    Map map = new HashMap();
                                    map.put("date",saveCurrentDate);
                                    map.put("time",saveCurrentTime);
                                    map.put("message", messageText);
                                    map.put("from" ,messageSenderID);
                                    map.put("type","text");

                                    messageReceiverRef2.updateChildren(map).addOnCompleteListener
                                            (new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task) {
                                                    if(task.isSuccessful())
                                                    {

                                                        userMessgaeInput.setText(" ");
                                                    }
                                                    else
                                                    {
                                                        Toast.makeText(ChatActivity.this,
                                                                "messgae couldn't sent ",
                                                                Toast.LENGTH_SHORT).show();

                                                        userMessgaeInput.setText(" ");
                                                    }

                                                }
                                            });
                                }
                            }
                        });

            }
        }




    private void displayReceiverInfo()
    {


        receiverName.setText(messageReceiverName);

      DatabaseReference rootRef2=  rootRef.child("Users").child(messageReceiverID);

      rootRef2.keepSynced(true);

      rootRef2.addValueEventListener(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot)
          {
              if(dataSnapshot.hasChild("profileImage"))
              {
                  final String profileImage = dataSnapshot.child("profileImage")
                          .getValue().toString();
                  Picasso.with(getApplicationContext()).load(profileImage)
                          .into(receiverProfileImage);
              }

              if(dataSnapshot.child("userState"). hasChild("type") )
              {
                  final String type = dataSnapshot.child("userState")
                          .child("type").getValue().toString();

                  final String lastDate = dataSnapshot.child("userState")
                          .child("date").getValue().toString();

                  final String lastTime = dataSnapshot.child("userState")
                          .child("time").getValue().toString();

                  if(type.equals("online"))
                  {
                      userLastSeen.setText("online");
                  }
                  else
                  {
                      userLastSeen.setText("last seen  "+ lastTime +"  "+lastDate );
                  }
              }

          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {

          }
      });
    }



    private void initialize_id()
    {

        mToolbar = (Toolbar) findViewById(R.id.chat_bar_layout);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);


        LayoutInflater layoutInflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar ,null);
        actionBar.setCustomView(action_bar_view);

        sendMessageButton = (FloatingActionButton) findViewById(R.id.id_chat_activity_send_message_button);
        userMessgaeInput= (EditText)findViewById(R.id.id_chat_activity_message_text);
        chatActivityRecyclerView = (RecyclerView)findViewById(R.id.id_chat_activity_recycle_view);
        userLastSeen = (TextView) findViewById(R.id.id_custom_user_last_seen);

        mAuth =FirebaseAuth.getInstance();
        UserID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        messageReceiverID = getIntent().getStringExtra("visitUserId");
        messageSenderID =mAuth.getCurrentUser().getUid();
        messageReceiverName = getIntent().getStringExtra("userName");
        messageRef = rootRef.child("Messages");
        userRef =FirebaseDatabase.getInstance().getReference().child("Users");

        receiverName = (TextView)findViewById(R.id.id_custom_profile_name);
        receiverProfileImage=(CircleImageView)findViewById(R.id.id_custom_profile_image);

        messageAdapter = new MessageAdapter(messagesList);
        chatActivityRecyclerView = (RecyclerView)findViewById(R.id.id_chat_activity_recycle_view);

        linearLayoutManager = new LinearLayoutManager(this);
        chatActivityRecyclerView.setHasFixedSize(true);
        chatActivityRecyclerView.setLayoutManager(linearLayoutManager);
        chatActivityRecyclerView.setAdapter(messageAdapter);


    }



}
