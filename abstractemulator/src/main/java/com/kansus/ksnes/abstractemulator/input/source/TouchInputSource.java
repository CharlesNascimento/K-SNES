package com.kansus.ksnes.abstractemulator.input.source;

import android.graphics.Canvas;

/**
 * Defines a sensor input source.
 * <p>
 * Created by Charles Nascimento on 30/08/2017.
 */
public interface TouchInputSource extends InputSource {

    /**
     * Resizes the touch controls according to the given width and height.
     *
     * @param width  The width.
     * @param height The height.
     */
    void resize(int width, int height);

    /**
     * Draws the touch controls to the given Canvas.
     *
     * @param canvas The Canvas.
     */
    void draw(Canvas canvas);

    /**
     * Triggers a touch event to this input source.
     *
     * @param data            The data of the event.
     * @param isScreenFlipped Whether the screen is flipped.
     * @return Whether the touch was processed.
     */
    boolean fireTouch(Object data, boolean isScreenFlipped);

    /**
     * Destroys the touch controls.
     */
    void destroy();
}