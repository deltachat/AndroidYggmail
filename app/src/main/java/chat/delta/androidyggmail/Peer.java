package chat.delta.androidyggmail;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Peer {
    /**
     * "tls://[2a09:8280:1::3:312]:10020": {
     *       "up": true,
     *       "key": "085da25dc55ad61dfbf095e441cd4e7a31fef3827e0a04544c891a9aac748464",
     *       "imported": 1634565613,
     *       "last_seen": 1634565613,
     *       "proto_minor": 4
     * }
     */

    public String address;
    public Boolean up;
    public String key;
    public Date imported;
    public Date lastSeen;
    public int protoMinor;
    public boolean isSelected;
    public boolean showItem;
    public boolean showSectionHeader;
    public String countryKey;

    public static Peer fromJson(String countryKey, String addressKey, JSONObject values) throws JSONException {
        Peer peer = new Peer();
        peer.address = addressKey;
        peer.countryKey = countryKey;
        peer.up = values.getBoolean("up");
        peer.key = values.optString("key");
        peer.imported = new Date(values.getLong("imported"));
        peer.lastSeen = new Date(values.optLong("last_seen"));
        peer.protoMinor = values.optInt("proto_minor");
        return peer;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("up", up);
        jsonObject.put("key", key);
        jsonObject.put("imported", imported.getTime());
        jsonObject.put("last_seen", lastSeen.getTime());
        jsonObject.put("proto_minor", protoMinor);
        return jsonObject;
    }

}
