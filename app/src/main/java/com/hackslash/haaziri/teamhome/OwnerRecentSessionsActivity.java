package com.hackslash.haaziri.teamhome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hackslash.haaziri.R;
import com.hackslash.haaziri.utils.Constants;

public class OwnerRecentSessionsActivity extends AppCompatActivity {

    private TextView teamNameTv;
    private TextView teamCodeTv;
    private String teamName;
    private String teamCode;
    private ImageView backBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_recent_sessions);
        getWindow().setStatusBarColor(getColor(R.color.home_status_bar_color));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        initVars();

        setupListeners();

    }

    private void setupListeners() {
        backBtn.setOnClickListener(v -> finish());
    }

    private void initVars() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        teamNameTv = toolbar.findViewById(R.id.teamNameTv);
        teamCodeTv = findViewById(R.id.teamCodeTv);
        backBtn = toolbar.findViewById(R.id.backBtn);
        Intent incommingIntent = getIntent();
        if (incommingIntent != null) {
            teamName = incommingIntent.getStringExtra(Constants.TEAM_NAME_KEY);
            teamCode = incommingIntent.getStringExtra(Constants.TEAM_CODE_KEY);
        }
        teamNameTv.setText(teamName);
        teamCodeTv.setText("Team code: " + teamCode);
    }
}