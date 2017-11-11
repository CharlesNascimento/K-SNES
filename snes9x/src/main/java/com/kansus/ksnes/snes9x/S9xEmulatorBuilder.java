package com.kansus.ksnes.snes9x;

import com.kansus.ksnes.abstractemulator.cheats.CheatsModule;
import com.kansus.ksnes.abstractemulator.video.VideoModule;
import com.kansus.ksnes.abstractemulator.input.InputModule;
import com.kansus.ksnes.abstractemulator.multiplayer.MultiPlayerModule;

/**
 * Builder for creating a {@link S9xEmulator}.
 * <p>
 * Created by Charles Nascimento on 07/09/2017.
 */
public class S9xEmulatorBuilder {

    private VideoModule mVideoModule;
    private InputModule mInputModule;
    private MultiPlayerModule mMultiPlayerModule;
    private CheatsModule mCheatsModule;

    /**
     * Sets the rendering module.
     *
     * @param videoModule The rendering module.
     * @return The builder instance.
     */
    public S9xEmulatorBuilder setRenderingModule(VideoModule videoModule) {
        mVideoModule = videoModule;
        return this;
    }

    /**
     * Sets the input module.
     *
     * @param inputModule The input module.
     * @return The builder instance.
     */
    public S9xEmulatorBuilder setInputModule(InputModule inputModule) {
        mInputModule = inputModule;
        return this;
    }

    /**
     * Sets the multiplayer module.
     *
     * @param multiPlayerModule The multiplayer module.
     * @return The builder instance.
     */
    public S9xEmulatorBuilder setMultiPlayerModule(MultiPlayerModule multiPlayerModule) {
        mMultiPlayerModule = multiPlayerModule;
        return this;
    }

    /**
     * Sets the cheats module.
     *
     * @param cheatsModule The multiplayer module.
     * @return The builder instance.
     */
    public S9xEmulatorBuilder setCheatsModule(CheatsModule cheatsModule) {
        mCheatsModule = cheatsModule;
        return this;
    }

    /**
     * Builds a new S9xEmulator instance.
     *
     * @return A new S9xEmulator instance.
     */
    public S9xEmulator build() {
        S9xEmulator emulator = new S9xEmulator();
        emulator.setRenderingModule(mVideoModule);
        emulator.setInputModule(mInputModule);
        emulator.setMultiPlayerModule(mMultiPlayerModule);
        emulator.setCheatsModule(mCheatsModule);

        return emulator;
    }
}