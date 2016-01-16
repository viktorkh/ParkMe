package app.parkme.com.clientapp.Account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Victor.Khazanov on 16/1/2016.
 */
public class ParkmeAuthenticatorService  extends Service {
    @Override
    public IBinder onBind(Intent intent) {

        ParkmeAuthenticator authenticator = new ParkmeAuthenticator(this);
        return authenticator.getIBinder();
    }
}
