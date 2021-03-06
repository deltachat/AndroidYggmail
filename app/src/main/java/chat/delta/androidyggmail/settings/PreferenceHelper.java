package chat.delta.androidyggmail.settings;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

public class PreferenceHelper {
    private static final String TAG = PreferenceHelper.class.getSimpleName();
    public static String SHARED_PREFERENCES = "YGGMAIL_PREFERENCES";
    public static String PREF_ON_BOOT = "PREF_ON_BOOT";
    public static String PREF_MULTICAST = "PREF_LOOKUP_LOCAL_PEERS";
    public static String PREF_CONNECT_TO_PUBLIC_PEERS = "PREF_CONNECT_TO_PUBLIC_PEERS";
    public static String PREF_PUBLIC_PEERS = "PREF_PUBLIC_PEERS";
    public static String PREF_SELECTED_PEERS = "PREF_SELECTED_PEERS";
    public static String PREF_SHOW_TIMESTAMPS = "PREF_SHOW_TIMESTAMPS";
    public static String PREF_SHOW_LOG_TAGS = "PREF_SHOW_LOG_TAGS";
    public static String PREF_ACCOUNT_NAME = "PREF_ACCOUNT_NAME";
    public static String PREF_CUSTOM_CLIENT = "PREF_CUSTOM_CLIENT";
    public static String PREF_LAST_UPDATED = "PREF_LAST_UPDATED";

    private static long THREE_DAYS = 1000*60*60*24L*3;

    public static void setLastUpdate(Context context, long timestamp) {
        putLong(context, PREF_LAST_UPDATED, timestamp);
    }

    public static boolean shouldUpdate(Context context) {
        long lastUpdated = getLong(context, PREF_LAST_UPDATED, 0);
        return System.currentTimeMillis() - lastUpdated > THREE_DAYS && getConnectToPublicPeers(context);
    }

    public static boolean useCustomMailClient(Context context) {
        return getBoolean(context, PREF_CUSTOM_CLIENT, false);
    }

    public static void setUseCustomMailClient(Context context, boolean useCustomMailClient) {
        putBoolean(context, PREF_CUSTOM_CLIENT, useCustomMailClient);
    }

    public static void setAccountName(Context context, String accountName) {
        putString(context, PREF_ACCOUNT_NAME, accountName);
    }

    public static String getAccountName(Context context) {
        return getString(context, PREF_ACCOUNT_NAME, "");
    }

    public static boolean getShowLogTags(Context context) {
        return getBoolean(context, PREF_SHOW_LOG_TAGS, true);
    }

    public static void setShowLogTags(Context context, boolean showLogTags) {
        putBoolean(context, PREF_SHOW_LOG_TAGS, showLogTags);
    }

    public static boolean getShowTimestamps(Context context) {
        return getBoolean(context, PREF_SHOW_TIMESTAMPS, true);
    }

    public static void setShowTimestamps(Context context, boolean showTimestamps) {
        putBoolean(context, PREF_SHOW_TIMESTAMPS, showTimestamps);
    }

    public static boolean getConnectToPublicPeers(Context context) {
        return getBoolean(context, PREF_CONNECT_TO_PUBLIC_PEERS, true);
    }

    public static void setConnectToPublicPeers(Context context, boolean connectToPublicPeers) {
        putBoolean(context, PREF_CONNECT_TO_PUBLIC_PEERS, connectToPublicPeers);
    }

    public static String getSelectedPublicPeers(Context context) {
        if (!getConnectToPublicPeers(context)) {
            return "";
        }

        StringBuilder resultBuilder = new StringBuilder();
        for (String peer : getSelectedPeers(context)) {
            resultBuilder.append(peer).append(",");
        }
        String result = resultBuilder.toString();

        if (result.length() > 0) {
            result = result.substring(0, result.length() - 2);
        }

        return result;
    }

    public static HashSet<String> getSelectedPeers(Context context) {
        if (!hasPreferenceKey(context, PREF_SELECTED_PEERS)) {
            HashSet<String> defaultValues = new HashSet<>();
            // german node
            defaultValues.add("tcp://bunkertreff.ddns.net:5454");
            // russian node
            defaultValues.add("tls://kazi.peer.cofob.ru:18001");
            // us node
            defaultValues.add("tls://167.160.89.98:7040");
            // brazil node
            defaultValues.add("tcp://[2804:49fc::ffff:ffff:5b5:e8be]:58301");
            return defaultValues;
        }
        return  (HashSet<String>) PreferenceHelper.getStringSet(context, PREF_SELECTED_PEERS);
    }

    public static void setSelectedPeers(Context context, HashSet<String> peers) {
        putStringSet(context, PREF_SELECTED_PEERS, peers);
    }

    public static String getPublicPeers(Context context) {
        return getString(context, PREF_PUBLIC_PEERS, "");
    }

    public static void setPublicPeers(Context context, String peersJson) {
        putString(context, PREF_PUBLIC_PEERS, peersJson);
    }

    public static boolean getStartOnBoot(Context context) {
        return getBoolean(context, PREF_ON_BOOT, true);
    }

    public static void setStartOnBoot(Context context, boolean startOnBoot) {
        putBoolean(context, PREF_ON_BOOT, startOnBoot);
    }


    public static void setMulticast(Context context, boolean peersInLocalNetwork) {
        putBoolean(context, PREF_MULTICAST, peersInLocalNetwork);
    }

    public static boolean getMulticast(Context context) {
        return getBoolean(context, PREF_MULTICAST, true);
    }

    private static boolean hasPreferenceKey(Context context, String key) {
        if (context == null) {
            return false;
        }
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        return preferences.contains(key);
    }

    private static Set<String> getStringSet(Context context, String key) {
        if (context == null) {
            return new HashSet<>();
        }
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        return new HashSet<>(preferences.getStringSet(key, new HashSet<>()));
    }

    private static void putStringSet(Context context, String key, Set<String> values) {
        if (context == null) {
            return;
        }
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        preferences.edit().putStringSet(key, values).apply();
    }

    private static String getString(Context context, String key, String defValue) {
        if (context == null) {
            return defValue;
        }
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        return preferences.getString(key, defValue);
    }

    private static void putString(Context context, String key, String value) {
        if (context == null) {
            return;
        }
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        preferences.edit().putString(key, value).apply();
    }

    private static Boolean getBoolean(Context context, String key, Boolean defValue) {
        if (context == null) {
            return defValue;
        }

        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        return preferences.getBoolean(key, defValue);
    }

    private static void putBoolean(Context context, String key, Boolean value) {
        if (context == null) {
            return;
        }

        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        preferences.edit().putBoolean(key, value).apply();
    }

    private static void putLong(Context context, String key, long value) {
        if (context == null) {
            return;
        }

        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        preferences.edit().putLong(key, value).apply();
    }

    private static long getLong(Context context, String key, long defValue) {
        if (context == null) {
            return defValue;
        }

        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        return preferences.getLong(key, defValue);
    }

}
