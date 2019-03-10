package com.app.wikidriver.models;

public class Flight {

    private String startCity;
    private String startCityCode;
    private long departureTime;
    private String endCity;
    private String endCityCode;
    private long arrivalTime;
    private String flightId;

    public Flight() {
    }

    public Flight(String startCity, String startCityCode, long arrivalTime, String flightId, String status, long departureTime, String endCity, String endCityCode) {
        this.startCity = startCity;
        this.startCityCode = startCityCode;
        this.arrivalTime = arrivalTime;
        this.flightId = flightId;
        this.departureTime = departureTime;
        this.endCity = endCity;
        this.endCityCode = endCityCode;
    }

    public String getStartCity() {
        return startCity;
    }

    public void setStartCity(String startCity) {
        this.startCity = startCity;
    }

    public String getStartCityCode() {
        return startCityCode;
    }

    public void setStartCityCode(String startCityCode) {
        this.startCityCode = startCityCode;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(long arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    public long getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(long departureTime) {
        this.departureTime = departureTime;
    }

    public String getEndCity() {
        return endCity;
    }

    public void setEndCity(String endCity) {
        this.endCity = endCity;
    }

    public String getEndCityCode() {
        return endCityCode;
    }

    public void setEndCityCode(String endCityCode) {
        this.endCityCode = endCityCode;
    }
}
