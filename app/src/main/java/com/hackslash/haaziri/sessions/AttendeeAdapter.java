package com.hackslash.haaziri.sessions;

import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.hackslash.haaziri.home.JoinedTeamsAdapter;
import com.hackslash.haaziri.home.TeamClickInterface;
import com.hackslash.haaziri.models.SessionAttendee;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.hackslash.haaziri.R;
import com.hackslash.haaziri.firebase.FirebaseVars;
import com.hackslash.haaziri.models.Team;

import java.util.ArrayList;
import java.util.HashMap;

public class AttendeeAdapter extends RecyclerView.Adapter<AttendeeAdapter.ViewHolder> {
    private static final String TAG="AttendeeAdapter";
    private final Context mContext;

    private ArrayList<SessionAttendee> attendeeIds;


    public AttendeeAdapter(  ArrayList<SessionAttendee> attendeeIds,Context mContext) {
        this.attendeeIds=attendeeIds;
        this.mContext = mContext;

    }

    @NonNull
    @Override
    public AttendeeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View joinView = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendee_card, parent, false);
        return new AttendeeAdapter.ViewHolder(joinView);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendeeAdapter.ViewHolder holder, int position) {
             holder.setData(attendeeIds.get(position));
    }

    @Override
    public int getItemCount( ) {

        return  attendeeIds.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView userImg;
        private ImageView icon;
        private TextView attendeeName;
        private CardView attendeeCardview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userImg = itemView.findViewById(R.id.userimg);
            icon = itemView.findViewById(R.id.ticktv);
            attendeeName = itemView.findViewById(R.id.attendeename);
            attendeeCardview = itemView.findViewById(R.id.attendeecardview);

        }



        private void setData(SessionAttendee value) {

            attendeeName.setText(value.getName());
        }

    }

}
