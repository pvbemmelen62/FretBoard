package nl.xs4all.pvbemmel.fretboard;

import android.content.Context;
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
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity
        implements ScalesDialogFragment.ScaleSelectionListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String SCALE_SELECTIONS_KEY = "scaleSelectionsKey";
    private FretboardView fretboardView;
    private OrientationEventListener oel;
    private TreeMap<String,Boolean> scaleSelections;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate(" + savedInstanceState + ")");
        super.onCreate(savedInstanceState);
        scaleSelections = null;
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
        setContentView(R.layout.activity_main);
        fretboardView = (FretboardView)findViewById(R.id.fretboard);
        fretboardView.removeScales();
        fretboardView.addScales(scaleSelections);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        oel = new MyOrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void handleScaleSelections(TreeMap<String, Boolean> scaleSelections) {
        if (scaleSelections != null) {
            this.scaleSelections.clear();
            for (Map.Entry<String, Boolean> entry : scaleSelections.entrySet()) {
                this.scaleSelections.put(entry.getKey(), entry.getValue());
            }
            fretboardView.removeScales();
            for(Scale scale : Scale.getScales()) {
                if(scaleSelections.get(scale.getName())) {
                    fretboardView.addScale(scale);
                }
            }
            fretboardView.invalidate();
        }
    }

    private class MyOrientationEventListener extends OrientationEventListener {
        int orientationRounded = -1;

        public MyOrientationEventListener(Context context, int rate) {
            super(context, rate);
        }
        @Override
        public void onOrientationChanged(int orientation) {
//              Log.i(TAG,
//                  "Orientation changed to " + orientation);
            validateIsOnUIThread();
                /* Orientation Rounded New */
            int orNew=-1;
            if(orientation<0) {
                // indeterminate orientation: do nothing.
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
                fretboardView.invalidate();
            }
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState(" + outState + ")");
        super.onSaveInstanceState(outState);
        Bundle bundle = new Bundle();
        bundle.putSerializable(SCALE_SELECTIONS_KEY, scaleSelections);
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
    }

}
