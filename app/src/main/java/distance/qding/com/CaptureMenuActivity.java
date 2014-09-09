package distance.qding.com;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class CaptureMenuActivity extends Activity {

    private static final String TAG = "CaptureMenuActivity";

    private static final String KEY_CAPTURE = "Capture";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        // DO NOT open the options menu right away.
        // openOptionsMenu();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocusFlag) {
        Log.d(TAG, "onWindowFocusChanged called");
        super.onWindowFocusChanged(hasFocusFlag);
        if (hasFocusFlag) {
            openOptionsMenu();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.distance_capture, menu);
        return true;
    }

    /**
     * Distribute tasks for different menu items.
     * We need a Capture action to fix the distance.
     * We need a Set Height action to allow use to select the height of the glass.
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected called");
        // Release capture status.
        Intent serviceIntent = new Intent(this, DistanceLiveCardService.class);
        serviceIntent.putExtra(KEY_CAPTURE, false);

        // This will trigger onStartCommand again.
        startService(serviceIntent);

        return true;
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        finish();
    }
}
