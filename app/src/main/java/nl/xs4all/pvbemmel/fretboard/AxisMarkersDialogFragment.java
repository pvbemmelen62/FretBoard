package nl.xs4all.pvbemmel.fretboard;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

/**
 * Created by Paul on 1/24/2016.
 */
public class AxisMarkersDialogFragment extends DialogFragment {

    private final static String TAG = AxisMarkersDialogFragment.class.getSimpleName();

    private CheckBox checkBox;

    private Boolean axisMarkersShow;

    public static AxisMarkersDialogFragment newInstance(boolean show) {
        AxisMarkersDialogFragment amdf = new AxisMarkersDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(MainActivity.AXIS_MARKERS_SHOW_KEY, show);
        amdf.setArguments(bundle);
        return amdf;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i(TAG, "onCreateDialog(" + savedInstanceState + ")");
        Bundle args = getArguments();
        Log.i(TAG, "getArguments(): " + args);
        Bundle bundle = args==null ? savedInstanceState : args;
        if(bundle!=null) {
            if(bundle.keySet().contains(MainActivity.AXIS_MARKERS_SHOW_KEY)) {
                axisMarkersShow = bundle.getBoolean(MainActivity.AXIS_MARKERS_SHOW_KEY);
            }
        }
        // http://developer.android.com/guide/topics/ui/dialogs.html
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.axis_markers_dialog, null);
        checkBox = (CheckBox)view.findViewById(R.id.showAxisMarkersCheckBox);
        checkBox.setChecked(axisMarkersShow);
        //
        builder
            .setView(view)
            .setPositiveButton(R.string.axis_markers_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    axisMarkersShow = checkBox.isChecked();
                    ((MainActivity)getActivity()).handleAxisMarkersShow(axisMarkersShow);
                }
            })
            .setNegativeButton(R.string.axis_markers_cancel, new DialogInterface.OnClickListener() {
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
