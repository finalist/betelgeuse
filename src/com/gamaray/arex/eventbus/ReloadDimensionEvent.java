package com.gamaray.arex.eventbus;

public class ReloadDimensionEvent extends Event<ReloadDimensionEvent.Handler>{

    
    public ReloadDimensionEvent(){
    }

    /**
     * Interface to describe this event. Handlers must implement.
     */
    public interface Handler extends EventHandler {
        public void onReloadDimension(ReloadDimensionEvent p);
    }

    @Override
    protected void dispatch(ReloadDimensionEvent.Handler handler) {
        handler.onReloadDimension(this);
    }

    @Override
    public Event.Type<ReloadDimensionEvent.Handler> getAssociatedType() {
        return TYPE;
    }

    public static final Event.Type<ReloadDimensionEvent.Handler> TYPE = new Event.Type<ReloadDimensionEvent.Handler>();

}
