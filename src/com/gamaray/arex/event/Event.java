package com.gamaray.arex.event;

public abstract class Event<T extends EventHandler> {

    protected abstract void dispatch(T handler);
    
    public abstract Event.Type<T> getAssociatedType();
    
    public final static class Type<T>{
        
        
    }

}
