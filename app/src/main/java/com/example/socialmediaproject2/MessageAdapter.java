package com.example.socialmediaproject2;

import android.graphics.Color;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter <MessageAdapter.MessageViewHolder>
{
    private List<Messages> userMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseRef;

    public MessageAdapter(List<Messages> userMessageList) {
        this.userMessageList = userMessageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_layout_user , parent ,false);

        mAuth =FirebaseAuth.getInstance();
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position)
    {

        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessageList.get(position);
        String fromUserId = messages.getFrom();
        String fromMessageType=messages.getType();

        userDatabaseRef = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(fromUserId);

        userDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("profileImage"))
                {
                    String image = dataSnapshot.child("profileImage")
                            .getValue().toString();

                    Picasso.with(holder.receiverProfileImage.getContext()).load(image)
                            .into(holder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(fromMessageType.equals("text")){
            holder.receiverMessageText.setVisibility(View.INVISIBLE);
            holder.receiverProfileImage.setVisibility(View.INVISIBLE);


            if(fromUserId.equals(messageSenderId))
            {

                holder.senderMessageText.setBackgroundResource(R.drawable.sender_message_text_background);
                holder.senderMessageText.setTextColor(Color.WHITE);
                holder.senderMessageText.setGravity(Gravity.LEFT);
                holder.senderMessageText.setText(messages.getMessage());

            }else
            {
                holder.senderMessageText.setVisibility(View.INVISIBLE);

                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_text_background);
                holder.receiverMessageText.setTextColor(Color.BLACK);
                holder.receiverMessageText.setGravity(Gravity.LEFT);
                holder.receiverMessageText.setText(messages.getMessage());

            }
        }
    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

    public class MessageViewHolder  extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener ,
            MenuItem.OnMenuItemClickListener

    {
        View mView;
        TextView senderMessageText , receiverMessageText;
        CircleImageView receiverProfileImage;

        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);
            mView = itemView;
            senderMessageText = (TextView)mView.findViewById(R.id.sender_message_text);
            receiverMessageText = (TextView)mView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = (CircleImageView)mView.findViewById(R.id.message_profile_image);

            senderMessageText.setOnCreateContextMenuListener(this);
        }


        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

            menu.setHeaderTitle("Options");
            MenuItem item1 = menu.add(Menu.NONE , 0 , 1 , "details");
            MenuItem item2 = menu.add(Menu.NONE , 1 , 2 ,"anything");

            item1.setOnMenuItemClickListener(this);
            item2.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId())
            {
                case 0:
                    System.out.println("Details pressed");
                    return true;
                case 1:
                    System.out.println("anything pressed");
                    return true;
            }
            return false;
        }
    }
}
