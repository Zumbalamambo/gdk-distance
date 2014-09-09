package distance.qding.com;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;

/**
 * A {@link Service} that publishes a {@link LiveCard} in the timeline.
 */
public class DistanceLiveCardService extends Service {

    private static final String LIVE_CARD_TAG = "DistanceLiveCardService";
    private static final String KEY_HEIGHT = "Height";
    private static final String KEY_CAPTURE = "Capture";

    private LiveCard mLiveCard;
    private LiveCardRenderer mRenderer = new LiveCardRenderer(this);

    private SensorManager mSensorManager;
    private Sensor mAccSensor;
    private Sensor mMagnetSensor;

    // The height of the user.
    private String mHeight;

    // Flag indicating whether we need to lock the distance or not.
    private boolean isCapture;

    @Override
    public void onCreate() {
        super.onCreate();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetSensor = mSensorManager
                .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Grab height information from intent extra
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            mHeight = "175";
        } else if (bundle.getString(KEY_HEIGHT) == null) {
            mHeight = "175";
        } else {
            mHeight = bundle.getString(KEY_HEIGHT);
        }

        isCapture = intent.getBooleanExtra(KEY_CAPTURE, false);
        // Log.d(LIVE_CARD_TAG, "mHeight: " + mHeight + "; isCapture: " + isCapture);

        mRenderer.setHeight(mHeight);
        mRenderer.setCapture(isCapture);

        if (mLiveCard == null) {
            mLiveCard = new LiveCard(this, LIVE_CARD_TAG);

            mSensorManager.registerListener(mRenderer, mAccSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(mRenderer, mMagnetSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);

            mLiveCard.setDirectRenderingEnabled(true).getSurfaceHolder().addCallback(mRenderer);

            // Display the options menu when the live card is tapped.
            Intent menuIntent = new Intent(this, LiveCardMenuActivity.class);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
            mLiveCard.attach(this);
            mLiveCard.publish(PublishMode.REVEAL);
        } else {
            if (!mLiveCard.isPublished()) {
                mLiveCard.navigate();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mLiveCard != null && mLiveCard.isPublished()) {
            mLiveCard.unpublish();
            mLiveCard = null;
        }
        super.onDestroy();
    }
}
