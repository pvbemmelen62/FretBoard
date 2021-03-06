package nl.xs4all.pvbemmel.fretboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.OrientationEventListener;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String SCALE_SELECTIONS_KEY = "scaleSelectionsKey";
    public static final String FONT_ROTATION_CORRECTION_KEY = "fontRotationCorrectionKey";
    public static final String BASE_NOTE_KEY = "baseToneKey";
    public static final String AXIS_MARKERS_SHOW_KEY = "axisMarkerKey";
    private FretboardView fretboardView;
    private OrientationEventListener oel;
    private TreeMap<String,Boolean> scaleSelections;
    /** For storing in SharedPreferences */
    private Set<String> scaleSelectionNames;
    private Integer fontRotationCorrection;
    private String baseNote;
    private Boolean axisMarkerShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate(" + savedInstanceState + ")");
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPrefs = getPreferences(Context.MODE_PRIVATE);
        //-----------------------------------------------------------------------------------
        scaleSelections = null;
        // You can't save TreeMap<String,Boolean> in SharedPreferences.
        // So instead, save the strings for which the boolean is true.
        scaleSelectionNames = sharedPrefs.getStringSet(SCALE_SELECTIONS_KEY, null);
        if(scaleSelectionNames!=null) {
            scaleSelections = new TreeMap<String,Boolean>();
            for(Scale scale : Scale.getScales()) {
                String scaleName = scale.getName();
                scaleSelections.put(scaleName, scaleSelectionNames.contains(scaleName));
            }
        }
        if(savedInstanceState!=null) {
            Bundle bundle = savedInstanceState;
            Serializable ser = bundle.getSerializable(SCALE_SELECTIONS_KEY);
            if(ser!=null) {
                scaleSelections = (TreeMap<String, Boolean>) ser;
            }
        }
        if(scaleSelections==null) {
            scaleSelections = new TreeMap<String,Boolean>();
            for(Scale scale : Scale.getScales()) {
                scaleSelections.put(scale.getName(), Boolean.TRUE);
            }
        }
        //-----------------------------------------------------------------------------------
        fontRotationCorrection = 0;
        int fontRotationCorrectionPref = sharedPrefs.getInt(FONT_ROTATION_CORRECTION_KEY, -1);
        Log.i(TAG, "SharedPreferences getInt("+FONT_ROTATION_CORRECTION_KEY + ", -1) returns "
            + fontRotationCorrectionPref);
        if(fontRotationCorrectionPref != -1) {
            fontRotationCorrection = fontRotationCorrectionPref;
        }
        if(savedInstanceState!=null) {
            fontRotationCorrection = savedInstanceState.getInt(FONT_ROTATION_CORRECTION_KEY,
                fontRotationCorrection);
        }
        //-----------------------------------------------------------------------------------
        baseNote = sharedPrefs.getString(BASE_NOTE_KEY, "C");
        if(savedInstanceState!=null) {
            baseNote = savedInstanceState.getString(BASE_NOTE_KEY, baseNote);
        }
        //-----------------------------------------------------------------------------------
        axisMarkerShow = sharedPrefs.getBoolean(AXIS_MARKERS_SHOW_KEY, true);
        if(savedInstanceState!=null) {
            axisMarkerShow = savedInstanceState.getBoolean(AXIS_MARKERS_SHOW_KEY, axisMarkerShow);
        }
        //-----------------------------------------------------------------------------------

        setContentView(R.layout.activity_main);

        fretboardView = (FretboardView)findViewById(R.id.fretboard);
        fretboardView.setScaleSelections(scaleSelections);
        fretboardView.setFontRotationCorrection(fontRotationCorrection);
        fretboardView.setBaseNote(baseNote);
        fretboardView.setAxisMarkersShow(axisMarkerShow);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        oel = new MyOrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL);
    }
    public void handleScaleSelections(TreeMap<String, Boolean> scaleSelections) {
        Log.i(TAG, "handleScaleSelections(" + scaleSelections + ")");
        // Is called from dialog, not from FretboardView
        if (scaleSelections != null) {
            this.scaleSelections.clear();
            this.scaleSelections.putAll(scaleSelections);
            fretboardView.setScaleSelections(this.scaleSelections);
            fretboardView.invalidate();
        }
    }
    public void handleFontRotationCorrection(int fontRotationCorrection) {
        // is called from dialog, not from FretboardView
        Log.i(TAG, "handleFontRotationCorrection(" + fontRotationCorrection + ")");
        if(fontRotationCorrection==fretboardView.getFontRotationCorrection()) {
            return;
        }
        this.fontRotationCorrection = fontRotationCorrection;
        fretboardView.setFontRotationCorrection(fontRotationCorrection);
        fretboardView.invalidate();
    }
    public void handleBaseNote(String baseNote) {
        // is called from FretboardView
        Log.i(TAG, "handleBaseNote(" + baseNote + ")");
        if(!this.baseNote.equals(baseNote)) {
            this.baseNote = baseNote;
        }
    }

    public void handleAxisMarkersShow(Boolean axisMarkersShow) {
        // is called from dialog
        Log.i(TAG, "handleAxisMarkersShow(" + axisMarkersShow + ")");
        if(this.axisMarkerShow != axisMarkersShow) {
            this.axisMarkerShow = axisMarkersShow;
            fretboardView.setAxisMarkersShow(axisMarkersShow);
            fretboardView.invalidate();
        }
    }

    private class MyOrientationEventListener extends OrientationEventListener {
        /**
         * 0 : for orientation in [0,45)  or in [315,360)
         * 1 : for orientation in [45,135)
         * 2 : for orientation in [135,225)
         * 3 : for orientation in [225,315)
         */
        int orientationRounded = OrientationEventListener.ORIENTATION_UNKNOWN;

        public MyOrientationEventListener(Context context, int rate) {
            super(context, rate);
        }
        @Override
        public void onOrientationChanged(int orientation) {
//              Log.i(TAG,
//                  "Orientation changed to " + orientation);
            validateIsOnUIThread();
                /* Orientation Rounded New */
            int orNew = OrientationEventListener.ORIENTATION_UNKNOWN;
            if(orientation<0) {
                // indeterminate orientation: do nothing.
                return;
            }
            else if(orientation<0+45) {
                orNew = 0;
            }
            else if(orientation<90+45) {
                orNew = 1;
            }
            else if(orientation<180+45) {
                orNew = 2;
            }
            else if(orientation<270+45) {
                orNew = 3;
            }
            else {
                orNew = 0;
            }
            if(orNew!=orientationRounded) {
                int orOld = orientationRounded;
                orientationRounded = orNew;
                Log.i(TAG,
                    "orientationRounded: " + orOld + " -> " + orientationRounded);
                fretboardView.setOrientationRounded(orientationRounded);
            }
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState(" + outState + ")");
        super.onSaveInstanceState(outState);
        Bundle bundle = new Bundle();
        bundle.putSerializable(SCALE_SELECTIONS_KEY, scaleSelections);
        bundle.putInt(FONT_ROTATION_CORRECTION_KEY, fontRotationCorrection);
        bundle.putString(BASE_NOTE_KEY, baseNote);
        bundle.putBoolean(AXIS_MARKERS_SHOW_KEY, axisMarkerShow);
    }

    private void validateIsOnUIThread() {
        if (Looper.getMainLooper().equals(Looper.myLooper())) {
            // okay.
        } else {
            throw new IllegalStateException("Not on UI thread");
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // http://developer.android.com/guide/topics/ui/menus.html
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scales_id:
                ScalesDialogFragment sdf = ScalesDialogFragment.newInstance(scaleSelections);
                sdf.show(getSupportFragmentManager(), "scalesDialogTag");
                return true;
            case R.id.font_rotation_correction_id:
                FontRotationCorrectionDialogFragment frdf = FontRotationCorrectionDialogFragment.
                    newInstance(fontRotationCorrection);
                frdf.show(getSupportFragmentManager(), "fontRotationCorrectionDialogTag");
                return true;
            case R.id.axis_markers_id:
                AxisMarkersDialogFragment amdf = AxisMarkersDialogFragment.newInstance(
                    axisMarkerShow);
                amdf.show(getSupportFragmentManager(), "axisMarkersDialogTag");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onResume() {
        super.onResume();
        Log.i(TAG, "oel.canDetectOrientation(): " + oel.canDetectOrientation());
        if (oel.canDetectOrientation()) {
            oel.enable();
        }
    }

    @Override
    protected void onPause() {
        if (oel.canDetectOrientation() == true) {
            oel.disable();
        }
        super.onPause();

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putInt(FONT_ROTATION_CORRECTION_KEY, fontRotationCorrection);
        Log.i(TAG, "SharedPreferences editor.putInt(" + FONT_ROTATION_CORRECTION_KEY + ", "
            + fontRotationCorrection + ")");

        scaleSelectionNames = new TreeSet<String>();
        for(String scaleName : scaleSelections.keySet()) {
            if(scaleSelections.get(scaleName)) {
                scaleSelectionNames.add(scaleName);
            }
        }
        editor.putStringSet(SCALE_SELECTIONS_KEY, scaleSelectionNames);
        Log.i(TAG, "SharedPreferences editor.putStringSet(" + SCALE_SELECTIONS_KEY + ", "
            + scaleSelectionNames + ")");

        editor.putString(BASE_NOTE_KEY, baseNote);

        editor.putBoolean(AXIS_MARKERS_SHOW_KEY, axisMarkerShow);

        editor.commit();
    }

}
