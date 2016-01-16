package app.parkme.com.clientapp;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Created by Victor.Khazanov on 11/1/2016.
 */
public class Utility {

    public static String getIMEI(Context context){

        TelephonyManager mngr = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        String imei = mngr.getDeviceId();
        return imei;

    }
    public static int getPhoneType(Context context){

        TelephonyManager mngr = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        int phoneType = mngr.getPhoneType();
        return phoneType;

    }

    public static String getSimSerialNumber(Context context){

        TelephonyManager mngr = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        String simSerialNumber = mngr.getSimSerialNumber();
        return simSerialNumber;

    }



    public static String getLine1Number(Context context){

        TelephonyManager mngr = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        String line1Number = mngr.getLine1Number();
        return line1Number;

    }



}
