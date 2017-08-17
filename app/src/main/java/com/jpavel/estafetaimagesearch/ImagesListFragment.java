package com.jpavel.estafetaimagesearch;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImagesListFragment extends Fragment {

    private ImageAdapter rvAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView rv = (RecyclerView) inflater.inflate(R.layout.fragment_images_list, container, false);
        rv.setLayoutManager(new LinearLayoutManager(rv.getContext()));

        rvAdapter = new ImageAdapter(getActivity());
        rv.setAdapter(rvAdapter);

        return rv;
    }

    public static class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;
        private List<ImageModel> imagesList;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public ImageModel imageModel;

            public final View mView;
            public final ImageView mImageView;
            public final TextView mTextView;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView = (ImageView) view.findViewById(R.id.avatar);
                mTextView = (TextView) view.findViewById(android.R.id.text1);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mTextView.getText();
            }
        }

        public ImageAdapter(Context context) {
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mBackground = mTypedValue.resourceId;
            imagesList = new ArrayList<>();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater
                            .from(parent.getContext())
                            .inflate(R.layout.fragment_images_list_item, parent, false);
            view.setBackgroundResource(mBackground);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.imageModel = imagesList.get(position);

            holder.mTextView.setText(holder.imageModel.title);

            Glide.with(holder.mImageView.getContext())
                    .load(holder.imageModel.filePath != null ? holder.imageModel.filePath : holder.imageModel.uri)
                    .fitCenter()
                    .into(holder.mImageView);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), ""+position, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return imagesList.size();
        }

        public void updateData(List<ImageModel> images) {
            imagesList.clear();
            imagesList.addAll(images);
            notifyDataSetChanged();
        }
    }

    public void updateListFragment(List<ImageModel> images){
        rvAdapter.updateData(images);
    }
}