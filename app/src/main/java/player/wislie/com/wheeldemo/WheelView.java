package player.wislie.com.wheeldemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.EdgeEffect;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class WheelView extends LinearLayout {

    private Scroller mScroller;

    private int visibleItems = 3; //可见的item数量
    private int itemHeight = 100;//每个item的高度

    private List<String> mDataList = new ArrayList<>();

    //滑轮的宽度
    private int mWheelWidth;
    //滑轮的高度
    private int mWheelHeight;

    //惯性最大最小速度
    protected int mMaximumVelocity, mMinimumVelocity;
    //检测手势的工具，可以获取手势的速度
    private VelocityTracker mVelocityTracker;

    private Paint mDividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int mMinY, mMaxY; //滑动的最小y值和最大y值
    private int initNeedScrollY; //初始时需要滑动的距离，这是中间的
    private float mLastY; //上一次的y坐标
    private int mCurrPosY; //当前的pos位置

    //默认的文字大小
    private static final int DEFAULT_TEXT_SIZE = 25;
    //最小的文字大小
    private static final int MIN_TEXT_SIZE = 20;
    //文字的最大透明度
    private static final int DEFAULT_TEXT_ALPHA = 255;
    //文字的最小透明度
    private static final int MIN_TEXT_ALPHA = 128;
    //文字缩放系数
    private static float mTextSizeCoeff = 1;

    //起始边缘
    private EdgeEffect mStartEdgeEffect;
    //末尾边缘
    private EdgeEffect mEndEdgeEffect;
    //边缘阴影的宽度
    private static final int EDGE_EFFECT_WIDTH = 15;
    //边缘阴影的颜色
    private static final int EDGE_COLOR = Color.BLUE;

    private WheelListener mWheelListener;

    public WheelView(Context context) {
        this(context, null);
    }

    public WheelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mScroller = new Scroller(context);
        mMaximumVelocity = ViewConfiguration.get(context)
                .getScaledMaximumFlingVelocity();
        mMinimumVelocity = ViewConfiguration.get(context)
                .getScaledMinimumFlingVelocity();
        initData();
        initLayoutParam();
        initPaint();
    }

    public void setWheelListener(WheelListener wheelListener) {
        this.mWheelListener = wheelListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int measuredWidth = DenisyUtil.getScreenWidth(getContext());
        setMeasuredDimension(measuredWidth, visibleItems * itemHeight);
    }

    private void initData() {
        mDataList.add("item0"); //0
        mDataList.add("item1"); //midY 1 mCurrPosY:-1 initNeedScrollY：100
        mDataList.add("item2"); //midY 2  mCurrPosY:0 initNeedScrollY：0
        mDataList.add("item3"); //midY 2 currPosY 0  initNeedScrollY 0
        mDataList.add("item4");
        mDataList.add("item5");
        mDataList.add("item6");
        mDataList.add("item7");
        mDataList.add("item8");
        mDataList.add("item9");
        mDataList.add("item10");
        mDataList.add("item11");

        //中间数字是1 往下移动1位
        //中间数字是2 不移动
        //中间数字是3 往上移动1位
        //中间数字是4 往上移动2位
        //...
        //初始化时位于滑轮中间的数字
        int middleDigit = mDataList.size() % 2 == 0 ? mDataList.size() / 2 : mDataList.size() / 2 + 1;
        //初始时需要滑动的距离
        initNeedScrollY = (2 - middleDigit) * itemHeight;
        // 1  minY -1*itemHeight, max 0*itemHeight
        // 12 minY -1*itemHeight max 0*itemHeight
        // 123 minY -1*itemHeight max 1*itemHeight
        // 1234 minY -1*itemHeight max 2*itemHeight
        // 12345 minY -1*itemHeight max 3*itemHeight
        // ...
        //计算minY 和 maxY的值
        mMinY = -itemHeight;
        if (mDataList.size() == 2) {
            mMaxY = 0;
        } else {
            mMaxY = (mDataList.size() - 1) * itemHeight + mMinY;
        }

        //文字大小系数
        mTextSizeCoeff = (float) ((DEFAULT_TEXT_SIZE - MIN_TEXT_SIZE) / Math.pow(itemHeight, 2));
    }

    private void initLayoutParam() {
        setOrientation(LinearLayout.VERTICAL);
        mWheelWidth = DenisyUtil.getScreenWidth(getContext());
        mWheelHeight = itemHeight * visibleItems;
        for (int i = 0; i < mDataList.size(); i++) {
            String content = mDataList.get(i);

            TextView itemView = new TextView(getContext());
            itemView.setLayoutParams(new LayoutParams(mWheelWidth, itemHeight));
            itemView.setGravity(Gravity.CENTER);
            itemView.setTextSize(DEFAULT_TEXT_SIZE);
            itemView.setTextColor(Color.BLACK);
            itemView.setText(content);
            addView(itemView);
        }
        this.setLayoutParams(new LayoutParams(mWheelWidth, mWheelHeight));
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                //初始滑动的位置,这里的目的是为了居中显示
                scrollTo(0, -initNeedScrollY);
            }
        });
    }

    private void initPaint() {
        setBackgroundColor(Color.WHITE);
        mDividerPaint.setStyle(Paint.Style.FILL);
        mDividerPaint.setColor(Color.BLACK);
        mDividerPaint.setStrokeWidth(2);
    }

    private float mDeltaY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        float currY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mLastY = currY;
                break;
            case MotionEvent.ACTION_MOVE:
                mDeltaY = -(currY - mLastY);
                scrollBy(0, (int) mDeltaY);
                mLastY = currY;
                initEdgeEffect();
                break;

            case MotionEvent.ACTION_UP:
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                float yVelocity = mVelocityTracker.getYVelocity();
                if (Math.abs(yVelocity) > mMinimumVelocity) {
                    //抛掷
                    mScroller.fling(0, getScrollY(), 0, (int) -yVelocity,
                            0, 0, mMinY, mMaxY);
                    invalidate();
                } else {
                    //使用startScroll 和 scrollBy 来控制偏移
                    offsetToIntPos();
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                releaseEdgeEffect();

                int position = mCurrPosY + 1;
                if (mWheelListener != null) {
                    mWheelListener.onPositionChanging(position);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                releaseEdgeEffect();
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());

            if (!mScroller.computeScrollOffset()) { //滚动结束,滑动偏移到整数位置
                offsetToIntPos();
            }
            //继续调用computeScroll方法
            postInvalidate();
        }
    }

    @Override
    public void scrollTo(int x, int y) {
        if (y < mMinY) {
            y = mMinY;
            //向上的边缘效果
            turnOnStartEdgeEffect();
        }
        if (y > mMaxY) {
            y = mMaxY;
            //向下的边缘效果
            turnOnEndEdgeEffect();
        }

        if (getScrollY() != y) {
            super.scrollTo(x, y);
        }
        //设置为整数
        mCurrPosY = scrollToPosY(y); //这是滑动的点 比如 -3,-2,-1, 0,1, 2, 3, 4...
    }

    //scrollTo方法中调用 调整位置
    private int scrollToPosY(int y) {
        int currPosY = y / itemHeight;
        if (y % itemHeight != 0) {
            if (Math.abs(y) > currPosY * itemHeight + 0.5 * itemHeight) {
                currPosY = currPosY + y / Math.abs(y);
            }
        }
        return currPosY;
    }

    //滑动偏移并调整到整数位置
    private void offsetToIntPos() {
        int scrollY = getScrollY();
        int currentY = mCurrPosY * itemHeight;
        int dy = currentY - scrollY;
        if (Math.abs(dy) > 1) {
            //渐变回弹
            mScroller.startScroll(0, getScrollY(), 0, dy, 500);
            invalidate();
        } else {
            //立刻回弹
            scrollBy(0, dy);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < this.getChildCount(); i++) {
            TextView childItem = (TextView) getChildAt(i);
            //这个最好画图
            double dy = getScrollY() + itemHeight - i * itemHeight;
            //设置子图大小
            float textSize = calculateTextSize(dy);
            childItem.setTextSize(textSize);
            //设置透明度
            float alpha = calculateTextAlpha(dy);
            childItem.setAlpha(alpha);
        }

        drawEdgeEffect(canvas);
    }

    //初始化边缘效果
    private void initEdgeEffect() {
        if (getOverScrollMode() != OVER_SCROLL_NEVER) {
            if (mStartEdgeEffect == null) {
                mStartEdgeEffect = new EdgeEffect(getContext());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mStartEdgeEffect.setColor(EDGE_COLOR);
                }
            }

            if (mEndEdgeEffect == null) {
                mEndEdgeEffect = new EdgeEffect(getContext());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mEndEdgeEffect.setColor(EDGE_COLOR);
                }
            }
        } else {
            mStartEdgeEffect = null;
            mEndEdgeEffect = null;
        }
    }

    //释放边缘效果
    private void releaseEdgeEffect() {
        if (mStartEdgeEffect != null) {
            mStartEdgeEffect.onRelease();
        }
        if (mEndEdgeEffect != null) {
            mEndEdgeEffect.onRelease();
        }
    }

    //启动边缘开始的效果
    private void turnOnStartEdgeEffect() {
        if (!mScroller.isFinished()) {
            if (mStartEdgeEffect != null) {
                mStartEdgeEffect.onAbsorb((int) mScroller.getCurrVelocity());
            }
            mScroller.abortAnimation();
        } else {
            if (mStartEdgeEffect != null) {
                //偏移量
                float deltaDistance = Math.abs(mDeltaY) / (visibleItems * itemHeight);
                mStartEdgeEffect.onPull(deltaDistance);
                mStartEdgeEffect.setSize(getWidth(), getHeight());
            }
        }
    }

    //启动边缘结束的效果
    private void turnOnEndEdgeEffect() {
        if (!mScroller.isFinished()) {
            if (mEndEdgeEffect != null) {
                mEndEdgeEffect.onAbsorb((int) mScroller.getCurrVelocity());
            }
            mScroller.abortAnimation();
        } else {
            if (mEndEdgeEffect != null) {
                //偏移量
                float deltaDistance = Math.abs(mDeltaY) / (visibleItems * itemHeight);
                mEndEdgeEffect.onPull(deltaDistance);
                mEndEdgeEffect.setSize(getWidth(), getHeight());
            }
        }
    }

    //绘制边缘效果
    private void drawEdgeEffect(Canvas canvas) {
        if (mStartEdgeEffect != null) {
            if (!mStartEdgeEffect.isFinished()) {
                int restoreCount = canvas.save();
                canvas.translate(0, mMinY);
                if (mStartEdgeEffect.draw(canvas)) {
                    postInvalidateOnAnimation();
                }
                canvas.restoreToCount(restoreCount);
            } else {
                mStartEdgeEffect.finish();
            }
        }

        if (mEndEdgeEffect != null) {
            if (!mEndEdgeEffect.isFinished()) {
                int restoreCount = canvas.save();
                canvas.rotate(180);
                float dy = mMinY - mDataList.size() * itemHeight;
                canvas.translate(-getWidth(), dy);
                if (mEndEdgeEffect.draw(canvas)) {
                    postInvalidateOnAnimation();
                }
                canvas.restoreToCount(restoreCount);
            } else {
                mEndEdgeEffect.finish();
            }
        }
    }

    /**
     * 计算文字字体大小 停止滑动时,选中的字体最大,距离选中字体的中心位置越近,字体越大,
     * 边界是 -itemHeight 到 itemHeight,超出这个边界,字体大小都是MIN_TEXT_SIZE
     * 计算公式 textSize = -mTextSizeCoeff*Math.pow(distance,2)+DEFAULT_TEXT_SIZE
     *
     * @param distance
     * @return
     */
    private float calculateTextSize(double distance) {
        float size = MIN_TEXT_SIZE;
        if (Math.abs(distance) < itemHeight) {
            size = (float) (-mTextSizeCoeff * Math.pow(distance, 2) + DEFAULT_TEXT_SIZE);
        }
        return size;
    }

    /**
     * 计算文字的透明度,停止滑动时,选中的字体透明度最高,距离选中字体的中心位置越近,透明度越高,
     * 边界是 -itemHeight 到 itemHeight,超出这个边界,透明度都是MIN_TEXT_ALPHA
     *
     * @param distance
     * @return
     */
    private float calculateTextAlpha(double distance) {
        float alpha = MIN_TEXT_ALPHA;
        if (Math.abs(distance) < itemHeight) {
            alpha = (float) ((MIN_TEXT_ALPHA - DEFAULT_TEXT_ALPHA) * distance / itemHeight + DEFAULT_TEXT_ALPHA);
        }
        return alpha;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) { //绘制顺序 onDraw -> dispatchDraw, 防止分割线被被覆盖
        super.dispatchDraw(canvas);
        drawDivider(canvas);
    }

    //绘制线条
    private void drawDivider(Canvas canvas) {

        canvas.save();
        int scrollY = getScrollY();
        canvas.translate(0, scrollY);

        canvas.drawLine(0, itemHeight, mWheelWidth, itemHeight, mDividerPaint);
        canvas.drawLine(0, itemHeight * 2, mWheelWidth, itemHeight * 2, mDividerPaint);

        canvas.restore();
    }


    public void setSelectedPos(int pos) {

    }

    public int getSelectedPos() {
        return mCurrPosY + 1;
    }


}
