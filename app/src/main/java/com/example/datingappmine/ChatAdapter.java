package com.example.datingappmine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    private List<Message> userMessages;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    public ChatAdapter(List<Message> userMessages) {
        this.userMessages = userMessages;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView senderMsgTxt;
        public TextView receivermsgTxt;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMsgTxt = itemView.findViewById(R.id.sender_msg);
            receivermsgTxt = itemView.findViewById(R.id.receiver_msg);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_cell, parent, false);
        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Message messages = userMessages.get(position);
        String fromUserId = messages.getFrom();
        usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(fromUserId);

        holder.receivermsgTxt.setVisibility(View.INVISIBLE);
        if (fromUserId.equals(messageSenderId)) {
            holder.senderMsgTxt.setBackgroundResource(R.drawable.sender_msg);
            holder.senderMsgTxt.setText(messages.getMessage());
        } else {
            holder.senderMsgTxt.setVisibility(View.INVISIBLE);
            holder.receivermsgTxt.setVisibility(View.VISIBLE);
            holder.receivermsgTxt.setBackgroundResource(R.drawable.reciever_msg);
            holder.receivermsgTxt.setText(messages.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return userMessages.size();
    }
}
