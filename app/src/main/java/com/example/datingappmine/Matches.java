package com.example.datingappmine;

import android.app.Activity;
import android.content.PeriodicSync;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Matches extends Fragment {
    List<Person> swipedRightList = new ArrayList<>();
    MatchesAdapter matchesAdapter;

    public static Matches newInstance(ArrayList<Person> swipedRightList) {
        Matches matches = new Matches();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("swiped_right_list", swipedRightList); // mutual!
        matches.setArguments(bundle);
        return matches;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            swipedRightList = getArguments().getParcelableArrayList("swiped_right_list");
        } else {
            Toast.makeText(getContext(), "fail", Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.matches_list, container, false);

        RecyclerView matchesRecycler = rootView.findViewById(R.id.matches_recycler);
        matchesRecycler.setHasFixedSize(true);
        matchesRecycler.setLayoutManager(new LinearLayoutManager(this.getActivity()));

        matchesAdapter = new MatchesAdapter(swipedRightList);

        matchesAdapter.setListener(new MatchesAdapter.MyMatchListener() {
            @Override
            public void onMatchClicked(int position, View view) {
                Person matchedPerson = swipedRightList.get(position);
                Chat chat = Chat.newInstance(matchedPerson);
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.add(R.id.root_container2, chat, "chat_tag");
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        matchesRecycler.setAdapter(matchesAdapter);

        return rootView;
    }

    public void onDatasetChanged() {
        if (matchesAdapter == null) {
            return;
        }
        matchesAdapter.notifyDataSetChanged();
    }

    public void updateList(Person newPerson) {
        swipedRightList.add(newPerson);
        onDatasetChanged();
    }
}
