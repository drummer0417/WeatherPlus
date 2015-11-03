package nl.androidappfactory.weatherplus;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

import nl.androidappfactory.weatherplus.data.Weather;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private Weather weather;
    TextView tvCurrentTemp;
    TextView tvWind;
    TextView tvPressure;
    TextView tvHumidity;
    TextView tvSunsetSunRise;

    Location location;
    LocationManager locationManager;
    String provider;

    ImageView ivIcon;
    private static final String API_URL = "http://api.openweathermap.org/data/2.5/weather?";
    private static final String API_KEY = "778cf1f4833e31ec3f0d6c5916e48724";
    private static final String UNITS = "&units=metric";
    private static final String IMAGE_URL = "http://openweathermap.org/img/w/";
    private static final String ENCODING = "UTF-8";

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

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        if (checkPermission()){
            return;
        }
        location = locationManager.getLastKnownLocation(provider);
        Log.d("Location info", location.toString());

//        etLocation.setText("Eindhoven");
        getWeather(null);

    }

    public void getWeaterCurrentLocation(View view){

        // by making etLocation empty the getWeather(View view) methoc will search on current location
        etLocation.setText(null);
        getWeather(view);
    }

    public void getWeather(View view) {


        String sLocation = etLocation.getText().toString().trim();

        weather = getWeatherData(sLocation);

        ivIcon.setImageBitmap(null);

        if (weather != null) {
            // get icon http://openweathermap.org/img/w/......
            ImageReader reader = new ImageReader();
            reader.execute(IMAGE_URL + weather.getIcon() + ".png");
            // hide keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etLocation.getWindowToken(), 0);

            etLocation.setText(weather.getCity() + ", " + weather.getCountry());
            tvCurrentTemp.setText(formatOneDecimal(weather.getCurrentTemp()));
            tvWind.setText(weather.getWindDirections() + " - " + weather.getWindSpeedBeuafort() + " Bft\n" + weather.getWindSpeed() + " m/s");
            tvPressure.setText(weather.getPressure() + " hPa");
            tvHumidity.setText(weather.getHumidity() + " %");
            tvSunsetSunRise.setText(weather.getSunRiseTime() + "\n" + weather.getSunSetTime());
        } else {
            tvCurrentTemp.setText("");
            tvWind.setText("");
            tvPressure.setText("");
            tvHumidity.setText("");
            tvSunsetSunRise.setText("");
            Toast.makeText(getApplicationContext(), "Location niet gevonden", Toast.LENGTH_LONG).show();
        }
    }

    private Weather getWeatherData(String sLocation) {

        HtmlDataReader reader = new HtmlDataReader();
        Weather weather = null;
        try {
            String param = null;
            if (sLocation != null && sLocation.length() != 0) {
                param = "q=" + URLEncoder.encode(sLocation, ENCODING);
            } else {
                param = "lat=" + location.getLatitude() + "&lon=" + location.getLongitude();
            }

            String url = API_URL + param + "&appid=" + API_KEY + UNITS;
            Log.d("URL", url);

            String weatherData = reader.execute(url).get();

            if (weatherData != null) {
                Log.d("HtmlDataReader: ", weatherData);

                JSONObject jsonObject = new JSONObject(weatherData);

                String cod = jsonObject.isNull("cod") ? null : jsonObject.getString("cod");
                String name = jsonObject.isNull("name") ? null : jsonObject.getString("name");
                Log.d("cod", cod);

//                int index = sLocation.indexOf(",");
//                String city = null;
//                if (index < 0) {
//                    city = location;
//                } else {
//                    city = location.substring(0, index);
//                    Log.d("city", city);
//                }
//                if ((cod != null && cod.equals("200")) && (name != null && name.equalsIgnoreCase(city))) {
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
                    if (weather != null) {
                        weather.setDescription(weatherArray.getJSONObject(0).getString("description"));
                        weather.setIcon(weatherArray.getJSONObject(0).getString("icon"));
                    }
                    JSONObject sys = jsonObject.isNull("sys") ? null : jsonObject.getJSONObject("sys");

                    weather.setCountry(sys.isNull("country") ? null : sys.getString("country"));
                    weather.setSunRise(sys.isNull("sunrise") ? null : sys.getString("sunrise"));
                    weather.setSunSet(sys.isNull("sunset") ? null : sys.getString("sunset"));

                    Log.d("weather", weather.toString());
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return weather;
    }

    private String formatOneDecimal(String s) {

        String result = null;
        try {
            result = String.format("%.1f", Double.parseDouble(s));
        } catch (Exception e) {
            result = s;
        }
        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermission()){
            return;
        }
        locationManager.requestLocationUpdates(provider, 60000l, 100f, this);
        Log.d("Location info", "Updates requested");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (checkPermission()){
            return;
        }
        locationManager.removeUpdates(this);
        Log.d("Location info", "Updates removed");
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
            } catch (MalformedURLException e) {
                e.printStackTrace();
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
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
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

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return false;
            }else {
                return true;
            }
        } else {
            return false;
        }
    }
}
