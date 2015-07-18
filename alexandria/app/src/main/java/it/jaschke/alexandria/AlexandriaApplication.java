package it.jaschke.alexandria;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by Jose on 7/12/15.
 */
public class AlexandriaApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();

        if(BuildConfig.DEBUG){
            Timber.plant(new Timber.DebugTree());
        }
    }
}
