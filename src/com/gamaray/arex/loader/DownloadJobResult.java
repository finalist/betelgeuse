package com.gamaray.arex.loader;

import com.gamaray.arex.ARXDownload;

public class DownloadJobResult {

    private final int format;
    private final Object obj;
    private final boolean error;
    private final String errorMsg;
    private final DownloadJobRequest errorRequest;

    public DownloadJobResult(int format, Object obj) {
        super();
        this.format = format;
        this.obj = obj;
        this.error = false;
        this.errorMsg = null;
        this.errorRequest = null;
    }


    public DownloadJobResult(String errorMsg,DownloadJobRequest downloadJobRequest) {
        super();
        this.format = ARXDownload.NONE;
        this.obj = null;
        this.error = true;
        this.errorMsg = errorMsg;
        this.errorRequest = downloadJobRequest;
    }


    public int getFormat() {
        return format;
    }


    public Object getObj() {
        return obj;
    }


    public boolean isError() {
        return error;
    }


    public String getErrorMsg() {
        return errorMsg;
    }


    public DownloadJobRequest getErrorRequest() {
        return errorRequest;
    }
    
    
}
