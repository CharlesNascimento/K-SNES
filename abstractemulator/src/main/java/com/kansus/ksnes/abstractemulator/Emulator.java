package com.kansus.ksnes.abstractemulator;

import android.view.SurfaceHolder;

import com.kansus.ksnes.abstractemulator.cheats.CheatsModule;
import com.kansus.ksnes.abstractemulator.input.InputModule;
import com.kansus.ksnes.abstractemulator.multiplayer.MultiPlayerModule;
import com.kansus.ksnes.abstractemulator.video.VideoModule;

import java.nio.Buffer;

/**
 * Defines common operations provided by an emulator.
 * <p>
 * Created by Charles Nascimento on 03/08/2017.
 */
public interface Emulator {

    /**
     * Returns the current input module of the emulator.
     *
     * @return The current input module of the emulator.
     */
    InputModule getInputModule();

    /**
     * Returns the current rendering module of the emulator.
     *
     * @return The current rendering module of the emulator.
     */
    VideoModule getVideoModule();

    /**
     * Returns the current multiplayer module of the emulator.
     *
     * @return The current multiplayer module of the emulator.
     */
    MultiPlayerModule getMultiPlayerModule();

    /**
     * Returns the current cheats module of the emulator.
     *
     * @return The current cheats module of the emulator.
     */
    CheatsModule getCheatsModule();

    /**
     * Sets input module of the emulator.
     *
     * @param inputModule The input module.
     */
    void setInputModule(InputModule inputModule);

    /**
     * Sets rendering module of the emulator.
     *
     * @param videoModule The rendering module.
     */
    void setRenderingModule(VideoModule videoModule);

    /**
     * Sets multiplayer module of the emulator.
     *
     * @param multiPlayerModule The multiplayer module.
     */
    void setMultiPlayerModule(MultiPlayerModule multiPlayerModule);

    /**
     * Sets the cheats module of the emulator.
     *
     * @param cheatsModule The cheats module of the emulator.
     */
    void setCheatsModule(CheatsModule cheatsModule);

    /**
     * Notifies the emulator whether the screen is currently flipped.
     *
     * @param screenFlipped Whether the screen is currently flipped.
     */
    void setScreenFlipped(boolean screenFlipped);

    /**
     * Loads a ROM into the emulator.
     *
     * @param file The path to the ROM file.
     * @return The result of the operation.
     */
    boolean loadROM(String file);

    /**
     * Unloads the currently loaded ROM.
     */
    void unloadROM();

    void setFrameUpdateListener(MultiPlayerModule.FrameUpdateListener frameUpdateListener);

    void setVideoFrameListener(VideoModule.VideoFrameListener videoFrameListener);

    /**
     * Sets the {@link SurfaceHolder} holding the surface where the
     * emulation frames will be drawn.
     *
     * @param surface The {@link SurfaceHolder}.
     */
    void setSurface(SurfaceHolder surface);

    /**
     * Sets the surface rectangle where the emulation frames will be drawn.
     *
     * @param x The x point of the rectangle.
     * @param y The y point of the rectangle.
     * @param w The width of the rectangle.
     * @param h the height of the rectangle.
     */
    void setSurfaceRegion(int x, int y, int w, int h);

    /**
     * Sets the states of the control keys of the emulator.
     *
     * @param states A 16 bits value where each bit corresponds
     *               to the state of a given key.
     */
    void setKeyStates(int states);

    /**
     * Processes the trackball input.
     *
     * @param key1      The first key.
     * @param duration1 The duration of the first key.
     * @param key2      The second key.
     * @param duration2 The duration of the second.
     */
    void processTrackball(int key1, int duration1, int key2, int duration2);

    /**
     * Processes the light gun input.
     *
     * @param x The x value of the fire coordinate.
     * @param y The y value of the fire coordinate.
     */
    void fireLightGun(int x, int y);

    /**
     * Returns the current video width.
     *
     * @return The current video width.
     */
    int getVideoWidth();

    /**
     * Returns the current video height.
     *
     * @return The current video height.
     */
    int getVideoHeight();

    /**
     * Resets the emulator.
     */
    void reset();

    /**
     * Turns the emulator off.
     */
    void power();

    /**
     * Pauses the emulator execution.
     */
    void pause();

    /**
     * Resets the emulator.
     */
    void resume();

    /**
     * Gets a screenshot of the currently drawn frame.
     *
     * @param buffer The {@link Buffer} that will receive the screenshot bytes.
     */
    void getScreenshot(Buffer buffer);

    /**
     * Saves the current state of the emulator to a file.
     *
     * @param file The path to the file where the state will be saved.
     * @return Whether the saving was successful.
     */
    boolean saveState(String file);

    /**
     * Loads the state of the emulator from a file.
     *
     * @param file The path to the file where the state will be loaded from.
     * @return Whether the saving was successful.
     */
    boolean loadState(String file);

    /**
     * Gets the value corresponding to the given option name.
     *
     * @param name The option name.
     * @return The value of the option.
     */
    int getOption(String name);

    /**
     * Sets a String value to the given option name.
     *
     * @param name  The option name.
     * @param value The String value.
     */
    void setOption(String name, String value);

    /**
     * Sets a boolean value to the given option name.
     *
     * @param name  The option name.
     * @param value The boolean value.
     */
    void setOption(String name, boolean value);

    /**
     * Sets a int value to the given option name.
     *
     * @param name  The option name.
     * @param value The int value.
     */
    void setOption(String name, int value);
}
