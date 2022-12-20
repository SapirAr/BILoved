package com.example.datingappmine;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProfilePage extends Fragment {
    TextView usersNameAgeTV, usersCityTV, usersBioTV;
    RecyclerView imgRecycler;
    ProfilePageAdapter profilePageAdapter;
    List<String> imgList;
    BigPictureFragment bigPictureFragment;

    public static ProfilePage newInstance(Person person) {
        ProfilePage profilePage = new ProfilePage();
        ArrayList<String> imgStringList = new ArrayList<>();
        Bundle bundle = new Bundle();
        bundle.putString("person_name", person.getName());
        bundle.putInt("person_age", person.getAge());
        bundle.putString("person_city", person.getCity());
        bundle.putString("person_bio", person.getDescription());
        bundle.putString("person_gender", person.getGender());

        for (Image img : person.getImageList()) {
            imgStringList.add(img.getImgUrl());
        }

        bundle.putStringArrayList("photos_list", imgStringList);
        profilePage.setArguments(bundle);

        return profilePage;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.profile_page, container, false);
        imgRecycler = rootView.findViewById(R.id.picture_recycler);
        imgRecycler.setHasFixedSize(true);
        imgRecycler.setLayoutManager(new LinearLayoutManager(this.getActivity(), LinearLayoutManager.HORIZONTAL, false));
        imgList = getArguments().getStringArrayList("photos_list");
        profilePageAdapter = new ProfilePageAdapter(imgList);
        usersNameAgeTV = rootView.findViewById(R.id.user_name_age);
        usersNameAgeTV.setText(getArguments().getString("person_name") + ", " + getArguments().getInt("person_age"));
        usersCityTV = rootView.findViewById(R.id.user_city);
        usersCityTV.setText(getArguments().getString("person_city"));
        usersBioTV = rootView.findViewById(R.id.user_bio);
        usersBioTV.setText(getArguments().getString("person_bio"));

        profilePageAdapter.setListener(new ProfilePageAdapter.MyProfilePageListener() {
            @Override
            public void onPictureClicked(int position, View view) {
                String img = imgList.get(position);
                bigPictureFragment = BigPictureFragment.newInstance(img);
                bigPictureFragment.show(getParentFragmentManager(), "image_dialog_fragment");
            }
        });

        imgRecycler.setAdapter(profilePageAdapter);
        return rootView;
    }
}