package nl.xs4all.pvbemmel.fretboard;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Allow user to select the scales whose notes to display, and return result to the
 * containing activity through interface ScaleSelectionListener.
 */
public class ScalesDialogFragment extends DialogFragment {

    public interface ScaleSelectionListener {
        public void handleScaleSelections(TreeMap<String,Boolean> scaleSelections);
    }
    private static final String TAG = ScalesDialogFragment.class.getSimpleName();

    private TreeMap<String,Boolean> scaleSelections;

    /**
     * Creates ScalesDialogFragment object.
     * @param scaleSelections Specifies which scales are selected; internally a copy of this
     *                        parameter is stored, so that the client and this ScalesDialogFragment
     *                        object hold separate instances.
     * @return
     */
    public static ScalesDialogFragment newInstance(
        TreeMap<String,Boolean> scaleSelections) {

        ScalesDialogFragment sdf = new ScalesDialogFragment();

        TreeMap<String,Boolean> copy = new TreeMap<String,Boolean>(scaleSelections);
        Bundle bundle = new Bundle();
        bundle.putSerializable(MainActivity.SCALE_SELECTIONS_KEY, copy);
        sdf.setArguments(bundle);

        return sdf;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i(TAG, "onCreateDialog(" + savedInstanceState + ")");
        // http://developer.android.com/guide/topics/ui/dialogs.html
        Bundle args = getArguments();
        Log.i(TAG, "getArguments(): " + args);
        Bundle bundle = args==null ? savedInstanceState : args;
        if(bundle!=null) {
            scaleSelections = (TreeMap<String, Boolean>)bundle.
                getSerializable(MainActivity.SCALE_SELECTIONS_KEY);
        }
        if(scaleSelections==null) {
            // When will this happen? Throw exception that gives some info:
            String nl = System.getProperty("line.separator");
            String msg = TAG + ": onCreateDialog(" + savedInstanceState + "):" + nl
                + "getArguments(): " + args + nl
                + "scaleSelections: " + scaleSelections;
            throw new IllegalStateException(msg);
        }
        ArrayList<String> namesAL = new ArrayList<String>();
        ArrayList<Boolean> boolAL = new ArrayList<Boolean>();
        for(Map.Entry<String,Boolean> entry : scaleSelections.entrySet()) {
            namesAL.add(entry.getKey());
            boolAL.add(entry.getValue());
        }
        final String[] namesArray = namesAL.toArray(new String[namesAL.size()]);
        final boolean[] boolsArray = new boolean[boolAL.size()];
        for(int i=0; i<boolAL.size(); ++i) {
            boolsArray[i] = boolAL.get(i);
        }

        final TreeMap<String,Boolean> pendingScaleSelections =
            new TreeMap<String,Boolean>(scaleSelections);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title
        builder.setTitle(R.string.select_scales_title)
            .setMultiChoiceItems(namesArray, boolsArray,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which,
                                        boolean isChecked) {
                        pendingScaleSelections.put(namesArray[which], isChecked);
                    }
                })
            .setPositiveButton(R.string.scales_select_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    for(Map.Entry<String,Boolean> entry : pendingScaleSelections.entrySet()) {
                        scaleSelections.put(entry.getKey(), entry.getValue());
                    }
                    ((MainActivity)getActivity()).handleScaleSelections(scaleSelections);
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
        outState.putSerializable(MainActivity.SCALE_SELECTIONS_KEY, scaleSelections);
    }
}
