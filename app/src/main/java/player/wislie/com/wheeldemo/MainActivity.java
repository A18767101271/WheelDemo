package player.wislie.com.wheeldemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final WheelView wheelView = findViewById(R.id.wheel_view);
        wheelView.setWheelListener(new WheelListener() {
            @Override
            public void onPositionChanging(int position) {

                Log.e("wislie"," postion:"+position);
            }
        });

        findViewById(R.id.clickBtn).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int pos = wheelView.getSelectedPos();
                Toast.makeText(MainActivity.this, "position:" + pos, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
