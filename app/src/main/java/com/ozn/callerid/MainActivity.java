package com.ozn.callerid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    public static final String DEMO_URL = "https://sten.app/WhoIsThat/";
    public static final int MY_PERMISSIONS_REQUEST_READ_CALL_LOG = 0;
    public static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 1;
    public static final int MY_PERMISSIONS_REQUEST_PROCESS_OUTGOING_CALLS = 2;

    CallReceiver callReceiver;


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        // Firstly, we check READ_CALL_LOG permission
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // We do not have this permission. Let's ask the user
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CALL_LOG}, MY_PERMISSIONS_REQUEST_READ_CALL_LOG);
        }

        // dynamically register CallReceiver
        if (callReceiver == null) {
            callReceiver = new CallReceiver();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        intentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        registerReceiver(callReceiver, intentFilter);


    }
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();


    }
    public void yaz(Date zaman, String gelenNumara){

        String gelenZaman = zaman.toString();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("GelenCagrilar").child(gelenZaman);

        myRef.setValue(gelenNumara);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // manually unregister CallReceiver
        if (callReceiver != null) {
            unregisterReceiver(callReceiver);
            callReceiver = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CALL_LOG: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted!
                    Log.d("###", "READ_CALL_LOG granted!");
                    // check READ_PHONE_STATE permission only when READ_CALL_LOG is granted
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        // We do not have this permission. Let's ask the user
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                    }
                } else {
                    // permission denied or has been cancelled
                    Log.d("###", "READ_CALL_LOG denied!");
                    //Toast.makeText(getApplicationContext(), "missing READ_CALL_LOG", Toast.LENGTH_LONG).show();
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted!
                    Log.d("###", "READ_PHONE_STATE granted!");
                    // check PROCESS_OUTGOING_CALLS permission only when READ_PHONE_STATE is granted
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.PROCESS_OUTGOING_CALLS) != PackageManager.PERMISSION_GRANTED) {
                        // We do not have this permission. Let's ask the user
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.PROCESS_OUTGOING_CALLS}, MY_PERMISSIONS_REQUEST_PROCESS_OUTGOING_CALLS);
                    }
                } else {
                    // permission denied or has been cancelled
                    Log.d("###", "READ_PHONE_STATE denied!");
                    //.makeText(getApplicationContext(), "missing READ_PHONE_STATE", Toast.LENGTH_LONG).show();
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_PROCESS_OUTGOING_CALLS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted!
                    Log.d("###", "PROCESS_OUTGOING_CALLS granted!");
                } else {
                    // permission denied or has been cancelled
                    Log.d("###", "PROCESS_OUTGOING_CALLS denied!");
                    //Toast.makeText(getApplicationContext(), "missing PROCESS_OUTGOING_CALLS", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }



    class CallReceiver extends PhonecallReceiver {

        @Override
        protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
            String msg = "start outgoing call: " + number + " at " + start;//giden aramayı başlat

            Log.d("###", msg);
           // Toast.makeText(ctx.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();



        }

        @Override
        protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
            String msg = "end outgoing call: " + number + " at " + end;//giden aramayı bitir

            Log.d("###", msg);
            //Toast.makeText(ctx.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();


        }

        @Override
        protected void onIncomingCallStarted(Context ctx, String number, Date start) {
            //String msg = "start incoming call: " + number + " at " + start;//gelen aramayı başlat
            String msg = "Gelen Arama: " + number + " Zamanı: " + start;//gelen aramayı başlat

            Log.d("###", msg);
            Toast.makeText(ctx.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();


            System.out.println("number start: "+number);//numara başlangıcı
            yaz(start, number);
        }

        @Override
        protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
            String msg = "end incoming call: " + number + " at " + end;//gelen aramayı bitir

            Log.d("###", msg);
            //Toast.makeText(ctx.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();


        }

        @Override
        protected void onMissedCall(Context ctx, String number, Date missed) {
            String msg = "missed call: " + number + " at " + missed;//Cevapsız çağrı

            Log.d("###", msg);
            //Toast.makeText(ctx.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();


            System.out.println("number missed cal: "+number);//numara cevapsız çağrı
        }
    }

    class JsObject {

        @JavascriptInterface
        public void debug(String msg) {
            //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            System.out.println("msj:"+msg);
        }
    }
}
