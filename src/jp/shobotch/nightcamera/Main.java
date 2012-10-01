package jp.shobotch.nightcamera;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.support.v4.app.NavUtils;

public class Main extends Activity implements OnClickListener {
	private static final String TAG = "main";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "‹N“®ŠJŽn");

        Button start_camera = (Button)findViewById(R.id.start_camera);
        start_camera.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.start_camera:
				Intent intent = new Intent(this, CameraActivity.class);
				startActivity(intent);
				break;
		}

	}


}
