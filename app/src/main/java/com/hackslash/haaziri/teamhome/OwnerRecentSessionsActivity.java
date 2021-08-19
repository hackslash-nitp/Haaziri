package com.hackslash.haaziri.teamhome;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.hackslash.haaziri.R;
import com.hackslash.haaziri.activitydialog.ActivityDialog;
import com.hackslash.haaziri.firebase.FirebaseVars;
import com.hackslash.haaziri.home.JoinedTeamsAdapter;
import com.hackslash.haaziri.home.OwnedTeamsAdapter;
import com.hackslash.haaziri.home.TeamClickInterface;
import com.hackslash.haaziri.models.Session;
import com.hackslash.haaziri.models.UserProfile;
import com.hackslash.haaziri.utils.Constants;

import java.util.ArrayList;

public class OwnerRecentSessionsActivity extends AppCompatActivity {

    private static final String TAG="OwnerRecentSessionActivity";

    private TextView teamNameTv;
    private TextView teamCodeTv;
    private String teamName;
    private String teamCode;
    private ImageView backBtn;
    private ArrayList<Session> recentSessions;
    private RecyclerView recentSessionRecycler;
    private RecentSessionsAdapter recentSessionsAdapter;
    private Context mContext = this;
    private ActivityDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_recent_sessions);
        getWindow().setStatusBarColor(getColor(R.color.home_status_bar_color));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        initVars();

        setupListeners();
        setupRecyclerViews();
        fetchData();

    }

    private void setupListeners() {
        backBtn.setOnClickListener(v -> finish());
    }

    private void initVars() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        teamNameTv = toolbar.findViewById(R.id.teamNameTv);
        teamCodeTv = findViewById(R.id.teamCodeTv);
        backBtn = toolbar.findViewById(R.id.backBtn);
        recentSessionRecycler=findViewById(R.id.recentSessionsRecycler);
        Intent incommingIntent = getIntent();
        if (incommingIntent != null) {
            teamName = incommingIntent.getStringExtra(Constants.TEAM_NAME_KEY);
            teamCode = incommingIntent.getStringExtra(Constants.TEAM_CODE_KEY);
        }
        teamNameTv.setText(teamName);
        teamCodeTv.setText("Team code: " + teamCode);
    }

    private void fetchData() {
        if (FirebaseVars.mRootRef == null) return;
        dialog.setTitle("Fetching Details");
        dialog.setMessage("Please wait while we fetch your details");
        dialog.showDialog();

        FirebaseVars.mRootRef.child(teamCodeTv+"/sessions/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //user data fetched successfully
                dialog.hideDialog();

                if (snapshot.exists()) {

                    recentSessions.clear();
                    for (DataSnapshot snapshot2 : snapshot.getChildren())
                        recentSessions.add(snapshot2.getValue(Session.class));
                        recentSessionsAdapter.notifyDataSetChanged();



                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.hideDialog();
                Toast.makeText(mContext, "Error occurred while fetching details", Toast.LENGTH_SHORT).show();
                Log.d(TAG, error.getMessage());
            }
            });
    }
    private void setupRecyclerViews() {

        recentSessions = new ArrayList<>();
        recentSessionsAdapter = new RecentSessionsAdapter(recentSessions, mContext);

        recentSessionRecycler.setLayoutManager(new LinearLayoutManager(mContext));
        recentSessionRecycler.setAdapter(recentSessionsAdapter);



    }

}
