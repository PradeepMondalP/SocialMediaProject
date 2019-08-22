package com.example.socialmediaproject2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateProfileSettingActivity extends AppCompatActivity {


    private Toolbar mToolbar;
    private EditText status ,userName , fullName , countryName ,dateOfBirth , gender , relationship;
    private Button update_acc_setting;
    CircleImageView profileImage;
    private ProgressDialog mDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference current_user_ref;
    private String current_userID;

    private static final int Gallery_Pick= 1;
    private Uri ImageUri;
    private StorageReference userProfileRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile_setting);

        initialize_id();

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Setting");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        displayAllTheDataFirst();

        update_acc_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateAccountInfo();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        updateUserStatus("online");


    }

    @Override
    protected void onStart()
    {
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

    private void displayAllTheDataFirst() {


        current_user_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("userName"))
                {
                    String userName2 = dataSnapshot.child("userName").getValue().toString();
                    userName.setText(userName2);
                }
                if(dataSnapshot.hasChild("fullName"))
                {
                    String fullName2 = dataSnapshot.child("fullName").getValue().toString();
                    fullName.setText(fullName2);
                }
                if(dataSnapshot.hasChild("countryName"))
                {
                    String countryName2 = dataSnapshot.child("countryName").getValue().toString();
                    countryName.setText(countryName2);
                }
                if(dataSnapshot.hasChild("status"))
                {
                    String status2 = dataSnapshot.child("status").getValue().toString();
                    status.setText(status2);
                }
                if(dataSnapshot.hasChild("gender"))
                {
                    String gender2 = dataSnapshot.child("gender").getValue().toString();
                    gender.setText(gender2);
                }
                if(dataSnapshot.hasChild("dob"))
                {
                    String dob2 = dataSnapshot.child("dob").getValue().toString();
                    dateOfBirth.setText(dob2);
                }
                if(dataSnapshot.hasChild("relationshipStatus"))
                {
                    String relation2 = dataSnapshot.child("relationshipStatus").getValue().toString();
                    relationship.setText(relation2);
                }
                if(dataSnapshot.hasChild("profileImage"))
                {
                    String profileImage2 = dataSnapshot.child("profileImage").getValue().toString();
                    Picasso.with(getApplicationContext()).load(profileImage2).into(profileImage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void openGallery() {
        Intent gallery_intent = new Intent();
        gallery_intent.setType("image/*");
        gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(gallery_intent ,Gallery_Pick );
    }

    private void ValidateAccountInfo() {
        String  statuss = status.getText().toString();
        String userNamee = userName.getText().toString();
        String fullNamee = fullName.getText().toString();
        String countryNamee = countryName.getText().toString();
        String dobb = dateOfBirth.getText().toString();
        String genderr = gender.getText().toString();
        String relaionShipp = relationship.getText().toString();

        if(TextUtils.isEmpty(statuss))
        {
            status.setError("Required");
            return;
        }
        else if(TextUtils.isEmpty(userNamee))
        {
            userName.setError("Required");
            return;
        }
        else if(TextUtils.isEmpty(fullNamee))
        {
            fullName.setError("Required");
            return;
        }
        else if(TextUtils.isEmpty(countryNamee))
        {
            countryName.setError("Required");
            return;
        }
        else if(TextUtils.isEmpty(dobb))
        {
            dateOfBirth.setError("Required");
            return;
        }
        else if(TextUtils.isEmpty(genderr))
        {
            gender.setError("Required");
            return;
        }
        else if(TextUtils.isEmpty(relaionShipp))
        {
            relationship.setError("Required");
            return;
        }

        else
        {
            mDialog.setTitle("Profile Image..");
            mDialog.setMessage("Please wait while upadating ur profile image....");
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();
            updateAccountInfo(statuss ,userNamee ,fullNamee,countryNamee ,dobb ,genderr ,relaionShipp);
        }

    }

    private void updateAccountInfo(String statuss, String userNamee,
                                   String fullNamee, String countryNamee,
                                   String dobb, String genderr, String relaionShipp) {

        HashMap userMap = new HashMap();
        userMap.put("userName" , userNamee);
        userMap.put("fullName" , fullNamee);
        userMap.put("countryName" , countryNamee);
        userMap.put("status" , statuss );
        userMap.put("gender" , genderr);
        userMap.put("dob" , dobb);
        userMap.put("relationshipStatus" , relaionShipp);

        current_user_ref.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {

                if(task.isSuccessful())
                {
                    Toast.makeText(getApplicationContext(),
                            "Account settings updated successfully", Toast.LENGTH_SHORT).show();
                    mDialog.dismiss();

                }
                else {
                    Toast.makeText(getApplicationContext(),
                            "couldnt upload ur setting info", Toast.LENGTH_SHORT).show();
                    mDialog.dismiss();
                }
            }
        });
    }

    private void initialize_id() {

        mToolbar = (Toolbar)findViewById(R.id.id_setting_toolbar)  ;
        status =(EditText)findViewById(R.id.id_setting_profile_status);
        userName =(EditText)findViewById(R.id.id_setting_profile_username);
        fullName =(EditText)findViewById(R.id.id_setting_profile_fullName);
        countryName =(EditText)findViewById(R.id.id_setting_profile_countryName);
        dateOfBirth =(EditText)findViewById(R.id.id_setting_profile_dob);
        gender =(EditText)findViewById(R.id.id_setting_profile_gender);
        relationship =(EditText)findViewById(R.id.id_setting_profile_relationship);
        update_acc_setting = (Button)findViewById(R.id.id_setting_profile_update_all_button);
        profileImage = (CircleImageView)findViewById(R.id.id_setting_profile_image);
        mDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        current_userID =mAuth.getCurrentUser().getUid();

        userProfileRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        current_user_ref = FirebaseDatabase.getInstance().getReference().child("Users").child(current_userID);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode==Gallery_Pick && resultCode ==RESULT_OK
                && data!=null && data.getData()!=null) {
            ImageUri = data.getData();

            // now i want to crop the image
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if(  requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                mDialog.setTitle("Profile Image");
                mDialog.setMessage("Please wait while upadating ur profile image....");
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();

                final Uri resultUri = result.getUri();

                // after cropping im displaying it immediately
                Picasso.with(getApplicationContext()).load(resultUri).into(profileImage);

                final StorageReference filePath = userProfileRef.child(current_userID +".jpg");

                //  saving to storage and confirming it
                filePath.putFile(resultUri).addOnCompleteListener
                        (new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                if(task.isSuccessful())
                                {
                                    Toast.makeText(getApplicationContext(),
                                            "profile image  successfully uploaded"
                                            , Toast.LENGTH_SHORT).show();


                                    //  getting the image url and saving it to the firebase database and confirming it

                                    filePath.getDownloadUrl().addOnSuccessListener
                                            (new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {

                                                    // uri returns the url of the image
                                                    final String downloadUrl = uri.toString();

                                                    // saving the url in the database and confirming it
                                                    current_user_ref.child("profileImage").setValue(downloadUrl)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if(task.isSuccessful())
                                                                    {
                                                                        //  come here and try to add finish
                                                                        startActivity(new Intent(
                                                                                getApplicationContext() ,
                                                                                UpdateProfileSettingActivity.class
                                                                        ));

                                                                        mDialog.dismiss();
                                                                    }
                                                                    else
                                                                    {
                                                                        Toast.makeText(getApplicationContext(),
                                                                                "error ocured"+
                                                                                        task.getException()
                                                                                                .getMessage(),
                                                                                Toast.LENGTH_SHORT).show();
                                                                        mDialog.dismiss();
                                                                    }
                                                                }
                                                            });

                                                }
                                            }) ;
                                }
                            }
                        });
            }
            else
            {
                Toast.makeText(getApplicationContext(),
                        "error occured: Image Cannot be cropped try again..",
                        Toast.LENGTH_SHORT).show();
                mDialog.dismiss();
            }
        }

    }
}
