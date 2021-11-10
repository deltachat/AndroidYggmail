package chat.delta.androidyggmail.logging;

public class LogItem {
    public final String content;
    public final String tag;
    public final String timestamp;

    public LogItem(String timestamp, String tag, String content) {
        this.timestamp = timestamp;
        this.tag = tag;
        this.content = content.trim();
    }

    @Override
    public String toString() {
        return timestamp + " " + tag + " " + content;
    }

    public String toString(boolean showTimestamp, boolean showTags) {
        String result = "";
        result += showTimestamp ? timestamp + " " : "";
        result += showTags ? tag + " " : "";
        result += content;
        return result;
    }
}
