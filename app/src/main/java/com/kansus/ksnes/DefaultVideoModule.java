package com.kansus.ksnes;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.kansus.ksnes.abstractemulator.video.VideoModule;
import com.kansus.ksnes.abstractemulator.input.TrackballListener;

/**
 * An emulator rendering module. More specifically, an Android View capable of showing the
 * emulation frames on the device screen.
 * <p>
 * Created by Charles Nascimento on 29/07/2017.
 */
public class DefaultVideoModule extends SurfaceView implements VideoModule, SurfaceHolder.Callback {

    //region Variables

    private int scalingMode = SCALING_PROPORTIONAL;
    private int actualWidth;
    private int actualHeight;
    private float aspectRatio;

    private VideoModule.SurfaceListener mSurfaceListener;
    private TrackballListener onTrackballListener;
    private RenderingSurfaceRegionListener mRenderingSurfaceRegionListener;

    //endregion

    //region Initializers

    public DefaultVideoModule(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    public DefaultVideoModule(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    //endregion

    //region Setters

    @Override
    public void setOnTrackballListener(TrackballListener listener) {
        onTrackballListener = listener;
    }

    @Override
    public void setSurfaceListener(VideoModule.SurfaceListener listener) {
        mSurfaceListener = listener;
    }

    @Override
    public void setRenderingSurfaceRegionListener(RenderingSurfaceRegionListener listener) {
        mRenderingSurfaceRegionListener = listener;
    }

    @Override
    public void setActualSize(int w, int h) {
        if (actualWidth != w || actualHeight != h) {
            actualWidth = w;
            actualHeight = h;
            updateSurfaceSize();
        }
    }

    @Override
    public void setScalingMode(int mode) {
        if (scalingMode != mode) {
            scalingMode = mode;
            updateSurfaceSize();
        }
    }

    @Override
    public void setAspectRatio(float ratio) {
        if (aspectRatio != ratio) {
            aspectRatio = ratio;
            updateSurfaceSize();
        }
    }

    //endregion

    //region Callbacks

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        updateSurfaceSize();
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        if (onTrackballListener != null) {
            if (onTrackballListener.onTrackball(event)) {
                return true;
            }
        }

        return super.onTrackballEvent(event);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (mSurfaceListener != null) {
            mSurfaceListener.onSurfaceCreated(surfaceHolder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        if (mSurfaceListener != null) {
            // TODO Check if "Rect surfaceRegion" can be used here as well
            mSurfaceListener.onSurfaceResized(width, height);
        }

        if (mRenderingSurfaceRegionListener != null) {
            Rect surfaceRegion = new Rect();
            surfaceRegion.left = (width - actualWidth) / 2;
            surfaceRegion.top = (height - actualHeight) / 2;
            surfaceRegion.right = surfaceRegion.left + actualWidth;
            surfaceRegion.bottom = surfaceRegion.top + actualHeight;

            mRenderingSurfaceRegionListener.onRenderingSurfaceRegionChanged(surfaceRegion);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (mSurfaceListener != null) {
            mSurfaceListener.onSurfaceDestroyed();
        }
    }

    //endregion

    //region Private methods

    /**
     * Updates the size of the SurfaceHolder according to the size of the view.
     */
    private void updateSurfaceSize() {
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        if (viewWidth == 0 || viewHeight == 0 ||
                actualWidth == 0 || actualHeight == 0)
            return;

        int w = 0;
        int h = 0;

        if (scalingMode != SCALING_STRETCH && aspectRatio != 0)
            viewWidth = (int) (viewWidth / aspectRatio);

        switch (scalingMode) {
            case SCALING_ORIGINAL:
                w = viewWidth;
                h = viewHeight;
                break;

            case SCALING_2X:
                w = viewWidth / 2;
                h = viewHeight / 2;
                break;

            case SCALING_STRETCH:
                if (viewWidth * actualHeight >= viewHeight * actualWidth) {
                    w = actualWidth;
                    h = actualHeight;
                }
                break;
        }

        if (w < actualWidth || h < actualHeight) {
            h = actualHeight;
            w = h * viewWidth / viewHeight;
            if (w < actualWidth) {
                w = actualWidth;
                h = w * viewHeight / viewWidth;
            }
        }
        getHolder().setFixedSize(w, h);
    }

    //endregion
}
