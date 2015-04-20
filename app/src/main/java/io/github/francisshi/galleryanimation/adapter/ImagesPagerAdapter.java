package io.github.francisshi.galleryanimation.adapter;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import io.github.francisshi.galleryanimation.ItemFragment;

/**
 * Created by Francis on 15/4/19.
 */
public class ImagesPagerAdapter extends FragmentPagerAdapter {

    private List<ItemFragment> mFragmentsList = new ArrayList<ItemFragment>();

    public ImagesPagerAdapter(FragmentManager fm,List<ItemFragment> fragmentList) {
        super(fm);
        mFragmentsList = fragmentList;
    }

    @Override
    public int getCount() {
        return mFragmentsList.size();
    }

    @Override
    public ItemFragment getItem(int position) {
        ItemFragment return_value = mFragmentsList.get(position);
        return return_value;
    }
}
