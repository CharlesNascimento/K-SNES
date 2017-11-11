package com.kansus.ksnes.abstractemulator.input.source;

import com.kansus.ksnes.abstractemulator.input.InputSourceListener;
import com.kansus.ksnes.abstractemulator.input.Mappable;

/**
 * Defines an input source.
 * <p>
 * Created by Charles Nascimento on 31/08/2017.
 */
public interface InputSource extends Mappable {

    /**
     * Gets the key states of this input source.
     *
     * @return A 16 bits value where each bit corresponds
     * to the state of a given key.
     */
    int getKeyStates();

    /**
     * Resets the key states of this input source.
     */
    void reset();

    /**
     * Enables this input source.
     */
    void enable();

    /**
     * Disables this input source.
     */
    void disable();

    /**
     * Sets the listener that will be notified when the key states
     * of this input source change.
     *
     * @param listener The {@link InputSourceListener}.
     */
    void setInputSourceListener(InputSourceListener listener);
}