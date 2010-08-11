package com.gamaray.arex.model;

public class Location {

    private final String id;
    private final Double lat;
    private final Double lon;
    private final Double alt;

    public Location(String id, Double lat, Double lon, Double alt) {
        super();
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
    }

    public String getId() {
        return id;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }

    public Double getAlt() {
        return alt;
    }

    @Override
    public String toString() {
        return "Location [id=" + id + ", lat=" + lat + ", lon=" + lon + ", alt=" + alt + "]";
    }

    
    
}
