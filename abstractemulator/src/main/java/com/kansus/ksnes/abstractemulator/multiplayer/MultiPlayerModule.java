package com.kansus.ksnes.abstractemulator.multiplayer;

import com.kansus.ksnes.abstractemulator.Module;

import java.io.IOException;

/**
 * Defines a module responsible for handling user input.
 * <p>
 * Created by Charles Nascimento on 29/08/2017.
 */
public interface MultiPlayerModule extends Module {

    void init();

    /**
     * Defines a listener to receive frame creation events.
     */
    interface FrameUpdateListener {

        /**
         * Invoked when a new frame is about to be created.
         *
         * @param keys The original key states in the moment of the frame creation.
         * @return The final key states used to create the frame. Can be
         * used to transform the original key states. Return "keys" if no transform
         * is necessary.
         * @throws IOException          // TODO
         * @throws InterruptedException // TODO
         */
        int onFrameUpdate(int keys) throws IOException, InterruptedException;
    }
}
