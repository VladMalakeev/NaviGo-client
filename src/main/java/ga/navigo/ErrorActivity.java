package ga.navigo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by ASUS 553 on 11.03.2018.
 */

public class ErrorActivity extends AppCompatActivity {
TextView error;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        error = (TextView) findViewById(R.id.error);
        error.setText("При первом запуске нужен интернет!!");
    }
}
