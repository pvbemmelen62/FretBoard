package nl.xs4all.pvbemmel.fretboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by Paul on 11/29/2015.
 */
public class FretboardView extends View {

    private static final String TAG = FretboardView.class.getSimpleName();

    private HashMap<String,ScaleDrawInfo> scaleDrawInfoMap;

    private int margin;
    private final int fretStart = 0;
    private final int fretEnd = 12;
    private final Tuning tuning = new Tuning(new String[] { "E2","A2","D3","G3","B3","E4"} );
    private final int stringStart = 0;
    /** inclusive */
    private int stringEnd = tuning.getNumberOfStrings()-1;
    /** long length/short length */
    private final double fretboardRatio = 4;
    private List<Scale> scales;
    private Matrix matrix;
    private int drawCount;
    private Paint countPaint;
    /** Most recent MotionEvent.ACTION_[POINTER_]DOWN as recorded by OnTouchListener */
    private float xDown;
    private float yDown;
    /** Most recent MotionEvent.ACTION_[POINTER_]UP as recorded by OnTouchListener */
    private float xUp;
    private float yUp;
    /** Most recent MotionEvent.ACTION_MOVE as recorded by OnTouchListener */
    private float xMove;
    private float yMove;
    private ArrayList<Integer> fretNumbers;

    private int fontRotationCorrection;
    /**
     * See MainActivity.MyOrientationEventListener.orientationRounded .
     */
    private int orientationRounded;


    public FretboardView(Context context) {
        super(context);
        init(context);
    }

    public FretboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FretboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    protected void init(Context ctx) {
        margin = 30;
        scales = new ArrayList<Scale>();
        scaleDrawInfoMap = new HashMap<String,ScaleDrawInfo>();
        matrix = null;
        drawCount = 0;
        countPaint = new Paint();
        countPaint.setColor(Color.BLACK);
        countPaint.setTypeface(Typeface.SANS_SERIF);
        countPaint.setTextSize(20);
        orientationRounded = OrientationEventListener.ORIENTATION_UNKNOWN;
        fontRotationCorrection = 0;
        fretNumbers = new ArrayList<Integer>(Arrays.asList(0,3,5,7,9,12,15));
        setOnClickListener(new MyOnClickListener());
        setOnLongClickListener(new MyOnLongClickListener());
        setOnTouchListener(new MyOnTouchListener());
    }
    private class MyOnClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "onClick at (" + ((int)xDown) + "," + ((int)yDown) + ")" );
        }
    }
    private class MyOnLongClickListener implements OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            Log.i(TAG, "onLongClick at (" + ((int)xDown) + "," + ((int)yDown) + ")" );
            return true;
        }
    }

    /**
     * Stores x,y coordinates of last ACTION_[POINTER_]DOWN and of last ACTION_[POINTER_]UP in
     * xDown,yDown, respectively xUp,yUp for use by the OnClickListener and OnLongClickListener.
     */
    private class MyOnTouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch(event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN: {
                    int pointerIndex = event.getActionIndex();
                    xDown = event.getX(pointerIndex);
                    yDown = event.getY(pointerIndex);
                    Log.i(TAG, "xDown,yDown <-- " + xDown + "," + yDown);
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP: {
                    int pointerIndex = event.getActionIndex();
                    xUp = event.getX(pointerIndex);
                    yUp = event.getY(pointerIndex);
                    Log.i(TAG, "xUp,yUp <-- " + xUp + "," + yUp);
                    break;
                }
                case MotionEvent.ACTION_MOVE:
                    int pointerIndex = event.getActionIndex();
                    xMove = event.getX(pointerIndex);
                    yMove = event.getY(pointerIndex);
                    Log.i(TAG, "xMove,yMove <-- " + xMove + "," + yMove);
                    break;
                default:
                    // Log.i(TAG, "Ignored MotionEvent: " + event);
            }
            return false;
        }
    }

    public int getFontRotationCorrection() {
        Log.i(TAG, "getFontRotationCorrection() will return " + fontRotationCorrection);
        return fontRotationCorrection;
    }
    public void setFontRotationCorrection(int fontRotationCorrection) {
        Log.i(TAG, "setFontRotationCorrection(" + fontRotationCorrection + ")");
        this.fontRotationCorrection = fontRotationCorrection;
    }
    public void addScale(Scale scale) {
        Log.i(TAG, "addScale(" + scale + ")");
        scales.add(scale);
    }
    public void addScales(TreeMap<String,Boolean> selections) {
        Log.i(TAG, "addScales(" + selections + ")");
        for(Scale scale : Scale.getScales()) {
            if(selections.get(scale.getName())) {
                addScale(scale);
            }
        }

    }
    public void removeScale(Scale scale) {
        Log.i(TAG, "removeScale(" + scale + ")");
        scales.remove(scale);
    }
    public void removeScale(String scaleName) {
        Log.i(TAG, "removeScale(" + scaleName + ")");
        for(int i=0; i<scales.size(); ++i) {
            if(scales.get(i).getName().equals(scaleName)) {
                scales.remove(i);
            }
        }
    }
    public void removeScales() {
        Log.i(TAG, "removeScales()");
        scales.clear();
    }

    @Override
    public void invalidate() {
        Log.i(TAG, "invalidate()");
        super.invalidate();
    }

    /**
     * Returns the list of scales, wrapped as an unmodifiable list.
     */
    public List<Scale> getScales() {
        return Collections.unmodifiableList(scales);
    }
    /** Calculates matrix that maps (fret,string) to (x,y) , and assigns it to instance variable
     *  <code>matrix</code>.
     *  <p>
     *  String 0 is the lowest (=bass) string.
     *  For horizontal fretboard: base E string is at bottom, fret 0 at left.
     *  For vertical fretboard: base E string is at left, fret 0 at top.
     *  </p>
     */
    private void calcMatrix(Canvas canvas) {
        int w = canvas.getWidth();
        int h = canvas.getHeight();

        // Fretboard horizontally or vertically so that it has maximum size.
        // The fretboard rectangle has aspect ratio fretboardRatio .
        // The fretboard rectangle has maximum size so that it has at least margin "margin" in its
        // view.
        float m = margin;
        float wm = w - 2*m;
        float hm = h - 2*m;

        /** Center x,y */
        float cx = w/2;
        float cy = h/2;

        /** Fretboard width and height */
        float wf, hf;
        float top, bottom, left, right;
        boolean horizontal = wm>hm;
        if(horizontal) {
            // horizontal fretboard
            float ratio = wm/hm;
            if(ratio > fretboardRatio) {
                // vertically restrained.
                hf = hm;
                wf = (int)(hf*fretboardRatio);
            }
            else {
                // horizontally restrained.
                wf = wm;
                hf = (int)(wf/fretboardRatio);
            }
        }
        else {
            // vertical fretboard
            float ratio = hm/wm;
            if(ratio > fretboardRatio) {
                // horizontally restrained.
                wf = wm;
                hf = (int)(wm * fretboardRatio);
            }
            else {
                // vertically restrained.
                hf = hm;
                wf = (int)(hf/fretboardRatio);
            }
        }
        left   = cx - wf/2;
        right  = cx + wf/2;
        top    = cy - hf/2;
        bottom = cy + hf/2;
        Matrix matrix = new Matrix();
        float[] fromCoords = new float[] { fretStart,stringStart, fretEnd,stringStart ,
            fretStart,stringEnd };
        float[] toCoords = null; // x,y, x,y, x,y
        if(horizontal) {
            toCoords = new float[] { left, bottom,  right, bottom,  left, top };
        }
        else {
            toCoords = new float[] { left, top,  left, bottom,  right, top };
        }
        matrix.setPolyToPoly(fromCoords, 0, toCoords, 0, 3);
        this.matrix = matrix;
    }

    protected void onDraw(Canvas canvas) {
        Log.i(TAG, "onDraw(Canvas canvas)");

        calcMatrix(canvas);

        Paint paintLines = new Paint();
        paintLines.setColor(Color.BLACK);
        paintLines.setStrokeWidth(5);

        drawStringsAndFrets(canvas, paintLines);

        Paint paintText = new Paint();
        paintText.setColor(Color.BLACK);
        paintText.setTypeface(Typeface.SANS_SERIF);
        paintText.setTextSize(20);

        drawFretNumbers(canvas, paintText);

        drawAxisMarkers(canvas);

        drawScales(canvas);

        ++drawCount;

//      drawCount(canvas);

//      drawOrientationRounded(canvas);
    }
    private void drawStringsAndFrets(Canvas canvas, Paint paintLines) {
        ArrayList<Point> points = new ArrayList<Point>();
        float[] dst = new float[2];
        // Strings:
        for(int string=0; string < tuning.getNumberOfStrings(); ++string) {
            for(int fret : new int[] {0,12}) {
                matrix.mapPoints(dst, new float[] { fret, string} );
                points.add(new Point((int)dst[0], (int)dst[1]));
            }
        }
        // Frets:
        for(int fret=0; fret<=12; ++fret) {
            for (int string : new int[]{0, tuning.getNumberOfStrings()-1}) {
                matrix.mapPoints(dst, new float[] { fret, string} );
                points.add(new Point((int)dst[0], (int)dst[1]));
            }
        }
        float[] coords = new float[points.size()*2];
        int i=0;
        for(Point point : points) {
            coords[i++] = point.x;
            coords[i++] = point.y;
        }
        canvas.drawLines(coords, paintLines);
    }
    private void drawFretNumbers(Canvas canvas, Paint paintText) {
        float[] dst = new float[2];
        float string = tuning.getNumberOfStrings()-1 + 1;
        for(int fret : fretNumbers) {
            matrix.mapPoints(dst, new float[] { fret, string});
            float x = dst[0];
            float y = dst[1];
            float degrees = -90 * orientationRounded;
            canvas.save();
            canvas.rotate(degrees + fontRotationCorrection, x, y);
            drawCenteredText(canvas, paintText, "" + fret, x, y);
            canvas.restore();
        }
    }
    private void drawCount(Canvas canvas) {
        canvas.drawText(""+drawCount, 100,100, countPaint);
    }
    private void drawOrientationRounded(Canvas canvas) {
        canvas.drawText(""+orientationRounded, 300,100, countPaint);
    }
    public void setOrientationRounded(int orientationRounded) {
        if(this.orientationRounded == orientationRounded) {
            return;
        }
        this.orientationRounded = orientationRounded;
        invalidate();
    }
    /**
     * For all strings, get fret positions in [startFret, endFret] that are part of scale.
     * @param scale
     * @param startFret
     * @param endFret
     * @return
     */
    private List<Position> getFretboardPositions(Scale scale, int startFret, int endFret) {
        ArrayList<Position> positions = new ArrayList<Position>();
        for(int string=0; string<tuning.getNumberOfStrings() ; ++string) {
            String sn = tuning.getStringName(string);
            Iterator<Integer> fretIter = scale.getFretIterator(new Note(sn), startFret, endFret);
            while(fretIter.hasNext()) {
                positions.add(new Position(string, fretIter.next()));
            }
        }
        return positions;
    }
    private void drawScales(Canvas canvas) {
        TreeSet<Position> filled = new TreeSet<Position>();
        for(Scale scale : scales) {
            drawScale(scale, filled, canvas);
        }
    }
    private void drawScale(Scale scale, TreeSet<Position> filled, Canvas canvas) {
        int r = 20;
        ScaleDrawInfo sdi = scale.getScaleDrawInfo();

        List<Position> positions = getFretboardPositions(scale, 0, 12);
        for (Position pos : positions) {
            if(filled.contains(pos)) {
                continue;
            }
            filled.add(pos);
            Note note = tuning.getNote((int) pos.string, (int) pos.fret);
            float[] src = new float[2];
            float[] dst = new float[2];
            src[0] = pos.fret;
            src[1] = pos.string;
            matrix.mapPoints(dst, src);
            float x = dst[0];
            float y = dst[1];
            float left = x - r;
            float top = y - r;
            float right = x + r;
            float bottom = y + r;

            canvas.drawOval(new RectF(left, top, right, bottom), sdi.bgPaint);

            float degrees = -90 * orientationRounded;
            canvas.save();
            canvas.rotate(degrees+fontRotationCorrection, x, y);
            drawCenteredText(canvas, sdi.textPaint, note.getLocalName(), x, y);
            canvas.restore();
        }
    }
    /**
     * Draw text centered at cx, cy.
     * @param canvas
     * @param paint
     * @param text
     * @param cx
     * @param cy
     */
    private void drawCenteredText(Canvas canvas, Paint paint, String text, float cx, float cy) {
        // http://stackoverflow.com/questions/11120392/android-center-text-on-canvas
        Rect r = new Rect();
        Paint.Align pa = paint.getTextAlign();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.getTextBounds(text, 0, text.length(), r);
        float x = cx - r.width() / 2f - r.left;
        float y = cy + r.height() / 2f - r.bottom;
        canvas.drawText(text, x, y, paint);
        paint.setTextAlign(pa);
    }
    private void drawAxisMarkers(Canvas canvas) {
        int r = 20;
        float left;
        float top;
        float right;
        float bottom;

        int w = canvas.getWidth();
        int h = canvas.getHeight();

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
//        Paint.Style ps = mPaint.getStyle();
//        mPaint.setStyle(Paint.Style.FILL);
        //
        left   =  0;
        top    =  0;
        right  = +r;
        bottom = +r;
        paint.setColor(Color.RED);
        canvas.drawRect(left, top, right, bottom, paint);
        //
        left   = w-r;
        top    =   0;
        right  = w  ;
        bottom =  +r;
        paint.setColor(Color.GREEN);
        canvas.drawRect(left, top, right, bottom, paint);
        //
        left   =   0;
        top    = h-r;
        right  =  +r;
        bottom = h  ;
        paint.setColor(Color.BLUE);
        canvas.drawRect(left, top, right, bottom, paint);

//        paint.setStyle(ps);
    }
}
