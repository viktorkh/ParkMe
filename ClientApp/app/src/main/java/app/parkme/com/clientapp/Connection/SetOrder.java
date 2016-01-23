package app.parkme.com.clientapp.Connection;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Victor.Khazanov on 11/1/2016.
 */
public class SetOrder extends AsyncTask<Void, Void, String> {

    public  final String ORDER_TIME="order_time";
    public  final String ORDER_CALENDAR="order_time";
    public  final String ORDER_DAY="order_day";
    public  final String ORDER_ADDRESS="order_address";
    public  final String ORDER_ADDRESS_LAT="order_address_lat";
    public  final String ORDER_ADDRESS_LONG="order_address_long";
    public  final String PHONE_ID="phoneId";

    final String BASE_URL =
            "http://clickconnectfrankfurt.clicksoftware.com/test/api/order/SetOrderGet";
            //?token=0544571497&orderTime=23-01-2016%2018:42&orderAddress=ddd";
    final String QUERY_PARAM = "";

    public final String TAG = SetOrder.class.getSimpleName();


    String requestMethod = "GET";
    String queryParam = "";
    String orderJsonStr;
    String phone="";
    String wholeDateTime="";
    String searchLocationAddressString="";


   // HashMap<String, String> postDataParams;

    public interface AsyncResponse {
        void processFinish(String output);
    }

    public AsyncResponse delegate = null;

    public SetOrder(String _time, String _displayName, String _searchLocationAddressString,
                    String _searchLocationAddressLat, String _searchLocationAddressLong,
                    String _wholeDateTime,String _phone, AsyncResponse _delegate) {

        this.delegate = _delegate;

        this.phone = _phone;
        this.wholeDateTime = _wholeDateTime;
        this.searchLocationAddressString = _searchLocationAddressString;
        queryParam = queryParam + "?token=" + _phone + "&orderTime=" + _wholeDateTime + "&orderAddress=" + _searchLocationAddressString;

//        postDataParams.put("time",_time);
//        postDataParams.put("displayName",_displayName);
//        postDataParams.put("searchLocationAddressString",_searchLocationAddressString);
//        postDataParams.put("searchLocationAddressLat",_searchLocationAddressLat);
//        postDataParams.put("searchLocationAddressLong",_searchLocationAddressLong);
//        postDataParams.put("wholeDateTime",_wholeDateTime);
//        postDataParams.put(PHONE_ID,_phone);

    }


    @Override
    protected String doInBackground(Void... params) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String response = "";

        try {

            String str = "http://clickconnectfrankfurt.clicksoftware.com/test/api/account";

            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter("token", this.phone)
                    .appendQueryParameter("orderTime", this.wholeDateTime)
                    .appendQueryParameter("orderAddress", this.searchLocationAddressString)
                    .build();

            Uri builtUri_Test = Uri.parse(str).buildUpon()
                    // .appendQueryParameter(QUERY_PARAM, queryParam)
                    .build();

          //  URL url = new URL(builtUri.toString());

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();

            response = readStream(urlConnection.getInputStream());

            return response;


        } catch (Exception ex) {

            Log.e(TAG, "Error ", ex);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }


        return null;
    }




    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }


    // @Override
    protected void onPostExecute(String result) {
        delegate.processFinish(result);
    }

    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();

        String str;
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            str=response.toString();
            int x=8;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }


}
