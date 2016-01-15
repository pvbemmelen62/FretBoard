package nl.xs4all.pvbemmel.fretboard;

import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Scale defined by tone intervals, and base note.
 */
public class Scale {

    /** Sum of intervals */
    final private static int length = 12;

    final private String name;
    final private int[] intervals;
    /** contains[note.getLocalOffset()] == true iff scale contains note */
    private boolean[] contains;
    private String baseNote;
    private ScaleDrawInfo scaleDrawInfo;

    private static ArrayList<Scale> scales = null;

    public static List<Scale> getScales() {
        if(scales==null) {
            scales = new ArrayList<Scale>();
            String baseNote = "C";

            scales.add(new Scale("Major", new int[]{2, 2, 1, 2, 2, 2, 1}, baseNote,
                new ScaleDrawInfo(Color.argb(255,255,255,0), Color.argb(255,0,0,0))));

            scales.add(new Scale("Pentatonic", new int[]{2, 2, 3, 2, 3}, baseNote,
                new ScaleDrawInfo(Color.argb(255,255,100,100), Color.argb(255,0,0,0))));

            scales.add(new Scale("Octave", new int[]{12}, baseNote,
                new ScaleDrawInfo(Color.argb(255,125,0,0), Color.argb(255,0,0,0))));

            for(Scale scale : scales) {
                scale.getScaleDrawInfo().bgPaint.setStyle(Paint.Style.FILL);
            }

            /* FretBoardView:
             *    Drawing of a scale note at a particular position occurs in scale order ,
             *    and only the first note is drawn. Therefore, sort:
             */
            Collections.sort(scales, new Comparator<Scale>() {
                @Override
                public int compare(Scale lhs, Scale rhs) {
                    return lhs.getNumberOfNotes() - rhs.getNumberOfNotes();
                }
            });
        }
        return scales;
    }
    /**
     * Scale defined by base note and successive intervals.
     * @param name name of scale
     * @param intervals intervals between notes of scale; sum of intervals must equal 12.
     * @param baseNote one of Note.getLocalNames() , i.e. without octave number.
     * @throws IllegalArgumentException if sum of intervals not equals 12.
     */
    public Scale(String name, int[] intervals, String baseNote, ScaleDrawInfo scaleDrawInfo) {
        this.name = name;
        this.intervals = intervals;
        this.baseNote = baseNote;
        checkIntervals(intervals);
        int localOffset = Note.getLocalOffsets().get(baseNote);
        contains = new boolean[length];
        for(int i=0; i<intervals.length; ++i) {
            contains[localOffset] = true;
            localOffset = (localOffset+intervals[i])%length;
        }
        this.scaleDrawInfo = scaleDrawInfo;
    }
    public String getName() {
        return name;
    }
    /** Returns internal ScaleDrawInfo object; may be directly manipulated by the client. */
    public ScaleDrawInfo getScaleDrawInfo() {
        return scaleDrawInfo;
    }

    public String toString() {
        return "{"
            + "name:" + name
            + ", intervals:" + intervals
            + ", baseNote:" + baseNote
            + "}";
    }
    private void checkIntervals(int[] intervals) {
        int sum=0;
        for(int i=0; i<intervals.length; ++i) {
            sum += intervals[i];
        }
        if(sum != 12) {
            throw new IllegalArgumentException("intervals do not add up to 12: " + intervals);
        }
    }
    public boolean contains(Note note) {
        return contains[note.getLocalOffset()];
    }
    public boolean contains(int localOffset) {
        return contains[localOffset];
    }
    /** Iterates over those frets from startFret to endFret (inclusive) that are part of scale. */
    public Iterator<Integer> getFretIterator(Note note, int startFret, int endFret) {
        return new FretIterator(note, startFret, endFret, true);
    }
    /** Iterates starting at fret <code>startFret</code> over those frets that are part of scale).*/
    public Iterator<Integer> getFretIterator(Note note, int startFret) {
        return new FretIterator(note, startFret);
    }
    /** Returns intervals.length-1 */
    public Integer getNumberOfNotes() {
        return intervals.length;
    }

    private class FretIterator implements Iterator<Integer> {
        private Note fret0Note;
        private int startFret;
        private int endFret;
        private boolean hasEndFret;
        private int localOffset;
        private int fret;
        private boolean hasNext;
        /**
         * Finds next fret that belongs to scale.
         * @param fret0Note the note at fret 0 .
         * @param startFret fret at which to start the iteration; may or may not belong to scale.
         */
        FretIterator(Note fret0Note, int startFret) {
            this(fret0Note, startFret, 0, false);
        }

        /**
         * Finds next fret that belongs to scale.
         * @param fret0Note the note at fret 0 .
         * @param startFret fret at which to start the iteration; may or may not belong to scale.
         * @param endFret last fret to consider
         * @param hasEndFret if false then use infinity rather than endFret
         */
        FretIterator(Note fret0Note, int startFret, int endFret, boolean hasEndFret) {
            this.fret0Note = fret0Note;
            this.startFret = startFret;
            this.endFret = endFret;
            this.hasEndFret = hasEndFret;
            // step one back, and then call next() to initialize.
            localOffset = normalizeOffset(fret0Note.getLocalOffset()+startFret-1);
            fret = startFret-1;
            hasNext = true;
            next();
        }
        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public Integer next() {
            if(!hasNext) {
                throw new NoSuchElementException();
            }
            int rv = fret;
            do {
                ++fret;
                ++localOffset;
                localOffset %= length;
            }
            while(!contains[localOffset]);
            hasNext = !hasEndFret || fret<=endFret;
            return rv;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        /** Apply "mod length" arithmetic to get localOffset in range of [0,length) */
        private int normalizeOffset(int localOffset) {
            while(localOffset<0) {
                localOffset += length;
            }
            localOffset %= length;
            return localOffset;
        }
    }
}
