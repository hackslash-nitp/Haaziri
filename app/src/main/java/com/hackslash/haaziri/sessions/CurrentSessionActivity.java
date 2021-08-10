package com.hackslash.haaziri.sessions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.hackslash.haaziri.R;
import com.hackslash.haaziri.activitydialog.ActivityDialog;
import com.hackslash.haaziri.firebase.FirebaseVars;
import com.hackslash.haaziri.intro.PrefManager;
import com.hackslash.haaziri.models.SessionAttendee;
import com.hackslash.haaziri.utils.Constants;
import com.hackslash.haaziri.utils.MotionToastUtitls;

import java.util.ArrayList;

public class CurrentSessionActivity extends AppCompatActivity {

    private static final String TAG="CurrentSessionActivity";

    private Toolbar toolbar;
    private Button stopBtn;
    private TextView sessionIdTv;
    private RecyclerView attendenceRecycler;
    private ImageView backBtn;
    private String teamCode = "";
    private String teamName = "";
    private String sessionId = "";
    private ArrayList<String> AttendeesIds;

    private ActivityDialog dialog;
    private Context mContext = this;
    private AttendeeAdapter attendeeAdapter;
    private ImageView userImg;
    private ImageView icon;
    private TextView attendeeName;
    private CardView attendeeCardview;
    private ArrayList<SessionAttendee> attendeeIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_session);

        //making status bar white with black icons
        getWindow().setStatusBarColor(getColor(R.color.home_status_bar_color));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        //getting incoming intent
        Intent intent = getIntent();
        teamCode = intent.getStringExtra(Constants.TEAM_CODE_KEY);
        teamName = intent.getStringExtra(Constants.TEAM_NAME_KEY);
        sessionId = intent.getStringExtra(Constants.SESSION_CODE_KEY);

        toolbar = findViewById(R.id.current_toolbar);
        //setting toolbar title to selected team title
        TextView titleTv = toolbar.findViewById(R.id.titleTv);
        titleTv.setText(teamName);
        setSupportActionBar(toolbar);

        initVars();

        setupListeners();
        setupRecyclerViews();
        fetchAttendeeData();
    }

    private void setupListeners() {
        backBtn.setOnClickListener(v -> closeSession());
        stopBtn.setOnClickListener(v -> closeSession());
    }

    private void closeSession() {
        AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                .setTitle("Close session?")
                .setMessage("Are you sure to close the sesssion")
                .setPositiveButton("Yes", (dialog, which) -> {
                    proceedToClose();
                })
                .setNegativeButton("No", (dialog, which) -> {

                })
                .setCancelable(false)
                .create();
        alertDialog.show();

    }

    private void proceedToClose() {
        dialog.setTitle("Stopping session");
        dialog.setMessage("Please wait while we wrap up your session and save the details");
        dialog.showDialog();
        //turning off bluetooth
        final BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = manager.getAdapter();
        PrefManager prefManager = new PrefManager(mContext);
        Handler nameChangeHandler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                bluetoothAdapter.setName(prefManager.getOriginalBluetoothName());
                if (bluetoothAdapter.getName().equals(prefManager.getOriginalBluetoothName())) {
                    prefManager.close();
                } else {
                    nameChangeHandler.postDelayed(this, 100);
                }
            }
        };
        nameChangeHandler.postDelayed(runnable, 100);
        bluetoothAdapter.disable();
        //stopping session from backend by removing id
        FirebaseVars.mRootRef.child("/teams/" + teamCode + "/currentSessionId/").setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialog.hideDialog();
                finish();
            }
        });
    }

    private void initVars() {
        stopBtn = findViewById(R.id.stopbtn);
        sessionIdTv = findViewById(R.id.sesstxtv);
        sessionIdTv.setText("Session Id: " + sessionId);
        attendenceRecycler = findViewById(R.id.attendance_recyclerview);
        backBtn = toolbar.findViewById(R.id.backBtn);
        dialog = new ActivityDialog(mContext);
        dialog.setCancelable(false);
        userImg = findViewById(R.id.userimg);
        icon = findViewById(R.id.ticktv);
        attendeeName = findViewById(R.id.attendeename);
        attendeeCardview = findViewById(R.id.attendeecardview);

    }
    public void fetchAttendeeData() {

        FirebaseVars.mRootRef.child("/teams/"+teamCode+ "/sessions/"+sessionId+"/attendees/" ).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //as we get the team object from firebase we populate the details in the UI,
                if (snapshot.exists()) {
                    attendeeIds.clear();
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        SessionAttendee attendee = snapshot1.getValue(SessionAttendee.class);
                        MotionToastUtitls.showSuccessToast(mContext, "Present", attendee.getName() + " Present");
                        attendeeIds.add(attendee);
                    }
                    //TODO: This is disabled temporarily because of crash
//                    attendeeAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        });
    }

    private void setupRecyclerViews() {
        attendeeIds = new ArrayList<>();

        attendeeAdapter = new AttendeeAdapter(attendeeIds, mContext);

        attendenceRecycler.setLayoutManager(new LinearLayoutManager(mContext));
        attendenceRecycler.setAdapter(attendeeAdapter);

    }


}