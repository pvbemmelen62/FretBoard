package nl.xs4all.pvbemmel.fretboard;

/**
 * Created by Paul on 12/4/2015.
 */
public class Tuning {
    private String[] stringNames;
    private Note[] strings;

    /** Names of open string notes, from bass to high strings. */
    public Tuning(String[] stringNames) {
        this.stringNames = stringNames;
        strings = new Note[stringNames.length];
        for(int i=0; i<stringNames.length; ++i) {
            strings[i] = new Note(stringNames[i]);
        }
    }
    /** Open string notes, from bass to high strings. */
    public Tuning(Note[] strings) {
        this.strings = strings;
        stringNames = new String[strings.length];
        for(int i=0; i<strings.length; ++i) {
            stringNames[i] = strings[i].toString();
        }
    }
    public String getStringName(int i) {
        return stringNames[i];
    }
    public Note getStringNote(int i) {
        return strings[i];
    }
    public String toString() {
        return stringNames.toString();
    }
    public int getNumberOfStrings() {
        return strings.length;
    }
    public Note getNote(int string, int fret) {
        Note baseNote = strings[string];
        Note note = new Note(baseNote.getGlobalOffset()+fret);
        return note;
    }
}
