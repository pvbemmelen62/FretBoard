package nl.xs4all.pvbemmel.fretboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Paul on 12/2/2015.
 */
public class Note {
// https://en.wikipedia.org/wiki/Scientific_pitch_notation
//
//  Octave number
//  The octave number increases by 1 upon an ascension from B to C (and not from G to A,
//  as one might expect). Thus "A4" refers to the first A above C4 (middle C). As another
//  example, in ascending the white keys on a keyboard, C4 immediately follows B3, as in
//  the following sequence: "C3 D3 E3 F3 G3 A3 B3 C4 D4"
//
//  Scientific pitch notation was originally designed as a companion to scientific pitch,
//  in which C4 was defined as exactly 256 Hz. A different standard pitch system, using A4
//  as exactly 440 Hz, had been informally adopted by the music industry as far back as 1926,
//  and A440 became the official international pitch standard (ISO 16.[3]) in 1955.

    /**
     * Each note has an offset wrt. note0 : it's the number of half notes that the note is
     * higher than note0.
     */
    private static final String note0 = "C0";
    private static Map<String, Integer> localOffsets = null;
    private static ArrayList<String> localNames = null;

    /** Invariant: globalOffset = octaveNumber*12 + localOffset */
    private int globalOffset;
    private int localOffset;
    private int octaveNumber;

    /**
     * Offset of note within its octave, with C having local offset 0.
     * Sharp notes are present, flat notes not. E.g. "C#" is present, but "Db" not.
     */
    public static Map<String, Integer> getLocalOffsets() {
        if (localOffsets == null) {
            localOffsets = new HashMap<String, Integer>();
            List<String> names = Arrays.asList("C", "D", "E", "F", "G", "A", "B");
            List<Integer> steps = Arrays.asList(2, 2, 1, 2, 2, 2, 1);
            int number = 0;
            for (int iName = 0; iName < names.size(); ++iName) {
                String name = names.get(iName);
                int step = steps.get(iName);
                if (step == 1) {
                    localOffsets.put(name, number++);
                }
                else {
                    localOffsets.put(name, number++);
                    localOffsets.put(name + "#", number++);
                }
            }
        }
        return localOffsets;
    }
    /** getLocalNames().get(0) = "C"   , ... , getLocalNames().get(11) = "B"  */
    public static ArrayList<String> getLocalNames() {
        if(localNames==null) {
            Map<String,Integer> localOffsets = getLocalOffsets();
            localNames = new ArrayList<String>(Collections.nCopies(localOffsets.size(), ""));
            for(Map.Entry<String,Integer> entry : localOffsets.entrySet()) {
                localNames.set(entry.getValue(), entry.getKey());
            }
        }
        return localNames;
    }

    /**
     * Get offset of note wrt C0 , i.e. the distance in number of half notes.
     *
     * @param note Must have format &lt;letter&gt;[#]&lt;number&gt; ; i.e. hash sigh is optional.
     * @return
     */
    public static int getGlobalOffset(String note) {
        int len = note.length();
        int octave = Integer.parseInt(note.substring(len-1,len));
        String localNote = note.substring(0, len - 1);
        Map<String, Integer> offsets = getLocalOffsets();
        Integer localOffset = offsets.get(localNote);
        if (localOffset == null) {
            throw new IllegalArgumentException("Not found: " + note);
        }
        return octave * 12 + localOffset;
    }
    public Note(int globalOffset) {
        setGlobalOffset(globalOffset);
    }
    public Note(String globalName) {
        setGlobalOffset(getGlobalOffset(globalName));
    }
    public int getOctaveNumber() {
        return octaveNumber;
    }
    /** Offset of note within its octave; for C it is 0 . */
    public int getLocalOffset() {
        return localOffset;
    }
    /** Offset with respect to C0 */
    public int getGlobalOffset() {
        return globalOffset;
    }
    /** Sets global offset, and updates local offset and octave number */
    public void setGlobalOffset(int globalOffset) {
        this.globalOffset = globalOffset;
        localOffset = globalOffset % 12;
        octaveNumber = globalOffset / 12;
    }
    /** local name + octave number */
    public String toString() {
        String rv = getLocalNames().get(localOffset) + octaveNumber;
        return rv;
    }
    public String getLocalName() {
        String rv = getLocalNames().get(localOffset);
        return rv;
    }
}
