package com.example.datingappmine;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
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

public class SwipeCardview extends Fragment {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference swipedUsers = database.getReference("users").child(mAuth.getCurrentUser().getUid()).child("swipedUsers");
    DatabaseReference mutual = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid()).child("mutual");
    DatabaseReference users = database.getReference("users");
    final String profile_fragment_tag = "profile_fragment";
    DatabaseReference notificationRef;
    List<Person> personList = new ArrayList<>();
    RecyclerView swipeRecyclerView;
    Matches matches;
    String messageSenderId, messageReceiverId, TOPIC;
    Person currentUser;
    ImageView noMoreUsersIV;
    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String FIREBASE_APP_KEY = "FIREBASE_APP_KEY";
    final private String serverKey = "key=" + FIREBASE_APP_KEY;
    final private String contentType = "application/json";
    final String TAG = "NOTIFICATION TAG";

    public void onDatasetChanged() {
        if (swipeRecyclerView == null || swipeRecyclerView.getAdapter() == null) {
            return;
        }

        swipeRecyclerView.getAdapter().notifyDataSetChanged();
        if (personList.isEmpty()) {
            swipeRecyclerView.setVisibility(View.INVISIBLE);
            noMoreUsersIV.setVisibility(View.VISIBLE);
        } else {
            noMoreUsersIV.setVisibility(View.INVISIBLE);
            swipeRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    public static SwipeCardview newInstance(ArrayList<Person> allUsers, Matches matches, Person currentUser) {
        SwipeCardview swipeCardview = new SwipeCardview();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("all_users_list", allUsers);
        swipeCardview.setCurrentUser(currentUser);
        swipeCardview.setArguments(bundle);
        swipeCardview.setMatches(matches);
        return swipeCardview;
    }

    public void setMatches(Matches matches) {
        this.matches = matches;
    }

    public void setCurrentUser(Person person) {
        this.currentUser = person;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            personList = getArguments().getParcelableArrayList("all_users_list");
        } else {
            Toast.makeText(getContext(), "null", Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.swipe_recycler, container, false);
        swipeRecyclerView = rootView.findViewById(R.id.swipe_rec);
        noMoreUsersIV = rootView.findViewById(R.id.no_more_users);
        swipeRecyclerView.setHasFixedSize(true);
        swipeRecyclerView.setLayoutManager(new UnscrollableLinearLayoutManager(getActivity()));
        messageSenderId = mAuth.getCurrentUser().getUid();
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        if (personList.isEmpty()) {
            swipeRecyclerView.setVisibility(View.INVISIBLE);
            noMoreUsersIV.setVisibility(View.VISIBLE);
        } else {
            noMoreUsersIV.setVisibility(View.INVISIBLE);
            swipeRecyclerView.setVisibility(View.VISIBLE);
        }

        createAdapter();
        return rootView;
    }

    private void createAdapter() {
        SwipeAdapter swipeAdapter = new SwipeAdapter(personList);
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.RIGHT) {
                    if (currentUser != null && !mAuth.getCurrentUser().isAnonymous()) {
                        Person chosen = personList.get(viewHolder.getAdapterPosition());
                        SwipedUsers swipedUser = new SwipedUsers(personList.get(viewHolder.getAdapterPosition()).getUID(), true);
                        users.child(swipedUser.getUID()).child("swipedUsers").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                SwipedUsers person = snapshot.getValue(SwipedUsers.class);
                                if (person != null) {
                                    if (person.getSwipedRight().equals(true)) {
                                        users.child(swipedUser.getUID()).child("mutual").child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getUid());
                                        mutual.child(swipedUser.getUID()).setValue(swipedUser.getUID());
                                        matches.updateList(chosen);
                                        TOPIC = swipedUser.getUID();
                                        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC);
                                        JSONObject notification = new JSONObject();
                                        JSONObject notificationBody = new JSONObject();

                                        try {
                                            notificationBody.put("title", getString(R.string.new_match));
                                            notificationBody.put("message", getString(R.string.new_match_msg));
                                            notification.put("to", "/topics/" + TOPIC);
                                            notification.put("data", notificationBody);
                                            sendNotification(notification);
                                        } catch (JSONException e) {
                                            Log.e(TAG, "onCreate: " + e.getMessage());
                                        }
                                    }
                                }

                                FirebaseMessaging.getInstance().subscribeToTopic(mAuth.getCurrentUser().getUid());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        messageReceiverId = swipedUser.getUID();
                        swipedUsers.child(personList.get(viewHolder.getAdapterPosition()).getUID()).setValue(swipedUser);
                        personList.remove(viewHolder.getAdapterPosition());
                        swipeAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());

                        HashMap<String, String> chatNotificationMap = new HashMap<>();
                        chatNotificationMap.put("from", messageSenderId);
                        chatNotificationMap.put("type", "been_liked");
                        notificationRef.child(messageReceiverId).push().setValue(chatNotificationMap);

                        if (personList.isEmpty()) {
                            swipeRecyclerView.setVisibility(View.INVISIBLE);
                            noMoreUsersIV.setVisibility(View.VISIBLE);
                        } else {
                            noMoreUsersIV.setVisibility(View.INVISIBLE);
                            swipeRecyclerView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if(mAuth.getCurrentUser().isAnonymous()){
                            Toast.makeText(getContext(), getString(R.string.first_register), Toast.LENGTH_SHORT).show();
                        }
                        else{
                        Toast.makeText(getContext(), getString(R.string.first_fill_profile), Toast.LENGTH_SHORT).show();
                    }
                    }
                } else if (direction == ItemTouchHelper.LEFT) {
                    SwipedUsers swipedUser = new SwipedUsers(personList.get(viewHolder.getAdapterPosition()).getUID(), false);
                    swipedUsers.child(personList.get(viewHolder.getAdapterPosition()).getUID()).setValue(swipedUser);
                    personList.remove(viewHolder.getAdapterPosition());
                    swipeAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());

                    if (personList.isEmpty()) {
                        swipeRecyclerView.setVisibility(View.INVISIBLE);
                        noMoreUsersIV.setVisibility(View.VISIBLE);
                    } else {
                        noMoreUsersIV.setVisibility(View.INVISIBLE);
                        swipeRecyclerView.setVisibility(View.VISIBLE);
                    }
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(swipeRecyclerView);
        swipeAdapter.setListener(new SwipeAdapter.MySwipeListener() {
            @Override
            public void onSwipeClicked(int position, View view) {
                //open the user's profile
                ProfilePage profilePage = ProfilePage.newInstance(swipeAdapter.getPerson(position));
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.add(R.id.root_container, profilePage, profile_fragment_tag);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        swipeRecyclerView.setAdapter(swipeAdapter);
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
}