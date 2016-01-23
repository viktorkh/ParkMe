package app.parkme.com.clientapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import butterknife.Bind;

public class SetOrderActivity extends AppCompatActivity {


    public static final String ORDER_TIME="order_time";

    @Bind(R.id.lblPickYourCar)
    TextView _lblPickYourCar;


    private  String orderTime="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_order);

        TextView lblPickYourCar = (TextView)findViewById(R.id.lblPickYourCar);
        Intent intent = getIntent();


        if(_lblPickYourCar != null) {
            _lblPickYourCar.setText("We will get  your car at: " + intent.getStringExtra(ORDER_TIME));
        }else {

            lblPickYourCar.setText("We will get  your car at: " + intent.getStringExtra(ORDER_TIME));

        }

    }

    public static Intent createIntent(Context context, String _ORDER_TIME){
        Intent intent = new Intent(context, SetOrderActivity.class);
        intent.putExtra(ORDER_TIME,_ORDER_TIME);
        return intent;
    }
}
