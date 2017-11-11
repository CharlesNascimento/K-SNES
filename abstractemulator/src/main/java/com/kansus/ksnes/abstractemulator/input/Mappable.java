package com.kansus.ksnes.abstractemulator.input;

/**
 * Defines a mappable input source.
 * <p>
 * Created by Charles Nascimento on 03/09/2017.
 */
public interface Mappable {

    /**
     * Maps a original key a new key.
     *
     * @param originalKey The console original key.
     * @param newKey      The new key.
     */
    void mapKey(int originalKey, int newKey);

    /**
     * Clears all the mapping.
     */
    void clearKeyMap();
}
