package com.example.datingappmine;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class EditProfile extends Fragment {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference users = database.getReference("users");
    final String editpictures_fragment_tag = "editpictures_fragment";
    Person person;
    EditText nameET, ageET, cityET, descriptionET;
    RadioGroup radioGroup;
    RadioButton genderRB;
    RadioButton female;
    RadioButton male;
    String gender;
    Button saveChangesBtn;
    TextView logoutBT;
    ImageButton picture_edit_screen;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.edit_profile, container, false);
        radioGroup = rootView.findViewById(R.id.radio_group);
        logoutBT = rootView.findViewById(R.id.log_out);
        nameET = rootView.findViewById(R.id.name);
        ageET = rootView.findViewById(R.id.age);
        cityET = rootView.findViewById(R.id.city);
        descriptionET = rootView.findViewById(R.id.description);
        picture_edit_screen = rootView.findViewById(R.id.picture_edit_btn);
        female = rootView.findViewById(R.id.female);
        male = rootView.findViewById(R.id.male);

        users.child(mAuth.getCurrentUser().getUid()).child("details").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                person = snapshot.getValue(Person.class);
                if (person == null) return;

                nameET.setText(person.getName() == null ? "" : person.getName());
                ageET.setText(person.getAge() == 0 ? "" : String.valueOf(person.getAge()));
                cityET.setText(person.getCity() == null ? "" : person.getCity());
                descriptionET.setText(person.getDescription() == null ? "" : person.getDescription());
                if(person.getGender().equals("Male")){
                    male.setChecked(true);
                }
                else{
                    female.setChecked(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        logoutBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                getActivity().finish();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }
        });

        saveChangesBtn = rootView.findViewById(R.id.save_changes);
        saveChangesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameET.getText().toString();
                String city = cityET.getText().toString();
                String description = descriptionET.getText().toString();
                int age;

                if (ageET.getText().toString().equals("")) {
                    age = 0;
                } else {
                    age = Integer.parseInt(ageET.getText().toString());
                }

                int selectedId = radioGroup.getCheckedRadioButtonId();
                genderRB = rootView.findViewById(selectedId);
                gender = genderRB.getText().toString();

                if(!mAuth.getCurrentUser().isAnonymous()){
                    if (!name.equals("") || !city.equals("") || !description.equals("") || age != 0) {
                        person = new Person((mAuth.getCurrentUser()).getUid(), name, age, city, description, gender);
                        users.child(mAuth.getCurrentUser().getUid()).child("details").setValue(person);
                        Toast.makeText(getContext(), getString(R.string.profile_saved), Toast.LENGTH_SHORT).show();
                        SecondActivity secondActivity = (SecondActivity)getActivity();
                        secondActivity.swipeCardview.setCurrentUser(person);

                    } else {
                        Toast.makeText(getContext(), getString(R.string.fill_details), Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(getContext(), getString(R.string.first_register), Toast.LENGTH_SHORT).show();
                }
            }
        });

        picture_edit_screen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditPictures editPictures = new EditPictures();
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.add(R.id.root_container3, editPictures, editpictures_fragment_tag);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void showProfilePic() {
        Activity secondActivity = getActivity();
        if (((SecondActivity) secondActivity) != null) {
            if (((SecondActivity) secondActivity).getCurrentUserPicturesList().size() > 0) {
                Image picture = ((SecondActivity) secondActivity).getCurrentUserPicturesList().get(0);
                if (!picture.getImgUrl().equals("android.resource://com.example.datingappmine/drawable/upload_person_img")) {
                    Glide.with(getContext())
                            .load(picture.getImgUrl()).centerCrop()
                            .into(picture_edit_screen);
                }
            }
        }
    }
}