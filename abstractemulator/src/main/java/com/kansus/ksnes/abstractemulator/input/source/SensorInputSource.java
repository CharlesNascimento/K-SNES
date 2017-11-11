package com.kansus.ksnes.abstractemulator.input.source;

/**
 * Defines a sensor input source.
 * <p>
 * Created by Charles Nascimento on 30/08/2017.
 */
public interface SensorInputSource extends InputSource {

    /**
     * Sets the sensitivity of the sensor.
     *
     * @param sensitivity The sensitivity of the sensor.
     */
    void setSensitivity(int sensitivity);
}