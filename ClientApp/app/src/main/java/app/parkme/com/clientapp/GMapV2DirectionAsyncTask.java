package app.parkme.com.clientapp;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Victor.Khazanov on 9/1/2016.
 */
public class GMapV2DirectionAsyncTask extends AsyncTask<String, Void, List<List<HashMap<String, String>>>> {


    public static final String PREFS_NAME = "ParkmePrefsFile";


    public interface AsyncResponse {
        void processFinish(List<List<HashMap<String, String>>> output);
    }

    public AsyncResponse delegate = null;

    private final static String TAG = GMapV2DirectionAsyncTask.class.getSimpleName();

    private LatLng start, end;
    private  long date;
    private String mode;

    public GMapV2DirectionAsyncTask(LatLng sourcePosition, LatLng destPosition, long date, AsyncResponse delegate) {
        this.start = sourcePosition;
        this.end = destPosition;
        this.date = date;
        this.delegate = delegate;
    }

    @Override
    protected List<List<HashMap<String, String>>> doInBackground(String... params) {

        String urlStr = "https://maps.googleapis.com/maps/api/directions/json?origin="
                +start.latitude+","+start.longitude
                +"&destination="
                +end.latitude+","+end.longitude
              //  +"&sensor=false&units=metric&key=AIzaSyDPAPbkxkFLbbhj6ozDW68ZkEY3j6wlClo";
                +"&departure_time="+date+"&units=metric&key=AIzaSyDPAPbkxkFLbbhj6ozDW68ZkEY3j6wlClo";
        Log.d("url: ", urlStr);

        URL url;
        BufferedReader reader = null;
        String s = "";
        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;


        try {

            url = new URL(urlStr);

            URLConnection con = url.openConnection();

            InputStream in = new BufferedInputStream(con.getInputStream());

            String str = readStream(in);

            MapsActivity.jSonRoute = str;

            jObject = new JSONObject(str);
            PathJSONParser parser = new PathJSONParser();
            routes = parser.parse(jObject);

            return routes;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while(i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }



   // @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
        delegate.processFinish(result);
    }
}
