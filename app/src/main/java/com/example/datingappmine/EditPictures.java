package com.example.datingappmine;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.JsonUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.UUID;

public class EditPictures extends Fragment {
    FloatingActionButton chooseFromGallery;
    private final int PICK_IMAGE_REQUEST = 22;
    FirebaseStorage storage;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    private Uri filePath;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    RecyclerView editPicRecyclerview;
    UploadPicFragment uploadPicFragment;
    BigPictureFragment bigPictureFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storageReference = FirebaseStorage.getInstance().getReference(mAuth.getCurrentUser().getUid());
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid()).child("photos");
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.edit_pictures, container, false);
        editPicRecyclerview = rootView.findViewById(R.id.edit_pic_recycler);
        editPicRecyclerview.setHasFixedSize(true);
        editPicRecyclerview.setLayoutManager(new GridLayoutManager(getContext(), 2));
        chooseFromGallery = rootView.findViewById(R.id.choose_from_gal);

        createAdapter();

        chooseFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });

        return rootView;
    }

    private void createAdapter() {
        Activity secondActivity = getActivity();
        PictureAdapter pictureAdapter = new PictureAdapter(getContext(), ((SecondActivity) secondActivity).getCurrentUserPicturesList());

        pictureAdapter.setListener(new PictureAdapter.MyPicturesListener() {
            @Override
            public void onPictureLongClicked(int position, View view) {
                Image selectedImage = ((SecondActivity) secondActivity).getCurrentUserPicturesList().get(position);
                if (selectedImage.getImgUrl().equals("android.resource://com.example.datingappmine/drawable/upload_person_img")) {
                    Toast.makeText(getContext(), getString(R.string.click_to_add), Toast.LENGTH_SHORT).show();
                } else {
                    String selectedKey = selectedImage.getKey();
                    StorageReference imageRef = storage.getReferenceFromUrl(selectedImage.getImgUrl());
                    if(((SecondActivity) secondActivity).getCurrentUserPicturesList().size() != 1) {
                        Snackbar.make(getContext(), view, getString(R.string.want_delete), BaseTransientBottomBar.LENGTH_LONG).setAction(getString(R.string.yes), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        databaseReference.child(selectedKey).removeValue();
                                        Toast.makeText(getContext(), getString(R.string.Picture_deleted), Toast.LENGTH_SHORT).show();
                                        ((SecondActivity) secondActivity).getCurrentUserPicturesList().remove(position);
                                        pictureAdapter.notifyItemRemoved(position);
                                        getParentFragmentManager().popBackStack();
                                    }
                                });
                            }
                        }).show();
                    }
                    else{
                        Toast.makeText(secondActivity, "You must have at least one picture, add another to delete", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onPictureClicked(int position, View view) {
                Image selectedImage = ((SecondActivity) secondActivity).getCurrentUserPicturesList().get(position);
                bigPictureFragment = BigPictureFragment.newInstance(selectedImage.getImgUrl());
                bigPictureFragment.show(getParentFragmentManager(), "image_dialog_fragment");
            }
        });

        editPicRecyclerview.setAdapter(pictureAdapter);
    }

    private void SelectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_img)), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            uploadPicFragment = UploadPicFragment.newInstance(filePath.toString());
            uploadPicFragment.show(getParentFragmentManager(), "upload_dialog_fragment");
        }
    }

    public void onDatasetChanged() {
        if (editPicRecyclerview == null || editPicRecyclerview.getAdapter() == null) {
            return;
        }

        editPicRecyclerview.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
