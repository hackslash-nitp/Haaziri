package com.hackslash.haaziri.teamhome;

import android.content.Context;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.hackslash.haaziri.R;
import com.hackslash.haaziri.firebase.FirebaseVars;
import com.hackslash.haaziri.models.SessionAttendee;

import java.util.ArrayList;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder>
{
    private static final String TAG = "MemberAdapter";
    private final Context mContext;

    private ArrayList<String> joinMemberIds;

    public MemberAdapter(Context mContext, ArrayList<String>joinMemberIds) {
        this.mContext = mContext;
        this.joinMemberIds = joinMemberIds;
    }

    @NonNull
    @Override
    public MemberAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View joinMember = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendee_card, parent, false);
       return new ViewHolder(joinMember);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberAdapter.ViewHolder holder, int position) {
        holder.fetchTeamData(joinMemberIds.get(position));
    }

    @Override
    public int getItemCount() {
        return joinMemberIds.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView memberName;
        private CardView memberCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            memberName = itemView.findViewById(R.id.memberName);
            memberCard = itemView.findViewById(R.id.memberCardView);
        }

        public void fetchTeamData(String s) {
            FirebaseVars.mRootRef.child("members").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists())
                        setData(snapshot.getValue(SessionAttendee.class));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, error.getMessage());
                }
            });
        }

        private void setData(SessionAttendee value) {
            memberName.setText(value.getName());
        }
    }
}
