package com.pestopasta.cluzcs160;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.cloud.backend.core.AsyncBlobUploader;
import com.google.cloud.backend.core.CloudBackend;
import com.google.cloud.backend.core.CloudEntity;
import com.google.cloud.backend.core.CloudQuery;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity implements LocationListener, GoogleMap.OnMapClickListener, SensorEventListener {

    Button addTag;
    //private LocationManager locationManager;
    //private LocationListener locListener;
    private String provider;
    Handler h = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getMyLocation();
        setUpMapIfNeeded();

        //Hardcoding marker
		/*MarkerOptions marker = new MarkerOptions().position(new LatLng(37.871993, -122.257862))
				.title("New Clue");
		Marker m = myMap.addMarker(marker);*/
        count = 0;
        /*if(count == 0) {
            db.put(count, new AudioFile("Campanile Tour", 37.871993, -122.257862,""+count, new File(Environment.getExternalStorageDirectory(), "test3.pcm")));
            count += 1;
            db.put(count, new AudioFile("Lost Watch", 37.871934, -122.258120,""+count, new File(Environment.getExternalStorageDirectory(), "test2.pcm")));
            count += 1;
            db.put(count, new AudioFile("Geocaching Clue", 37.873012, -122.258785,""+count, new File(Environment.getExternalStorageDirectory(), "test1.pcm")));
            count += 1;
        }
        addMarkers();*/
        image = (ImageView) findViewById(R.id.compassIcon);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        reload(null);


        addTag = (Button)findViewById(R.id.button1);

        addTag.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddTagActivity.class);
                System.out.println("YAYAYAYAYYAYAYYAOOOOOO:" + myLat);
                intent.putExtra("lat", String.valueOf(myLat));
                intent.putExtra("long", String.valueOf(myLong));
                startActivity(intent);

            }
        });

        myMap.setOnMarkerClickListener(new OnMarkerClickListener(){

            @Override
            public boolean onMarkerClick(Marker arg0) {
                //arg0.showInfoWindow();

                int i = mapper.get(arg0.getSnippet());
                AudioFile af = db.get(i);
                currAf = af;
                System.out.println("CLICK CLICK CLICK CLICK " + af.title);
                Intent intent= new Intent(MainActivity.this, PlayBackActivity.class);
                startActivity(intent);
                return false;
            }
        });

        SystemBarTintManager tintManager = new SystemBarTintManager(com.pestopasta.cluzcs160.MainActivity.this);
        tintManager.setStatusBarTintEnabled(true);
        int actionBarColor = Color.parseColor("#BBffffff");
        tintManager.setStatusBarTintColor(actionBarColor);

        Thread t = new Thread(animateActionBarHide);
        t.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Thread t = new Thread(animateActionBarHide);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
        t.start();
    }

	/*@Override
	protected void onResume() {
		super.onResume();
		getMyLocation();
		setUpMapIfNeeded();

		//Hardcoding marker
		MarkerOptions marker = new MarkerOptions().position(new LatLng(37.871993, -122.257862))
				.title("New Clue").snippet("Take the elevator!");
		myMap.addMarker(marker);


		addTag = (Button)findViewById(R.id.button1);

		addTag.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, AddTagActivity.class);
				System.out.println("YAYAYAYAYYAYAYYAOOOOOO:" + myLat);
				intent.putExtra("lat", String.valueOf(myLat));
				intent.putExtra("long", String.valueOf(myLong));
				startActivity(intent);

			}
		});
	}*/
	/*@Override
	 protected void onResume() {
	super.onResume();
	locationManager.requestLocationUpdates(provider, 400, 1, this);

	 }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);


        }
    }

    @Override
    public void onLocationChanged(Location arg0) {
        // TODO Auto-generated method stub
        //Toast.makeText(this, (myLat + "," + myLong), Toast.LENGTH_LONG);
    }

    @Override
    public void onProviderDisabled(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        // TODO Auto-generated method stub

    }

    public void getMyLocation() {
        MyLocation location = new MyLocation();
        MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {

            @Override
            public void gotLocation(Location loc) {
                currentLocation = loc;
                if (loc != null) {
                    myLat = loc.getLatitude();
                    myLong = loc.getLongitude();
                    latlonglocation = new LatLng(myLat, myLong);
                    cameraPosition = new CameraPosition(latlonglocation, 18, 0,
                            0);
                    updateMap();

                }
            };
        }; // ends LocationResult
        location.getLocation(this, locationResult);

    }// ends getMyLocation method

    private void setUpMapIfNeeded() {
        if (myMap == null) {
            myMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();
            myMap.setMyLocationEnabled(true);
            int actionBarHeight = 0;
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
            {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                SystemBarTintManager tintManager = new SystemBarTintManager(com.pestopasta.cluzcs160.MainActivity.this);
                int statusBarHeight = tintManager.getConfig().getStatusBarHeight();
                myMap.setPadding(0,statusBarHeight+actionBarHeight,0,0);
            } else {
                myMap.setPadding(0, actionBarHeight, 0, 0);
            }
            myMap.setOnMapClickListener(this);
            //myMap.setOnMarkerClickListener(this);
        }
    }

    private void updateMap() {
        CameraUpdate cameraUpdate = CameraUpdateFactory
                .newCameraPosition(cameraPosition);
        myMap.moveCamera(cameraUpdate);
    }

    public void addMarkers() {
        System.out.println("HHHHHHHHEEEEERRRRRRRRE" + count);
        for(int i = 0; i < count; i += 1) {
            if(db.containsKey(i)) {
                System.out.println(i);
                AudioFile temp = db.get(i);
                MarkerOptions marker = new MarkerOptions().position(new LatLng(temp.x, temp.y))
                        .title(temp.title).snippet(temp.mySnip);
                Marker m = myMap.addMarker(marker);
                mapper.put(temp.mySnip, i);
            }
        }
    }


    public GoogleMap myMap;
    public double myLat;
    public double myLong;
    public LatLng latlonglocation;
    private CameraPosition cameraPosition;
    Location currentLocation;


    public static HashMap<Integer, AudioFile> db = new HashMap<Integer, AudioFile> ();
    public static HashMap<String, Integer> mapper = new HashMap<String, Integer> ();
    public static int count;
    public static AudioFile currAf;

	/*public boolean onMarkerClick(Marker marker) {
		// TODO Auto-generated method stub
		System.out.println("Click");
		int i = mapper.get(marker.getSnippet());
		AudioFile af = db.get(i);
		Toast.makeText(this, af.mySnip, Toast.LENGTH_LONG);
		return false;
	}*/

    public void onMapClick(LatLng point) {
        Thread t = new Thread(animateActionBarShow);
        t.start();
    }

    Runnable hideActionbarRunnable = new Runnable() {
        @Override
        public void run() {
            ActionBar bar = getActionBar();
            if (bar != null) {

                bar.hide();
                setUpMapIfNeeded();
                /*
                // Only set the tint if the device is running KitKat or above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    SystemBarTintManager tintManager = new SystemBarTintManager(com.pestopasta.cluzcs160.MainActivity.this);
                    tintManager.setStatusBarTintEnabled(true);
                    tintManager.setTintAlpha(0);
                    int id = getResources().getIdentifier("config_enableTranslucentDecor", "bool", "android");
                    if (id == 0) {
                        // not on KitKat
                    } else {
                        boolean enabled = getResources().getBoolean(id);
                        // enabled = are translucent bars supported on this device
                        if (enabled) {
                            Window w = getWindow();
                            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                        }
                    }
                }
                */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    SystemBarTintManager tintManager = new SystemBarTintManager(com.pestopasta.cluzcs160.MainActivity.this);
                    int statusBarHeight = tintManager.getConfig().getStatusBarHeight();
                    myMap.setPadding(0, statusBarHeight, 0, 0);
                } else {
                    myMap.setPadding(0, 0, 0, 0);
                }
            }
        }
    };

    Runnable showActionbarRunnable = new Runnable() {
        @Override
        public void run() {
            ActionBar bar = getActionBar();
            if (bar != null) {
                bar.show();
                setUpMapIfNeeded();
                int actionBarHeight = 0;
                TypedValue tv = new TypedValue();
                if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                    actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
                }
                // Only set the tint if the device is running KitKat or above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    SystemBarTintManager tintManager = new SystemBarTintManager(com.pestopasta.cluzcs160.MainActivity.this);
                    //tintManager.setStatusBarTintEnabled(true);
                    //int actionBarColor = Color.parseColor("#BBffffff");
                    //tintManager.setStatusBarTintColor(actionBarColor);
                    int statusBarHeight = tintManager.getConfig().getStatusBarHeight();
                    myMap.setPadding(0, statusBarHeight + actionBarHeight, 0, 0);
                } else {
                    myMap.setPadding(0, actionBarHeight, 0, 0);
                }
            }
        }
    };

    Runnable animateActionBarShow = new Runnable() {
        @Override
        public void run() {
            h.post(showActionbarRunnable);
            h.postDelayed(hideActionbarRunnable,8000);
        }
    };

    Runnable animateActionBarHide = new Runnable() {
        @Override
        public void run() {
            h.postDelayed(hideActionbarRunnable,8000);
        }
    };

    public boolean putTag(double latitude, double longitude, String title, Bundle wrapper) {
        //if (wrapper.containsKey("Tags") && wrapper.containsKey("tagTitle") && wrapper.containsKey("tagContentType") && wrapper.containsKey("fileName")) {
        if (wrapper.containsKey("fileName")) {
            CloudEntity tag = new CloudEntity("Tag");
            String tagTitle = wrapper.getString("tagTitle");
            final int tagContentType = wrapper.getInt("tagContentType");
            String fileName = wrapper.getString("fileName");
            tag.put("title", title);
            tag.put("contentType", tagContentType);
            tag.put("latitude", latitude);
            tag.put("longitude", longitude);

            System.out.println("Shit works!");

            final CloudBackend cb;
            try {
                cb = new CloudBackend();
                cb.insert(tag);
            } catch (IOException e) {
                Log.e("Error", "Updating database failed");
                return false;
            }
            return true;
        } else {
            Log.d("Placing tag", "Missing necessary values");
            return false;
        }
    }


    public void getTagsWithinCooords(double minLatitude, double minLongitude, double maxLatitude, double maxLongitude) {
        System.out.println("INCOOORD");
        try {
            CloudBackend cb = new CloudBackend();
            CloudQuery cq = new CloudQuery("Tag");
            //Filter F = new Filter();
            //cq.setFilter(F.and(F.gt("latitude", minLatitude), F.lt("latitude", maxLatitude), F.gt("longitude", minLongitude), F.lt("longitude", maxLongitude)));
            List<CloudEntity> l = cb.list(cq);
            /*CloudEntity[] arr = (CloudEntity[]) l.toArray();
            for (int i =0; i < arr.length; i += 1) {
                System.out.println(arr[i].get("title"));
            }*/
            Iterator<CloudEntity> iter = l.iterator();
            File f = null;
            while (iter.hasNext()) {
                CloudEntity temp = iter.next();
                String tt = (String) temp.get("title");
                double lat = ((BigDecimal) temp.get("latitude")).doubleValue();
                double longt = ((BigDecimal) temp.get("longitude")).doubleValue();
                System.out.println(tt+ " " + lat + " " + longt + " " + count);
                db.put(count, new AudioFile(tt, lat, longt,""+count, f));
                ll.add(new AudioFile(tt, lat, longt,""+ count, f));
                count += 1;
            }
            //addMarkers();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("getTAGERROR", "SHOOT");
        }
    }

    public static LinkedList<AudioFile> ll = new LinkedList<AudioFile>();

    public void listAdd() {
        while (ll.size() != 0) {
            Log.d("ListAdd", ("" + ll.size()));
            AudioFile temp = ll.pollFirst();
            MarkerOptions marker = new MarkerOptions().position(new LatLng(temp.x, temp.y))
                    .title(temp.title).snippet(temp.mySnip);
            Marker m = myMap.addMarker(marker);
        }
    }



    //FLASK STUFF




    //fetch the list of messages from the server
    public void reload(View v) {
        LoadTask task = new LoadTask();
        task.execute();
    }

    private class PostTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String url = "http://justmunim.pythonanywhere.com/messages";

            HttpResponse response;
            HttpClient httpclient = new DefaultHttpClient();
            try {

                HttpPost post = new HttpPost(url);
                List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                postParameters.add(new BasicNameValuePair("name", params[0]));
                postParameters.add(new BasicNameValuePair("comment", MainActivity.currAf.title +"^"+ params[1]));

                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParameters);
                post.setEntity(entity);

                response = httpclient.execute(post);

            } catch (ClientProtocolException e) {
                //TODO Handle problems..
            } catch (IOException e) {
                //TODO Handle problems..
            }

            return null;
        }

        @Override
        protected void onPostExecute(String arg0) {
            reload(null);
        }

    }

    private class DeleteTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String id = params[0];
            String url = "http://justmunim.pythonanywhere.com/messages/" + id;
            HttpResponse response;
            HttpClient httpclient = new DefaultHttpClient();
            try {
                HttpDelete delete = new HttpDelete(url);
                response = httpclient.execute(delete);

            } catch (ClientProtocolException e) {
                //TODO Handle problems..
            } catch (IOException e) {
                //TODO Handle problems..
            }
            return null;
        }

        @Override
        protected void onPostExecute(String arg0) {
            reload(null);
        }

    }

    private class LoadTask extends AsyncTask<Void, Void, JSONArray> {

        protected JSONArray doInBackground(Void...arg0) {
            String url = "http://justmunim.pythonanywhere.com/messages";
            HttpResponse response;
            HttpClient httpclient = new DefaultHttpClient();
            String responseString = "";

            try {
                response = httpclient.execute(new HttpGet(url));
                if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();

                } else{
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(response.getStatusLine().getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                //TODO Handle problems..
            } catch (IOException e) {
                //TODO Handle problems..
            }
            try {
                JSONArray messages = new JSONArray(responseString);
                return messages;
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }


        protected void onPostExecute(JSONArray itemsList) {
            //data.clear();
            //String temp = MainActivity.currAf.title +"^";
            for (int i = itemsList.length(); i > 0; i--) {
                //System.out.println("BAHHAHAHAHHAHAHAHAHAHHA");
                try {
                    JSONArray current = itemsList.getJSONArray(i - 1);
                    Map<String, String> listItem = new HashMap<String, String>(2);
                    //if (current.getString(2).contains(temp)) {
                        listItem.put("ID", current.getString(0));
                        listItem.put("Name", current.getString(1));
                        listItem.put("Comment", current.getString(2));
                        String tt = current.getString(1);
                        String[] arr = current.getString(2).split(" ");
                        double d1 = Double.parseDouble(arr[0]);
                        double d2 = Double.parseDouble(arr[1]);
                        db.put(count, new AudioFile(tt, d1, d2,""+count, null));
                        count += 1;
                        //data.add(listItem);
                    //}

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            //adapter.notifyDataSetChanged();
            addMarkers();
        }

    }

    // define the display assembly compass picture
    private ImageView image;

    // record the compass picture angle turned
    private float currentDegree = 0f;

    // device sensor manager
    private SensorManager mSensorManager;

    @Override
    public void onSensorChanged(SensorEvent event) {

        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        image.startAnimation(ra);
        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }

    public class Compass extends Activity implements SensorEventListener {

        // define the display assembly compass picture
        private ImageView image;

        // record the compass picture angle turned
        private float currentDegree = 0f;

        // device sensor manager
        private SensorManager mSensorManager;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //setContentView(R.layout.compass);
            image = (ImageView) findViewById(R.id.compassIcon);


            // initialize your android device sensor capabilities
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        }

		/* @Override
	    public void onCreateView(View v){

	    }*/

        @Override
        public void onResume() {
            super.onResume();

            // for the system's orientation sensor registered listeners
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                    SensorManager.SENSOR_DELAY_GAME);
        }

        @Override
        public void onPause() {
            super.onPause();

            // to stop the listener and save battery
            mSensorManager.unregisterListener(this);
        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            // get the angle around the z-axis rotated
            float degree = Math.round(event.values[0]);

            // create a rotation animation (reverse turn degree degrees)
            RotateAnimation ra = new RotateAnimation(
                    currentDegree,
                    -degree,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            // how long the animation will take place
            ra.setDuration(210);

            // set the animation after the end of the reservation status
            ra.setFillAfter(true);

            // Start the animation
            image.startAnimation(ra);
            currentDegree = -degree;

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
}
