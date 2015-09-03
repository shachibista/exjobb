package kth.exjobb.autodermo;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import com.biowink.clue.ArcUtils;

/**
 * Builds the camera grid.
 */

public class CameraGrid extends View {
    private Paint paint;

    public CameraGrid(Context context) {
        super(context);

        paint = new Paint();
    }

    public CameraGrid(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
    }

    public CameraGrid(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        paint = new Paint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        // set aspect ratio of 1:1
        setMeasuredDimension(width, width);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);

        drawGrid(canvas, paint);

        // draws a white-on-black stroke for visibility
        // across a varying background
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3);

        drawGrid(canvas, paint);
    }

    /**
     * Draws grid, see report for specification
     * @param canvas
     * @param paint
     */
    private void drawGrid(Canvas canvas, Paint paint) {
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();

        float radius = (float) (width * 0.25);

        float cx = width / 2;
        float cy = height / 2;

        int arcMargin = 5;

        int end = 90 - 2 * arcMargin;

        ArcUtils.drawArc(canvas, new PointF(cx, cy), radius, arcMargin, end, paint);
        ArcUtils.drawArc(canvas, new PointF(cx, cy), radius, 90 + arcMargin, end, paint);
        ArcUtils.drawArc(canvas, new PointF(cx, cy), radius, 180 + arcMargin, end, paint);
        ArcUtils.drawArc(canvas, new PointF(cx, cy), radius, 270 + arcMargin, end, paint);

        canvas.drawCircle(cx, cy, (float) (width * 0.125), paint);

        canvas.drawLine(0, cy, cx - radius, cy, paint);
        canvas.drawLine(cx + radius, cy, width, cy, paint);

        canvas.drawLine(cx, 0, cx, cy - radius, paint);
        canvas.drawLine(cx, cy + radius, cx, height, paint);

        float crossHairLength = (float) (width * 0.01);

        canvas.drawLine(cx - crossHairLength, cy, cx + crossHairLength, cy, paint);
        canvas.drawLine(cx, cy - crossHairLength, cx, cy + crossHairLength, paint);
    }

    /**
     * Draws a rule-of-thirds grid
     * @param canvas
     * @param paint
     */
    @Deprecated
    private void drawThirds(Canvas canvas, Paint paint){
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();

        int lowerThird = width / 3;
        int upperThird = lowerThird * 2;

        canvas.drawLine(lowerThird, 0, lowerThird, height, paint);
        canvas.drawLine(upperThird, 0, upperThird, height, paint);

        canvas.drawLine(0, lowerThird, width, lowerThird, paint);
        canvas.drawLine(0, upperThird, width, upperThird, paint);
    }
}
