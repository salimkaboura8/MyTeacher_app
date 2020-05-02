package com.example.thedraft;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.model.Document;
import com.google.firestore.v1.WriteResult;

import java.util.List;

public class JoinClass extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = "DocSnippets";
    private FirebaseFirestore db;
    private EditText codeET;
    private Button confirmButton;
    private DrawerLayout drawerLayout;
    private String code;
    private NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
// Initialize Firebase Auth
        db = FirebaseFirestore.getInstance();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);

        codeET = findViewById(R.id.codeET);
        confirmButton = findViewById(R.id.confirmButton);

        confirmButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                code = codeET.getText().toString().trim();
                if (code.isEmpty()) {
                    codeET.setError("Please enter the code.");
                } else
                    verifyCode(v);
            }

            void verifyCode(View v) {

                DocumentReference docIdRef = db.collection("classes").document(codeET.getText().toString().trim());
                docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                DocumentReference cryptoRef = db.collection("classes").document(code);

                                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                DocumentReference userRef = FirebaseFirestore.getInstance().document("users/" + userId);
                                cryptoRef.update("members", FieldValue.arrayUnion(userRef));

                                Toast.makeText(JoinClass.this, "Class joined successfully",
                                        Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), UserActivity.class));
                                finish();

                            } else {
                                codeET.setError("This code doesn't belong to any class!");
                            }
                        } else {
                            Toast.makeText(JoinClass.this, "Unexpected error",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), UserActivity.class));
                            finish();
                        }
                    }
                });
            }

            ;
        });

    }

    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_join:
                startActivity(new Intent(getApplicationContext(), JoinClass.class));
                finish();
                break;
            case R.id.nav_classes:
                startActivity(new Intent(getApplicationContext(), MyClasses.class));
                finish();
                break;
            case R.id.nav_logout:
                FirebaseAuth.getInstance().signOut();
                finish();
                break;
            case R.id.nav_lessons:
                startActivity(new Intent(getApplicationContext(), UserActivity.class));
                finish();
                break;
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), UserActivity.class));
        finish();
    }
}