package sparsh.cool.developer.sunshine.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Sparsha on 7/10/2015.
 */
public class AuthenticatorService extends Service {

    private Authenticator mAuthenticator;

    @Override
    public IBinder onBind(Intent intent) {
        mAuthenticator = new Authenticator(this);
        return mAuthenticator.getIBinder();
    }
}
