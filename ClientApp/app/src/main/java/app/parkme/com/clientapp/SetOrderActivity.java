package app.parkme.com.clientapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import butterknife.Bind;

public class SetOrderActivity extends AppCompatActivity {


    public static final String ORDER_TIME="order_time";
    public static final String ORDER_ID="order_id";

    @Bind(R.id.lblPickYourCar)
    TextView _lblPickYourCar;

    @Bind(R.id.lblOrderId)
    TextView _lblOrderId;


    private  String orderTime="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_order);

        TextView lblPickYourCar = (TextView)findViewById(R.id.lblPickYourCar);
        TextView lblOrderId = (TextView)findViewById(R.id.lblOrderId);

        Intent intent = getIntent();


        if(_lblPickYourCar != null) {
            _lblPickYourCar.setText("We will take  your car at: " + intent.getStringExtra(ORDER_TIME));
        }else {

            lblPickYourCar.setText("We will take  your car at: " + intent.getStringExtra(ORDER_TIME));

        }

        if(_lblOrderId != null) {
            _lblOrderId.setText(intent.getStringExtra(ORDER_ID));
        }else {

            lblOrderId.setText( intent.getStringExtra(ORDER_ID));

        }

    }

    public static Intent createIntent(Context context, String _ORDER_TIME, String _ORDER_ID){
        Intent intent = new Intent(context, SetOrderActivity.class);
        intent.putExtra(ORDER_TIME,_ORDER_TIME);
        intent.putExtra(ORDER_ID,_ORDER_ID);
        return intent;
    }
}
