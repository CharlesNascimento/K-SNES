package com.kansus.ksnes.abstractemulator.input;

/**
 * Listens for input source related events.
 * <p>
 * Created by Charles Nascimento on 02/09/2017.
 */
public interface InputSourceListener {

    /**
     * Invoked when the key states of the input source change.
     */
    void onKeyStatesChanged();
}
