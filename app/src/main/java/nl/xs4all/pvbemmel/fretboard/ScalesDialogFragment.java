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
 * Created by Paul on 12/14/2015.
 */
public class ScalesDialogFragment extends DialogFragment {
    private static final String TAG = ScalesDialogFragment.class.getSimpleName();

    private ArrayList mSelectedItems;
    private TreeMap<String,Boolean> scaleSelections;

    public static ScalesDialogFragment newInstance(
        TreeMap<String,Boolean> scaleSelections) {

        ScalesDialogFragment sdf = new ScalesDialogFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(MainActivity.SCALE_SELECTIONS_KEY, scaleSelections);
        sdf.setArguments(bundle);

        return sdf;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i(TAG, "onCreateDialog(" + savedInstanceState + ")");
        // http://developer.android.com/guide/topics/ui/dialogs.html

        scaleSelections = (TreeMap<String,Boolean>)getArguments().
            getSerializable(MainActivity.SCALE_SELECTIONS_KEY);
//        scaleSelections = (TreeMap<String,Boolean>)savedInstanceState.getSerializable(
//            MainActivity.SCALE_SELECTIONS_KEY);

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
                }
            })
            .setNegativeButton(R.string.scales_select_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    scaleSelections = null;
                }
            });

        return builder.create();
    }

    /**
     * Returns map with name of scale, and boolean that tells whether the scale is
     * selected.
     * @return null if the dialog was cancelled.
     */
    public TreeMap<String,Boolean> getSelections() {
        Log.i(TAG, "getSelections() returns " + scaleSelections);
        return scaleSelections;
    }
}
