package com.kansus.ksnes.snes9x;

import android.content.Context;
import android.os.Build;
import android.view.SurfaceHolder;

import com.kansus.ksnes.abstractemulator.CheatManager;
import com.kansus.ksnes.abstractemulator.Emulator;

import java.nio.Buffer;

public class S9xEmulator implements Emulator {

    private static String engineLib;
    private static Emulator emulator;
    private Thread thread;
    private String romFileName;
    private boolean cheatsEnabled;
    private CheatManager cheats;

    public static Emulator createInstance(Context context, String engine) {
        if (emulator == null)
            System.loadLibrary("emu");

        final String libDir = "/data/data/" + context.getPackageName() + "/lib";
        if (!engine.equals(engineLib)) {
            engineLib = engine;
            loadEngine(libDir, engine);
        }

        if (emulator == null)
            emulator = new S9xEmulator(libDir);
        return emulator;
    }

    public static Emulator getInstance() {
        return emulator;
    }

    private S9xEmulator(String libDir) {
        initialize(libDir, Integer.parseInt(Build.VERSION.SDK));

        thread = new Thread() {
            public void run() {
                nativeRun();
            }
        };
        thread.start();
    }

    @Override
    public final void enableCheats(boolean enable) {
        cheatsEnabled = enable;
        if (romFileName == null)
            return;

        if (enable && cheats == null)
            cheats = (CheatManager) new S9xCheatManager(romFileName);
        else if (!enable && cheats != null) {
            cheats.destroy();
            cheats = null;
        }
    }

    @Override
    public final CheatManager getCheats() {
        return cheats;
    }

    @Override
    public final boolean loadROM(String file) {
        if (!nativeLoadROM(file))
            return false;

        romFileName = file;
        if (cheatsEnabled)
            cheats = new S9xCheatManager(file);
        return true;
    }

    @Override
    public final void unloadROM() {
        nativeUnloadROM();

        cheats = null;
        romFileName = null;
    }

    @Override
    public void setOnFrameDrawnListener(OnFrameDrawnListener frameDrawnListener) {
        S9xMediaManager.setOnFrameDrawnListener(frameDrawnListener);
    }

    @Override
    public native void setFrameUpdateListener(FrameUpdateListener frameUpdateListener);

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

    public static native boolean loadEngine(String libDir, String lib);

    public native boolean initialize(String libDir, int sdk);

    public native void nativeRun();

    public native boolean nativeLoadROM(String file);

    public native void nativeUnloadROM();

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

    @Override
    public void setOption(String name, boolean value) {
        setOption(name, value ? "true" : "false");
    }

    @Override
    public void setOption(String name, int value) {
        setOption(name, Integer.toString(value));
    }
}
