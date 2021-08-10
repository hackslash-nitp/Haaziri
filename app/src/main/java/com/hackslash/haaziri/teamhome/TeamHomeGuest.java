package com.hackslash.haaziri.teamhome;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.hackslash.haaziri.R;
import com.hackslash.haaziri.activitydialog.ActivityDialog;
import com.hackslash.haaziri.firebase.FirebaseVars;
import com.hackslash.haaziri.models.SessionAttendee;
import com.hackslash.haaziri.models.Team;
import com.hackslash.haaziri.models.UserProfile;
import com.hackslash.haaziri.utils.Constants;
import com.hackslash.haaziri.utils.MotionToastUtitls;

import java.util.ArrayList;
import java.util.Arrays;

public class TeamHomeGuest extends AppCompatActivity {

    private static final String TAG = "TeamHomeGuest";
    private final int ACCESS_COARSE_LOCATION_REQUEST_CODE = 101;
    private final int ACCESS_FINE_LOCATION_REQUEST_CODE = 102;

    private Button giveHaaziriBtn;
    private String teamCode = "";
    private ActivityDialog dialog;
    private Context mContext = this;
    private String currentSessionId = "";
    private Team currentTeam;
    private ArrayList<String> nearbyDevices;
    private BluetoothAdapter adapter;
    private boolean found = false;
    private UserProfile currentUserProfile;
    private FirebaseUser currentUser;
    private TextView teamNameTv;
    private TextView teamCodeTv;
    private ImageView backBtn;
    /**
     * This is a broadcast receiver that handles new found devices
     * and adds its name to found devices list, from where we would
     * check whether the host device is present or not
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                searchForHost(foundDevice.getName());
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (!found) {
                    dialog.hideDialog();
                    MotionToastUtitls.showWarningDialog(mContext, "Unable to make haaziri", "Make sure the host is near you");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_home_guest);

        //making status bar white with black icons
        getWindow().setStatusBarColor(getColor(R.color.home_status_bar_color));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        initVars();

        setupListeners();

        fetchUserDetails();

        fetchTeamDetails();
    }

    private void fetchTeamDetails() {
        dialog.setTitle("Fetching team details");
        dialog.setMessage("Please wait while we fetch the team details");
        dialog.showDialog();
        FirebaseVars.mRootRef.child("/teams/" + teamCode + "/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dialog.hideDialog();
                currentSessionId = snapshot.child("currentSessionId").getValue(String.class);
                currentTeam = snapshot.getValue(Team.class);
                teamNameTv.setText(currentTeam.getTeamName());
                teamCodeTv.setText("Team code: " + currentTeam.getTeamCode());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.hideDialog();
                MotionToastUtitls.showErrorToast(mContext, "Error", "Some error occurred in fetching current session details");
                Log.d(TAG, "onCancelled: " + error.getDetails());
            }
        });
    }

    private void fetchUserDetails() {
        FirebaseVars.mRootRef.child("/users/" + currentUser.getUid() + "/profile/").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUserProfile = snapshot.getValue(UserProfile.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                MotionToastUtitls.showErrorToast(mContext, "Error", "Error fetching user details");
                Log.d(TAG, "onCancelled: " + error.getMessage());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACCESS_COARSE_LOCATION_REQUEST_CODE || requestCode == ACCESS_FINE_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissions();
            } else {
                MotionToastUtitls.showErrorToast(mContext, "Error", "These permissions are required for making haaziri");
            }
        }
    }

    private void setupListeners() {
        giveHaaziriBtn.setOnClickListener(v -> {
            if (currentSessionId == null || currentSessionId.isEmpty()) {
                MotionToastUtitls.showInfoToast(mContext, "No session going on", "Currently no session is going on");
            } else {
                checkPermissions();
            }
        });
        backBtn.setOnClickListener(v -> finish());
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(TeamHomeGuest.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_COARSE_LOCATION_REQUEST_CODE);
        else if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(TeamHomeGuest.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_REQUEST_CODE);
        else
            proceedToHaazari();
    }

    private void proceedToHaazari() {
        dialog.setTitle("Making your Haaziri");
        dialog.setMessage("Please wait while we make your Haaziri for this session");
        dialog.showDialog();
        nearbyDevices = new ArrayList<>();
        adapter.enable();
        adapter.startDiscovery();
    }

    private void initVars() {
        Intent intent = getIntent();
        teamCode = intent.getStringExtra(Constants.TEAM_CODE_KEY);
        giveHaaziriBtn = findViewById(R.id.giveHaaziriBtn);
        dialog = new ActivityDialog(mContext);
        dialog.setCancelable(false);
        Toolbar toolbar = findViewById(R.id.toolbar);
        teamNameTv = toolbar.findViewById(R.id.teamNameTv);
        teamCodeTv = findViewById(R.id.teamCodeTv);
        backBtn = toolbar.findViewById(R.id.backBtn);

        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        adapter = manager.getAdapter();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void searchForHost(String name) {
        String[] temp = name.split(" ");
        ArrayList<String> nameSplit = new ArrayList<>(Arrays.asList(temp));
        if (nameSplit.contains(currentSessionId) && !found) {
            found = true;

            //save the haaziri in database
            SessionAttendee attendee = new SessionAttendee(currentUserProfile.getName(), currentUserProfile.getEmail(), currentUser.getUid());
            FirebaseVars.mRootRef.child("/teams/" + teamCode + "/sessions/" + currentSessionId + "/attendees/").push().setValue(attendee).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        dialog.hideDialog();
                        MotionToastUtitls.showSuccessToast(mContext, "Wohoo", "Your Haaziri just got registered");
                    } else {
                        dialog.hideDialog();
                        MotionToastUtitls.showErrorToast(mContext, "Error", "Some error occured in registering attendence");
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null)
            unregisterReceiver(receiver);
    }
}