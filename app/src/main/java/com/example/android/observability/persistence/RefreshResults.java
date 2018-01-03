
package com.example.android.observability.persistence;

import android.os.AsyncTask;
import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;

public abstract class RefreshResults {

    @MainThread
    public RefreshResults() {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                refreshResults();
                return null;
            }

        }.execute();
    }


    @WorkerThread
    protected abstract void refreshResults();


}