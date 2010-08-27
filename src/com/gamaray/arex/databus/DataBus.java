package com.gamaray.arex.databus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.gamaray.arex.eventbus.DataChangeEvent;

public class DataBus {

    private static final DataBus INSTANCE = new DataBus();

    private Map<DataChannel, DataProvider<?>> providers = new HashMap<DataChannel, DataProvider<?>>();

    private Map<DataChannel, Set<DataChangeEvent.Handler>> handlers = new HashMap<DataChannel, Set<DataChangeEvent.Handler>>();
    
    public static DataBus instance() {
        return INSTANCE;
    }

    public void registerProvider(DataProvider<?> dataprovider) {
        if (providers.get(dataprovider.getChannel()) != null) {
            // TODO Throw exception
        } else {
            providers.put(dataprovider.getChannel(), dataprovider);
        }
    }

    public void registerListener(DataChannel channel, DataChangeEvent.Handler listener) {
        if (handlers.get(channel)==null){
            handlers.put(channel,new HashSet<DataChangeEvent.Handler>());
        }
        handlers.get(channel).add(listener);
    }

    public Object get(DataChannel channel) {
        if (providers.get(channel) == null) {
            // TODO Throw exception
            return null;
        } else {
            return providers.get(channel).getValue();
        }
    }

    public void onChange(DataChannel channel) {
        if (handlers.get(channel)!=null){
            DataChangeEvent changeEvent=new DataChangeEvent();
            for (DataChangeEvent.Handler handler: handlers.get(channel)) {
                handler.onDataChange(changeEvent);
            };
        }
    }

}
