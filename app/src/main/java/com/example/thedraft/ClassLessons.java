package com.example.thedraft;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
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
import com.github.barteksc.pdfviewer.PDFView;
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

import org.w3c.dom.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ClassLessons extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseFirestore db;
    private DrawerLayout drawerLayout;
    private FirebaseAuth firebaseAuth;
    private RecyclerView mFireStoreList;
    private ImageView fab_add;
    private FirestoreRecyclerAdapter adapter;
    private NavigationView navigationView;
    public static final String EXTRA_ID = "com.example.thedraft.EXTRA_ID";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classlessons);

        fab_add = findViewById(R.id.fab_add2);

        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        final String id = getIntent().getStringExtra(MyClasses.EXTRA_ID);
        db.collection("classes").document(id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentReference classCreator = (DocumentReference) task.getResult().get("creator");
                        if (db.collection("users").document(firebaseAuth.getCurrentUser().getUid()).equals(classCreator)){
                           fab_add.setVisibility(View.VISIBLE);
                        }
                    }
                });

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddLesson.class);
                intent.putExtra(EXTRA_ID, id);
                startActivity(intent);
                finish();
            }
        });

        navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);

        UserActivity.updateHeader(this);

        mFireStoreList = findViewById(R.id.classLessonsRecycler);
        Query query = db.collection("classes").document(id).collection("lessons");

        FirestoreRecyclerOptions<LessonModel> options = new FirestoreRecyclerOptions.Builder<LessonModel>()
                .setQuery(query, LessonModel.class).build();

        adapter = new FirestoreRecyclerAdapter<LessonModel, ClassLessonsViewHolder>(options) {
            @NonNull

            @Override
            public ClassLessonsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.class_row2, parent, false);
                return new ClassLessonsViewHolder(v);
            }

            @Override
            protected void onBindViewHolder(@NonNull final ClassLessonsViewHolder holder,
                                            int position, @NonNull LessonModel model) {
                holder.class_name.setText(model.getName());
                holder.setUrl(model.getUrl());

                db.collection("classes").document(id)
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        DocumentReference documentReference = (DocumentReference) documentSnapshot.get("creator");
                        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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

    private class ClassLessonsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView class_name;
        private TextView class_creator;
        private PDFView pdfView = (PDFView) findViewById(R.id.pdfView);
        private String url;

        public void setUrl(String url) {
            this.url = url;
        }

        public ClassLessonsViewHolder(@NonNull View itemView) {
            super(itemView);

            class_name = itemView.findViewById(R.id.className);
            class_creator = itemView.findViewById(R.id.classCreator);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setDataAndType(Uri.parse(url), "application/pdf");
            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            Intent intent = Intent.createChooser(target, "Open File");
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                // Instruct the user to install a PDF reader here, or something
                Toast.makeText(ClassLessons.this, "Unable to open PDF. Please, install a PDF reader app.", Toast.LENGTH_SHORT);
            }
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
        startActivity(new Intent(getApplicationContext(), MyClasses.class));
        finish();
    }
}