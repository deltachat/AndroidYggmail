package org.cyberta.logging;

import android.util.Log;

import org.cyberta.Util;

import java.util.ArrayList;
import java.util.Observable;

public class LogObservable extends Observable {

    private static final String TAG = LogObservable.class.getName();
    ArrayList<LogListContent.LogListItem> logs;

    private LogObservable() {
        logs = new ArrayList<>();
    }

    private static LogObservable instance;
    public static LogObservable getInstance() {
        if (instance == null) {
            instance = new LogObservable();
        }
        return instance;
    }

    public void addLog(String message) {
        Util.runOnMain(() -> {
            logs.add(new LogListContent.LogListItem(message.trim()));
            Log.d(TAG, "add Log: " + message.trim());
            setChanged();
            notifyObservers();
        });
    }

    public ArrayList<LogListContent.LogListItem> getLogs() {
        return logs;
    }

    public String getLogsAsString() {
        StringBuilder sb = new StringBuilder();
        for (LogListContent.LogListItem logItem : logs) {
            sb.append(logItem.content).append("\n");
        }
        return sb.toString();
    }


}
