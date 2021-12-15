package com.food.recognizer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "Food Recognizer :: " + SignUpActivity.class.getSimpleName();

    private static final int RC_SIGN_IN = 234;

    private FirebaseAuth mAuth;

    private EditText email;
    private EditText pass;

    private GoogleSignInClient mGoogleSignIn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.email_signup);
        pass = findViewById(R.id.password_signup);

        email.addTextChangedListener(emailTextWatcher);
        pass.addTextChangedListener(passTextWatcher);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignIn = GoogleSignIn.getClient(this, gso);

        final SignInButton googleLogin = findViewById(R.id.google_button_signup);
        googleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent googleIntent = mGoogleSignIn.getSignInIntent();
                startActivityForResult(googleIntent, RC_SIGN_IN);
            }
        });

        final Button signUpBtn = findViewById(R.id.signup_btn);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!isValidEmail() || !isValidPassword()){
                    // do nothing
                } else {
                    String uname = email.getText().toString();
                    String password = pass.getText().toString();

                    mAuth.createUserWithEmailAndPassword(uname,password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if(task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "Registration Successfull", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(getApplicationContext(),"Registration Unsuccessfull", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);

                //authenticating with firebase
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.e(TAG, e.toString());
                Toast.makeText(getApplicationContext(), "Error while signing in", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acc) {

        AuthCredential authCredential = GoogleAuthProvider.getCredential(acc.getIdToken(), null);

        mAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Error while logining with google", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private TextWatcher emailTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

            isValidEmail();

        }
    };

    private TextWatcher passTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

            isValidPassword();

        }
    };

    private boolean isValidEmail() {

        if(email.getText().toString().isEmpty()) {
            email.setError("Enter email");
            return false;
        }

        if(!email.getText().toString().contains("@") ||
                !email.getText().toString().contains(".")) {
            email.setError("Enter valid email address");
            return false;
        }

        return true;
    }

    private boolean isValidPassword() {

        if(pass.getText().toString().isEmpty()) {
            pass.setError("Enter password");
            return false;
        }

        if(pass.getText().toString().length() < 5) {
            pass.setError("Password must be of > 5 characters");
            return false;
        }

        return true;
    }
}
