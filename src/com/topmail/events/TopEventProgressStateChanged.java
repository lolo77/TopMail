package com.topmail.events;

public class TopEventProgressStateChanged extends TopEventBase {
    private String state;

    public TopEventProgressStateChanged(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }
}
