package com.topmail.events;

import com.topmail.sender.SenderState;

public class TopEventReportChanged extends TopEventBase {
    SenderState state;

    public TopEventReportChanged(SenderState state) {
        this.state = state;
    }

    public SenderState getState() {
        return state;
    }
}
