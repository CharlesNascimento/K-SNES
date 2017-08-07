package com.kansus.ksnes.abstractemulator;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.nio.Buffer;

/**
 * Defines common operations provided by an emulator.
 *
 * Created by Charles Nascimento on 03/08/2017.
 */
public interface Emulator {

    void enableCheats(boolean enable);

    CheatManager getCheats();

    boolean loadROM(String file);

    void unloadROM();

    void setFrameUpdateListener(FrameUpdateListener frameUpdateListener);

    void setOnFrameDrawnListener(OnFrameDrawnListener frameDrawnListener);

    void setSurface(SurfaceHolder surface);

    void setSurfaceRegion(int x, int y, int w, int h);

    void setKeyStates(int states);

    void processTrackball(int key1, int duration1, int key2, int duration2);

    void fireLightGun(int x, int y);

    void setOption(String name, String value);

    int getOption(String name);

    int getVideoWidth();

    int getVideoHeight();

    void reset();

    void power();

    void pause();

    void resume();

    void getScreenshot(Buffer buffer);

    boolean saveState(String file);

    boolean loadState(String file);

    void setOption(String name, boolean value);

    void setOption(String name, int value);

    /**
     * Listens for events regarding game frame update.
     *
     * Created by Charles Nascimento on 03/08/2017.
     */
    public interface FrameUpdateListener {

        int onFrameUpdate(int keys) throws IOException, InterruptedException;
    }

    /**
     * Listens for events regarding game frame rendering.
     *
     * Created by Charles Nascimento on 03/08/2017.
     */
    public interface OnFrameDrawnListener {

        void onFrameDrawn(Canvas canvas);
    }
}
