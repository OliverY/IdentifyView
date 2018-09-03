package com.yxj.identifyview.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.yxj.identifyview.R;

/**
 * Author:  yxj
 * Date:    2018/9/3 下午5:28
 * ----------------------------------------------------
 * Description:
 */
public class IdentifyView extends View {

    private Bitmap mBitmap;
    private Paint mBitmapPaint;
    private Paint mButtonPaint;
    private Paint movePaint;

    private int bitmapWidth,bitmapHeight;

    private int buttonX;
    private int buttonWidth = 150;
    private int buttonHeight = 100;

    private int startX,targetX,moveY;
    private int moveWidth,moveHeight;

    private ResultCallback mResultCallback;

    public IdentifyView(Context context) {
        this(context,null);
    }

    public IdentifyView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public IdentifyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBitmapPaint.setColor(Color.BLACK);

        mButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mButtonPaint.setColor(Color.BLACK);

        movePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        movePaint.setColor(Color.RED);
//        movePaint.setStyle(Paint.Style.STROKE);
//        movePaint.setStrokeWidth(10);

        mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.jsy12);
        bitmapWidth = mBitmap.getWidth();
        bitmapHeight = mBitmap.getHeight();

        moveY = 300;
        moveWidth = moveHeight = 200;
        startX = 100;
        targetX = bitmapWidth - moveWidth - 100;

        mFloat = (targetX - startX) * 1.0f / (bitmapWidth - buttonWidth);
        Log.e("yxj","mFloat::"+mFloat);
    }

    float mFloat;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 画图片
        Matrix matrix = new Matrix();
        canvas.drawBitmap(mBitmap,matrix,mBitmapPaint);

        RectF targetRectF = new RectF(targetX,moveY,targetX+moveWidth,moveY+moveHeight);
        canvas.drawRect(targetRectF,mButtonPaint);

        canvas.save();

        Shader shader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        movePaint.setShader(shader);

        Rect rect = new Rect(targetX,moveY,targetX+moveWidth,moveY+moveHeight);
        int moveX = - (targetX - startX) + (int) (buttonX*mFloat);
        canvas.translate(moveX,0);
        canvas.drawBitmap(mBitmap,rect,rect,movePaint);

        canvas.restore();

        RectF rectF = new RectF(buttonX,bitmapHeight,buttonX+buttonWidth,bitmapHeight+buttonHeight);
        canvas.drawRect(rectF,mButtonPaint);


    }

    int downX,downY;
    boolean startMoving;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (action){
            case MotionEvent.ACTION_DOWN:
                if(x< buttonWidth && y>bitmapHeight){
                    downX = x;
                    downY = y;
                    startMoving = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(startMoving){
                    buttonX = x - downX;
                    if(buttonX > bitmapWidth - buttonWidth){
                        buttonX = bitmapWidth - buttonWidth;
                    }else if(buttonX < 0){
                        buttonX = 0;
                    }
                    invalidate();
                }

                break;
            case MotionEvent.ACTION_UP:
                if(mResultCallback != null){
                    if(Math.abs(buttonX - (bitmapWidth - buttonWidth))<20){
                        mResultCallback.onSuccess();
                        Toast.makeText(getContext(),"验证通过",Toast.LENGTH_SHORT).show();
                    }else{
                        mResultCallback.onFailed();
                        Toast.makeText(getContext(),"请重试",Toast.LENGTH_SHORT).show();
                    }
                }

                startMoving = false;
                downX = 0;
                downY = 0;
                break;
        }

        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int height = bitmapHeight + buttonHeight;
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(bitmapWidth,widthMode),MeasureSpec.makeMeasureSpec(height,heightMode));

    }

    public void setResultCallback(ResultCallback callback){
        this.mResultCallback = callback;
    }

    public interface ResultCallback{

        void onSuccess();

        void onFailed();
    }
}