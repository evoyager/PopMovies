package com.voyager.popmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ImageAdapter extends ArrayAdapter<String> {

    LayoutInflater mInflater;
    Picasso mPicasso;
    ArrayList<String> objects;

    public ImageAdapter(Context context, ArrayList<String> objects) {
        super(context, R.layout.list_item_movie, objects);
        mInflater = LayoutInflater.from(context);
        mPicasso = Picasso.with(context);
        this.objects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(R.layout.list_item_movie, parent, false);
        }
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView1);
        mPicasso.load(objects.get(position)).into(imageView);
        return view;
    }

}