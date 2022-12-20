package com.example.datingappmine;

import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class MatchesAdapter extends RecyclerView.Adapter<MatchesAdapter.MatchesViewHolder> {

    List<Person> matchesList;
    private MyMatchListener listener;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference swipedUsers = database.getReference("users").child(mAuth.getCurrentUser().getUid()).child("swipedUsers");
    DatabaseReference mutual = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid()).child("mutual");
    DatabaseReference users = FirebaseDatabase.getInstance().getReference("users");

    interface MyMatchListener {
        void onMatchClicked(int position, View view);
    }

    public void setListener(MyMatchListener listener) {
        this.listener = listener;
    }

    public MatchesAdapter(List<Person> matchesList) {
        this.matchesList = matchesList;
    }

    @NonNull
    @Override
    public MatchesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.match_cell, parent, false);
        MatchesViewHolder matchesViewHolder = new MatchesViewHolder(view);
        return matchesViewHolder;
    }

    public class MatchesViewHolder extends RecyclerView.ViewHolder {
        ImageView matchImg;
        TextView matchName;
        ImageButton unmatch;

        public MatchesViewHolder(@NonNull View itemView) {
            super(itemView);
            matchImg = itemView.findViewById(R.id.match_img);
            matchName = itemView.findViewById(R.id.match_name);
            unmatch = itemView.findViewById(R.id.unmatch);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onMatchClicked(getAdapterPosition(), v);
                    }
                }
            });
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MatchesViewHolder holder, int position) {
        Person matchUser = matchesList.get(position);
        holder.matchName.setText(matchUser.getName());

        if (matchUser.getImageList().size() > 0) {
            Glide.with(holder.matchImg.getContext())
                    .load(matchUser.getImageList().get(0).getImgUrl()).centerCrop()
                    .into(holder.matchImg);
        }

        holder.unmatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(holder.matchImg.getContext(), v, holder.matchImg.getContext().getString(R.string.unmatch), BaseTransientBottomBar.LENGTH_LONG).setAction("Yes", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        matchesList.remove(matchUser);
                        SwipedUsers swipedUser = new SwipedUsers(matchUser.getUID(), false);
                        swipedUsers.child(matchUser.getUID()).setValue(swipedUser);
                        mutual.child(matchUser.getUID()).removeValue();
                        users.child(matchUser.getUID()).child("mutual").child(mAuth.getCurrentUser().getUid()).removeValue();
                        notifyDataSetChanged();
                    }
                }).show();
            }
        });
    }

    public Person getPerson(int position) {
        return matchesList.get(position);
    }

    @Override
    public int getItemCount() {
        return matchesList.size();
    }
}