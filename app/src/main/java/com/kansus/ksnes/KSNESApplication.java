package com.kansus.ksnes;

import android.app.Application;

import com.kansus.ksnes.dagger.DaggerDgApplicationComponent;
import com.kansus.ksnes.dagger.DgApplicationComponent;
import com.kansus.ksnes.dagger.DgApplicationModule;
import com.kansus.ksnes.dagger.DgEmulatorComponent;
import com.kansus.ksnes.dagger.DgEmulatorModule;

/**
 *
 * Created by charl on 26/08/2017.
 */
public class KSNESApplication extends Application {

    private DgEmulatorComponent mDgEmulatorComponent;
    private DgApplicationComponent mApplicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        // Dagger%COMPONENT_NAME%
        /*mDgEmulatorComponent = DaggerDgEmulatorComponent.builder()
                // list of modules that are part of this component need to be created here too
                //.appModule(new DgApplicationModule(this)) // This also corresponds to the name of your module: %component_name%Module
                .emulatorModule(new DgEmulatorModule())
                .build();*/
        mApplicationComponent = DaggerDgApplicationComponent.builder().dgApplicationModule(new DgApplicationModule((this))).build();
    }

    public DgApplicationComponent getApplicationComponent() {
        return mApplicationComponent;
    }

    public DgEmulatorComponent getEmulatorComponent() {
        return mDgEmulatorComponent;
    }
}
