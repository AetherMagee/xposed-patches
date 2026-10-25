package app.xposed.extension;

import android.content.Context;
import android.content.SharedPreferences;

/** User settings, mirroring the extension's options. Defaults follow X-Posed's Display settings as used here. */
final class Settings {
    static final String PREFS = "xposed_settings";

    static final String BADGE_SIZE = "badge_size";
    static final String BADGE_BACKGROUND = "badge_background";
    static final String BADGE_FLAGS = "badge_flags";
    static final String BADGE_DEVICES = "badge_devices";
    static final String BADGE_VPN = "badge_vpn";
    static final String BADGE_INFO = "badge_info";
    static final String FILTER_MODE = "filter_mode";
    static final String FILTER_LOCATIONS = "filter_locations";
    static final String ZERO_ENGAGEMENT = "zero_engagement";
    static final String COMMUNITY_CACHE = "community_cache";

    static final String MODE_HIGHLIGHT = "highlight";
    static final String MODE_HIDE = "hide";

    private static volatile SharedPreferences prefs;

    private Settings() {
    }

    static void init(Context context) {
        if (prefs != null) return;
        synchronized (Settings.class) {
            if (prefs == null) {
                prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            }
        }
    }

    /** Null before init(), e.g. for events dispatched before the first post is shown. */
    static SharedPreferences prefs() {
        return prefs;
    }

    static BadgeStyle.Size badgeSize() {
        String s = string(BADGE_SIZE, "medium");
        return "small".equals(s) ? BadgeStyle.Size.SMALL : "large".equals(s) ? BadgeStyle.Size.LARGE : BadgeStyle.Size.MEDIUM;
    }

    static boolean badgeBackground() {
        return bool(BADGE_BACKGROUND, true);
    }

    static boolean badgeFlags() {
        return bool(BADGE_FLAGS, true);
    }

    static boolean badgeDevices() {
        return bool(BADGE_DEVICES, false);
    }

    static boolean badgeVpn() {
        return bool(BADGE_VPN, false);
    }

    static boolean badgeInfo() {
        return bool(BADGE_INFO, true);
    }

    static boolean highlightMode() {
        return !MODE_HIDE.equals(string(FILTER_MODE, MODE_HIGHLIGHT));
    }

    static boolean zeroEngagement() {
        return bool(ZERO_ENGAGEMENT, true);
    }

    static boolean communityCache() {
        return bool(COMMUNITY_CACHE, true);
    }

    private static boolean bool(String key, boolean def) {
        SharedPreferences p = prefs;
        return p == null ? def : p.getBoolean(key, def);
    }

    private static String string(String key, String def) {
        SharedPreferences p = prefs;
        return p == null ? def : p.getString(key, def);
    }
}
