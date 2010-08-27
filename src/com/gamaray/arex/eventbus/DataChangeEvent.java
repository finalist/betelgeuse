package com.gamaray.arex.eventbus;

public class DataChangeEvent extends Event<DataChangeEvent.Handler>{

    
    public DataChangeEvent(){
    }

    /**
     * Interface to describe this event. Handlers must implement.
     */
    public interface Handler extends EventHandler {
        public void onDataChange(DataChangeEvent p);
    }

    @Override
    protected void dispatch(DataChangeEvent.Handler handler) {
        handler.onDataChange(this);
    }

    @Override
    public Event.Type<DataChangeEvent.Handler> getAssociatedType() {
        return TYPE;
    }

    public static final Event.Type<DataChangeEvent.Handler> TYPE = new Event.Type<DataChangeEvent.Handler>();

}
