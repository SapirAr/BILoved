package com.example.datingappmine;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chat extends Fragment {

    private Button sendMsgBtn;
    private EditText msgInputTxt;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef, notificationRef;
    private TextView matchName;
    private final List<Message> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private ChatAdapter chatAdapter;
    private RecyclerView recyclerView;
    private String messageReceiverId, matchPicString, messageSenderId;
    private ImageView matchPicIV;
    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String FIREBASE_ACCESS_KEY = "FIREBASE_ACCESS_KEY";
    final private String serverKey = "key=" + FIREBASE_ACCESS_KEY;
    final private String contentType = "application/json";
    final String TAG = "NOTIFICATION TAG";
    Boolean exist=true;
    String NOTIFICATION_TITLE, NOTIFICATION_MESSAGE, TOPIC;

    public static Chat newInstance(Person matchClickedOn) {
        Chat chat = new Chat();
        Bundle bundle = new Bundle();
        bundle.putSerializable("f", matchClickedOn);
        chat.setArguments(bundle);
        return chat;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.chat, container, false);
        SecondActivity secondActivity = (SecondActivity) getActivity();
        Person person = secondActivity.currentUser;
        msgInputTxt = rootView.findViewById(R.id.message);
        sendMsgBtn = rootView.findViewById(R.id.send);
        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        matchName = rootView.findViewById(R.id.match_name);
        matchPicIV = rootView.findViewById(R.id.match_pic);
        Person match = (Person) getArguments().getSerializable("f");

        DatabaseReference mutual = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid()).child("mutual");
        ValueEventListener userExist= new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot mutual : snapshot.getChildren()) {
                    String uid = mutual.getValue(String.class);
                    if(uid.equals(messageReceiverId)){
                        exist=true;
                    }
                    else{
                        exist=false;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        messageReceiverId = match.getUID();
        if (match.getImageList().size() > 0) {
            matchPicString = match.getImageList().get(0).getImgUrl();
            Glide.with(getContext())
                    .load(matchPicString)
                    .into(matchPicIV);
        }

        matchName.setText(match.getName());
        matchName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfilePage profilePage = ProfilePage.newInstance(match);
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.add(R.id.root_container4, profilePage, "profile_fragment_tag");

                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mutual.addListenerForSingleValueEvent(userExist);

                if(!exist){
                    Toast.makeText(secondActivity, getString(R.string.been_unmatched), Toast.LENGTH_SHORT).show();
                }
                else{
                    String messageText = msgInputTxt.getText().toString();
                    TOPIC = messageReceiverId;
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(messageReceiverId);
                    FirebaseMessaging.getInstance().subscribeToTopic(mAuth.getCurrentUser().getUid());
                    NOTIFICATION_TITLE = person.getName()+ " " + getString(R.string.msg_from);
                    NOTIFICATION_MESSAGE = messageText;
                    if (TextUtils.isEmpty(messageText)) {
                        Toast.makeText(getContext(), getString(R.string.Write_msg), Toast.LENGTH_SHORT).show();
                    } else {
                        DatabaseReference userMsgKeyRef = rootRef.child("Messages").child(messageSenderId).child(messageReceiverId).push();

                        Map messageTextBody = new HashMap();
                        messageTextBody.put("message", messageText);
                        messageTextBody.put("from", messageSenderId);
                        Map messageBodyDetails = new HashMap();
                        messageBodyDetails.put("Messages/" + messageSenderId + "/" + messageReceiverId + "/" + userMsgKeyRef.getKey(), messageTextBody);
                        messageBodyDetails.put("Messages/" + messageReceiverId + "/" + messageSenderId + "/" + userMsgKeyRef.getKey(), messageTextBody);

                        rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), getString(R.string.msg_sent), Toast.LENGTH_SHORT).show();

                                    HashMap<String, String> chatNotificationMap = new HashMap<>();
                                    chatNotificationMap.put("from", messageSenderId);
                                    chatNotificationMap.put("type", "received_msg");
                                    notificationRef.child(messageReceiverId).push().setValue(chatNotificationMap);

                                    JSONObject notification = new JSONObject();
                                    JSONObject notificationBody = new JSONObject();
                                    try {
                                        notificationBody.put("title", NOTIFICATION_TITLE);
                                        notificationBody.put("message", NOTIFICATION_MESSAGE);
                                        notification.put("to", "/topics/" + TOPIC);
                                        notification.put("data", notificationBody);
                                        sendNotification(notification);
                                    } catch (JSONException e) {
                                        Log.e(TAG, "onCreate: " + e.getMessage());
                                    }
                                } else {
                                    Toast.makeText(getContext(), getString(R.string.Error_sending), Toast.LENGTH_SHORT).show();
                                }
                                msgInputTxt.setText("");
                            }
                        });
                    }
                }
            }
        });

        chatAdapter = new ChatAdapter(messageList);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(chatAdapter);

        return rootView;
    }

    private void sendNotification(JSONObject notification) {
        RequestQueue queue = Volley.newRequestQueue(this.getContext());
        StringRequest request = new StringRequest(Request.Method.POST, FCM_API, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", contentType);
                headers.put("Authorization", serverKey);
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return notification.toString().getBytes();
            }
        };

        queue.add(request);
        queue.start();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        rootRef.child("Messages").child(messageSenderId).child(messageReceiverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message messages = snapshot.getValue(Message.class);
                messageList.add(messages);
                chatAdapter.notifyDataSetChanged();

                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}