package com.example.datingappmine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ProfilePageAdapter extends RecyclerView.Adapter<ProfilePageAdapter.ProfilePageViewHolder> {
    private List<String> personImgList;
    private MyProfilePageListener listener;

    @NonNull
    @Override
    public ProfilePageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.picture_cell, parent, false);
        ProfilePageViewHolder profilePageViewHolder = new ProfilePageViewHolder(view);

        return profilePageViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ProfilePageViewHolder holder, int position) {
        String image = personImgList.get(position);

        Glide.with(holder.userImgIV.getContext())
                .load(image).centerCrop()
                .into(holder.userImgIV);
    }

    @Override
    public int getItemCount() {
        return personImgList.size();
    }

    interface MyProfilePageListener {
        void onPictureClicked(int positing, View view);
    }

    public void setListener(MyProfilePageListener listener) {
        this.listener = listener;
    }

    public ProfilePageAdapter(List<String> personImgList) {
        this.personImgList = personImgList;
    }

    public class ProfilePageViewHolder extends RecyclerView.ViewHolder {
        ImageView userImgIV;

        public ProfilePageViewHolder(@NonNull View itemView) {
            super(itemView);
            userImgIV = itemView.findViewById(R.id.user_picture);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onPictureClicked(getAdapterPosition(), v);
                    }
                }
            });
        }
    }
}