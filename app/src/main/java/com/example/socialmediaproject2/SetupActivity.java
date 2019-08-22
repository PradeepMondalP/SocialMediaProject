package com.example.socialmediaproject2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private CircleImageView profile_pic;
    private EditText user_Name ,full_name ,country_name ;
    private Button save;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    String current_user_id;
    private ProgressDialog mDialog;
    private static final int Gallery_Pick = 1;
    private StorageReference userProfileRef;

    private Uri ImageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        initialize_id();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAccountSetupInformation();
            }
        });

        profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent gallery_intent = new Intent();
                gallery_intent.setType("image/*");
                gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(gallery_intent ,Gallery_Pick );
            }
        });
    }

    private void initialize_id() {
        profile_pic = (CircleImageView)findViewById(R.id.id_profile_image_setup);
        user_Name = (EditText)findViewById(R.id.id_multiline_username);
        full_name = (EditText)findViewById(R.id.id_multiline_fullName);
        country_name =(EditText)findViewById(R.id.id_multiline_country);
        save = (Button)findViewById(R.id.id_saveInfo_Setup);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id);
        mDialog = new ProgressDialog(this);
        userProfileRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
    }



    private void saveAccountSetupInformation() {

        String userName = user_Name.getText().toString().trim();
        String fullName = full_name.getText().toString().trim();
        String countryName = country_name.getText().toString().trim();

        if(TextUtils.isEmpty(userName))
        {
            user_Name.setError("Please enter the name");
            return;
        }
        else if(TextUtils.isEmpty(fullName))
        {
            full_name.setError("Please enter the full name");
            return;
        }
        else if(TextUtils.isEmpty(countryName))
        {
            country_name.setError("Please enter the country name");
            return;
        }

        // adding this 1 extra
        else if(ImageUri==null)
        {
            Toast.makeText(getApplicationContext(), "you must select a pix",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        // untill here.........................
        else
        {
            mDialog.setTitle("Saving information..");
            mDialog.setMessage("Please wait while we are creating new Account...");
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();

            HashMap userMap = new HashMap();
            userMap.put("userName" , userName);
            userMap.put("fullName" , fullName);
            userMap.put("countryName" , countryName);
            userMap.put("status" , "null" );
            userMap.put("gender" , "male");
            userMap.put("dob" , "null");
            userMap.put("relationshipStatus" , "None");

            userRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if(task.isSuccessful())
                    {
                        Toast.makeText(SetupActivity.this,
                                "account creation done", Toast.LENGTH_SHORT).show();
                        mDialog.dismiss();
                        sendToMainActivity();

                    }
                    else
                    {
                        mDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "" +
                                "couldn't save account info", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
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

            if(resultCode ==RESULT_OK)
            {
                mDialog.setTitle("Profile Image");
                mDialog.setMessage("Please wait while upadating ur profile image....");
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();

                final Uri resultUri = result.getUri();

                // after cropping im displaying it immediately
                Picasso.with(getApplicationContext()).load(resultUri).into(profile_pic);

                final StorageReference filePath = userProfileRef.child(current_user_id +".jpg");

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
                                                    userRef.child("profileImage").setValue(downloadUrl)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if(task.isSuccessful())
                                                                    {
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


    private void sendToMainActivity() {
        Intent intent = new Intent(getApplicationContext() , MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
