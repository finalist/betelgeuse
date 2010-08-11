package com.gamaray.arex;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.gamaray.arex.context.ARXContext;
import com.gamaray.arex.format3d.GAMA3DHandler;
import com.gamaray.arex.io.ARXHttpInputStream;
import com.gamaray.arex.loader.DimensionParser;
import com.gamaray.arex.loader.DownloadJobRequest;
import com.gamaray.arex.loader.DownloadJobResult;
import com.gamaray.arex.loader.Loader;
import com.gamaray.arex.model.Dimension;

public class ARXDownload implements Runnable {
    public final static int NONE = 0, GAMADIM = 1, GAMA3D = 2, OBJ = 3, PNG = 4, JPG = 4;
    public static final int NOT_STARTED = 0, WAITING = 1, WORKING = 2, PAUSED = 3, STOPPED = 4;

    private boolean stop = false, pause = false, action = false;
    private int state = NOT_STARTED;

    private int id = 0;
    private Map<String,DownloadJobRequest> workingList = new HashMap<String, DownloadJobRequest>();
    private Map<String,DownloadJobResult> completedList = new HashMap<String, DownloadJobResult>();
    private Map<String,DownloadJobResult> cache = new HashMap<String, DownloadJobResult>();
    private ARXHttpInputStream is;

    private String activeJobId = null;
    private final ARXContext ctx;

    public ARXDownload(ARXContext ctx) {
        this.ctx = ctx;
    }

    public void run() {
        String jobId;
        DownloadJobRequest request;
        DownloadJobResult result;

        stop = false;
        pause = false;
        action = false;
        state = WAITING;

        while (!stop) {
            jobId = null;
            request = null;
            result = null;

            // Wait for action
            while (!stop && !pause) {
                synchronized (this) {
                    if (workingList.size() > 0) {
                        jobId = getNextRequestId();
                        request = (DownloadJobRequest) workingList.get(jobId);
                        action = true;
                    }
                }

                // Do action
                if (action) {
                    state = WORKING;
                    activeJobId = jobId;
                    result = processRequest(request);

                    synchronized (this) {
                        workingList.remove(jobId);

                        if (!ARXHttpInputStream.isKillMode()) {
                            completedList.put(jobId, result);
                        } else {
                            workingList.put(jobId, request);
                        }

                        action = false;
                    }
                }
                state = WAITING;

                if (!stop && !pause)
                    sleep(100);
            }

            // Do pause
            while (!stop && pause) {
                state = PAUSED;
                sleep(100);
            }
            state = WAITING;
        }

        // Do stop
        state = STOPPED;
        // clearLists();
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (java.lang.InterruptedException ex) {

        }
    }

    private String getNextRequestId() {
        return (String) workingList.keySet().iterator().next();
    }

    boolean emptyCache = false;

    public void emptyCache() {
        emptyCache = true;
    }

    private DownloadJobResult processRequest(DownloadJobRequest request) {
        DownloadJobResult result=null;

        try {
            if (emptyCache) {
                cache.clear();
                emptyCache = false;
            }

            if (request.isCacheable() && cache.containsKey(request.getUrl())) {
                return (DownloadJobResult) cache.get(request.getUrl());
            }

            if (request.getFormat() == GAMA3D) {
                is = ctx.getHttpGETInputStream(request.getUrl());

                GAMA3DHandler handler = new GAMA3DHandler();
                handler.load(is, null);
                handler.getMesh().calc();
                
                result=new DownloadJobResult(request.getFormat(), handler.getMesh());
                
            } else if (request.getFormat() == PNG || request.getFormat() == JPG) {
                is = ctx.getHttpGETInputStream(request.getUrl());
                result=new DownloadJobResult(request.getFormat(), ctx.createBitmap(is));
                
            } else if (request.getFormat() == GAMADIM) {
                is = ctx.getHttpPOSTInputStream(request.getUrl(), request.getParams());
//                String line;
//                StringBuffer sb= new StringBuffer();
//                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//                while ((line = reader.readLine()) != null) {
//                    sb.append(line).append("\n");
//                }
//                System.out.println(sb.toString());
//                is = ctx.getHttpPOSTInputStream(request.getUrl(), request.getParams());
                org.w3c.dom.Element dimensionElement=Loader.readXML(is);
                Dimension dimension=DimensionParser.loadDimension(dimensionElement);

                result=new DownloadJobResult(request.getFormat(), new Layer(dimension));
            }



            if (request.isCacheable()){
                cache.put(request.getUrl(), result);
            }
        } catch (Exception ex) {
            result=new DownloadJobResult(ex.getMessage(), request);
            //replace with logging.
            Log.e("Gamaray", ex.getMessage());
            Log.e("Gamaray", "failed on request : "+request.getUrl());
            ex.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    //TODO:replace with logging.
                    e.printStackTrace();
                }
            }
            
        }

        activeJobId = null;

        return result;
    }

    public synchronized void clearLists() {
        workingList.clear();
        completedList.clear();
    }

    public synchronized String submitJob(DownloadJobRequest job) {
        String jobId = "ID_" + (id++);
        workingList.put(jobId, job);

        return jobId;
    }

    public synchronized boolean isJobComplete(String jobId) {
        return completedList.containsKey(jobId);
    }

    public synchronized DownloadJobResult getJobResult(String jobId) {
        DownloadJobResult result = (DownloadJobResult) completedList.get(jobId);
        completedList.remove(jobId);

        return result;
    }

    public String getActiveJobId() {
        return activeJobId;
    }

    public float getActiveJobPctComplete() {
        return (is != null) ? is.pctDownloadComplete() : 0;
    }

    public void pause() {
        pause = true;
    }

    public void restart() {
        pause = false;
    }

    public void stop() {
        stop = true;
    }

    public int getState() {
        return state;
    }

//    public String getStateString() {
//        if (state == NOT_STARTED)
//            return "NOT_STARTED";
//        else if (state == WAITING)
//            return "WAITING";
//        else if (state == WORKING)
//            return "WORKING";
//        else if (state == PAUSED)
//            return "PAUSED";
//        else if (state == STOPPED)
//            return "STOPPED";
//        else
//            return "ERROR";
//    }
}