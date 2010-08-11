package com.gamaray.arex.model;

public class Asset {

    private final String id;
    private final String format;
    private final String url;

    public Asset(String id, String format, String url) {
        super();
        this.id = id;
        this.format = format;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public String getFormat() {
        return format;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "Asset [id=" + id + ", format=" + format + ", url=" + url + "]";
    }

    
    
}
