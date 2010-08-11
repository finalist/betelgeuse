package com.gamaray.arex;

import com.gamaray.arex.loader.DownloadJobResult;

public class Asset {
    String xmlId;
    String xmlName;
    String xmlUrl;
    int xmlFormat;

    // Download properties
    int downloadStatus = ARXState.NOT_STARTED;
    String downloadJobId;
    DownloadJobResult downloadResult;
    boolean isDownloadJobActive;
    float downloadJobPctComplete;

    public void copyDownloadState(Asset dest) {
        Asset src = this;

        if (src.downloadStatus == ARXState.READY) {
            if (dest.xmlUrl.equals(src.xmlUrl)) {
                dest.downloadStatus = src.downloadStatus;
                dest.downloadJobId = src.downloadJobId;
                dest.downloadResult = src.downloadResult;
            }
        }
    }
}
