package io.github.francisshi.galleryanimation;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by Francis on 15/4/20.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                        .build());
    }
}
