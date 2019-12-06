package nl.androidappfactory.weatherplus;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import nl.androidappfactory.weatherplus.data.Weather;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

public class MainActivity extends Activity implements LocationListener {

    private Weather weather;
    private TextView tvCurrentTemp;
    private TextView tvWind;
    private TextView tvPressure;
    private TextView tvHumidity;
    private TextView tvSunsetSunRise;

    private Location location;
    private LocationManager locationManager;
    private String provider;

    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private static final long MIN_DISTANCE_CHANGE = 10; // 10 meters
    private static final long MIN_TIME_UPDATES = 1000 * 60; // 1 minute

    private static final String[] INITIAL_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    ImageView ivIcon;
    private static final String API_URL = "http://api.openweathermap.org/data/2.5/weather?";
    private static final String API_KEY = "778cf1f4833e31ec3f0d6c5916e48724";
    private static final String UNITS = "&units=metric";
    private static final String IMAGE_URL = "http://openweathermap.org/img/w/";
    private static final String ENCODING = "UTF-8";
    private static final String DEFAULT_LOCATION = "Eindhoven";

    private static final int INITIAL_REQUEST = 1337;
    private static final int CAMERA_REQUEST = INITIAL_REQUEST + 1;
    private static final int CONTACTS_REQUEST = INITIAL_REQUEST + 2;
    private static final int LOCATION_REQUEST = INITIAL_REQUEST + 3;
    private EditText etLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        tvCurrentTemp = (TextView) findViewById(R.id.etCurrentTemp);

        tvWind = (TextView) findViewById(R.id.etWind);
        tvPressure = (TextView) findViewById(R.id.etPressure);
        tvHumidity = (TextView) findViewById(R.id.etHumidity);
        tvSunsetSunRise = (TextView) findViewById(R.id.etSunsetRise);
        ivIcon = (ImageView) findViewById(R.id.ivIcon);
        etLocation = ((EditText) findViewById(R.id.etLocation));

        if (!canAccessLocation()) {
            requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
        }
//        location = getLocation(this);
//        Log.d("Location info", location != null ? location.toString() : "null");

        getWeatherByCurrentLocation(null);

    }

    public void getWeatherByCurrentLocation(View view) {

        // by making etLocation empty the getWeather(View view) method will search on current location
        etLocation.setText(null);

        location = getLocation(this);
        Log.d("By current location", location.toString());

        String param;
        if (location != null) {
            param = "lat=" + location.getLatitude() + "&lon=" + location.getLongitude();
        } else {
            param = "q=" + DEFAULT_LOCATION;
        }
        String url = API_URL + param + "&appid=" + API_KEY + UNITS;

        getWeather(url);
    }

    public void getWeatherByInput(View view) {

        String param = null;
        try {
            param = "q=" + URLEncoder.encode(etLocation.getText().toString().trim(), ENCODING);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url = API_URL + param + "&appid=" + API_KEY + UNITS;

        getWeather(url);
    }

    public void getWeather(String url) {

        weather = getWeatherData(url);

        ivIcon.setImageBitmap(null);

        if (weather != null) {
            // get icon http://openweathermap.org/img/w/......
            ImageReader reader = new ImageReader();
            reader.execute(IMAGE_URL + weather.getIcon() + ".png");
            // hide keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etLocation.getWindowToken(), 0);

            etLocation.setText(concatStrings(weather.getCity(), getString(R.string.comma), weather.getCountry()));
            tvCurrentTemp.setText(formatOneDecimal(weather.getCurrentTemp()));
            tvWind.setText(concatStrings(weather.getWindDirections(), getString(R.string.hyphen), weather.getWindSpeedBeuafort(), getString(R.string.Bft), getString(R.string.newLine), weather.getWindSpeed(), getString(R.string.m_s)));
            tvPressure.setText(concatStrings(weather.getPressure(), getString(R.string.hPa)));
            tvHumidity.setText(concatStrings(weather.getHumidity(), " %"));
            tvSunsetSunRise.setText(concatStrings(weather.getSunRiseTime(), "\n", weather.getSunSetTime()));
        } else {
            tvCurrentTemp.setText("");
            tvWind.setText("");
            tvPressure.setText("");
            tvHumidity.setText("");
            tvSunsetSunRise.setText("");
            Toast.makeText(getApplicationContext(), "Location niet gevonden", Toast.LENGTH_LONG).show();
        }
    }

    private Weather getWeatherData(String url) {

        HtmlDataReader reader = new HtmlDataReader();
        Weather weather = null;
        try {
            Log.d("URL", url);

            String weatherData = reader.execute(url).get();

            if (weatherData != null) {
                Log.d("HtmlDataReader: ", weatherData);

                JSONObject jsonObject = new JSONObject(weatherData);

                String cod = jsonObject.isNull("cod") ? null : jsonObject.getString("cod");
                String name = jsonObject.isNull("name") ? null : jsonObject.getString("name");
                Log.d("cod", cod);

                if ((cod != null && cod.equals("200")) && (name != null)) {

                    Log.d("Name", name);
                    weather = new Weather();

                    JSONObject main = jsonObject.isNull("main") ? null : jsonObject.getJSONObject("main");
                    weather.setCity(name);
                    if (main != null) {
                        weather.setCurrentTemp(main.isNull("temp") ? null : main.getString("temp"));
                        weather.setMinTemp(main.isNull("temp_min") ? null : main.getString("temp_min"));
                        weather.setMaxTemp(main.isNull("temp_max") ? null : main.getString("temp_max"));
                        weather.setHumidity(main.isNull("humidity") ? null : main.getString("humidity"));
                        weather.setPressure(main.isNull("pressure") ? null : main.getString("pressure"));
                    }
                    JSONObject wind = jsonObject.isNull("wind") ? null : jsonObject.getJSONObject("wind");
                    if (wind != null) {
                        weather.setWindSpeed(wind.getString("speed"));
                        weather.setWindDeg(wind.getString("deg"));
                    }
                    JSONArray weatherArray = jsonObject.isNull("weather") ? null : jsonObject.getJSONArray("weather");
                    if (weatherArray != null && !weatherArray.isNull(0)) {
                        weather.setDescription(weatherArray.getJSONObject(0).getString("description"));
                        weather.setIcon(weatherArray.getJSONObject(0).getString("icon"));
                    }
                    JSONObject sys = jsonObject.isNull("sys") ? null : jsonObject.getJSONObject("sys");
                    if (sys != null) {
                        weather.setCountry(sys.isNull("country") ? null : sys.getString("country"));
                        weather.setSunRise(sys.isNull("sunrise") ? null : sys.getString("sunrise"));
                        weather.setSunSet(sys.isNull("sunset") ? null : sys.getString("sunset"));
                    }
                    Log.d("weather", weather.toString());
                }
            }
        } catch (InterruptedException | ExecutionException | JSONException | NullPointerException e) {
            e.printStackTrace();
        }
        return weather;
    }

    private Location getLocation(Context context) {

        Location theLocation = null;

        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isGPSEnabled || isNetworkEnabled)
            if (isNetworkEnabled) {
                // Permission check required as of version 23
                checkPermission();
                Log.d("Network", "Network");
                if (locationManager != null) {
                    theLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }
        // if GPS Enabled get lat/long using GPS Services
        if (isGPSEnabled) {
            if (theLocation == null) {
                Log.d("GPS Enabled", "GPS Enabled");
                if (locationManager != null) {
                    theLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
            }
        }
        return theLocation;
    }

    private String formatOneDecimal(String s) {

        String result;
        try {
            result = String.format("%.1f", Double.parseDouble(s));
        } catch (Exception e) {
            result = s;
        }
        return result;
    }

    @Override
    public void onLocationChanged(Location location) {

        Log.d("Location info", "location changed: " + location.toString());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public class ImageReader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {

            Bitmap bitmap = null;
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream is = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            super.onPostExecute(bitmap);
            if (bitmap != null) {
                ivIcon.setImageBitmap(bitmap);
            }
        }
    }

    public class HtmlDataReader extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            StringBuffer sb = null;
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                InputStream is = null;
                InputStream is = connection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                sb = new StringBuffer();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return (sb == null) ? null : sb.toString();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String concatStrings(String... strings) {

        StringBuilder sb = new StringBuilder();

        for (String string : strings) {
            sb.append(string);
        }

        return sb.toString();
    }

    private boolean canAccessLocation() {

        return (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private boolean hasPermission(String perm) {

        return (PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm));
    }

    private boolean checkPermission() {


        return Build.VERSION.SDK_INT >= 23 && !(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED);

    }
}
