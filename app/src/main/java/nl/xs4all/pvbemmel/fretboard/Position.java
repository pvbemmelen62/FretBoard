package nl.xs4all.pvbemmel.fretboard;

/**
 * Created by Paul on 1/15/2016.
 */
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
        if (position == null) {
            return false;
        }
        Position that = position;
        return this.string == that.string && this.fret == that.fret;
    }

    @Override
    public int compareTo(Position position) {
        Position that = position;
        if (that == null) {
            throw new NullPointerException();
        }
        if (this.string != that.string) {
            return this.string < that.string ? -1 : 1;
        }
        if (this.fret != that.fret) {
            return this.fret < that.fret ? -1 : 1;
        }
        return 0;
    }
}
