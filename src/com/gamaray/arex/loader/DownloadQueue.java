package com.gamaray.arex.loader;

import com.gamaray.arex.util.TaskQueue;

public class DownloadQueue {

        private TaskQueue taskQueue;
        private static DownloadQueue singleton;

        public static DownloadQueue getInstance() {
          if (singleton == null) {
            singleton = new DownloadQueue();
            singleton.start();
          }
          return singleton;
        }

        private DownloadQueue() {
          taskQueue = new TaskQueue();
        }

        public void start() {
          taskQueue.start();
        }

        public void stop() {
          taskQueue.stop();
        }

        public void addTask(DownloadJobRequest request) {
          taskQueue.addTask(new DownloadJobRequestAdapter(request));
        }

        private class DownloadJobRequestAdapter implements Runnable {
          private final DownloadJobRequest request;

          DownloadJobRequestAdapter(DownloadJobRequest request) {
            this.request = request;
          }

          public void run() {
          }
      }
    
    
    
    
    
    
}
