package io.github.francisshi.galleryanimation;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import io.github.francisshi.galleryanimation.util.Util;
import io.github.francisshi.galleryanimation.view.ZoomImageView;

/**
 * Created by Francis on 15/4/19.
 */
public class ItemFragment extends Fragment {

    private ZoomImageView mZoomImageView;
    private int mImageRes;
    private final Handler mHandler = new Handler();
    private Bundle mBundle;
    private int mWidth;
    private int mHeight;
    private int mPagerWidth;
    private int mPagerHeight;
    private int mImageWidth;
    private int mImageHeight;
    private float mPositionX;
    private float mPositionY;
    private Point mSuitableSize;
    private ParentFragment mParentFragment;
    private static final int ANIM_DURATION = 250;

    public void setImageRes(int imageRes){
          this.mImageRes = imageRes;
    }

//    public void ItemFragment(ItemBuilder itemBuilder) {
//        this.mImageRes = itemBuilder.mmImageRes;
//        this.mPositionX = itemBuilder.mmPostionX;
//        this.mPositionY = itemBuilder.mmPositionY;
//        this.mPagerWidth = itemBuilder.mmPagerWidth;
//        this.mPagerHeight = itemBuilder.mmPagerHeight;
//        this.mWidth = itemBuilder.mmWidth;
//        this.mHeight = itemBuilder.mmHeight;
//    }
//
//    public static class ItemBuilder {
//
//        public ItemBuilder(int imageRes) {
//            this.mmImageRes = imageRes;
//        }
//
//        private int mmImageRes;
//        private float mmPostionX;
//        private float mmPositionY;
//        private int mmPagerWidth;
//        private int mmPagerHeight;
//        private int mmWidth;
//        private int mmHeight;
//
//        public ItemBuilder positionX(float positionX) {
//            this.mmPostionX = positionX;
//            return this;
//        }
//
//        public ItemBuilder positionY(float positionY) {
//            this.mmPositionY = positionY;
//            return this;
//        }
//
//        public ItemBuilder pagerWidth(int pagerWidth) {
//            this.mmPagerWidth = pagerWidth;
//            return this;
//        }
//
//        public ItemBuilder pagerHeight(int pagerHeight) {
//            this.mmPagerHeight = pagerHeight;
//            return this;
//        }
//
//        public ItemBuilder width(int width) {
//            this.mmWidth = width;
//            return this;
//        }
//
//        public ItemBuilder height(int height) {
//            this.mmHeight = height;
//            return this;
//        }
//
//        public ItemFragment build() {
//            return new ItemFragment(ItemBuilder.this);
//        }
//
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item, container, false);
        mBundle = getArguments();
        mParentFragment = (ParentFragment) this.getParentFragment();

        mZoomImageView = (ZoomImageView) view.findViewById(R.id.zoomImageView);
        mZoomImageView.setImageResource(mImageRes);

        mPositionX = mParentFragment.getPositionX();
        mPositionY = mParentFragment.getPositionY();
        mPagerWidth = mBundle.getInt("pagerWidth", 0);
        mPagerHeight = mBundle.getInt("pagerHeight", 0);
        mWidth = mBundle.getInt("width", 0);
        mHeight = mBundle.getInt("height", 0);

        // 服务端不传image size
        mImageWidth = mBundle.getInt("image_width", 0);
        mImageHeight = mBundle.getInt("image_height", 0);

        mSuitableSize = Util.getFitSize(mImageWidth, mImageHeight, mPagerWidth, mPagerHeight);

        mHandler.post(new Runnable() {

            @Override
            public void run() {
                runEnterAnimation(mPagerWidth, mPagerHeight);
            }
        });

        mZoomImageView.setOnViewTapListener(new ZoomImageView.OnViewTapListener() {

            public void onViewTap(View view, float x, float y) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        runExitAnimation(mPagerWidth, mPagerHeight);
                    }
                });
            }
        });

        return view;

    }

    public void runEnterAnimation(final int containerWidth, final int containerHeight) {
        final ObjectAnimator widthAnim = ObjectAnimator.ofInt(mZoomImageView, "contentWidth", mWidth, mSuitableSize.x).setDuration(ANIM_DURATION);
        final ObjectAnimator heightAnim = ObjectAnimator.ofInt(mZoomImageView, "contentHeight", mHeight, mSuitableSize.y).setDuration(ANIM_DURATION);
        final ObjectAnimator xAnim = ObjectAnimator.ofInt(mZoomImageView, "contentX", (int) mPositionX, (containerWidth - mSuitableSize.x) / 2).setDuration(
                ANIM_DURATION);
        final ObjectAnimator yAnim = ObjectAnimator.ofInt(mZoomImageView, "contentY", (int) mPositionY, (containerHeight - mSuitableSize.y) / 2).setDuration(
                ANIM_DURATION);
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(widthAnim, heightAnim, xAnim, yAnim);
        animSet.start();
        animSet.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                mZoomImageView.setContentX(0);
                mZoomImageView.setContentY(0);
                mZoomImageView.setContentHeight(-1);
                mZoomImageView.setContentWidth(-1);
            }

            @Override
            public void onAnimationCancel(Animator arg0) {
            }
        });
    }

    public void runExitAnimation(final int containerWidth, final int containerHeight) {
        if (mSuitableSize != null) {
            final ObjectAnimator widthAnim = ObjectAnimator.ofInt(mZoomImageView, "contentWidth", mSuitableSize.x, mWidth).setDuration(ANIM_DURATION);
            final ObjectAnimator heightAnim = ObjectAnimator.ofInt(mZoomImageView, "contentHeight", mSuitableSize.y, mHeight).setDuration(ANIM_DURATION);
            final ObjectAnimator xAnim = ObjectAnimator.ofInt(mZoomImageView, "contentX", (containerWidth - mSuitableSize.x) / 2,
                    (int) mParentFragment.getPositionX()).setDuration(ANIM_DURATION);
            final ObjectAnimator yAnim = ObjectAnimator.ofInt(mZoomImageView, "contentY", (containerHeight - mSuitableSize.y) / 2,
                    (int) mParentFragment.getPositionY()).setDuration(ANIM_DURATION);
            final ObjectAnimator backColor = ObjectAnimator.ofFloat(mZoomImageView, "alpha", 0.5f).setDuration(ANIM_DURATION);
            AnimatorSet animSet = new AnimatorSet();
            animSet.playTogether(widthAnim, heightAnim, xAnim, yAnim, backColor);
            animSet.start();
            animSet.addListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationStart(Animator arg0) {
                    mZoomImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    mZoomImageView.setContentX((containerWidth - mSuitableSize.x) / 2);
                    mZoomImageView.setContentY((containerHeight - mSuitableSize.y) / 2);
                    mZoomImageView.setContentHeight(mSuitableSize.y);
                    mZoomImageView.setContentWidth(mSuitableSize.x);
                }

                @Override
                public void onAnimationRepeat(Animator arg0) {
                }

                @Override
                public void onAnimationEnd(Animator arg0) {
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    FragmentTransaction trans = manager.beginTransaction();
                    trans.remove(mParentFragment).commitAllowingStateLoss();
                    manager.popBackStack();
                }

                @Override
                public void onAnimationCancel(Animator arg0) {
                }
            });
        }
    }
}