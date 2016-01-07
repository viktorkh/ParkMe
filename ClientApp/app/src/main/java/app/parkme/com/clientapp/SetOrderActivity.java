package app.parkme.com.clientapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SetOrderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_order);
    }

    public static Intent createIntent(Context context, String first){
        Intent intent = new Intent(context, SetOrderActivity.class);
//        intent.putExtra(TARGET_ACTIVITY_DATA_KEY,first);
//        intent.putExtra(TARGET_ACTIVITY_MORE_DATA_KEY,second);
//        intent.putExtra(TARGET_ACTIVITY_EVEN_MORE_DATA_KEY,third);
        return intent;
    }
}
