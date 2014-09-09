package distance.qding.com;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

import com.google.android.glass.timeline.DirectRenderingCallback;
import com.google.android.glass.timeline.LiveCard;

import java.text.DecimalFormat;

/**
 * Renders a fading "Hello world!" in a {@link LiveCard}.
 */
public class LiveCardRenderer implements DirectRenderingCallback, SensorEventListener {

    private static final String TAG = "LiveCardRenderer";
    /**
     * The duration, in millisconds, of one frame.
     */
    private static long FRAME_TIME_MILLIS = 40;

    /**
     * "Hello world" text size.
     */
    private static final float TEXT_SIZE = 70f;

    private DecimalFormat mDecimalFormat = new DecimalFormat("#.##");

    /**
     * Alpha variation per frame.
     */
    // private static final int ALPHA_INCREMENT = 5;

    /**
     * Max alpha value.
     */
    // private static final int MAX_ALPHA = 256;

    // Paint object to draw on live card canvas
    private final Paint mPaint;

    // Text to render on the live card
    private String mText;

    // Height of the user
    private String mHeight;

    // Sensor parameters to get distance
    private float[] mGravity;
    private float[] mGeoMagnetic;

    // private float mAzimuth;
    private float mPitch;
    // private float mRoll;

    // Distance from camera to object.
    private float mDistance;

    // Lock distance for captured display.
    private float mDistanceLocked;

    // Flag indicating whether we need to lock the distance or not.
    private boolean isCapture;

    private int mCenterX;
    private int mCenterY;

    private SurfaceHolder mHolder;
    private boolean mRenderingPaused;

    private RenderThread mRenderThread;

    public LiveCardRenderer(Context context) {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(TEXT_SIZE);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));
        mPaint.setAlpha(100);
    }

    public void setHeight(String height) {
        // set mHeight
        mHeight = height;
        Log.d(TAG, "mHeight: " + mHeight);
    }

    public void setCapture(boolean capture) {
        // set capture flag
        isCapture = capture;
        Log.d(TAG, "isCapture: " + isCapture);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCenterX = width / 2;
        mCenterY = height / 2;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        mRenderingPaused = false;
        updateRenderingState();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder = null;
        updateRenderingState();
    }

    @Override
    public void renderingPaused(SurfaceHolder holder, boolean paused) {
        mRenderingPaused = paused;
        updateRenderingState();
    }

    /**
     * Starts or stops rendering according to the {@link LiveCard}'s state.
     */
    private void updateRenderingState() {
        boolean shouldRender = (mHolder != null) && !mRenderingPaused;
        boolean isRendering = (mRenderThread != null);

        if (shouldRender != isRendering) {
            if (shouldRender) {
                mRenderThread = new RenderThread();
                mRenderThread.start();
            } else {
                mRenderThread.quit();
                mRenderThread = null;
            }
        }
    }

    /**
     * Draws the view in the SurfaceHolder's canvas.
     */
    private void draw() {
        Canvas canvas;
        try {
            canvas = mHolder.lockCanvas();
        } catch (Exception e) {
            return;
        }
        if (canvas != null) {
            getDistance();

            // If in capture status change refresh rate to infinite.
            if (isCapture) {
                FRAME_TIME_MILLIS = Long.MAX_VALUE;
            } else {
                FRAME_TIME_MILLIS = 40;
            }

            // Clear the canvas.
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            // Update the text alpha and draw the text on the canvas.
            // mPaint.setAlpha((mPaint.getAlpha() + ALPHA_INCREMENT) % MAX_ALPHA);
            canvas.drawText(mText, mCenterX, mCenterY, mPaint);

            // Unlock the canvas and post the updates.
            mHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void getDistance() {
        // Calculate distance based on sensor output
        mDistance = Math.abs((float) ((Integer.parseInt(mHeight) - 10) * Math.tan(mPitch * Math.PI / 180)));

        // Convert distance to string
        mText = mDecimalFormat.format(mDistance);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Log.d(TAG, "onSensorChanged called");
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values.clone();
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeoMagnetic = event.values.clone();
        }

        if (mGravity != null && mGeoMagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity,
                    mGeoMagnetic);
            if (success) {
                // Orientation has azimuth, pitch and roll
                float orientation[] = new float[3];

                SensorManager.getOrientation(R, orientation);

                // mAzimuth = 57.29578f * orientation[0];
                mPitch = 57.29578f * orientation[1];
                // mRoll = 57.29578f * orientation[2];
                // Log.d(TAG, "orientation values: " + mAzimuth + " / " + mPitch
                // + " / " + mRoll);
            }
        }
    }

    /**
     * Redraws the {@link View} in the background.
     */
    private class RenderThread extends Thread {
        private boolean mShouldRun;

        /**
         * Initializes the background rendering thread.
         */
        public RenderThread() {
            mShouldRun = true;
        }

        /**
         * Returns true if the rendering thread should continue to run.
         *
         * @return true if the rendering thread should continue to run
         */
        private synchronized boolean shouldRun() {
            return mShouldRun;
        }

        /**
         * Requests that the rendering thread exit at the next opportunity.
         */
        public synchronized void quit() {
            mShouldRun = false;
        }

        @Override
        public void run() {
            while (shouldRun()) {
                long frameStart = SystemClock.elapsedRealtime();
                draw();
                long frameLength = SystemClock.elapsedRealtime() - frameStart;

                long sleepTime = FRAME_TIME_MILLIS - frameLength;
                if (sleepTime > 0) {
                    SystemClock.sleep(sleepTime);
                }
            }
        }
    }

}
