package com.example.firstdraft;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class stop_journey extends AppCompatActivity {
    private ArrayList permissionsToRequest;
    private ArrayList permissionsRejected = new ArrayList();
    private ArrayList permissions = new ArrayList();

    RequestQueue requestQueue;

    private final static int ALL_PERMISSIONS_RESULT = 101;
    LocationTrack locationTrack;

    Intent intent = getIntent();

    private Button btnEnd;

    String client;
    String add;
    /*String oid;*/
    String time2;

    double longi2;
    double lat2;

    int dist;
    String lat;
    String longi;
    int result;

    TextView cl;
    TextView address;
    /*TextView order;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_journey);

        requestQueue = Volley.newRequestQueue(stop_journey.this);

        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);

        permissionsToRequest = findUnAskedPermissions(permissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0)
                requestPermissions((String[]) permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }

        Date date = new Date();
        Timestamp timestamp2 = new Timestamp(date.getTime());
        final long m = timestamp2.getTime();
        time2 = time(m);
        System.out.println(time2);

        Intent intent = getIntent();
        client = intent.getStringExtra(MainActivity.EXTRA_TEXT);
        add = intent.getStringExtra(MainActivity.EXTRA_TEXT2);
        final long milli = intent.getLongExtra(item_select.EXTRA_TEXT3,0);
        final double lat1 = intent.getDoubleExtra(item_select.EXTRA_TEXT4,0);
        final double longi1 = intent.getDoubleExtra(item_select.EXTRA_TEXT5,0);
        String result = intent.getStringExtra(item_select.EXTRA_TEXT6);

        cl = (TextView) findViewById(R.id.cl);
        /*order = (TextView) findViewById(R.id.order);*/
        address = (TextView) findViewById(R.id.address);

        cl.setText(client);
        /*order.setText(oid);*/
        address.setText(add);

        btnEnd = (Button) this.findViewById(R.id.btnEnd);
        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationTrack = new LocationTrack(stop_journey.this);
                if (locationTrack.canGetLocation()) {
                    longi2 = locationTrack.getLongitude();
                    lat2 = locationTrack.getLatitude();
                    lat = Double.toString(lat2);
                    longi = Double.toString(longi2);

                    dist = distance(lat1,lat2,longi1,longi2);
                }
                else {
                    locationTrack.showSettingsAlert();
                }
                postData(requestQueue);
                /*System.out.println(timer(milli));*/

                final Intent intent1 = new Intent(stop_journey.this, MainActivity.class);
                /*intent1.putExtra(EXTRA_TEXT, client);
                intent1.putExtra(EXTRA_TEXT2, oid);*/
                startActivity(intent1);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_right);
            }
        });
    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(stop_journey.this)
                .setTitle("Journey still in progress")
                .setMessage("This will end the app. Use the home button instead.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                    }
                }).show();

    }

    public String time(long milliseconds) {
        Date currentDate = new Date(milliseconds);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(currentDate);
    }
    public static String timer(long milli){
        final Date date = new Date();
        Timestamp timestamp2 = new Timestamp(date.getTime());
        long m = timestamp2.getTime() - milli;
        int s = (int) (m / 1000) % 60 ;
        int min = (int) ((m / (1000*60)) % 60);
        int h   = (int) ((m / (1000*60*60)) % 24);
        return String.format("%02d:%02d:%02d", h,min,s);
    }
    public int distance(double lat1,double lat2,double long1,double long2){
        if((lat1==lat2) && (long1==long2)){
            return (0);
        }
        else{
            double theta = long1 - long2;
            double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
            dist = Math.acos(dist);
            dist = rad2deg(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1.609344;
            int dis = (int)dist;
            return(dis) ;
        }
    }
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public void postData(RequestQueue requestQueue) {
        String obj = "{\n" +
                "  \"jsonrpc\": \"2.0\",\n" +
                "  \"method\": \"call\",\n" +
                "  \"params\": {\n" +
                "    \"args\": [\n" +
                "      [\n" +
                         result +
                "      ],\n" +
                "      {\n" +
                "        \"check_out\":\""+ time2 + "\",\n" +
                "        \"x_check_out_lat\":\"" + lat + "\",\n" +
                "        \"x_check_out_long\":\"" + longi + "\",\n" +
                "        \"x_distance_km\": " + dist + "\n" +
                "      }\n" +
                "    ],\n" +
                "    \"model\": \"hr.attendance\",\n" +
                "    \"method\": \"write\",\n" +
                "    \"kwargs\": {\n" +
                "      \"context\": {\n" +
                "        \"lang\": \"en_US\",\n" +
                "        \"tz\": \"Asia/Kolkata\",\n" +
                "        \"uid\": 2,\n" +
                "        \"params\": {\n" +
                "          \"id\": 15,\n" +
                "          \"action\": 179,\n" +
                "          \"model\": \"hr.attendance\",\n" +
                "          \"view_type\": \"form\",\n" +
                "          \"menu_id\": 141\n" +
                "        },\n" +
                "        \"search_default_today\": 1\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"id\": 329478684\n" +
                "}";
        JSONObject object = null;
        try {
            object = new JSONObject(obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(object);
        String stopurl = "http://34.87.62.211/web/dataset/call_kw/hr.attendance/write";
        CustomRequest customRequest = new CustomRequest(Request.Method.POST, stopurl, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                locationTrack = new LocationTrack(stop_journey.this);
                if (locationTrack.canGetLocation()) {
                    double longitude = locationTrack.getLongitude();
                    double latitude = locationTrack.getLatitude();
                    /*System.out.println(latitude);
                    System.out.println(longitude);*/
                }
                else{
                    locationTrack.showSettingsAlert();
                }
                System.out.println(response);
                System.out.println("works");
                try {
                    result = (int) response.get("result");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("Cookie", "session_id=5532515de4dba12c8836e4842814f9459f8df9dc");
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
        List<String> cookies = new ArrayList<>();
        cookies.add("session_id=5532515de4dba12c8836e4842814f9459f8df9dc");
        customRequest.setCookies(cookies);
        requestQueue.add(customRequest);
    }

    private ArrayList findUnAskedPermissions(ArrayList wanted) {
        ArrayList result = new ArrayList();
        for (Object perm : wanted) {
            if (!hasPermission((String) perm)) {
                result.add(perm);
            }
        }
        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case ALL_PERMISSIONS_RESULT:
                for (Object perms : permissionsToRequest) {
                    if (!hasPermission((String) perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale((String) permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions((String[]) permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;
        }

    }
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(stop_journey.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
/*
    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationTrack.stopListener();
    }*/
}

