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

import chat.delta.androidyggmail.Peer;
import chat.delta.androidyggmail.PeerConnection;
import chat.delta.androidyggmail.Util;
import chat.delta.androidyggmail.YggmailObservable;

public class LogObservable extends Observable {

    // to understand the regex and it's groups, use a regex plotting tool like https://ihateregex.io/ and paste the regex
    private static final Pattern LOG_PATTERN = Pattern.compile("(\\d{4}\\/\\d{1,2}\\/\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}) (\\[[\\s\\w]*\\]) (.*)");
    private static final Pattern LOCAL_PEER_CONNECTION_CONNECTED = Pattern.compile("(Connected (TCP|TLS): )(?<prefix>([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))(@)(?<linklocal>fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,})(, source )(?<source>fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,})");
    private static final Pattern LOCAL_PEER_CONNECTION_DISCONNECTED = Pattern.compile("(Disconnected (TCP|TLS): )(?<prefix>([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))(@)(?<linklocal>fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,})(, source )(?<source>fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,})(; error: )(.*)");
    private static final Pattern PUBLIC_PEER_CONNECTION_CONNECTED = Pattern.compile("(Connected (TCP|TLS): )(((([0-9a-fA-F]{0,4}:){1,8})([0-9a-fA-F]{1,4}))(:([0-9]{0,5})){0,2})(@)((((([0-9a-fA-F]{0,4}:){1,8})([0-9a-fA-F]{1,4}))|((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)){3}))(:([0-9]{0,6})){0,2})(, source )((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)){3}|(([0-9a-fA-F]{0,4}:){1,8})([0-9a-fA-F]{1,4}))");
    private static final Pattern PUBLIC_PEER_CONNECTION_DISCONNECTED = Pattern.compile("(Disconnected (TCP|TLS): )(((([0-9a-fA-F]{0,4}:){1,8})([0-9a-fA-F]{1,4}))(:([0-9]{0,5})){0,2})(@)(((((([0-9a-fA-F]{0,4}:){1,8})([0-9a-fA-F]{1,4}))|((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)){3}))(:([0-9]{0,6})){0,2}))(, source )((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)){3}|(([0-9a-fA-F]{0,4}:){1,8})([0-9a-fA-F]{1,4}))(; error: )(.*)");

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
            String content = m.group(3) != null ? m.group(3) : "";
            item = new LogItem(m.group(1), m.group(2), content);
            Matcher peerConnectionConnected = LOCAL_PEER_CONNECTION_CONNECTED.matcher(content);
            Matcher peerConnectionDisconnected = LOCAL_PEER_CONNECTION_DISCONNECTED.matcher(content);
            Matcher publicPeerConnectionConnected = PUBLIC_PEER_CONNECTION_CONNECTED.matcher(content);
            Matcher publicPeerConnectionDisconnected = PUBLIC_PEER_CONNECTION_DISCONNECTED.matcher(content);
            try {
                if (peerConnectionConnected.matches()) {
                    Log.d(TAG, "REGEX: connected local peer");
                    PeerConnection localPeerConnection = new PeerConnection(peerConnectionConnected.group(3), peerConnectionConnected.group(34), peerConnectionConnected.group(37));
                    YggmailObservable.getInstance().addLocalPeerConnection(localPeerConnection);
                } else if (peerConnectionDisconnected.matches()) {
                    Log.d(TAG, "REGEX: disconnected local peer");
                    PeerConnection localPeerConnection = new PeerConnection(peerConnectionDisconnected.group(3), peerConnectionDisconnected.group(34), peerConnectionDisconnected.group(37));
                    YggmailObservable.getInstance().removeLocalPeerConnection(localPeerConnection);
                } else if (publicPeerConnectionConnected.matches()) {
                    Log.d(TAG, "REGEX: connected public peer");
                    PeerConnection publicPeerConnection = new PeerConnection(publicPeerConnectionConnected.group(3), publicPeerConnectionConnected.group(11), publicPeerConnectionConnected.group(24));
                    YggmailObservable.getInstance().addPublicPeerConnection(publicPeerConnection);
                } else if (publicPeerConnectionDisconnected.matches()) {
                    Log.d(TAG, "REGEX: disconnected public peer");
                    PeerConnection publicPeerConnection = new PeerConnection(publicPeerConnectionDisconnected.group(3), publicPeerConnectionDisconnected.group(11), publicPeerConnectionDisconnected.group(25));
                    YggmailObservable.getInstance().removePublicPeerConnection(publicPeerConnection);
                }
            } catch (IllegalStateException e) {
                Log.e(TAG, "error regex matching: " + e.getLocalizedMessage());
                e.printStackTrace();
            }

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
