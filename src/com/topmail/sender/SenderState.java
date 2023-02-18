package com.topmail.sender;

import java.util.HashMap;

public class SenderState {


    public static class EmailState {
        private Exception cause;

        public EmailState(Exception cause) {
            this.cause = cause;
        }

        public Exception getCause() {
            return cause;
        }
    }

    private boolean running;
    private boolean ended;
    private boolean interruptionRequested;
    private int nbSent;

    private HashMap<String, EmailState> hmReport;

    private Exception exception;

    public SenderState() {
        running = false;
        ended = false;
        interruptionRequested = false;
        nbSent = 0;
        exception = null;
        hmReport = new HashMap<>();
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isEnded() {
        return ended;
    }

    public void setEnded(boolean ended) {
        this.ended = ended;
    }

    public boolean isInterruptionRequested() {
        return interruptionRequested;
    }

    public void setInterruptionRequested(boolean interruptionRequested) {
        this.interruptionRequested = interruptionRequested;
    }

    public int getNbSent() {
        return nbSent;
    }

    public void setNbSent(int nbSent) {
        this.nbSent = nbSent;
    }

    public HashMap<String, EmailState> getHmReport() {
        return hmReport;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
