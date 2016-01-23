package app.parkme.com.clientapp.Connection;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Victor.Khazanov on 23/1/2016.
 */
public class ConfirmTask extends AsyncTask<Void, Void, Void> {


    public  final String ORDER_ID="orderId";
    public  final String TASK_ID="taskId";
    public  final String PHONE_ID="phoneId";

    final String BASE_URL =
            "http://api.openweathermap.org/data/2.5/forecast/daily?";
    final String QUERY_PARAM = "";

    public final String TAG = SetOrder.class.getSimpleName();


    String requestMethod = "POST";
    String queryParam = "";
    String orderJsonStr;

    HashMap<String, String> postDataParams;

    public interface AsyncResponse {
        void processFinish(String output);
    }

    public AsyncResponse delegate = null;

    public ConfirmTask(String orderId,String taskId, String _phone, AsyncResponse _delegate) {

        this.delegate = _delegate;

        postDataParams.put(ORDER_ID,orderId);
        postDataParams.put(TASK_ID,taskId);
        postDataParams.put(PHONE_ID,_phone);

    }


    @Override
    protected Void doInBackground(Void... params) {

        HttpsURLConnection urlConnection = null;
        BufferedReader reader = null;
        String response = "";

        try {

            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, queryParam)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setReadTimeout(15000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod(requestMethod);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);


            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();
            int responseCode=urlConnection.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
            }
            else {
                response="";

            }


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

}
