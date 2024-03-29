package sparsh.cool.developer.sunshine.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.util.Objects;

/**
 * Created by Sparsha on 7/10/2015.
 */
public class SyncService extends Service {

    private static SyncAdapter sSyncAdapter = null;
    private static final Object sSyncAdapterLock = new Object();

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (sSyncAdapterLock){
            if(sSyncAdapter == null)
                sSyncAdapter = new SyncAdapter(getApplicationContext(),true);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
