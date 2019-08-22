package com.example.socialmediaproject2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText email , pass;
    private Button login;
    private TextView signUp;
    private ProgressDialog mDialog;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initialize_id();

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext() , RegisterActivity.class));
                finish();
            }
        });


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mEmail = email.getText().toString().trim();
                String mPass = pass.getText().toString().trim();

                if(TextUtils.isEmpty(mEmail)){
                    email.setError("Enter a mail");
                    return;
                }
                if(TextUtils.isEmpty(mPass)){
                    pass.setError("Enter a password");
                    return;
                }
                else {
                    mDialog.setTitle("Logging in");
                    mDialog.setMessage("Loading....");
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();

                    mAuth.signInWithEmailAndPassword(mEmail , mPass)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if(task.isSuccessful()){
                                        mDialog.dismiss();
                                        sendToMainActivity();
                                        Toast.makeText(getApplicationContext(),
                                                "logged in successfully", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        Toast.makeText(getApplicationContext(),
                                                "Failed to log in", Toast.LENGTH_SHORT).show();
                                        mDialog.dismiss();
                                    }
                                }
                            });
                }
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();


        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null)
        {
            sendToMainActivity();
        }
    }



    private void sendToMainActivity() {
        Intent intent = new Intent(getApplicationContext() , MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void initialize_id()
    {
        email =(EditText) findViewById(R.id.id_email);
        pass = (EditText) findViewById(R.id.id_pass);
        login = (Button) findViewById(R.id.id_login);
        signUp = (TextView) findViewById(R.id.id_signup);

        mAuth = FirebaseAuth.getInstance();
        mDialog = new ProgressDialog(this);

    }
}
