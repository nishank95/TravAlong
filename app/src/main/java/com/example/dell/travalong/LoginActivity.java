package com.example.dell.travalong;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail,editTextPassword;
    private Button signup_btn,login_btn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        editTextEmail = (EditText) findViewById(R.id.login_email_input);
        editTextPassword = (EditText) findViewById(R.id.login_password_input);
        signup_btn = (Button) findViewById(R.id.signup_btn);
        login_btn = (Button) findViewById(R.id.login_btn);

        mAuth = FirebaseAuth.getInstance();

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();

                if(checkInput(email,password))
                {
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task)
                                {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d("Firebase-Auth Status", "signInWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        if (user != null) {
                                            Toast.makeText(LoginActivity.this, "Login Successful! Welcome, " + user.getDisplayName() ,
                                                    Toast.LENGTH_SHORT).show();
                                            Intent callHomeAct = new Intent(getBaseContext(),HomeActivity.class);
                                            startActivity(callHomeAct);
                                        }
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w("Firebase-Auth Status", "signInWithEmail:failure", task.getException());
                                        Toast.makeText(LoginActivity.this, "Incorrect Email or Password!",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });



        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callRegisterAct = new Intent(view.getContext(),RegisterActivity.class);
                startActivity(callRegisterAct);
            }
        });
    }

    private boolean checkInput(String email,String password){

        if(email.isEmpty())
        {
            editTextEmail.setError("Email Field Required!");
            editTextEmail.requestFocus();
            return false;
        }
        if(password.isEmpty())
        {
            editTextPassword.setError("Password Required!");
            editTextPassword.requestFocus();
            return false;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            editTextEmail.setError("Invalid Email-ID!");
            editTextEmail.requestFocus();
            return false;
        }


        return true;
    }

}
