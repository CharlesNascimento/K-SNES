package com.kansus.ksnes.abstractemulator.video;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.SurfaceHolder;

import com.kansus.ksnes.abstractemulator.input.TrackballListener;

// FIXME Remove all android dependencies

/**
 * Defines a module responsible for handling video operations.
 * <p>
 * Created by Charles Nascimento on 30/08/2017.
 */
public interface VideoModule {

    int SCALING_ORIGINAL = 0;
    int SCALING_2X = 1;
    int SCALING_PROPORTIONAL = 2;
    int SCALING_STRETCH = 3;

    /**
     * Sets the size of the video.
     *
     * @param w The width.
     * @param h The height.
     */
    void setActualSize(int w, int h);

    /**
     * Sets the scaling mode of the video.
     *
     * @param mode The scaling mode. Possible values are SCALING_ORIGINAL, SCALING_2X,
     *             SCALING_PROPORTIONAL, SCALING_STRETCH.
     */
    void setScalingMode(int mode);

    /**
     * Sets the aspect ratio of the video.
     *
     * @param ratio A float value representing the ratio.
     */
    void setAspectRatio(float ratio);

    /**
     * Sets the listener that will be notified whenever a trackball event happens.
     *
     * @param listener A {@link TrackballListener}.
     */
    void setOnTrackballListener(TrackballListener listener);

    /**
     * Sets the listener that will be notified whenever a rendering surface's event happens.
     *
     * @param listener A {@link SurfaceListener}.
     */
    void setSurfaceListener(SurfaceListener listener);

    /**
     * Sets the listener that will be notified whenever the rendering surface rectangle changes.
     *
     * @param listener A {@link TrackballListener}.
     */
    void setRenderingSurfaceRegionListener(RenderingSurfaceRegionListener listener);

    /**
     * Defines a listener that will be notified whenever a rendering surface's event happens.
     */
    interface SurfaceListener {

        /**
         * Invoked when a rendering surface is created.
         *
         * @param holder The newly created surface.
         */
        void onSurfaceCreated(SurfaceHolder holder);

        /**
         * Invoked when the size of the rendering surface changes.
         *
         * @param width  The new surface width.
         * @param height The new surface height.
         */
        void onSurfaceResized(int width, int height);

        /**
         * Invoked when the rendering surface is destroyed.
         */
        void onSurfaceDestroyed();
    }

    /**
     * Defines a listener that will be notified whenever the rendering surface rectangle changes.
     */
    interface RenderingSurfaceRegionListener {

        /**
         * Invoked whenever the rendering surface rectangle changes.
         *
         * @param rect The new surface region rectangle.
         */
        void onRenderingSurfaceRegionChanged(Rect rect);
    }

    /**
     * Listens for events regarding game frame update.
     * <p>
     * Created by Charles Nascimento on 03/08/2017.
     */
    interface VideoFrameListener {

        /**
         * Invoked when the frame is drawn to the screen.
         *
         * @param canvas The canvas where the frame was drawn.
         */
        void onFrameDrawn(Canvas canvas);
    }
}
