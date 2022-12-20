package com.example.datingappmine;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.PicturEditViewHolder> {
    private List<Image> pictures;
    private MyPicturesListener listener;
    private Context context;

    public class PicturEditViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;

        public PicturEditViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.photo_edit_page);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (listener != null) {
                        listener.onPictureLongClicked(getAdapterPosition(), v);
                    }
                    return false;
                }
            });

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

    interface MyPicturesListener {
        void onPictureLongClicked(int position, View view);
        void onPictureClicked(int position, View view);
    }

    public void setListener(MyPicturesListener listener) {
        this.listener = listener;
    }

    public PictureAdapter(Context context, List<Image> pictures) {
        this.context = context;
        this.pictures = pictures;
    }

    @NonNull
    @Override
    public PicturEditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.edit_picture_cell, parent, false);
        PicturEditViewHolder picturEditViewHolder = new PicturEditViewHolder(view);

        return picturEditViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PicturEditViewHolder holder, int position) {
        Image picture = pictures.get(position);
        Glide.with(context)
                .load(picture.getImgUrl())
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return pictures.size();
    }
}