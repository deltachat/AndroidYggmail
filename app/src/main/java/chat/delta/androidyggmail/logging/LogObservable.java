package chat.delta.androidyggmail.logging;

import java.util.ArrayList;
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
