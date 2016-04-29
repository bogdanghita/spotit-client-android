package com.it.spot.intro;

/**
 * Created by teo on 29.04.2016.
 */
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.it.spot.R;
import com.squareup.picasso.Picasso;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import jp.wasabeef.picasso.transformations.CropSquareTransformation;
import jp.wasabeef.picasso.transformations.CropTransformation;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class SampleSlide extends Fragment {

    private static final String ARG_LAYOUT_RES_ID = "layoutResId";
    private static final String ARG_IMAGE_RES_NAME = "imageResName";
    public static SampleSlide newInstance(int layoutResId,String imageName) {
        SampleSlide sampleSlide = new SampleSlide();

        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_RES_ID, layoutResId);
        args.putString(ARG_IMAGE_RES_NAME, imageName);
        sampleSlide.setArguments(args);

        return sampleSlide;
    }

    private int layoutResId;
    private String imageName;

    public SampleSlide() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null && getArguments().containsKey(ARG_LAYOUT_RES_ID)) {
            layoutResId = getArguments().getInt(ARG_LAYOUT_RES_ID);
            imageName = getArguments().getString(ARG_IMAGE_RES_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(layoutResId, container, false);
        ImageView img = (ImageView) view.findViewById(R.id.imageView);
        int h = img.getHeight();
        int w = img.getHeight();
        Context c = getActivity().getApplicationContext();
        Picasso.with(c).load("file:///android_asset/intro/" + imageName)
                .transform(new CropTransformation(200,0,900,1200))
                .into(img);
        return view;
    }

}