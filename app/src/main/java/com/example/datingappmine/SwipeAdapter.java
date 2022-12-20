package com.example.datingappmine;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class SwipeAdapter extends RecyclerView.Adapter<SwipeAdapter.SwipeCardviewHolder> {
    private List<Person> personList;
    private MySwipeListener listener;

    interface MySwipeListener {
        void onSwipeClicked(int position, View view);
    }

    public void setListener(MySwipeListener listener) {
        this.listener = listener;
    }

    public SwipeAdapter(List<Person> personList) {
        this.personList = personList;
    }

    @NonNull
    @Override
    public SwipeCardviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.swipe_cardview, parent, false);
        SwipeCardviewHolder swipeCardviewHolder = new SwipeCardviewHolder(view);

        return swipeCardviewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SwipeCardviewHolder holder, int position) {
        Person person = personList.get(position);
        holder.userNameAgeTv.setText(person.getName() + ", " + person.getAge());

        if (person.getImageList().size() > 0) {
            Glide.with(holder.userCardIv.getContext())
                    .load(person.getImageList().get(0).getImgUrl()).centerCrop()
                    .into(holder.userCardIv);
        }
    }

    @Override
    public int getItemCount() {
        return personList.size();
    }

    public Person getPerson(int position) {
        return personList.get(position);
    }

    public class SwipeCardviewHolder extends RecyclerView.ViewHolder {
        ImageView userCardIv;
        TextView userNameAgeTv;

        public SwipeCardviewHolder(@NonNull View itemView) {
            super(itemView);
            userCardIv = itemView.findViewById(R.id.swipe_image);
            userNameAgeTv = itemView.findViewById(R.id.swipe_tv);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onSwipeClicked(getAdapterPosition(), v);
                    }
                }
            });
        }
    }
}