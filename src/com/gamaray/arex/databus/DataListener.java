package com.gamaray.arex.databus;

import com.gamaray.arex.eventbus.DataChangeEvent;
import com.gamaray.arex.eventbus.EventHandler;

public interface DataListener extends EventHandler{

    void onChange(DataChangeEvent datachangeEvent);
    
}
