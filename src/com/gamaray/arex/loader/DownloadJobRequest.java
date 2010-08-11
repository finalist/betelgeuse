package com.gamaray.arex.loader;

public class DownloadJobRequest {

    private final int format;
    private final String url;
    private final String params;
    private final boolean cacheable;

    public DownloadJobRequest(int format, String url, String params, boolean cacheable) {
        super();
        this.format = format;
        this.url = url;
        this.params = params;
        this.cacheable = cacheable;
    }


    public int getFormat() {
        return format;
    }

    public String getUrl() {
        return url;
    }

    public String getParams() {
        return params;
    }

    public boolean isCacheable() {
        return cacheable;
    }

}
