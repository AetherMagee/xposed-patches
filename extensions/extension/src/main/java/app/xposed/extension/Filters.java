package app.xposed.extension;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Country and region filters, like X-Posed's Countries and X regions rules. */
final class Filters {
    enum Action {NONE, HIDE, HIGHLIGHT}

    private static final String PREFS = "xposed_settings";
    private static final String KEY_LOCATIONS = "filter_locations";
    private static final String KEY_HIGHLIGHT = "filter_highlight";

    private static SharedPreferences prefs;
    /** Display names as X reported them; matched by canonical key. */
    private static Set<String> locations = Collections.emptySet();
    private static Set<String> keys = Collections.emptySet();
    private static boolean highlightMode;

    private Filters() {
    }

    static synchronized void init(Context context) {
        if (prefs != null) return;
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        highlightMode = prefs.getBoolean(KEY_HIGHLIGHT, true);
        setLocations(prefs.getStringSet(KEY_LOCATIONS, Collections.emptySet()));
    }

    static synchronized Action actionFor(AccountInfo info) {
        if (info == null || info.location == null || keys.isEmpty()) return Action.NONE;
        if (!keys.contains(Countries.canonicalKey(info.location))) return Action.NONE;
        return highlightMode ? Action.HIGHLIGHT : Action.HIDE;
    }

    static synchronized boolean isHighlightMode() {
        return highlightMode;
    }

    static synchronized boolean isFiltered(String location) {
        return location != null && keys.contains(Countries.canonicalKey(location));
    }

    /** Filtered locations, sorted for display. */
    static synchronized List<String> locations() {
        List<String> list = new ArrayList<>(locations);
        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        return list;
    }

    static synchronized void save(Set<String> newLocations, boolean highlight) {
        setLocations(newLocations);
        highlightMode = highlight;
        prefs.edit()
                .putStringSet(KEY_LOCATIONS, new HashSet<>(locations))
                .putBoolean(KEY_HIGHLIGHT, highlight)
                .apply();
    }

    private static void setLocations(Set<String> newLocations) {
        Set<String> l = new HashSet<>();
        Set<String> k = new HashSet<>();
        for (String location : newLocations) {
            // One entry per country, whichever spelling was saved first.
            if (k.add(Countries.canonicalKey(location))) l.add(location);
        }
        locations = l;
        keys = k;
    }
}
