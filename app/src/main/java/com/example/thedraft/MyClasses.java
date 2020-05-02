package com.example.thedraft;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.List;

public class MyClasses extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String EXTRA_ID = "com.example.thedraft.EXTRA_ID";
    private FirebaseFirestore db;
    private DrawerLayout drawerLayout;
    private FirebaseAuth firebaseAuth;
    private RecyclerView mFireStoreList;
    private FirestoreRecyclerAdapter adapter;
    private ImageView fab_add;
    private FrameLayout fab_frame;
    private NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myclasses);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);

        fab_add = findViewById(R.id.fab_add);
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        db.collection("users").document(firebaseAuth.getCurrentUser().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                String urole = task.getResult().getString("role");
                if (urole.equals("t")) {
                    fab_add.setVisibility(View.VISIBLE);
                }
            }
        });

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), CreateClass.class));
                finish();
            }
        });

        UserActivity.updateHeader(this);

        mFireStoreList = findViewById(R.id.classesRecycler);

        Query query = db.collection("classes")
                .whereArrayContains("members",
                        db.getInstance().document("/users/" + firebaseAuth.getCurrentUser().getUid()));

        FirestoreRecyclerOptions<ClassModel> options = new FirestoreRecyclerOptions.Builder<ClassModel>()
                .setQuery(query, ClassModel.class).build();

        adapter = new FirestoreRecyclerAdapter<ClassModel, ClassesViewHolder>(options) {
            @NonNull

            @Override
            public ClassesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.class_row, parent, false);
                return new ClassesViewHolder(v);
            }

            @Override
            protected void onBindViewHolder(@NonNull final ClassesViewHolder holder, int position, @NonNull ClassModel model) {
                holder.class_name.setText(model.getName());
                DocumentSnapshot snapshot = getSnapshots().getSnapshot(position);
                holder.setId(snapshot.getId());


                model.getCreator().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) holder.class_creator
                                .setText("posted by " +
                                        document.getString("firstname")
                                        + " " + document.getString("lastname"));
                    }
                });
            }

            ;
        };
        mFireStoreList.setHasFixedSize(false);
        mFireStoreList.setLayoutManager(new LinearLayoutManager(this));
        mFireStoreList.setAdapter(adapter);
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

    private class ClassesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView class_name;
        private TextView class_creator;
        private String id;

        public void setId(String id) {
            this.id = id;
        }

        public ClassesViewHolder(@NonNull View itemView) {
            super(itemView);

            class_name = itemView.findViewById(R.id.className);
            class_creator = itemView.findViewById(R.id.classCreator);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), ClassLessons.class);
            intent.putExtra(EXTRA_ID, id);
            startActivity(intent);

            finish();
        }
    }

    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), UserActivity.class));
        finish();
    }
}