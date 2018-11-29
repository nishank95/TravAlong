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

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    @BindView(R.id.full_name_input)EditText editTextEmail;
    @BindView(R.id.login_password_input)EditText editTextPassword;
    @BindView(R.id.confirm_password_input)EditText editTextConfirmPassword;
    @BindView(R.id.signup_btn) Button signUpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();

        signUpBtn.setOnClickListener(view -> {
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();
            String confirmPassword = editTextConfirmPassword.getText().toString();

            //validates Input Email and Password
            if (checkInput(email, password, confirmPassword))
            {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegisterActivity.this, task -> {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("Firebase-Auth Status", "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(RegisterActivity.this, R.string.user_create_message,
                                        Toast.LENGTH_SHORT).show();
                                Intent callUpdateProfileAct = new Intent(getBaseContext(),UpdateProfileActivity.class);
                                startActivity(callUpdateProfileAct);
                            }
                            else {
                                // If sign in fails, display a message to the user.
                                Log.d("Firebase-Auth Status", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(RegisterActivity.this, R.string.auth_fail_message,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }

    private boolean checkInput(String email,String password,String confirmPassword){

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
        if(!password.equals(confirmPassword)){
            editTextConfirmPassword.setError(getString(R.string.pass_match_error));
            editTextPassword.setError(getString(R.string.pass_match_error));
            editTextPassword.requestFocus();
            editTextConfirmPassword.requestFocus();
            return false;
        }
        if(password.length() < 8)
        {
            editTextPassword.setError(getString(R.string.pass_length_message));
            editTextPassword.requestFocus();
            return false;
        }

        return true;
    }

}
