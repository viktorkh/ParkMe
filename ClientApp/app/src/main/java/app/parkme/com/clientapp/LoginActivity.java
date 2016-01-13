package app.parkme.com.clientapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final Button button = (Button) findViewById(R.id.btnLogin);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(LoginActivity.this, MapsActivity.class);
                //myIntent.putExtra("key", value); //Optional parameters
                startActivity(myIntent);
            }
        });


        Calendar c= Calendar.getInstance();

        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:mm a");

        Calendar cal = Calendar.getInstance();


       String str= format.format(c.getTime());

        try {
            cal.setTime(format.parse(str));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar c1 = format.getCalendar();
        int x=3;
    }
}
