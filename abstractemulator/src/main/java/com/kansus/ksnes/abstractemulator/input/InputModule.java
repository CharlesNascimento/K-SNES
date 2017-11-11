package com.kansus.ksnes.abstractemulator.input;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.kansus.ksnes.abstractemulator.Module;
import com.kansus.ksnes.abstractemulator.input.source.InputSource;
import com.kansus.ksnes.abstractemulator.input.source.SensorInputSource;
import com.kansus.ksnes.abstractemulator.input.source.TouchInputSource;

/**
 * Defines a module responsible for handling user input.
 * <p>
 * Created by Charles Nascimento on 27/08/2017.
 */
public interface InputModule extends Module, TrackballListener {

    /**
     * Returns the keyboard input source.
     *
     * @return The keyboard input source.
     */
    InputSource getKeyboard();

    /**
     * Returns the sensor input source.
     *
     * @return The sensor input source.
     */
    SensorInputSource getInputSensor();

    /**
     * Returns the virtual keypad input source.
     *
     * @return The virtual keypad input source.
     */
    TouchInputSource getVirtualKeypad();

    /**
     * Sets whether the light gun input is enabled.
     *
     * @param enabled Whether the light gun input should be enabled.
     */
    void setLightGunEnabled(boolean enabled);

    /**
     * Sets whether the trackball input is enabled.
     *
     * @param enabled Whether the trackball input should be enabled.
     */
    void setTrackballEnabled(boolean enabled);

    /**
     * Sets whether the sensor input is enabled.
     *
     * @param enabled Whether the sensor input should be enabled.
     */
    void setSensorInputSourceEnabled(boolean enabled);

    /**
     * Sets whether the touch input is enabled.
     *
     * @param enabled Whether the touch input should be enabled.
     */
    void setTouchInputSourceEnabled(boolean enabled);

    /**
     * Sets the sensibility of the trackball.
     *
     * @param sensitivity The sensibility of the trackball.
     */
    void setTrackballSensitivity(int sensitivity);

    /**
     * Resets the state of all input sources.
     */
    void reset();

    /**
     * Loads the keypad key bindings from user preferences.
     *
     * @param prefs The user preferences.
     */
    void loadKeyBindings(SharedPreferences prefs);

    /**
     * Notifies the module whether the device screen is flipped.
     *
     * @param screenFlipped Where
     */
    void setScreenFlipped(boolean screenFlipped);

    /**
     * Notifies the module that a frame has been drawn.
     *
     * @param canvas The {@link Canvas} where the frame was drawn.
     */
    void notifyFrameDrawn(Canvas canvas);

    /**
     * Notifies the module the rectangle of the area where frames are drawn.
     *
     * @param rect The rectangle of the area where frames are drawn.
     */
    void setRenderingSurfaceRegion(Rect rect);

    /**
     * Sets the listener that will receive events concerning user input.
     *
     * @param inputListener The InputListener.
     */
    void setInputListener(InputListener inputListener);
}
