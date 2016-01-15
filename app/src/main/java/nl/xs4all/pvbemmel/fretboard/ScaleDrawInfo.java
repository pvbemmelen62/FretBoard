package nl.xs4all.pvbemmel.fretboard;

import android.graphics.Paint;

/**
 * Created by Paul on 1/15/2016.
 */
public class ScaleDrawInfo {
    public Paint bgPaint;
    public Paint textPaint;

    public ScaleDrawInfo(int bgColor, int textColor) {
        bgPaint = new Paint();
        bgPaint.setColor(bgColor);
        textPaint = new Paint();
        textPaint.setColor(textColor);
    }
    public ScaleDrawInfo(Paint bgPaint, Paint textPaint) {
        this.bgPaint = bgPaint;
        this.textPaint = textPaint;
    }
    public String toString() {
        return "{bgPaint:" + toString(bgPaint.getColor())
            + ", textPaint:" + toString(textPaint.getColor()) + "}";
    }
    private String toString(int color) {
        return "{" + Integer.toHexString(color) + "}";
    }
}
