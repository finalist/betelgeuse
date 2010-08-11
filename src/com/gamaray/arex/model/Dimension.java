package com.gamaray.arex.model;

import java.util.Collection;

public class Dimension {

    private final String name;
    private final String version;
    private final String playSoundUrl;
    private final Boolean relativeAltitude;
    private final Boolean radarAvailable;
    private final Boolean waitForAssets;
    private final String refreshUrl;
    private final Integer refreshTime;
    private final Integer refreshDistance;
    private final Integer radarRange;

    private final Collection<Location> locations;
    private final Collection<Asset> assets;
    private final Collection<Feature> features;
    private final Collection<Overlay> overlays;

    public Dimension(String name, String version, String playSoundUrl, Boolean relativeAltitude,
            Boolean radarAvailable, Boolean waitForAssets, String refreshUrl, Integer refreshTime, Integer refreshDistance,
            Integer radarRange, Collection<Location> locations, Collection<Asset> assets, Collection<Feature> features,
            Collection<Overlay> overlays) {
        super();
        this.name = name;
        this.version = version;
        this.relativeAltitude = relativeAltitude;
        this.radarAvailable = radarAvailable;
        this.refreshUrl = refreshUrl;
        this.refreshTime = refreshTime;
        this.refreshDistance = refreshDistance;
        this.radarRange = radarRange;
        this.locations = locations;
        this.assets = assets;
        this.features = features;
        this.overlays = overlays;
        this.playSoundUrl = playSoundUrl;
        this.waitForAssets = waitForAssets;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public Boolean getRelativeAltitude() {
        return relativeAltitude;
    }

    public String getRefreshUrl() {
        return refreshUrl;
    }

    public Integer getRefreshTime() {
        return refreshTime;
    }

    public Integer getRefreshDistance() {
        return refreshDistance;
    }

    public Integer getRadarRange() {
        return radarRange;
    }

    public Collection<Location> getLocations() {
        return locations;
    }

    public Collection<Asset> getAssets() {
        return assets;
    }

    public Collection<Feature> getFeatures() {
        return features;
    }

    public Collection<Overlay> getOverlays() {
        return overlays;
    }

    public String getPlaySoundUrl() {
        return playSoundUrl;
    }

    public Boolean getRadarAvailable() {
        return radarAvailable;
    }

    public Boolean getWaitForAssets() {
        return waitForAssets;
    }


}
