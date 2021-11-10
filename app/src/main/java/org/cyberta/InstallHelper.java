package org.cyberta;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

public class InstallHelper {
    public final static String DELTA_CHAT_WEBSITE = "https://get.delta.chat";
    public final static String FDROID_PACKAGE = "org.fdroid.fdroid";
    public final static String GPLAY_PACKAGE = "com.android.vending";
    public final static String GPLAY_DELTACHAT_PACKAGE = "chat.delta";
    public final static String GPLAY_DELTACHAT_BETA_PACKAGE = GPLAY_DELTACHAT_PACKAGE + ".beta";
    public final static String FDROID_DELTACHAT_PACKAGE = "com.b44t.messenger";
    public final static String FDROID_DELTACHAT_BETA_PACKAGE = FDROID_DELTACHAT_PACKAGE + ".beta";
    public final static String DC_IPC_SERVICE = "org.thoughtcrime.securesms.service.IPCAddAccountsService";
    public final static String DC_IPC_ACTION_ADD_ACCOUNT = "chat.delta.addaccount";
    public final static String MARKET_URI = "market://details?id=";

    public static void sendDeltaChatInstallIntent(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);

        String appPackageName = null;
        String storePackageName = null;
        //F-Droid installed?
        if (isPackageInstalled(context.getPackageManager(), FDROID_PACKAGE)) {
            appPackageName = FDROID_DELTACHAT_PACKAGE;
            storePackageName = FDROID_PACKAGE;
        } else if (isPackageInstalled(context.getPackageManager(), GPLAY_PACKAGE)) {
            appPackageName = GPLAY_DELTACHAT_PACKAGE;
            storePackageName = GPLAY_PACKAGE;
        }

        intent.setData(Uri.parse(MARKET_URI + appPackageName));
        intent.setPackage(storePackageName);
        try {
            context.startActivity(intent);
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(DELTA_CHAT_WEBSITE)));
        }
    }

    public static boolean isDeltaChatInstalled(PackageManager packageManager) {
        String[] dcPackageNames = new String[]{FDROID_DELTACHAT_PACKAGE, GPLAY_DELTACHAT_PACKAGE, FDROID_DELTACHAT_BETA_PACKAGE, GPLAY_DELTACHAT_BETA_PACKAGE};
        for (String packageName : dcPackageNames) {
            if (isPackageInstalled(packageManager, packageName)) {
                return true;
            }
        }
        return false;
    }

    // returns the first delta chat package name, prefers F-Droid over Gplay, stable releases over beta-releases
    public static String getInstalledDeltaChatPackageName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        String[] dcPackageNames = new String[]{FDROID_DELTACHAT_PACKAGE, GPLAY_DELTACHAT_PACKAGE, FDROID_DELTACHAT_BETA_PACKAGE, GPLAY_DELTACHAT_BETA_PACKAGE};
        for (String packageName : dcPackageNames) {
            if (isPackageInstalled(packageManager, packageName)) {
                return packageName;
            }
        }
        return null;
    }

    public static boolean isPackageInstalled(PackageManager packageManager, String packageName) {
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

}
