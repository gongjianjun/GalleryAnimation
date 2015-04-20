package io.github.francisshi.galleryanimation.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import io.github.francisshi.galleryanimation.MainActivity;
import io.github.francisshi.galleryanimation.ParentFragment;
import io.github.francisshi.galleryanimation.R;
import io.github.francisshi.galleryanimation.util.Util;

/**
 * Created by Francis on 15/4/17.
 */
public class GridViewAdapter extends BaseAdapter {

    private ArrayList<Integer> mImagesRes;
    private FragmentActivity mActivity;
    private int horizontalSpacing;
    private int verticalSpacing;
    Bundle mBundle;

    public GridViewAdapter(FragmentActivity activity) {
        mActivity = activity;
        mImagesRes = Util.getImageRes(activity);
        mBundle = new Bundle();
    }

    public void setSpacing(int horizontalSpacing,int verticalSpacing){
        this.horizontalSpacing = horizontalSpacing;
        this.verticalSpacing = verticalSpacing;
    }

    @Override
    public int getCount() {
        int count = mImagesRes.size();
        return count;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mActivity.getLayoutInflater().inflate(R.layout.image_item, parent, false);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
            imageView.setImageResource(mImagesRes.get(position));
            imageView.setOnTouchListener(imageTouchListener);
            imageView.setContentDescription(position + "");
            imageView.setOnClickListener(imageClickListener);
        }
        return convertView;
    }

    private View.OnTouchListener imageTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                mBundle.putFloat("positionX", event.getRawX() - event.getX());
                mBundle.putFloat("positionY", event.getRawY() - event.getY() - Util.getStatusBarHeight(mActivity));
                mBundle.putInt("width", v.getWidth());
                mBundle.putInt("height", v.getHeight());
                mBundle.putInt("horizontalSpacing", horizontalSpacing);
                mBundle.putInt("verticalSpacing", verticalSpacing);
            }
            return false;
        }
    };

    private View.OnClickListener imageClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            int clickPosition = Integer.parseInt(v.getContentDescription().toString());
            FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            ParentFragment fragment = (ParentFragment) fragmentManager.findFragmentByTag("multiImage");
            if (fragment == null) {
                fragment = new ParentFragment();
                mBundle.putIntegerArrayList("images_res", mImagesRes);
                mBundle.putInt("click_position", clickPosition);
                int imageRes = mImagesRes.get(clickPosition);
                Bitmap bm = BitmapFactory.decodeResource(mActivity.getResources(), imageRes);
                BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                bitmapOptions.inJustDecodeBounds = true;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.PNG, 0, bos);
                byte[] bitmapdata = bos.toByteArray();
                ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
                BitmapFactory.decodeStream(bs, null, bitmapOptions);
                int imageWidth = bitmapOptions.outWidth;
                int imageHeight = bitmapOptions.outHeight;
                try {
                    bos.close();
                    bs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mBundle.putInt("image_width", imageWidth);
                mBundle.putInt("image_height", imageHeight);
                fragment.setArguments(mBundle);
                if (mActivity instanceof MainActivity) {
                    fragmentTransaction.add(R.id.fragmentContainer, fragment, "multiImage");
                }
                fragmentTransaction.addToBackStack("multiImage");
                fragmentTransaction.commit();
            }
        }
    };

}
