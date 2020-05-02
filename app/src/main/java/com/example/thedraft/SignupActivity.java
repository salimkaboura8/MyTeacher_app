package com.example.thedraft;

import android.app.Activity;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class SignupActivity extends Activity {

    public static final String TAG = "TAG";
    private FirebaseFirestore fStore;
    private EditText firstnameET, lastnameET, emailET, passwordET, confirmpasswordET;
    private Button signupButton;
    private FirebaseAuth mAuth;
    private String email, password, firstname, lastname, confirmpassword, role;
    private RadioGroup radioGroup;
    private RadioButton studentRB, teacherRB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        emailET = findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passwordET);
        confirmpasswordET = findViewById(R.id.confirmpasswordET);
        firstnameET = findViewById(R.id.firstnameET);
        lastnameET = findViewById(R.id.lastnameET);
        signupButton = findViewById(R.id.signupButton);
        radioGroup = findViewById(R.id.radioGroup);
        if (radioGroup.getCheckedRadioButtonId() == R.id.studentRB) role = "s";
        else role = "t";

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = emailET.getText().toString().trim();
                password = passwordET.getText().toString().trim();
                confirmpassword = confirmpasswordET.getText().toString().trim();
                firstname = firstnameET.getText().toString().trim();
                lastname = lastnameET.getText().toString().trim();

                if (verifyFields(v)) {
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("EmailPassword", "signInWithEmail:success");
                                        Toast.makeText(SignupActivity.this, "User created successfully.", Toast.LENGTH_SHORT).show();
                                        final String userID = mAuth.getCurrentUser().getUid();
                                        fStore = FirebaseFirestore.getInstance();
                                        DocumentReference documentReference = fStore.collection("users").document(userID);
                                        Map<String,Object> user = new HashMap<>();
                                        user.put("firstname", firstname);
                                        user.put("lastname", lastname);
                                        user.put("email", email);
                                        user.put("role", role);
                                        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d( TAG,"onSuccess: User profile is created for "+userID);
                                            }
                                        });
                                        startActivity(new Intent(getApplicationContext(), UserActivity.class));
                                    } else {
                                        task.getException().printStackTrace();
                                        Log.w("EmailPassword", "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(SignupActivity.this, "Something went wrong!",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }

    protected boolean verifyFields(View v) {

        boolean result = true;
        if (firstname.length() == 0) {
            firstnameET.setError("This field can't be empty");
            result = false;
        } else firstnameET.setError(null);

        if (lastname.length() == 0) {
            lastnameET.setError("This field can't be empty");
            result = false;
        } else lastnameET.setError(null);

        if (email.length() == 0) {
            emailET.setError("This field can't be empty");
            result = false;
        } else emailET.setError(null);

        if (password.length() == 0) {
            passwordET.setError("This field can't be empty");
            result = false;
        } else passwordET.setError(null);

        if (confirmpassword.length() == 0) {
            confirmpasswordET.setError("This field can't be empty");
            result = false;
        } else {
            if (!(confirmpassword.equals(password))) {
                confirmpasswordET.setError("The passwords must be the same");
                result = false;
            } else confirmpasswordET.setError(null);
        }
        return result;
    }
}
