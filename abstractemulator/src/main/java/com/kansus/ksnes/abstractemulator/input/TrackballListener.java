package com.kansus.ksnes.abstractemulator.input;

import android.view.MotionEvent;

/**
 * Listens for trackball input related events.
 * <p>
 * Created by Charles Nascimento on 02/09/2017.
 */
public interface TrackballListener {

    /**
     * Invoked when the trackball is moved.
     *
     * @param event The event data.
     * @return Whether the event was processed.
     */
    boolean onTrackball(MotionEvent event);
}