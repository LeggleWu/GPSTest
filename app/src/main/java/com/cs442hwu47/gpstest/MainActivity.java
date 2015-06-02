package com.cs442hwu47.gpstest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    private Button btnStart;
    private Button btnStop;
    private TextView time;
    private static final String tvTime = "time";
    public static String TIMER_ACTION = "com.cs442hwu47.gpstest.TIMER_ACTION";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerBroadcastReceiver();

        btnStart = (Button)findViewById(R.id.Start);
        btnStop = (Button)findViewById(R.id.Stop);
        time = (TextView)this.findViewById(R.id.time);
        if (savedInstanceState != null) {
            String savedText = savedInstanceState.getString(tvTime);
            time.setText(savedText);
        }

        btnStart.setOnClickListener(btnClickListener);
        btnStop.setOnClickListener(btnClickListener);
    }

    private void registerBroadcastReceiver(){
        UITimeReceiver receiver = new UITimeReceiver();
        IntentFilter filter = new IntentFilter(TIMER_ACTION);
        registerReceiver(receiver, filter);
    }

    private class UITimeReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TIMER_ACTION.equals(action)){
                Bundle bundle = intent.getExtras();
                String strtime = bundle.getString("time");
                time.setText(strtime);
            }
        }
    }

    public Button.OnClickListener btnClickListener = new Button.OnClickListener()
    {
        public void onClick(View v)
        {

            Intent intent = new Intent(MainActivity.this, GPSService.class);

            switch (v.getId()){
                case R.id.Start:
                    intent.putExtra("timetext", time.getText().toString());
                    //sendBroadcast(intent);
                    startService(intent);

                    break;
                case R.id.Stop:
                    stopService(intent);
                    time.setText("00:00:00");

                    break;
            }

        }
    };


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(tvTime, time.getText());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



}
