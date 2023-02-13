package com.topmail.events;

import java.util.ArrayList;
import java.util.List;

public class TopEventDispatcher {
    private static List<TopEventListener> listeners = new ArrayList<>();

    public TopEventDispatcher() {

    }

    public static void addListener(TopEventListener l) {
        listeners.add(l);
    }

    public static void dispatch(TopEventBase e) {
        for (TopEventListener l : listeners) {
            l.processTopEvent(e);
        }
    }
}
