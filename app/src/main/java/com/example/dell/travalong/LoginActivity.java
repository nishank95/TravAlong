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

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.login_email_input)EditText editTextEmail;
    @BindView(R.id.login_password_input)EditText editTextPassword;
    @BindView(R.id.signup_btn)Button signup_btn;
    @BindView(R.id.login_btn)Button login_btn;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();

        login_btn.setOnClickListener(view -> {

            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();

            if(checkInput(email,password))
            {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, task -> {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("Firebase-Auth Status", "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    Toast.makeText(LoginActivity.this, getString(R.string.login_welcome_message) + user.getDisplayName() ,
                                            Toast.LENGTH_SHORT).show();
                                    Intent callHomeAct = new Intent(getBaseContext(),HomeActivity.class);
                                    startActivity(callHomeAct);
                                }
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("Firebase-Auth Status", "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, R.string.login_error_message,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });



        signup_btn.setOnClickListener(view -> {
            Intent callRegisterAct = new Intent(view.getContext(),RegisterActivity.class);
            startActivity(callRegisterAct);
        });
    }

    private boolean checkInput(String email,String password){

        if(email.isEmpty())
        {
            editTextEmail.setError(getString(R.string.email_required_message));
            editTextEmail.requestFocus();
            return false;
        }
        if(password.isEmpty())
        {
            editTextPassword.setError(getString(R.string.pass_required_message));
            editTextPassword.requestFocus();
            return false;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            editTextEmail.setError(getString(R.string.email_invalid_message));
            editTextEmail.requestFocus();
            return false;
        }


        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent callHomeAct = new Intent(getBaseContext(),HomeActivity.class);
            startActivity(callHomeAct);
        }
    }
}
