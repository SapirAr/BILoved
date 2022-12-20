package com.example.datingappmine;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SecondActivity extends AppCompatActivity {
    TabFragmentAdapter viewPagerAdapter;
    ViewPager viewPager;
    TabLayout tabLayout;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference users = database.getReference("users");
    DatabaseReference swipedUsers = database.getReference("users").child(mAuth.getCurrentUser().getUid()).child("swipedUsers");
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid()).child("photos");
    DatabaseReference mutual = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid()).child("mutual");
    List<Person> personList = new ArrayList<>();
    List<SwipedUsers> swipedUsersList = new ArrayList<>();
    List<Image> currentUserPicturesList = new ArrayList<>();
    Person currentUser;
    List<String> mutualUidList = new ArrayList<>();
    List<Person> mutualList = new ArrayList<>();
    SwipeCardview swipeCardview;
    EditProfile editProfile;
    EditPictures editPictures;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Matches matches = Matches.newInstance((ArrayList<Person>) mutualList);
        FirebaseMessaging.getInstance().subscribeToTopic(mAuth.getCurrentUser().getUid());
        swipeCardview = SwipeCardview.newInstance((ArrayList<Person>) personList, matches, currentUser);
        editPictures = new EditPictures();
        editProfile = new EditProfile();

        ValueEventListener addAllSwipedUsersToList = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    SwipedUsers swipedUser = ds.getValue(SwipedUsers.class);
                    swipedUsersList.add(swipedUser);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        swipedUsers.addListenerForSingleValueEvent(addAllSwipedUsersToList);

        mutual.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot mutual : snapshot.getChildren()) {
                    String uid = mutual.getValue(String.class);
                    mutualUidList.add(uid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        ValueEventListener addAllUsersToList = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Person person = ds.child("details").getValue(Person.class);

                    if (person == null) {
                        continue;
                    }
                    for (DataSnapshot meow : ds.child("photos").getChildren()) {
                        Image userImg = meow.getValue(Image.class);
                        person.getImageList().add(userImg);
                    }

                    for (String Uid : mutualUidList) {
                        if (Uid.equals(person.getUID())) {
                            mutualList.add(person);
                        }
                    }

                    boolean isSwiped = false;

                    for (SwipedUsers user : swipedUsersList) {
                        String personAlreadySwiped = user.getUID();
                        if (personAlreadySwiped.equals(person.getUID())) {
                            isSwiped = true;
                            break;
                        }
                    }
                    if (!isSwiped) {
                        if (person.getUID() != null) {
                            if (!(person.getUID().equals(mAuth.getCurrentUser().getUid()))) {
                                personList.add(person);
                            } else {
                                currentUser = person;
                                swipeCardview.setCurrentUser(currentUser);
                            }
                        } else {
                            Toast.makeText(SecondActivity.this, getString(R.string.no_more_users), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                swipeCardview.onDatasetChanged();
                editProfile.showProfilePic();
                matches.onDatasetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        users.addListenerForSingleValueEvent(addAllUsersToList);

        setContentView(R.layout.second_activity);

        viewPager = findViewById(R.id.pager);
        viewPagerAdapter = new TabFragmentAdapter(getSupportFragmentManager());
        viewPagerAdapter.add(swipeCardview, getString(R.string.Swipe));
        viewPagerAdapter.add(editProfile, getString(R.string.Edit_profile));
        viewPagerAdapter.add(matches, getString(R.string.Matches));
        viewPager.setAdapter(viewPagerAdapter);

        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUserPicturesList.clear();

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Image image = postSnapshot.getValue(Image.class);
                    image.setKey(postSnapshot.getKey());
                    currentUserPicturesList.add(image);
                }

                if (currentUserPicturesList.isEmpty()) {
                    Uri uri = Uri.parse("android.resource://com.example.datingappmine/drawable/upload_person_img");
                    Image img = new Image(uri.toString());
                    currentUserPicturesList.add(img);
                }

                editPictures.onDatasetChanged();
                editProfile.showProfilePic();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public List<Image> getCurrentUserPicturesList() {
        return currentUserPicturesList;
    }
}