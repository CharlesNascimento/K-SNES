package com.kansus.ksnes.snes9x;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.SurfaceHolder;

import com.kansus.ksnes.abstractemulator.cheats.CheatsModule;
import com.kansus.ksnes.abstractemulator.Emulator;
import com.kansus.ksnes.abstractemulator.video.VideoModule;
import com.kansus.ksnes.abstractemulator.input.InputListener;
import com.kansus.ksnes.abstractemulator.input.InputModule;
import com.kansus.ksnes.abstractemulator.input.source.TouchInputSource;
import com.kansus.ksnes.abstractemulator.multiplayer.MultiPlayerModule;

import java.nio.Buffer;

/**
 * Emulator implementation that uses Snes9x as the emulation engine.
 * <p>
 * Created by Charles Nascimento on 29/07/2017.
 */
class S9xEmulator implements Emulator,
        VideoModule.SurfaceListener,
        VideoModule.VideoFrameListener,
        InputListener {

    //region Variables

    private VideoModule mVideoModule;
    private InputModule mInputModule;
    private MultiPlayerModule mMultiPlayerModule;
    private CheatsModule mCheatsModule;

    private VideoModule.VideoFrameListener mFrameDrawnListener;

    //endregion

    //region Initializers

    S9xEmulator() {
        S9xMediaManager.setOnFrameDrawnListener(this);

        System.loadLibrary("emu");

        initialize();

        Thread thread = new Thread() {
            public void run() {
                nativeRun();
            }
        };
        thread.start();
    }

    //endregion

    //region Public operations

    @Override
    public final boolean loadROM(String file) {
        if (!nativeLoadROM(file)) {
            return false;
        }

        mCheatsModule.setROM(file);

        return true;
    }

    @Override
    public final void unloadROM() {
        nativeUnloadROM();

        mCheatsModule.setROM(null);
    }

    //endregion

    //region Callbacks

    @Override
    public void onKeyStatesChanged(int state) {
        setKeyStates(state);
    }

    @Override
    public void onFireLightGun(int x, int y) {
        fireLightGun(x, y);
    }

    @Override
    public void onTrackball(int key1, int duration1, int key2, int duration2) {
        processTrackball(key1, duration1, key2, duration2);
    }

    @Override
    public void onFrameDrawn(Canvas canvas) {
        if (mFrameDrawnListener != null) {
            mFrameDrawnListener.onFrameDrawn(canvas);
        }

        if (mInputModule != null) {
            mInputModule.notifyFrameDrawn(canvas);
        }
    }

    @Override
    public void onSurfaceCreated(SurfaceHolder holder) {
        this.setSurface(holder);
    }

    @Override
    public void onSurfaceResized(int width, int height) {
        TouchInputSource touchInputSource = mInputModule.getVirtualKeypad();

        if (touchInputSource != null) {
            touchInputSource.resize(width, height);
        }

        final int w = this.getVideoWidth();
        final int h = this.getVideoHeight();

        Rect surfaceRegion = new Rect();
        surfaceRegion.left = (width - w) / 2;
        surfaceRegion.top = (height - h) / 2;
        surfaceRegion.right = surfaceRegion.left + w;
        surfaceRegion.bottom = surfaceRegion.top + h;

        setSurfaceRegion(surfaceRegion.left, surfaceRegion.top, w, h);

        if (mInputModule != null) {
            mInputModule.setRenderingSurfaceRegion(surfaceRegion);
        }
    }

    @Override
    public void onSurfaceDestroyed() {
        TouchInputSource touchInputSource = mInputModule.getVirtualKeypad();
        if (touchInputSource != null)
            touchInputSource.destroy();

        setSurface(null);
    }

    //endregion

    //region Getters

    @Override
    public InputModule getInputModule() {
        return mInputModule;
    }

    @Override
    public VideoModule getVideoModule() {
        return mVideoModule;
    }

    @Override
    public MultiPlayerModule getMultiPlayerModule() {
        return mMultiPlayerModule;
    }

    @Override
    public final CheatsModule getCheatsModule() {
        return mCheatsModule;
    }

    //endregion

    //region Setters

    @Override
    public void setVideoFrameListener(VideoModule.VideoFrameListener videoFrameListener) {
        mFrameDrawnListener = videoFrameListener;
    }

    @Override
    public void setScreenFlipped(boolean screenFlipped) {
        mInputModule.setScreenFlipped(screenFlipped);
    }

    @Override
    public void setInputModule(InputModule inputModule) {
        mInputModule = inputModule;
        mInputModule.setInputListener(this);
    }

    @Override
    public void setRenderingModule(VideoModule videoModule) {
        mVideoModule = videoModule;
        mVideoModule.setSurfaceListener(this);
        mVideoModule.setOnTrackballListener(mInputModule);
    }

    @Override
    public void setMultiPlayerModule(MultiPlayerModule multiPlayerModule) {
        mMultiPlayerModule = multiPlayerModule;
    }

    @Override
    public void setCheatsModule(CheatsModule cheatsModule) {
        mCheatsModule = cheatsModule;
    }

    @Override
    public void setOption(String name, boolean value) {
        setOption(name, value ? "true" : "false");
    }

    @Override
    public void setOption(String name, int value) {
        setOption(name, Integer.toString(value));
    }

    //endregion

    //region Native

    @Override
    public native void setFrameUpdateListener(MultiPlayerModule.FrameUpdateListener frameUpdateListener);

    @Override
    public native void setSurface(SurfaceHolder surface);

    @Override
    public native void setSurfaceRegion(int x, int y, int w, int h);

    @Override
    public native void setKeyStates(int states);

    @Override
    public native void processTrackball(int key1, int duration1, int key2, int duration2);

    @Override
    public native void fireLightGun(int x, int y);

    @Override
    public native void setOption(String name, String value);

    @Override
    public native int getOption(String name);

    @Override
    public native int getVideoWidth();

    @Override
    public native int getVideoHeight();

    @Override
    public native void reset();

    @Override
    public native void power();

    @Override
    public native void pause();

    @Override
    public native void resume();

    @Override
    public native void getScreenshot(Buffer buffer);

    @Override
    public native boolean saveState(String file);

    @Override
    public native boolean loadState(String file);

    private native boolean initialize();

    private native void nativeRun();

    private native boolean nativeLoadROM(String file);

    private native void nativeUnloadROM();

    //endregion
}
