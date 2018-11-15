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

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText editTextEmail,editTextPassword,editTextConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        editTextEmail = (EditText) findViewById(R.id.full_name_input);
        editTextPassword = (EditText) findViewById(R.id.login_password_input);
        editTextConfirmPassword = (EditText) findViewById(R.id.confirm_password_input);
        Button signUpBtn = (Button) findViewById(R.id.signup_btn);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();
                String confirmPassword = editTextConfirmPassword.getText().toString();

                //validates Input Email and Password
                if (checkInput(email, password, confirmPassword))
                {
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d("Firebase-Auth Status", "createUserWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        Toast.makeText(RegisterActivity.this, "User Created!",
                                                Toast.LENGTH_SHORT).show();
                                        Intent callHomeAct = new Intent(getBaseContext(),HomeActivity.class);
                                        startActivity(callHomeAct);
                                    }
                                    else {
                                        // If sign in fails, display a message to the user.
                                        Log.d("Firebase-Auth Status", "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
            });

    }

    private boolean checkInput(String email,String password,String confirmPassword){

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
        if(!password.equals(confirmPassword)){
            editTextConfirmPassword.setError("Passwords doesn't match");
            editTextPassword.setError("Passwords doesn't match");
            editTextPassword.requestFocus();
            editTextConfirmPassword.requestFocus();
            return false;
        }
        if(password.length() < 8)
        {
            editTextPassword.setError("Passwords Length should be atleast 8 characters!");
            editTextPassword.requestFocus();
            return false;
        }

        return true;
    }

}
