package com.cs442hwu47.gpstest;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class GPSService extends Service {

    //////////Gmail API 5.17

    private String username = "whtleggle", password="wht486947123";

    private Session createSessionObject() {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        return Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    private Message createMessage(String email, String subject, String messageBody, Session session) throws MessagingException, UnsupportedEncodingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("whtleggle@gmail.com", "Location Test" ));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(email, email));
        message.setSubject(subject);
        message.setText(messageBody);
        return message;
    }

    private class SendMailTask extends AsyncTask<Message, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progressDialog = ProgressDialog.show(MainActivity.class, "Please wait", "Sending mail", true, false);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //progressDialog.dismiss();
        }

        @Override
        protected Void doInBackground(Message... messages) {
            try {
                Transport.send(messages[0]);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void sendMail(String email, String subject, String messageBody) {
        Session session = createSessionObject();

        try {
            Message message = createMessage(email, subject, messageBody, session);
            new SendMailTask().execute(message);
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    //////////////




    private Timer timer = null;
    private Timer timer01 = new Timer();
    private Intent timeIntent = null;
    private Bundle bundle = null;
    private int h, m, s = 0;
    private String initTime = null;
    /////////// 5.13

    private Boolean isPause = false;

    private TimerTask tt = null;

    private double distance;
    private double latA = 41.88, lngA = -87.65;

    Location locationA = new Location("point A");

    ///////////

    private static final long minTime = 60000;
    private static final float minDistance = 0;
    String tag = this.toString();
    private LocationManager locationManager;
    private LocationListener locationListener;
    private final IBinder mBinder = new GPSServiceBinder();
    String str1 = " ";



    @Override
    public void onCreate() {

        super.onCreate();
        //
        startService();
        Log.v(tag, "GPSService Started.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Bundle b = intent.getExtras();
        initTime = b.getString("timetext");
        String[] hms = initTime.split(":");
        h = Integer.parseInt(hms[0]);
        m = Integer.parseInt(hms[1]);
        s = Integer.parseInt(hms[2]);
        Log.e("TAG", initTime);

        return super.onStartCommand(intent, flags, startId);
    }

    private void sendTimeChangedBroadcast(String times){

        bundle.putString("time", times);
        timeIntent.putExtras(bundle);
        timeIntent.setAction(MainActivity.TIMER_ACTION);
        sendBroadcast(timeIntent);
    }

    public void startService() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateLocation(location);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance,
                locationListener);

        ////////

        timer = new Timer();
        timeIntent = new Intent();
        bundle = new Bundle();

            timer.schedule(tt = new TimerTask() {
                @Override
                public void run() {
                    s += 1;
                    if (s >= 60) {
                        s = 0;
                        m++;
                    }
                    if (m >= 60) {
                        m = 0;
                        h++;
                    }
                    String hh = h < 10 ? "0" : "";
                    String mm = m < 10 ? "0" : "";
                    String ss = s < 10 ? "0" : "";
                    String strTime = hh + h + ":" + mm + m + ":" + ss + s + "";

                    sendTimeChangedBroadcast(strTime);

                    do {
                        try {
                            //Log.i("TAG", "sleep(1000)...");
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }
                    } while (isPause);
                }
            }, 1000, 1000);

    }

    private void updateLocation(Location location) {

        try
        {

            locationA.setLatitude(latA);
            locationA.setLongitude(lngA);

            Log.e("latA", latA + "");
            Log.e("lngA", lngA + "");

            distance = locationA.distanceTo(location);

            Log.e("Distance", distance+"");

            if (distance > 300){

                isPause = true;


            } else {
                isPause = false;
            }

            Calendar c = Calendar.getInstance();

            FileOutputStream fos = new FileOutputStream(
                    android.os.Environment.getExternalStorageDirectory()
                            + "/gps01.txt", true);

            str1= Integer.toString(c.get(Calendar.YEAR))+"-"+Integer.toString(c.get(Calendar.MONTH)+1)+"-"
                    +Integer.toString(c.get(Calendar.DAY_OF_MONTH))+" "+ Integer.toString(c.get(Calendar.HOUR_OF_DAY))
                    + ":" + Integer.toString(c.get(Calendar.MINUTE))+"  "
                    +Double.toString(location.getLongitude())+","+Double.toString(location.getLatitude())+";"+"\n";

            ///////////////


            sendMail("hwu47@hawk.iit.edu", "test", str1);



            ////////////////

            Toast.makeText(this, str1, Toast.LENGTH_LONG).show();

            fos.write(str1.getBytes("utf-8"));
            fos.close();

        }
        catch (Exception e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    public void endService() {
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
        timer.cancel();
    }
    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return mBinder;
    }

    @Override
    public void onDestroy() {
        endService();
        Log.v(tag, "GPSService Ended.");
    }
    public class GPSServiceBinder extends Binder {
        GPSService getService() {
            return GPSService.this;
        }
    }
}
