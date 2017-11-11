package com.kansus.ksnes.abstractemulator;

/**
 * Defines an emulator module.
 *
 * Created by Charles Nascimento on 08/09/2017.
 */
public interface Module {

    /**
     * Enables the module.
     */
    void enable();

    /**
     * Disables the module.
     */
    void disable();

    /**
     * Checks whether this module is currently enabled.
     *
     * @return Whether this module is currently enabled.
     */
    boolean isEnabled();
}
