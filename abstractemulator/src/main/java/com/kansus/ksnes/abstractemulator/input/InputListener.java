package com.kansus.ksnes.abstractemulator.input;

/**
 * Listens to input related events.
 * <p>
 * Created by Charles Nascimento on 29/07/2017.
 */
public interface InputListener {

    /**
     * Invoked when key states are changed.
     *
     * @param state A 16 bits value where each bit corresponds
     *              to the state of a given key.
     */
    void onKeyStatesChanged(int state);

    /**
     * Invoked when the light gun is fired.
     *
     * @param x The x coordinate of the fire point.
     * @param y The y coordinate of the fire point.
     */
    void onFireLightGun(int x, int y);

    /**
     * Invoked when the trackball is used.
     *
     * @param key1      The first key.
     * @param duration1 The duration of the first key.
     * @param key2      The second key.
     * @param duration2 The duration of the second key.
     */
    void onTrackball(int key1, int duration1, int key2, int duration2);
}
