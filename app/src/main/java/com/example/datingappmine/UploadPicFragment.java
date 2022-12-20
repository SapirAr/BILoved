package com.example.datingappmine;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class UploadPicFragment extends DialogFragment {
    ImageView pictureLargeIV;
    Uri filePath;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    StorageReference storageReference;
    DatabaseReference databaseReference;
    FloatingActionButton uploadImg;

    public static UploadPicFragment newInstance(String bigPic) {
        UploadPicFragment uploadPicFragment = new UploadPicFragment();
        Bundle bundle = new Bundle();
        bundle.putString("big_picture", bigPic);
        uploadPicFragment.setArguments(bundle);
        return uploadPicFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.upload_pic_fragment, container, false);
        getDialog().setCanceledOnTouchOutside(false);

        pictureLargeIV = rootView.findViewById(R.id.big_picture);
        uploadImg = rootView.findViewById(R.id.floating_check_btn);
        storageReference = FirebaseStorage.getInstance().getReference(mAuth.getCurrentUser().getUid());
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid()).child("photos");
        String img = getArguments().getString("big_picture");
        filePath = Uri.parse(img);

        Glide.with(getContext())
                .load(filePath)
                .into(pictureLargeIV);

        uploadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mAuth.getCurrentUser().isAnonymous()){
                    uploadImage();
                    getParentFragmentManager().popBackStack();
                    Toast.makeText(getContext(), getString(R.string.Image_uploaded), Toast.LENGTH_SHORT).show();
                    dismiss();
                }
                else{
                    Toast.makeText(getContext(), getString(R.string.first_register), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return rootView;
    }

    private void uploadImage() {
        if (filePath != null) {
            storageReference.child(UUID.randomUUID().toString()).putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful()) ;
                    Uri downloadUrl = urlTask.getResult();
                    Image image = new Image(downloadUrl.toString());
                    String imageId = databaseReference.push().getKey();
                    databaseReference.child(imageId).setValue(image);
                }
            });
        }
    }
}