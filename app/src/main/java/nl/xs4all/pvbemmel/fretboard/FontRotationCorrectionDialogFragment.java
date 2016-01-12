package nl.xs4all.pvbemmel.fretboard;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Paul on 1/7/2016.
 */
public class FontRotationCorrectionDialogFragment extends DialogFragment {

    public interface FontRotationListener {
        public void handleFontRotationCorrection
            (int fontRotationCorrection);
    }
    private final static String TAG = FontRotationCorrectionDialogFragment.class.getSimpleName();
    private final static ArrayList<Integer> validCorrections = new ArrayList<Integer>(Arrays.asList(
        0, 90, 180, 270));
    private final static String[] labels = new String[validCorrections.size()];
    static {
        for(int i=0; i<labels.length; ++i) {
            labels[i] = ""+ validCorrections.get(i);
        }
    }

    /** Font rotation correction, in degrees; must be one of <code>validCorrections</code>,
     *  or -1 to indicate unassigned. */
    private int fontRotationCorrection = -1;

    public static FontRotationCorrectionDialogFragment newInstance(int rotation) {

        FontRotationCorrectionDialogFragment frdf = new FontRotationCorrectionDialogFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(MainActivity.FONT_ROTATION_CORRECTION_KEY, rotation);
        frdf.setArguments(bundle);

        return frdf;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i(TAG, "onCreateDialog(" + savedInstanceState + ")");
        // http://developer.android.com/guide/topics/ui/dialogs.html
        Bundle args = getArguments();
        Log.i(TAG, "getArguments(): " + args);
        Bundle bundle = args==null ? savedInstanceState : args;
        if(bundle!=null) {
            fontRotationCorrection = bundle.getInt(MainActivity.FONT_ROTATION_CORRECTION_KEY);
        }
        if(fontRotationCorrection==-1) {
            // When will this happen? Throw exception that gives some info:
            String nl = System.getProperty("line.separator");
            String msg = TAG + ": onCreateDialog(" + savedInstanceState + "):" + nl
                + "getArguments(): " + args + nl
                + "fontRotationCorrection: " + fontRotationCorrection;
            throw new IllegalStateException(msg);
        }
        int selectedIndex = validCorrections.indexOf(fontRotationCorrection);
        if(selectedIndex==-1) {
            throw new IllegalStateException("Not a valid font rotation correction: "
                + fontRotationCorrection);
        }
        final int[] selectionIndexArray = { selectedIndex };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.font_rotation_correction_title)
            .setSingleChoiceItems(labels, selectedIndex, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    selectionIndexArray[0] = which;
                }
            })
            .setPositiveButton(R.string.scales_select_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    FontRotationListener frsl =
                        (FontRotationListener)getActivity();
                    frsl.handleFontRotationCorrection(validCorrections.get(selectionIndexArray[0]));
                }
            })
            .setNegativeButton(R.string.scales_select_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // noop
                }
            });

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState(" + outState + ")");
        super.onSaveInstanceState(outState);
    }
}
