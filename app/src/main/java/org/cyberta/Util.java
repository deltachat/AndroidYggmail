package org.cyberta;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.Context;

import androidx.annotation.NonNull;

public class Util {
    public static void writeTextToClipboard(@NonNull Context context, @NonNull String text) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            @SuppressWarnings("deprecation") android.text.ClipboardManager clipboard =
                    (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            copyToClipboardSdk11(context, text);
        }
    }

    @TargetApi(android.os.Build.VERSION_CODES.HONEYCOMB)
    private static void copyToClipboardSdk11(Context context, String text) {
        android.content.ClipboardManager clipboard =
                (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(context.getString(R.string.app_name), text);
        clipboard.setPrimaryClip(clip);
    }

}
