package com.example.thedraft;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thedraft.R;
import com.example.thedraft.UserActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.auth.User;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CreateClass extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseFirestore db;
    private DrawerLayout drawerLayout;
    private FirebaseAuth firebaseAuth;
    private NavigationView navigationView;
    private TextInputEditText codeET, nameET;
    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createclass);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);

        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        UserActivity.updateHeader(this);

        codeET = findViewById(R.id.classcodeET);
        nameET = findViewById(R.id.classnameET);
        confirmButton = findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createClass(v);
            }
        });
    }

    private void createClass(View v) {

        if (verifyFields()) {

            DocumentReference docIdRef = db.collection("classes").document(codeET.getText().toString().trim());
            docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            codeET.setError("Try a different class code.");
                        } else {

                            final Map<String, Object> newClass = new HashMap<>();
                            newClass.put("name", nameET.getText().toString().trim());
                            DocumentReference dRef = db.document("users/" + firebaseAuth.getCurrentUser().getUid());
                            newClass.put("creator", dRef);
                            ArrayList<DocumentReference> members = new ArrayList<DocumentReference>();
                            members.add(db.collection("users")
                                    .document(firebaseAuth.getCurrentUser().getUid()));
                            newClass.put("members", members);

                            db.collection("classes").document( codeET.getText().toString().trim())
                                    .set(newClass).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(CreateClass.this, "Class created successfully!", Toast.LENGTH_LONG).show();
                                            startActivity(new Intent(getApplicationContext(), MyClasses.class));
                                            finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(CreateClass.this, "Unexpected error.", Toast.LENGTH_LONG).show();
                                            startActivity(new Intent(getApplicationContext(), MyClasses.class));
                                            finish();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(CreateClass.this, "Unexpected error.", Toast.LENGTH_LONG);
                    }
                }
            });


        }
    }

    private boolean verifyFields() {
        boolean result = true;
        if (codeET.getText().toString().trim().length() < 4 || codeET.getText().toString().isEmpty()) {
            codeET.setError("The class code must contain 4 or more characters.");
            result = false;
        } else codeET.setError(null);

        if (nameET.getText().toString().trim().isEmpty()) {
            nameET.setError("The class name can't be empty.");
            result = false;
        } else codeET.setError(null);

        return result;
    }


    @Override
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
        startActivity(new Intent(getApplicationContext(), MyClasses.class));
        finish();
    }
}