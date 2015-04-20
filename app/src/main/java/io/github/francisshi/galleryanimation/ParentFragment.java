package io.github.francisshi.galleryanimation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

import io.github.francisshi.galleryanimation.util.Util;
import io.github.francisshi.galleryanimation.adapter.ImagesPagerAdapter;

/**
 * Created by Francis on 15/4/19.
 */
public class ParentFragment extends Fragment {

    private ViewPager mPager;
    private CirclePageIndicator mIndicator;
    private float positionX;
    private float positionY;
    private int horizontalSpacingAndWidth;
    private int vertivalSpacingAndHeight;
    private List<ItemFragment> mFragmentsList = new ArrayList<ItemFragment>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_parent, container, false);
        mPager = (ViewPager) rootView.findViewById(R.id.pager);
        mIndicator = (CirclePageIndicator) rootView.findViewById(R.id.indicator);
        final Bundle parentBundle = getArguments();
        horizontalSpacingAndWidth = parentBundle.getInt("width")+parentBundle.getInt("horizontalSpacing");
        vertivalSpacingAndHeight = parentBundle.getInt("height")+parentBundle.getInt("verticalSpacing");
        final int clickPosition = parentBundle.getInt("click_position");
        ViewTreeObserver vto = mPager.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                mPager.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                ArrayList<Integer> imagesRes = parentBundle.getIntegerArrayList("images_res");
                int pagerWidth = mPager.getMeasuredWidth();
                int pagerHeight = mPager.getMeasuredHeight();
                parentBundle.putInt("pagerWidth", pagerWidth);
                parentBundle.putInt("pagerHeight", pagerHeight);

                for (int i = 0; i < imagesRes.size(); i++) {
                    ItemFragment itemFragment = new ItemFragment();
                    int imageRes = imagesRes.get(i);
                    itemFragment.setImageRes(imageRes);
                    itemFragment.setArguments(parentBundle);
                    mFragmentsList.add(itemFragment);
                }

                mPager.setAdapter(new ImagesPagerAdapter(ParentFragment.this.getChildFragmentManager(), mFragmentsList));
                mPager.setCurrentItem(clickPosition);
                mIndicator.setViewPager(mPager);
            }
        });


        currentItem = clickPosition;


        // mFragmentsList.get(clickPosition).setmIsFirstFragment(true);
        mIndicator.setFillColor(this.getResources().getColor(R.color.default_line_indicator_selected_color));

        // 更新 position
        positionY = parentBundle.getFloat("positionY", 0);
        positionX = parentBundle.getFloat("positionX", 0);
        currentItem = clickPosition;

        mIndicator.setOnPageChangeListener(pageChangeListener);

        return rootView;

    }

    // <= nine pictures
    private int currentItem;
    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int selectedPage) {
            float x = getPositionX();
            float y = getPositionY();
            switch (mFragmentsList.size()) {
                case 2:
                    if (selectedPage > currentItem) {
                        x += horizontalSpacingAndWidth;
                    } else {
                        x -= horizontalSpacingAndWidth;
                    }
                    break;
                case 4:
                    if (selectedPage > currentItem) {
                        if (selectedPage % 2 == 0) {
                            y += vertivalSpacingAndHeight;
                            x -= horizontalSpacingAndWidth;
                        } else {
                            x += horizontalSpacingAndWidth;
                        }
                    } else if (currentItem % 2 == 0) {
                        y -= vertivalSpacingAndHeight;
                        x += horizontalSpacingAndWidth;
                    } else {
                        x -= horizontalSpacingAndWidth;
                    }
                    break;
                case 3:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                    if (selectedPage > currentItem) {
                        if (selectedPage % 3 == 0) {
                            y += vertivalSpacingAndHeight;
                            x -= 2 * horizontalSpacingAndWidth;
                        } else {
                            x += horizontalSpacingAndWidth;
                        }
                    } else if (currentItem % 3 == 0) {
                        y -= vertivalSpacingAndHeight;
                        x += 2 * horizontalSpacingAndWidth;
                    } else {
                        x -= horizontalSpacingAndWidth;
                    }

                    break;
            }
            setPositionX(x);
            setPositionY(y);
            currentItem = selectedPage > currentItem ? currentItem + 1 : currentItem - 1;
        }

        @Override
        public void onPageScrolled(int paramInt1, float paramFloat, int paramInt2) {
        }

        @Override
        public void onPageScrollStateChanged(int paramInt) {
        }
    };

    // 更新 position
    public float getPositionX() {
        return positionX;
    }

    public float getPositionY() {
        return positionY;
    }

    private void setPositionX(float positionX) {
        this.positionX = positionX;
    }

    private void setPositionY(float positionY) {
        this.positionY = positionY;
    }

}
