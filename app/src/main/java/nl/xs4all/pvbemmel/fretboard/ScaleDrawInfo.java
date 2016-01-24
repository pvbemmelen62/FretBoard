package nl.xs4all.pvbemmel.fretboard;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import java.util.HashMap;

/**
 * Created by Paul on 1/15/2016.
 */
public class ScaleDrawInfo {
    public Paint bgPaint;
    public Paint textPaint;

    private static HashMap<String,ScaleDrawInfo> sdiMap = null;

    public static HashMap<String,ScaleDrawInfo> getScaleDrawInfoMap() {
        if(sdiMap==null) {
            sdiMap = new HashMap<>();
            sdiMap.put("Major",
                new ScaleDrawInfo(Color.argb(255, 255, 255, 0), Color.argb(255,0,0,0)));

            sdiMap.put("Pentatonic",
                new ScaleDrawInfo(Color.argb(255,255,100,100), Color.argb(255,0,0,0)));

            sdiMap.put("Root note",
                new ScaleDrawInfo(Color.argb(255,125,0,0), Color.argb(255,0,0,0)));

            for(ScaleDrawInfo sdi : sdiMap.values()) {
                sdi.bgPaint.setStyle(Paint.Style.FILL);
            }
        }
        return sdiMap;
    }
    public ScaleDrawInfo(int bgColor, int textColor) {
        bgPaint = new Paint();
        bgPaint.setColor(bgColor);
        textPaint = new Paint();
        textPaint.setColor(textColor);
        Typeface typeface= Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
        textPaint.setTypeface(typeface);
        //paintText.setTypeface(Typeface.SANS_SERIF);
        textPaint.setTextSize(20);
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
