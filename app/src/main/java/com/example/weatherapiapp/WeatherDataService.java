package com.example.weatherapiapp;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WeatherDataService {

    public static final String QUERY_FOR_CITYID = "https://www.metaweather.com/api/location/search/?query=";
    public static final String QUERY_FOR_CITY_WEATHER_BY_ID = "https://www.metaweather.com/api/location/";

    Context context;
    String cityID;

    public WeatherDataService(Context context) {
        this.context = context;
    }

    public interface VolleyResponseListener {
        void onError(String message);
        void onResponse(String cityID);
    }

    public void getCityID(String cityName, VolleyResponseListener volleyResponseListener){
        String url = QUERY_FOR_CITYID + cityName;
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                cityID= "";
                try {
                    JSONObject cityInfo = response.getJSONObject(0);
                    cityID = cityInfo.getString("woeid");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                volleyResponseListener.onResponse(cityID);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyResponseListener.onError("something wrong");
            }
        });
        // Add the request to the RequestQueue.
        MySingleton.getInstance(context).addToRequestQueue(request);
    }

    public interface ForecastByIDResponse {
        void onError(String message);
        void onResponse(List<WeatherReportModel> weatherReportModels);
    }

    public void getCityForecastByID(String cityID, ForecastByIDResponse forecastByIDResponse){
        List<WeatherReportModel> weatherReportModels = new ArrayList<>();
        String url = QUERY_FOR_CITY_WEATHER_BY_ID + cityID;
        //get the json object
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //Toast.makeText(context, response.toString(), Toast.LENGTH_SHORT).show();
                try {
                    JSONArray consolidated_weather_list = response.getJSONArray("consolidated_weather");
                    //get 1st item in array
                    for (int i = 0; i < consolidated_weather_list.length(); i++){
                        WeatherReportModel oneDay = new WeatherReportModel();

                        JSONObject first_day_from_api = (JSONObject) consolidated_weather_list.get(i);
                        oneDay.setId(first_day_from_api.getInt("id"));
                        oneDay.setWeather_state_name(first_day_from_api.getString("weather_state_name"));
                        oneDay.setWeather_state_abbr(first_day_from_api.getString("weather_state_abbr"));
                        oneDay.setWind_direction_compass(first_day_from_api.getString("wind_direction_compass"));
                        oneDay.setCreated(first_day_from_api.getString("created"));
                        oneDay.setApplicable_date(first_day_from_api.getString("applicable_date"));
                        oneDay.setMin_temp(first_day_from_api.getLong("min_temp"));
                        oneDay.setMax_temp(first_day_from_api.getLong("max_temp"));
                        oneDay.setThe_temp(first_day_from_api.getLong("the_temp"));
                        oneDay.setWind_speed(first_day_from_api.getLong("wind_speed"));
                        oneDay.setWind_direction(first_day_from_api.getLong("wind_direction"));
                        oneDay.setAir_pressure(first_day_from_api.getInt("air_pressure"));
                        oneDay.setHumidity(first_day_from_api.getInt("humidity"));
                        oneDay.setVisibility(first_day_from_api.getLong("visibility"));
                        oneDay.setPredictability(first_day_from_api.getInt("predictability"));
                        weatherReportModels.add(oneDay);
                    }
                    forecastByIDResponse.onResponse(weatherReportModels);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
                //get the property called "consolidated_weather" which is an array

                //get each item in the array and assign it to a new WeatherReportModel object

        // Add the request to the RequestQueue.
        MySingleton.getInstance(context).addToRequestQueue(request);
    }

    public interface GetCityForecastByNameCallback {
        void onError(String message);
        void onResponse(List<WeatherReportModel> weatherReportModels);
    }

    public void getCityForecastByName(String cityName, GetCityForecastByNameCallback getCityForecastByNameCallback){
        //fetch the city id given the city name
        getCityID(cityName, new VolleyResponseListener() {
            @Override
            public void onError(String message) {

            }

            @Override
            public void onResponse(String cityID) {
                //now we have the city id
                getCityForecastByID(cityID, new ForecastByIDResponse() {
                    @Override
                    public void onError(String message) {

                    }

                    @Override
                    public void onResponse(List<WeatherReportModel> weatherReportModels) {
                        //we have the weather report
                        getCityForecastByNameCallback.onResponse(weatherReportModels);
                    }
                });
            }
        });
        //fetch city forecast given city id
    }
}
