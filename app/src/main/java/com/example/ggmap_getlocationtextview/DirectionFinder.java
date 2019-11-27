package com.example.ggmap_getlocationtextview;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DirectionFinder {
    private double currentLatitude;
    private double currentLongtitude;
    private double wasteLatitude;
    private double wasteLongtitude;
    private DirectionFinderListener listener;

    public DirectionFinder(DirectionFinderListener listener, double currentLatitude, double currentLongtitude,
                           double wasteLatitude, double wasteLongtitude) {
        this.listener = listener;
        this.currentLatitude = currentLatitude;
        this.currentLongtitude = currentLongtitude;
        this.wasteLatitude = wasteLatitude;
        this.wasteLongtitude = wasteLongtitude;
    }

    public void execute() {
        new DownloadRawData().execute(createURL());
    }

    public String createURL() {
        String DIRECTION_URL = "https://api.mapbox.com/directions/v5/mapbox/driving/";
        String olat = String.valueOf(currentLatitude);
        String olon = String.valueOf(currentLongtitude);
        String dlat = String.valueOf(wasteLatitude);
        String dlon = String.valueOf(wasteLongtitude);
        String accessToken = "pk.eyJ1IjoiZGlldWhpZW4xMDM2IiwiYSI6ImNrMzM2NGVsbzBoeGMzaW1tdWRoYWpsNzgifQ.Fpf-Zqy6gRMgjS-2ixcXuA";
        return DIRECTION_URL + olon + "," + olat + ";" + dlon + "," + dlat + "?" + "access_token=" + accessToken;
    }

    public class DownloadRawData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String link = params[0];
            try {
                URL url = new URL(link);
                InputStream is = url.openConnection().getInputStream();
                StringBuffer buffer = new StringBuffer();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                return buffer.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                parseJSon(s);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void parseJSon(String data) throws JSONException {
        if (data == null) {
            return;
        }
        Route route = new Route();
        List<Route> routes = new ArrayList<>();
        JSONObject jsonData = new JSONObject(data);
        JSONArray jsonRoutes = jsonData.getJSONArray("routes");

        double distance = 0;
        double duration = 0;
        String jsonGeometry = null;
        for (int i = 0; i < jsonRoutes.length(); i++) {
            JSONObject jsonRoute = jsonRoutes.getJSONObject(i);

            jsonGeometry = jsonRoute.getString("geometry");
            distance = Math.round(jsonRoute.getDouble("distance")/1000);
            duration = Math.round(jsonRoute.getDouble("duration")/60);
        }

        route.distance = distance;
        route.duration = duration;
        route.points = decodePolyLine(jsonGeometry);

        routes.add(route);
        listener.setText(routes);
    }

    private List<LatLng> decodePolyLine(final String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }
}


