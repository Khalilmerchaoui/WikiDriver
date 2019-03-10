package com.app.wikidriver.fragments;



import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.app.wikidriver.R;
import com.app.wikidriver.adapters.FlightListAdapter;
import com.app.wikidriver.models.Airport;
import com.app.wikidriver.models.Flight;
import com.app.wikidriver.utils.Config;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class CalendarFragment extends Fragment {//implements OnMapReadyCallback {


    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private Spinner spAirport;
    private SpotsDialog spotsDialog;
    private RecyclerView recyclerView;
    private FlightListAdapter flightListAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<String> airportNames = new ArrayList<>();

    private List<Flight> flightList = new ArrayList<>();

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        spotsDialog = new SpotsDialog(getActivity(), R.style.CustomRegister);

        spAirport = getActivity().findViewById(R.id.spAirport);
        recyclerView = getActivity().findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);


        for(Airport airport : Config.getAirportName(getActivity()))
            airportNames.add(airport.getName());

        final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.airport_spinner_item_layout, airportNames);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);

        spAirport.setAdapter(dataAdapter);
        if(spAirport.getSelectedItem() == null) {
            spAirport.setSelection(0);
        }
        //spotsDialog.show();
        spAirport.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                flightList = new ArrayList<>();
                int offset = 0;
                counter = 0;
               // try {
                    /*if(!new APIAsync().execute(position, offset).get()) {
                        flightListAdapter = new FlightListAdapter(getActivity(), flightList);
                        recyclerView.setAdapter(flightListAdapter);

                    }else {*/
                    flightListAdapter = new FlightListAdapter(getActivity(), flightList);
                    recyclerView.setAdapter(flightListAdapter);

                    APIAsync apiAsync = new APIAsync();
                    apiAsync.execute(position, offset);// + 15);

                    //}
                /*} catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }*/


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        /*SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setUpLocation();*/
        getActivity().getTheme().applyStyle(R.style.AppTheme, true);
         return inflater.inflate(R.layout.calendar_fragment, container, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }

        }
    }


    class APIAsync extends AsyncTask<Integer, Void, String> {

        String username = "messaoudiatef";
        String apiKey = "6c491b8aae5fad72e6617c6ef0607c0b41c72d89";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(!spotsDialog.isShowing())
                spotsDialog.show();
        }

        int position, offset;
        @Override
        protected String doInBackground(Integer... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.

            position = params[0];
            offset = params[1];
            String airportCode = Config.getAirportName(getActivity()).get(position).getCode();
            String urlString = "http://flightxml.flightaware.com/json/FlightXML2/Enroute?airport=" + airportCode + "&filter=airline&howMany=15&offset="+ offset;
            String credentials = username + ":" + apiKey;
            String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL(urlString);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Authorization", "Basic " + base64EncodedCredentials);
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();

                try {
                    JSONObject jsonObject = new JSONObject(forecastJsonStr.toString());
                    JSONObject enrouteObject = jsonObject.getJSONObject("EnrouteResult");
                    JSONArray enrouteArray = enrouteObject.getJSONArray("enroute");

                    Log.i("enroute", enrouteArray.toString(2));

                    for (int i = 0; i < enrouteArray.length(); i++) {
                        JSONObject flightObject = enrouteArray.getJSONObject(i);
                        String originCode = flightObject.getString("origin");
                        String originCity = flightObject.getString("originCity");
                        String endCityCode = flightObject.getString("destination");
                        String endCity = flightObject.getString("destinationCity");
                        String id = flightObject.getString("ident");
                        long time = System.currentTimeMillis() / 1000;
                        long departureTime = flightObject.getLong("filed_departuretime");
                        long arrivalTime = flightObject.getLong("estimatedarrivaltime");


                        Flight flight = new Flight();
                        flight.setFlightId(id);
                        flight.setStartCityCode(Config.toIATACode(getActivity(), originCode));
                        flight.setStartCity(originCity);
                        flight.setArrivalTime(arrivalTime);
                        flight.setDepartureTime(departureTime);
                        flight.setEndCity(endCity);
                        flight.setEndCityCode(Config.toIATACode(getActivity(), endCityCode));


                        if (arrivalTime - time > 0) {
                            flightList.add(flight);
                        }
                    }

                    Collections.sort(flightList, new Comparator<Flight>() {
                        @Override
                        public int compare(Flight flight1, Flight flight2) {
                            return String.valueOf(flight1.getArrivalTime()).compareTo(String.valueOf(flight2.getArrivalTime()));
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return forecastJsonStr;
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            Log.i("json", response);

            if (flightList.size() < 15) {
                new APIAsync().execute(position, offset + 15);
            } else {
                spotsDialog.dismiss();
                flightListAdapter = new FlightListAdapter(getActivity(), flightList);
                recyclerView.setAdapter(flightListAdapter);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        flightListAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }
    int counter = 0;
}
