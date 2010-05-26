package com.gamaray.arex.io;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class AndroidHttpInputStream extends ARXHttpInputStream {
    private HttpURLConnection conn;

    public AndroidHttpInputStream(HttpURLConnection conn, InputStream is, int contentLength) {
        super(new BufferedInputStream(is), contentLength);

        this.conn = conn;
    }

    public void close() throws IOException {
        super.close();

        conn.disconnect();
    }
}
