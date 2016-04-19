package com.it.spot.maps;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.telecom.Call;

/**
 * Created by teo on 13.04.2016.
 */
public class CircleReportSpotDialogDrawable extends Drawable {

    private Paint paint;
    private RectF rectF;
    private int color;

    public CircleReportSpotDialogDrawable(int color) {
        this.color = color;
        paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        rectF = new RectF();
    }

    public int getColor() {
        return color;
    }

    /**
     * A 32bit color not a color resources.
     * @param color
     */
    public void setColor(int color) {
        this.color = color;
        paint.setColor(color);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();

        Rect bounds = getBounds();
        rectF.set(bounds);


//        Path path = new Path();
//        path.cubicTo(bounds.left, 150, bounds.right / 5, 0,bounds.right, 50);
//        path.moveTo(0,150);
//        path.cubicTo(bounds.right * 2.5f / 5.0f, 30.0f, bounds.right * 3.5f / 5.0f, 0.0f, bounds.right, 50.0f);
//        path.lineTo(bounds.right,bounds.bottom);
//        path.lineTo(bounds.left,bounds.bottom);
//        path.addArc(0,150,bounds.right,100,20,120);
//        path.addOval(0,0,100,100, Path.Direction.CW);
//        path.addCircle((float)bounds.right * 2.0f / 3,bounds.bottom,bounds.bottom * 1.5f, Path.Direction.CCW);
//        canvas.drawPath(path, paint);
//        canvas.drawArc(rectF, -120, 120, true, paint);
//        canvas.drawArc(rectF, -180, 180, true, paint);
//        canvas.drawArc(0,0,bounds.right,100,90,90,true,paint);
//        canvas.drawArc(rectF,-90,90,true,paint);
//        canvas.drawCircle(900,1100,1000,paint);
        int x = (int) (bounds.right * 0.625f);
        int r = (int) (bounds.bottom * 0.9f);
        canvas.drawCircle(x, bounds.bottom, r, paint);
    }

    @Override
    public void setAlpha(int alpha) {
        // Has no effect
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        // Has no effect
    }

    @Override
    public int getOpacity() {
        // Not Implemented
        return 0;
    }

}