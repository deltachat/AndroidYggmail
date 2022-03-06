package chat.delta.androidyggmail.logging;

import android.text.format.DateUtils;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Observable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import chat.delta.androidyggmail.Util;

public class LogObservable extends Observable {

    private static final Pattern LOG_PATTERN = Pattern.compile("(\\d{4}\\/\\d{1,2}\\/\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}) (\\[[\\s\\w]*\\]) (.*)");
    private static final String TAG = LogObservable.class.getName();
    ArrayList<LogItem> logs;

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

    public void addLog(String tag, String message) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        String result = dateFormat.format(System.currentTimeMillis()) + " " + tag + " " + message;
        addLog(result);
    }

    public void addLog(String message) {

        Matcher m = LOG_PATTERN.matcher(message.trim());
        final LogItem item;
        if (m.matches()) {
            item = new LogItem(m.group(1), m.group(2), m.group(3));
        } else {
            item = new LogItem("", "", message.trim());
        }
        Util.runOnMain(() -> {
            logs.add(item);
            setChanged();
            notifyObservers();
        });
    }

    public void clearLog() {
        Util.runOnMain(() -> {
            logs.clear();
            setChanged();
            notifyObservers();
        });
    }

    public ArrayList<LogItem> getLogs() {
        return logs;
    }

    public String getLogsAsString(boolean addTimestamp, boolean addTag) {
        StringBuilder sb = new StringBuilder();
        for (LogItem logItem : logs) {
            if (addTimestamp) {
                sb.append(logItem.timestamp).append(" ");
            }
            if (addTag) {
                sb.append(logItem.tag).append(" ");
            }
            sb.append(logItem.content).append("\n");
        }
        return sb.toString();
    }


}
