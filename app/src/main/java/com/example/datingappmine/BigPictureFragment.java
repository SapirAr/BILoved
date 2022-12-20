package com.example.datingappmine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;

public class BigPictureFragment extends DialogFragment {
    ImageView pictureLargeIV;

    public static BigPictureFragment newInstance(String bigPic) {
        BigPictureFragment bigPictureFragment = new BigPictureFragment();
        Bundle bundle = new Bundle();

        bundle.putString("big_pic", bigPic);
        bigPictureFragment.setArguments(bundle);
        return bigPictureFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.big_picture_frag, container, false);
        getDialog().setCanceledOnTouchOutside(false);

        pictureLargeIV = rootView.findViewById(R.id.big_pic);

        Glide.with(getContext())
                .load(getArguments().getString("big_pic"))
                .into(pictureLargeIV);

        return rootView;
    }
}