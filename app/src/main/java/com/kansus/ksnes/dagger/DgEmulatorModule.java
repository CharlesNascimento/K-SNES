package com.kansus.ksnes.dagger;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;

import com.kansus.ksnes.DefaultVideoModule;
import com.kansus.ksnes.abstractemulator.cheats.CheatsModule;
import com.kansus.ksnes.abstractemulator.Emulator;
import com.kansus.ksnes.abstractemulator.video.VideoModule;
import com.kansus.ksnes.abstractemulator.input.InputModule;
import com.kansus.ksnes.abstractemulator.multiplayer.MultiPlayerModule;
import com.kansus.ksnes.dagger.scopes.EmulatorScope;
import com.kansus.ksnes.dagger.scopes.PerActivity;
import com.kansus.ksnes.input.DefaultInputModule;
import com.kansus.ksnes.multiplayer.DefaultMultiPlayerModule;
import com.kansus.ksnes.snes9x.S9xCheatsModule;
import com.kansus.ksnes.snes9x.S9xEmulatorBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DgEmulatorModule {

    private static final String LOG_TAG = "DgEmulatorModule";

    public DgEmulatorModule() {

    }

    @Provides
    @PerActivity
    public InputModule provideInputModule(VideoModule videoModule) {
        Log.d(LOG_TAG, "provideInputModule");
        View view = (View) videoModule;
        return new DefaultInputModule(view);
    }

    @Provides
    @PerActivity
    public MultiPlayerModule provideMultiPlayerModule() {
        Log.d(LOG_TAG, "provideMultiPlayerModule");
        return new DefaultMultiPlayerModule();
    }

    @Provides
    @PerActivity
    public VideoModule provideVideoModule(Activity activity) {
        Log.d(LOG_TAG, "provideVideoModule");
        return new DefaultVideoModule(activity);
    }

    @Provides
    @PerActivity
    public CheatsModule provideCheatsModule() {
        Log.d(LOG_TAG, "provideCheatsModule");
        return new S9xCheatsModule();
    }

    @Provides
    @PerActivity
    public Emulator provideEmulator(VideoModule videoModule,
                                    InputModule inputModule,
                                    MultiPlayerModule multiPlayerModule,
                                    CheatsModule cheatsModule) {
        Log.d(LOG_TAG, "provideEmulator");
        return new S9xEmulatorBuilder()
                .setRenderingModule(videoModule)
                .setInputModule(inputModule)
                .setMultiPlayerModule(multiPlayerModule)
                .setCheatsModule(cheatsModule)
                .build();
    }
}