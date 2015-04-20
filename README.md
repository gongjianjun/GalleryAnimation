PS：上传的只是一个GridView demo，实现起来的话应该都不难。
在大图情况下，点击回退到小图，此时需要考虑到滑动之后小图的改变位置。demo中我只考虑了<= 9张的情况。
————————————————
最近项目中添加了群动态功能，类似微信朋友圈，对于list item中的图片打开效果，我参考了微信，包括微博其实也是这么做的。
在点击item中的图片时，背景变黑，将原来的小图逐渐放大；再次点击时，图片缩小，并回到原来的位置。当然了，在大图界面下，多图的话可以滑动。
其中在动画方面，我一开始用的是Rebound,后来放弃改为属性动画，缩放我使用的是zoomImageView，滑动方面用的是ViewPagerIndicator。

先看一下最终的[视频效果:](https://youtu.be/ZAzzMxQbqRw)
[![ScreenShot](http://githubpages.qiniudn.com/实现类似朋友圈的查看大图动画1.png)](https://youtu.be/ZAzzMxQbqRw)

### 说一下动画方面的具体思路。
1. 在小图状态下点击时，新建一个fragment（原来用的是activity，可是有闪屏，动画就显得不够柔和了），记为parentFragment；
2. 将小图的位置，大小，在多图中的position传递过去；
3. 在parentFragment中利用ViewPagerIndicator，将每张图片放在一个fragment，记为一个itemFragment；
4. 在itemFragment中用zoomImageView展示图片，这时想让图片从起始位置开始展开动画，就要用到第2步中的位置，和大小，对zoomImageView进行初始设置；
5. 根据第2步中传递过来的原小图imageView的大小和parentFragment中viewPager的大小，计算出动画时需要的缩放比例；
6. 当用户点击大图想返回时，将parentFragment背景设置为透明，并且viewPagerIndicator的Indicator不可见，这样在图片缩小的过程中，感觉整个界面也跟着缩小了，实际上只是parentFragment透明了而已；
7. 如果原来小图的imageView是CENTER_CROP，不要忘了将zoomImageView在缩小时也设置成CENTER_CROP，这样可以保证完美对接到小图。
<!--more-->
### 代码实现
打开小图：
```java
private View.OnTouchListener imageTouchListener = new View.OnTouchListener() {

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            intent = mCommonActivity.getIntent();
            if (event.getAction() == MotionEvent.ACTION_UP) {
                intent.putExtra("positionX", event.getRawX() - event.getX());
                intent.putExtra("positionY", event.getRawY() - event.getY() - Utility.getStatusBarHeight(mCommonActivity));
                intent.putExtra("width", v.getWidth());
                intent.putExtra("height", v.getHeight());
            }
            return false;
        }
    };

    private View.OnClickListener imageOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            ArrayList<String> imagesBigList = new ArrayList<String>();
            for (int i = 0; i < mImagesList.size(); i++) {
                JSONObject imagesJsonObject = mImagesList.get(i);
                String imageBigString = null;
                if (imagesJsonObject != null && imagesJsonObject.length() > 0) {
                    imageBigString = imagesJsonObject.optString("image_url");
                    if (imageBigString != null && imageBigString.length() > 0) {
                        imagesBigList.add(imageBigString);
                    }
                }
            }
            int clickPosition = Integer.parseInt(v.getContentDescription().toString());
            intent.putStringArrayListExtra("images_url", imagesBigList);
            intent.putExtra("click_position", clickPosition);
            FragmentManager fragmentManager = mCommonActivity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            MutiImageFragment fragment = new MutiImageFragment(mCommonActivity);
            fragmentTransaction.add(R.id.fragment_container, fragment, "multiImage");
            fragmentTransaction.commit();
        }
    };
```

展现小图我用的是netWorkImageView，分别设置它的onTouch()和onClick(),onTouch()会优先于onClick()执行。在onTouch()中，获取netWorkImageView的位置和大小。
注意event.getRawX()和event.getX()的区别，getRawX()是获取点击位置相对于整个屏幕原点(左上)的x距离，getX()则是相对于自身的x距离，所以event.getRawX() - event.getX()得到的就是newWorkImageView左上角相对屏幕的x距离。当然了，对于y轴，还要减去状态栏的距离。
在onClick()时，除了将images_url和click_position传递过去之外，创建了展示多图的parentFragment。

在parentFragment中需要得到viewPager的大小，由于onCreateView时是无法得到其宽和高的（view还没有创建好），所以设置OnGlobalLayoutListener
```java
@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_muti_image, container,false);
        mPager = (ViewPager) view.findViewById(R.id.pager);
        mIndicator = (CirclePageIndicator) view.findViewById(R.id.indicator);
        intent = mCommonActivity.getIntent();
        ViewTreeObserver vto = mPager.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                mPager.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int pagerWidth = mPager.getMeasuredWidth();
                int pagerHeight = mPager.getMeasuredHeight();
                intent.putExtra("pagerWidth", pagerWidth);
                intent.putExtra("pagerHeight", pagerHeight);
            }
        });
        if (intent.hasExtra("images_url")) {
            List<String> imagesUrl = intent.getStringArrayListExtra("images_url");
            for (int i = 0; i < imagesUrl.size(); i++) {
                String image = imagesUrl.get(i);
                mFragmentsList.add(new MutiImageItemFragment(image,mCommonActivity,this));
            }
        }
        int clickPosition = 0;
        if (intent.hasExtra("click_position")) {
            clickPosition = intent.getIntExtra("click_position", 0);
        }
        mPager.setAdapter(new ImagesPagerAdapter(this.getChildFragmentManager()));
        mPager.setCurrentItem(clickPosition);
        mIndicator.setFillColor(this.getResources().getColor(R.color.default_line_indicator_selected_color));
        mIndicator.setViewPager(mPager);
        return view;
    }
```

这样就得到viewPager的大小，接下来就是在itemFragment中设置zoomImageView的初始位置。
动画当然是在图片加载好之后，所以在图片加载成功后，获取各个参数并设置初始位置：
```java
ImageRequest imageRequest = new ImageRequest(mImageUrl, new Listener<Bitmap>() {

            @Override
            public void onResponse(Bitmap response) {
                mZoomImageView.setImageBitmap(response);
                if (isFirstFragment) {
                    if (intent != null) {
                        if (intent.hasExtra("positionX")) {
                            positionX = intent.getFloatExtra("positionX", 0);
                            mZoomImageView.setPivotX(0);
                            mZoomImageView.setTranslationX(positionX);
                        }
                        if (intent.hasExtra("positionY")) {
                            positionY = intent.getFloatExtra("positionY", 0);
                            mZoomImageView.setPivotY(0);
                            mZoomImageView.setTranslationY(positionY);
                        }
                        if (intent.hasExtra("width")) {
                            mWidth = intent.getIntExtra("width", 0);
                            mZoomImageView.getLayoutParams().width = mWidth;
                        }
                        if (intent.hasExtra("height")) {
                            mHeight = intent.getIntExtra("height", 0);
                            mZoomImageView.getLayoutParams().height = mHeight;
                        }
                        if (intent.hasExtra("pagerWidth")) {
                            mPagerWidth = intent.getIntExtra("pagerWidth", 0);
                        }
                        if (intent.hasExtra("pagerHeight")) {
                            mPagerHeight = intent.getIntExtra("pagerHeight", 0);
                        }

                        int endValue = mSpring.getEndValue() == 0 ? 1 : 0;
                        mSpring.setEndValue(endValue);
                    }
                    isFirstFragment = false;
                }
            }
        }, 0, 0, Config.RGB_565, new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                mZoomImageView.setImageResource(R.drawable.blank);
            }
        });
```

mSpring.setEndValue(endValue);会触发弹簧updata：
```java
@Override
    public void onSpringUpdate(Spring arg0) {
        render();
    }
```

```java
private void render() {
        float xlatX = (float) SpringUtil.mapValueFromRangeToRange(mSpring.getCurrentValue(), 0, 1, positionX, 0);
        float xlatY = (float) SpringUtil.mapValueFromRangeToRange(mSpring.getCurrentValue(), 0, 1, positionY, 0);
        mZoomImageView.setPivotX(0);
        mZoomImageView.setPivotY(0);
        mZoomImageView.setTranslationX(xlatX);
        mZoomImageView.setTranslationY(xlatY);

        float sx = (float) mPagerWidth / mWidth;
        float sy = (float) mPagerHeight / mHeight;

        if (mSpring.getEndValue() == 0) {
            // 缩小时 原来的小图是CENTER_CROP，确保对接到小图的确切位置
            mZoomImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        float sX = (float) SpringUtil.mapValueFromRangeToRange(mSpring.getCurrentValue(), 0, 1, 1, sx);
        float sY = (float) SpringUtil.mapValueFromRangeToRange(mSpring.getCurrentValue(), 0, 1, 1, sy);
        mZoomImageView.setScaleX(sX);
        mZoomImageView.setScaleY(sY);
    }
```
注意在缩小时设置为CENTER_CROP。

在点击大图退出时，设置parentFragment的背景：
```java
    mZoomImageView.setOnViewTapListener(new OnViewTapListener() {
        public void onViewTap(View view, float x, float y) {
            View parentFragmentView = mParentFragment.getView();
            if (parentFragmentView != null) {
                parentFragmentView.findViewById(R.id.frameLayoutBackGround).setBackgroundResource(android.R.color.transparent);
                parentFragmentView.findViewById(R.id.indicator).setVisibility(View.GONE);
                int endValue = mSpring.getEndValue() == 0 ? 1 : 0;
                mSpring.setEndValue(endValue);
            }
        }
    });
```

最后在动画结束时，去掉parentFragment：
```java
public void onSpringAtRest(Spring arg0) {
        if (mSpring.isAtRest() && mSpring.getCurrentValue() == 0) {
            mCommonActivity.getSupportFragmentManager().beginTransaction().remove(mParentFragment).commit();
        }
    }
```

一开始感觉效果还不错，但是其实有3个非常严重的bug
1. 图片被放大到viewPager的大小，也就是宽高都被拉满了，这样就破坏了原来图片的比例；
2. 在多图状况下，并没有更新滑动图片的返回位置，这样不管滑到哪个图片还是缩回到一开始打开图片的位置；
3. zoomImageView失效，无法（或者说几乎不能）拉伸。

### 解决办法
1. 既要考虑到图片的比例，又要考虑到viewPager容器的宽高。所以图片的缩放系数显然是由这两方面的size决定的；
2. 在pager滑动的时候，要根据当前itemFragment的位置，更新返回所需要的位置，那么这些更新应该在parentFragment中进行，并且开放接口，在itemFragment要退出的时候调用，得到返回位置。
3. 我一开始用的rebound，原理可以想象成这么一个场景：在弹簧弹动的一段时间内，有许多个时间点，在这些时间点上，对view的一些属性进行设置，从而整个时间段内就产生了动画效果，而且具有更加类似真实世界的物理感觉。在上文的render()中我是设置了这两个属性：
```java
mZoomImageView.setTranslationX(xlatX);
mZoomImageView.setScaleX(sX);
```
那么问题就来了，**mZoomImageView的位置和大小并没有改变**（和初始位置一样，还是在左上角；宽高还是动画开始前的宽高：mZoomImageView.getLayoutParams().height = mHeight;）。这样mZoomImageView的touchEvent中就会发生看似奇怪的事情了...所以合理的解决办法应该是在动画过程中，确确实实改变它的位置和宽高，这给了我另外一个思路：**属性动画**

#### 确定缩放比例
在itemFragment中根据ViewPager的大小和image的原始大小，返回合适的大小：
```java
private Point getFitSize(final int width, final int height, final int containerWidth, final int containerHeight) {
        int resultHeight, resultWidth;
        resultHeight = height * containerWidth / width;
        if (resultHeight <= containerHeight) {
            resultWidth = containerWidth;
        } else {
            resultWidth = width * containerHeight / height;
            resultHeight = containerHeight;
        }
        return new Point(resultWidth, resultHeight);
    }
```

#### 更新返回位置
一开始我是对pager设置OnPageChangeListener，但是发现ViewPagerIndicator已经对pager设置了OnPageChangeListener，于是二二的想创建一个newListener和oldListener,然后在newListener中调用olderListener来实现ViewPagerIndicator的滑动。其实吧，这么ugly的做法是在错误的路上越走越远，为什么不先看看人家的源码呢？
在ViewPagerIndicator中的onPageSelected():
```java
@Override
    public void onPageSelected(int position) {
        if (mSnap || mScrollState == ViewPager.SCROLL_STATE_IDLE) {
            mCurrentPage = position;
            mSnapPage = position;
            invalidate();
        }

        if (mListener != null) {
            mListener.onPageSelected(position);
        }
    }
```
下面那个mListener是啥，貌似执行个回调。
```java 
@Override
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mListener = listener;
    }
```
直接对indicator设置OnPageChangeListener就行了！还是Linux大神的那句话，RTFSC（Read the Fucking Source Code）
Anyway...更新位置：
```java
private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int selectedPage) {
            float x = getPositionX();
            float y = getPositionY();
            switch (mFragmentsList.size()) {
            case 2:
                if (selectedPage > currentItem) {
                    x += itemHeightAndSpace;
                } else {
                    x -= itemHeightAndSpace;
                }
                break;
            case 4:
                if (selectedPage > currentItem) {
                    if (selectedPage % 2 == 0) {
                        y += itemHeightAndSpace;
                        x -= itemHeightAndSpace;
                    } else {
                        x += itemHeightAndSpace;
                    }
                } else if (currentItem % 2 == 0) {
                    y -= itemHeightAndSpace;
                    x += itemHeightAndSpace;
                } else {
                    x -= itemHeightAndSpace;
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
                        y += itemHeightAndSpace;
                        x -= 2 * itemHeightAndSpace;
                    } else {
                        x += itemHeightAndSpace;
                    }
                } else if (currentItem % 3 == 0) {
                    y -= itemHeightAndSpace;
                    x += 2 * itemHeightAndSpace;
                } else {
                    x -= itemHeightAndSpace;
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
```
需要考虑到前进还是后退，不同张数有不同的布局，切换时是否有换行，所以稍微有一点麻烦。

#### 使用属性动画
``` java
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
        animSet.addListener(new AnimatorListener() {

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
```
更新了mZoomImageView的属性contentWidth、contentHeight、contentX、contentY。
需要注意的是，在动画的过程中mZoomImageView的宽高是刚好包裹着整个图片，那么结束的时候，它的宽高也是刚好包裹着整个图片，但是我们可不想让缩放范围只落在图片区域内，而应该是整个viewPager的宽高。所以在onAnimationEnd()的时候，把他的宽高设为了-1，即matchParent。同理，在退出动画的时候，把它的宽高再设回到刚好包裹住。
```java
public void runExitAnimation(final int containerWidth, final int containerHeight) {
        if (mSuitableSize != null) {
            final ObjectAnimator widthAnim = ObjectAnimator.ofInt(mZoomImageView, "contentWidth", mSuitableSize.x, mWidth).setDuration(ANIM_DURATION);
            final ObjectAnimator heightAnim = ObjectAnimator.ofInt(mZoomImageView, "contentHeight", mSuitableSize.y, mHeight).setDuration(ANIM_DURATION);
            final ObjectAnimator xAnim = ObjectAnimator.ofInt(mZoomImageView, "contentX", (containerWidth - mSuitableSize.x) / 2,
                    (int) ((MutiImageFragment) mParentFragment).getPositionX()).setDuration(ANIM_DURATION);
            final ObjectAnimator yAnim = ObjectAnimator.ofInt(mZoomImageView, "contentY", (containerHeight - mSuitableSize.y) / 2,
                    (int) ((MutiImageFragment) mParentFragment).getPositionY()).setDuration(ANIM_DURATION);
            final ObjectAnimator backColor = ObjectAnimator.ofFloat(mZoomImageView, "alpha", 0.5f).setDuration(ANIM_DURATION);
            AnimatorSet animSet = new AnimatorSet();
            animSet.playTogether(widthAnim, heightAnim, xAnim, yAnim, backColor);
            animSet.start();
            animSet.addListener(new AnimatorListener() {

                @Override
                public void onAnimationStart(Animator arg0) {
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
                    FragmentManager manager = mCommonActivity.getSupportFragmentManager();
                    FragmentTransaction trans = manager.beginTransaction();
                    trans.remove(mParentFragment).commit();
                    manager.popBackStack();
                }

                @Override
                public void onAnimationCancel(Animator arg0) {
                }
            });
        }
    }
```
动画的退出过程中我加入了alpha，不至于退回到白底色的屏幕太刺眼。在动画结束时remove掉mParentFragment。
在ZoomImageView中设置属性动画会调用的方法：
```java
public void setContentWidth(final int contentWidth) {
        final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        layoutParams.width = contentWidth;
        setLayoutParams(layoutParams);
    }
    
    public int getContentWidth() {
        return getLayoutParams().width;
    }
    
    public void setContentHeight(final int contentHeight) {
        final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        layoutParams.height = contentHeight;
        setLayoutParams(layoutParams);
    }

    public int getContentHeight() {
        return getLayoutParams().height;
    }

    public void setContentX(final int contentX) {
        final MarginLayoutParams layoutParams = (MarginLayoutParams) getLayoutParams();
        layoutParams.leftMargin = contentX;
        setLayoutParams(layoutParams);
    }
    
    public int getContentX() {
        return ((MarginLayoutParams) getLayoutParams()).leftMargin;
    }

    public void setContentY(final int contentY) {
        final MarginLayoutParams layoutParams = (MarginLayoutParams) getLayoutParams();
        layoutParams.topMargin = contentY;
        setLayoutParams(layoutParams);
    }
    
    public int getContentY() {
        return ((MarginLayoutParams) getLayoutParams()).topMargin;
    }
```
确实是改变了“属性”。