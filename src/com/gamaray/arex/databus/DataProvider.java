package com.gamaray.arex.databus;

public interface DataProvider<T> {

    public abstract T getValue();

    public abstract DataChannel getChannel();

}