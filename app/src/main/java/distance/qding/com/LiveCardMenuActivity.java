package distance.qding.com;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.os.Handler;
import android.view.WindowManager;

import com.google.android.glass.timeline.LiveCard;

/**
 * A transparent {@link Activity} displaying a "Stop" options menu to remove the {@link LiveCard}.
 */
public class LiveCardMenuActivity extends Activity {

    private static final int SET_HEIGHT = 1;
    private static final int SET_CAPTURE = 2;
    private static final String KEY_HEIGHT = "Height";
    private static final String KEY_CAPTURE = "Capture";
    private static final String TAG = "LiveCardMenuActivity";

    @Override
    public void onAttachedToWindow() {
        Log.d(TAG, "onAttachedToWindow called");
        super.onAttachedToWindow();
        // Open the options menu right away.
        // openOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu called");
        getMenuInflater().inflate(R.menu.distance_live_card, menu);
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
        switch (item.getItemId()) {
            case R.id.action_capture:
                // Change the menu item.
                Intent captureIntent = new Intent(this, CaptureMenuActivity.class);
                startActivityForResult(captureIntent, SET_CAPTURE);

                // Allow user to capture the distance and fix the live card by calling onStartCommand again with new parameters.
                Intent serviceIntent = new Intent(this, DistanceLiveCardService.class);
                serviceIntent.putExtra(KEY_CAPTURE, true);
                startService(serviceIntent);
                return true;
            case R.id.action_set_height:
                // Allow user to select height from SetHeightMenuActivity.
                Intent setHeightIntent = new Intent(LiveCardMenuActivity.this, SetHeightMenuActivity.class);
                startActivityForResult(setHeightIntent, SET_HEIGHT);
                return true;
            case R.id.action_stop:
                // Stop the service which will unpublish the live card.
                stopService(new Intent(this, DistanceLiveCardService.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        Log.d(TAG, "onOptionsMenuClosed called");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult called");

        // Grab height information from SetHeight activity and pass it to the service.
        if (resultCode != RESULT_CANCELED) {
            if (requestCode == SET_HEIGHT && data != null) {
                String height = data.getExtras().getString(KEY_HEIGHT);

                Intent serviceIntent = new Intent(this, DistanceLiveCardService.class);
                serviceIntent.putExtra(KEY_HEIGHT, height);
                startService(serviceIntent);
                Log.d(TAG, "service started with height: " + height);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * We need to call openOptionsMenu in onWindowFocusChanged instead of onResume
     * because we have to wait for the window focus to change. Otherwise we will get a
     * BadTokenException from WindowManager.
     *
     * @param hasFocusFlag
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocusFlag) {
        Log.d(TAG, "onWindowFocusChanged called");
        super.onWindowFocusChanged(hasFocusFlag);
        if (hasFocusFlag) {
            openOptionsMenu();
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume called");
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy called");
        super.onDestroy();
    }

}
