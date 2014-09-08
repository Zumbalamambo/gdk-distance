package distance.qding.com;

import com.google.android.glass.timeline.LiveCard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * A transparent {@link Activity} displaying a "Stop" options menu to remove the {@link LiveCard}.
 */
public class LiveCardMenuActivity extends Activity {

    private static final int SET_HEIGHT = 1;
    private static final String KEY_HEIGHT = "Height";
    private static final String TAG = "LiveCardMenuActivity";

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Open the options menu right away.
        openOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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

            case R.id.action_set_height:
                // Allow user to select height from SetHeightMenuActivity
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
        // Nothing else to do, finish the Activity.
        // finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult called");

        // Grab height information from SetHeight activity and pass it to the service.
        if (resultCode != RESULT_CANCELED) {
            if (requestCode == SET_HEIGHT && data != null) {
                String height = data.getExtras().getString(KEY_HEIGHT);

                Intent serviceIntent = new Intent(this, DistanceLiveCardService.class);
                Bundle extras = serviceIntent.getExtras();
                extras.putString(KEY_HEIGHT, height);
                startService(serviceIntent);
                Log.d(TAG, "service started with height: " + height);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy called");
        super.onDestroy();
    }

}
