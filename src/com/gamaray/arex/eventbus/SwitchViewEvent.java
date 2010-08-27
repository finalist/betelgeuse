package com.gamaray.arex.eventbus;

public class SwitchViewEvent extends Event<SwitchViewEvent.Handler>{

    
    public SwitchViewEvent(){
    }

    /**
     * Interface to describe this event. Handlers must implement.
     */
    public interface Handler extends EventHandler {
        public void onSwitchView(SwitchViewEvent p);
    }

    @Override
    protected void dispatch(SwitchViewEvent.Handler handler) {
        handler.onSwitchView(this);
    }

    @Override
    public Event.Type<SwitchViewEvent.Handler> getAssociatedType() {
        return TYPE;
    }

    public static final Event.Type<SwitchViewEvent.Handler> TYPE = new Event.Type<SwitchViewEvent.Handler>();

}
