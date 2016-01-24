package nl.xs4all.pvbemmel.nl.xs4all.pvbemmel.test;

import java.util.Arrays;

import static java.lang.System.out;

/**
 * Created by Paul on 1/22/2016.
 */
public class TestModulo {

    public static void main(String[] args) {

        int size = 12;

        for(int i : Arrays.asList(-14, -10, -2, 0, 2, 10, 14)) {
            out.println("" + i + "%" + size + " -> " + (i%size));
        }
        out.println();
        for(int i : Arrays.asList(-14, -10, -2, 0, 2, 10, 14)) {
            String s = String.format(
                "%d<0 ? %d+(%d%%%d) : %d%%%d -> %d" ,
                i, size, i, size, i, size,
                i<0 ? size+(i%size) : i%size);
            out.println(s);
        }
    }
}
