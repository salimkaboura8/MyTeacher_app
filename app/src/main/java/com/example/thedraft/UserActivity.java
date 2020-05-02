package com.example.thedraft;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseFirestore db;
    FirebaseAuth fAuth;
    DrawerLayout drawerLayout;
    ArrayList<LessonModel> arrList;
    FloatingSearchView mSearchView;
    NavigationView navigationView;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        db = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        arrList = new ArrayList<LessonModel>();
        listView = findViewById(R.id.lessonsListView);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);

        updateHeader(this);

        //Rani 7assel fe ListView te3 My Lessons

        FirebaseUser firebaseUser = fAuth.getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            db.collection("users").document(uid).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    List<DocumentReference> list = (List<DocumentReference>) document.get("lessons");
                                    List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                                    if (list!=null){
                                    for (DocumentReference documentReference : list) {
                                        Task<DocumentSnapshot> documentSnapshotTask = documentReference.get();
                                        tasks.add(documentSnapshotTask);
                                    }
                                    Tasks.whenAllSuccess(tasks).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
                                        @Override
                                        public void onSuccess(List<Object> list) {
                                            //Do what you need to do with your list
                                            for (Object object : list) {
                                                LessonModel lesson = ((DocumentSnapshot) object).toObject(LessonModel.class);
                                                arrList.add(lesson);
                                            }
                                        }
                                    });
                                }}
                            }
                        }
                    });
        }

        DocumentReference dr = db.document("users/"+fAuth.getCurrentUser().getUid());
        LessonModel newLesson = new LessonModel("cours img", "google.com");

        AdapterLesson adapter = new AdapterLesson(UserActivity.this, R.layout.lesson_row, arrList);
        listView.setAdapter(adapter);

    }

    public static void updateHeader(final Activity activity) {

        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Task<DocumentSnapshot> task = db.collection("users")
                .document(fAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String fullname = document.getString("firstname") + " " +
                                        document.getString("lastname");
                                TextView fullnameTV = activity.findViewById(R.id.username);
                                TextView roleTV = activity.findViewById(R.id.role);
                                fullnameTV.setText(fullname);
                                String role = document.getString("role");
                                if (role.equals("t")) roleTV.setText("Teacher");
                                else roleTV.setText("Student");
                            } else {
                            }
                        } else {
                        }
                    }
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
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}

