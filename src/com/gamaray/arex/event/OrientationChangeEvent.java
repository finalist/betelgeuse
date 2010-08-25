package com.gamaray.arex.event;

public class OrientationChangeEvent extends Event<OrientationChangeEvent.Handler>{

    
    public OrientationChangeEvent(){
    }

    /**
     * Interface to describe this event. Handlers must implement.
     */
    public interface Handler extends EventHandler {
        public void onOrientationChange(OrientationChangeEvent p);
    }

    @Override
    protected void dispatch(OrientationChangeEvent.Handler handler) {
        handler.onOrientationChange(this);
    }

    @Override
    public Event.Type<OrientationChangeEvent.Handler> getAssociatedType() {
        return TYPE;
    }

    public static final Event.Type<OrientationChangeEvent.Handler> TYPE = new Event.Type<OrientationChangeEvent.Handler>();

}
