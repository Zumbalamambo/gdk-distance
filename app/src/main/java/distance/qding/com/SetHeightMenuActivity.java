package distance.qding.com;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class SetHeightMenuActivity extends Activity {

    private static final String TAG = "SetHeightMenuActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Log.d(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Open the options menu right away.
        openOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.distance_height, menu);
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
        // Log.d(TAG, "onOptionsItemSelected called");
        // getTitle returns CharSequence. We need to convert it to string and then parse to int.
        String height = item.getTitle().toString();

        // Send height information back to LiveCardMenuActivity.
        Intent returnIntent = new Intent();
        returnIntent.putExtra("Height", height);
        setResult(RESULT_OK, returnIntent);
        // Log.d(TAG, "set height to " + height);
        // finish();

        return true;
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        Log.d(TAG, "onOptionsMenuClosed called");
        super.onOptionsMenuClosed(menu);
        // Nothing else to do, finish the Activity.
        finish();
    }
}
