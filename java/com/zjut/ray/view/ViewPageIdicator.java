package com.zjut.ray.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zjut.ray.viewpageindicator.R;

import java.util.List;

/**
 * Created by Administrator on 2017/2/18.
 */

public class ViewPageIdicator extends HorizontalScrollView {
    private Paint mPaint;
    private Path mPath;
    private int mTrangleWidth;
    private int mTrangleHeight;
    private static final float RADIO_TRANGLE_WIDTH = 1 / 6f;
    private final int MAX_TRANGLE_WIDTH = (int) (getScreenWidth() / 4 * RADIO_TRANGLE_WIDTH);
    private int mInitTranslationX;
    private int mTanslationX;
    private int mTabVisibleCount;
    private static final int COUNT_DEFAULT_TAB = 4;
    private List<String> mTitles;
    private static final int COLOR_TEXT_NORMAL = 0x77ffffff;
    private static final int COLOR_TEXT_HightLight = 0xffffffff;
    private static final String TAG = "ViewPageIdicator";
    private ViewPager mViewPager;
    private LinearLayout linearLayout;

    public PageOnChangeListener mListener;

    public void setPageOnChangeListener(PageOnChangeListener mListener) {
        this.mListener = mListener;
    }


    public interface PageOnChangeListener {
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

        public void onPageSelected(int position);

        public void onPageScrollStateChanged(int state);
    }

    public ViewPageIdicator(Context context) {
        this(context, null);
    }

    public ViewPageIdicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        //获取可见Tab的数量
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewPageIdicator);
        mTabVisibleCount = a.getInt(R.styleable.ViewPageIdicator_visible_tab_count, COUNT_DEFAULT_TAB);
        if (mTabVisibleCount <= 0) {
            mTabVisibleCount = COUNT_DEFAULT_TAB;
        }
        a.recycle();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setPathEffect(new CornerPathEffect(3));
        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        HorizontalScrollView.LayoutParams params = new HorizontalScrollView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        addView(linearLayout, params);
        setHorizontalScrollBarEnabled(false);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        int cCount = linearLayout.getChildCount();
        if (cCount == 0) {
            return;
        }
        for (int i = 0; i < cCount; i++) {
            View view = linearLayout.getChildAt(i);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
            lp.weight = 0;
            lp.width = getScreenWidth() / mTabVisibleCount;
            view.setLayoutParams(lp);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(mInitTranslationX + mTanslationX, getHeight() + 2);
        canvas.drawPath(mPath, mPaint);
        canvas.restore();
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTrangleWidth = (int) (w / mTabVisibleCount * RADIO_TRANGLE_WIDTH);
        mTrangleWidth = Math.min(mTrangleWidth, MAX_TRANGLE_WIDTH);
        mInitTranslationX = w / mTabVisibleCount / 2 - mTrangleWidth / 2;
        initTriangle();
    }

    @Override
    public void fling(int velocityX) {
        super.fling(velocityX/3);
    }

    private void initTriangle() {
        mTrangleHeight = mTrangleWidth / 2;
        mPath = new Path();
        mPath.moveTo(0, 0);
        mPath.lineTo(mTrangleWidth, 0);
        mPath.lineTo(mTrangleWidth / 2, -mTrangleHeight);
        mPath.close();

    }

    public void scroll(int position, float positionOffset) {
        int tabWidth = getWidth() / mTabVisibleCount;
        if (position >= (mTabVisibleCount - 3) && positionOffset > 0 && linearLayout.getChildCount() >=mTabVisibleCount && position <= linearLayout.getChildCount() - 2) {
//            Log.i(TAG, "scroll: 111");
            scrollTo((position - (mTabVisibleCount - 2)) * tabWidth + (int) (tabWidth * positionOffset), 0);
        }
        mTanslationX = (int) (tabWidth * (positionOffset + position));
        invalidate();
    }

    public void setTabItemTitles(List<String> titles) {
        if (titles != null && titles.size() > 0) {
            linearLayout.removeAllViews();
            mTitles = titles;
            for (String title : mTitles) {
                linearLayout.addView(generateTextView(title));
            }
            setItemClickEvent();
        }
    }

    public void setVisibleTabCount(int count) {
        mTabVisibleCount = count;
    }


    private View generateTextView(String title) {
        TextView tv = new TextView(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.width = getScreenWidth() / mTabVisibleCount;
        tv.setLayoutParams(lp);
        tv.setText(title);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tv.setTextColor(COLOR_TEXT_NORMAL);
        return tv;
    }

    public int getScreenWidth() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics d = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(d);
        return d.widthPixels;
    }

    public void setViewPager(ViewPager viewPager, int pos) {
        mViewPager = viewPager;
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (mListener != null) {
                    mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
                }
                scroll(position, positionOffset);
            }

            @Override
            public void onPageSelected(int position) {
                if (mListener != null) {
                    mListener.onPageSelected(position);
                }
                hightLightTextView(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (mListener != null) {
                    mListener.onPageScrollStateChanged(state);
                }
            }
        });
        mViewPager.setCurrentItem(pos);
        hightLightTextView(pos);
    }

    private void resetTextView() {
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            View view = linearLayout.getChildAt(i);
            if (view instanceof TextView) {
                ((TextView) view).setTextColor(COLOR_TEXT_NORMAL);
            }
        }
    }

    private void hightLightTextView(int pos) {
        resetTextView();
        View view = linearLayout.getChildAt(pos);
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(COLOR_TEXT_HightLight);
        }
    }

    private void setItemClickEvent() {
        final int cCount = linearLayout.getChildCount();
        for (int i = 0; i < cCount; i++) {
            final int j = i;
            View view = linearLayout.getChildAt(i);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(j!=0&&j!=cCount){
                        int tabWidth = getWidth() / mTabVisibleCount;
                        scrollTo(tabWidth * (j-1), 0);
                        //Log.i(TAG, "onClick: "+j);
                    }
                    mViewPager.setCurrentItem(j, false);
                }
            });
        }
    }
}
