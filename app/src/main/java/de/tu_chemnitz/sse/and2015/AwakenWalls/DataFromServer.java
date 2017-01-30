package de.tu_chemnitz.sse.and2015.AwakenWalls;

/**
 * Created by mohammadasif on 08/01/2017.
 */

//Class for sensors data fetched from server

public class DataFromServer {

    private String temperature;
    private String humidity;
    private String light;
    private String distance;

    public DataFromServer(String temperature,String humidity, String light, String distance) {
        setHumidity(humidity);
        setLight(light);
        setTemperature(temperature);
        setDistance(distance);
    }

    public float getDistance() {
        return Float.parseFloat(distance);
    }

    public void setDistance(String distance) {

        this.distance = distance;
    }

    public float getHumidity() {
        return Float.parseFloat(humidity);
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public float getLight() {
        return Float.parseFloat(light);
    }

    public void setLight(String light) {
        this.light = light;
    }

    public float getTemperature() {
        return Float.parseFloat(temperature);
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }
}
