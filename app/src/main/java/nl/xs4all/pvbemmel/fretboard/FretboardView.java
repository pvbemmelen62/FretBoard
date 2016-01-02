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
import android.view.OrientationEventListener;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Paul on 11/29/2015.
 */
public class FretboardView extends View {

    private static final String TAG = FretboardView.class.getSimpleName();
    //private Paint mPaint;
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
//        mPaint = new Paint();
//        mPaint.setColor(Color.BLACK);
        margin = 30;
        scales = new ArrayList<Scale>();
        matrix = null;
        drawCount = 0;
        countPaint = new Paint();
        countPaint.setColor(Color.BLACK);
        countPaint.setTypeface(Typeface.SANS_SERIF);
        countPaint.setTextSize(20);
        orientationRounded = OrientationEventListener.ORIENTATION_UNKNOWN;
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
    /** Matrix maps (fret,string) to (x,y)
     *  String 0 is the lowest (=bass) string.
     *  For horizontal fretboard: base E string is at bottom, fret 0 at left.
     *  For vertical fretboard: base E string is at left, fret 0 at top.
     */
    private Matrix calcMatrix(Canvas canvas) {
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
        return matrix;
    }

    protected void onDraw(Canvas canvas) {
        Log.i(TAG, "onDraw(Canvas canvas)");
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        matrix = calcMatrix(canvas);
        ArrayList<Point> points = new ArrayList<Point>();
        float[] dst = new float[2];
        for(int string=0; string < tuning.getNumberOfStrings(); ++string) {
            for(int fret : new int[] {0,12}) {
                matrix.mapPoints(dst, new float[] { fret, string} );
                points.add(new Point((int)dst[0], (int)dst[1]));
            }
        }
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
        paint.setStrokeWidth(5);
        canvas.drawLines(coords, paint);

        drawAxisMarkers(canvas);

        drawScales(canvas);

        ++drawCount;
        drawCount(canvas);
    }
    private void drawCount(Canvas canvas) {
        canvas.drawText(""+drawCount, 100,100, countPaint);
    }

    public void setOrientationRounded(int orientationRounded) {
        if(this.orientationRounded == orientationRounded) {
            return;
        }
        this.orientationRounded = orientationRounded;
        invalidate();
    }

    class Position implements Comparable<Position> {
        public float string;
        public float fret;
        public Position(int string, int fret) {
            this.string = string;
            this.fret = fret;
        }
        public String toString() {
            return "(string:" + string + ", fret:" + fret + ")";
        }
        public boolean equals(Position position) {
            if(position==null) {
                return false;
            }
            Position that = position;
            return this.string==that.string && this.fret==that.fret;
        }
        @Override
        public int compareTo(Position position) {
            Position that = position;
            if(that==null) {
                throw new NullPointerException();
            }
            if(this.string != that.string) {
                return this.string < that.string ? -1 : 1;
            }
            if(this.fret != that.fret) {
                return this.fret < that.fret ? -1 : 1;
            }
            return 0;
        }
    }
    class PositionInfo {
        public Scale scale;
        public Note note;
        PositionInfo(Scale scale, Note note) {
            this.scale = scale;
            this.note = note;
        }
        public String toString() {
            return "(scale:"+scale.getName() + ", note:"+note + ")";
        }
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

    /**
     * Returns per position, a sorted map with PositionInfo objects.
     * A position may have multiple PositionInfo objects, because a position may be part of a major
     * scale, and at the same time also of a pentatonic scale, and also of an octave scale.
     * The sorted map for such a position will contains keys 1, 5, 7 .
     */
    private TreeMap<Position,TreeMap<Integer,PositionInfo>> getPositionInfoMap() {
        TreeMap<Position,TreeMap<Integer,PositionInfo>> posMap =
            new TreeMap<Position,TreeMap<Integer,PositionInfo>>();
        for(Scale scale : scales) {
            Log.i(TAG, "adding positions for scale " + scale.getName());
            List<Position> positions = getFretboardPositions(scale, 0, 12);
            for (Position pos : positions) {
                TreeMap<Integer, PositionInfo> piMap = posMap.get(pos);
                if (piMap == null) {
                    piMap = new TreeMap<Integer, PositionInfo>();
                    posMap.put(pos, piMap);
                }
                Note note = tuning.getNote((int) pos.string, (int) pos.fret);
                piMap.put(scale.getNumberOfNotes(), new PositionInfo(scale, note));
            }
        }
        return posMap;
    }
    private void drawScales(Canvas canvas) {
        Paint paintText = new Paint();
        paintText.setColor(Color.BLACK);
        paintText.setTypeface(Typeface.SANS_SERIF);
        paintText.setTextSize(20);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        TreeMap<Position,TreeMap<Integer,PositionInfo>> posMap = getPositionInfoMap();
        int r = 20;
        for(Map.Entry<Position,TreeMap<Integer,PositionInfo>> entry : posMap.entrySet()) {
            Position pos = entry.getKey();
            TreeMap<Integer, PositionInfo> piMap = entry.getValue();
            // piMap entries are in ascending order, so first entry is for scale with lowest
            // number of notes.
            Map.Entry<Integer, PositionInfo> entry2 = piMap.firstEntry();
            int numberOfNotes = entry2.getKey();
            Note note = entry2.getValue().note;
            //
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
            int color = 0;
            if (numberOfNotes == 7) {
                color = Color.argb(255, 255, 255, 0);
            }
            else if (numberOfNotes == 5) {
                color = Color.argb(255, 255, 100, 100);
            }
            else if (numberOfNotes == 1) {
                color = Color.argb(255, 125, 0, 0);
            }
            else {
                throw new IllegalStateException();
            }
            paint.setColor(color);
            canvas.drawOval(new RectF(left, top, right, bottom), paint);

            float degrees = -90 * orientationRounded;
            canvas.save();
            canvas.rotate(degrees, x, y);
            drawCentered(canvas, paintText, note.getLocalName(), x, y);
            canvas.restore();
            //canvas.drawText(note.getLocalName(), left, bottom, paintText);
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
    private void drawCentered(Canvas canvas, Paint paint, String text, float cx, float cy) {
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
