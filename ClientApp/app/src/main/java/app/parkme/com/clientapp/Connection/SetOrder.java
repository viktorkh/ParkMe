package app.parkme.com.clientapp.Connection;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Victor.Khazanov on 11/1/2016.
 */
public class SetOrder extends AsyncTask<Void, Void, Void> {

    final String BASE_URL =
            "http://api.openweathermap.org/data/2.5/forecast/daily?";
    final String QUERY_PARAM = "q";

    public final String LOG_TAG = SetOrder.class.getSimpleName();


    String requestMethod="GET";

    String queryParam="";

    String orderJsonStr;

    public interface AsyncResponse {
        void processFinish(String output);
    }

    public AsyncResponse delegate = null;




    @Override
    protected Void doInBackground(Void... params) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;




        try {

            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, queryParam)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(requestMethod);
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            orderJsonStr = buffer.toString();



        } catch (Exception ex){

            Log.e(LOG_TAG, "Error ", ex);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }


        return null;
    }




    // @Override
    protected void onPostExecute(String result) {
        delegate.processFinish(result);
    }

}
