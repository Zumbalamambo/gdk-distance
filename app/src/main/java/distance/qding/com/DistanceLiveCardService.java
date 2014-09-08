package distance.qding.com;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.SurfaceView;
import android.widget.TextView;

/**
 * A {@link Service} that publishes a {@link LiveCard} in the timeline.
 */
public class DistanceLiveCardService extends Service {

    private static final String LIVE_CARD_TAG = "DistanceLiveCardService";
    private static final String KEY_HEIGHT = "Height";

    private LiveCard mLiveCard;

    private SensorManager mSensorManager;
    private Sensor mAccSensor;
    private Sensor mMagnetSensor;

    private String mHeight;

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
        mHeight = intent.getStringExtra(KEY_HEIGHT);
        if (mHeight == null) {
            mHeight = "175";
        }

        if (mLiveCard == null) {
            mLiveCard = new LiveCard(this, LIVE_CARD_TAG);

            LiveCardRenderer renderer = new LiveCardRenderer(this, mHeight);

            mSensorManager.registerListener(renderer, mAccSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(renderer, mMagnetSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);

            mLiveCard.setDirectRenderingEnabled(true).getSurfaceHolder().addCallback(renderer);

            // Display the options menu when the live card is tapped.
            Intent menuIntent = new Intent(this, LiveCardMenuActivity.class);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
            mLiveCard.attach(this);
            mLiveCard.publish(PublishMode.REVEAL);
        } else {
            mLiveCard.navigate();
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
