package com.gamaray.arex.event;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class EventBus {

    private static EventBus instance = null;
    private final static Map<Event.Type<EventHandler>,Collection<EventHandler>> handlers=new HashMap<Event.Type<EventHandler>, Collection<EventHandler>>();
    
    private EventBus() {
       // Exists only to defeat instantiation.
    }
    
    public static EventBus get() {
       if(instance == null) {
          instance = new EventBus();
       }
       return instance;
    }

    
    public void register(Event.Type<? extends EventHandler> type,EventHandler eventHandler){
        if (handlers.get(type)==null){
            handlers.put((Event.Type<EventHandler>)type, new HashSet<EventHandler>());
        }
        handlers.get(type).add(eventHandler);
    }
    
    public void unregister(Event.Type<EventHandler> type,EventHandler eventHandler){
        if (handlers.get(type)==null){
            handlers.get(type).remove(eventHandler);
        }
    }
    
    public void fire(Event<? extends EventHandler> event){
        Collection<EventHandler> eventHandlers=handlers.get(event.getAssociatedType());
        if (eventHandlers!=null){
            for (EventHandler eventHandler : eventHandlers) {
                ((Event<EventHandler>)event).dispatch(eventHandler);
            }
        }
    }
    
}
