package app.xposed.extension;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Country and region filters, like X-Posed's Countries and X regions rules. */
final class Filters {
    enum Action {NONE, HIDE, HIGHLIGHT}

    /** Display names as saved; matched by canonical key. */
    private static Set<String> locations = Collections.emptySet();
    private static Set<String> keys = Collections.emptySet();
    private static boolean loaded;
    // Held strongly: SharedPreferences only keeps weak references to listeners.
    private static final SharedPreferences.OnSharedPreferenceChangeListener listener = (p, key) -> {
        if (Settings.FILTER_LOCATIONS.equals(key)) reload();
    };

    private Filters() {
    }

    static synchronized Action actionFor(AccountInfo info) {
        if (info == null || info.location == null) return Action.NONE;
        load();
        if (keys.isEmpty() || !keys.contains(Countries.canonicalKey(info.location))) return Action.NONE;
        return Settings.highlightMode() ? Action.HIGHLIGHT : Action.HIDE;
    }

    static synchronized boolean isFiltered(String location) {
        load();
        return location != null && keys.contains(Countries.canonicalKey(location));
    }

    /** Filtered locations, sorted for display. */
    static synchronized List<String> locations() {
        load();
        List<String> list = new ArrayList<>(locations);
        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        return list;
    }

    static synchronized void save(Set<String> newLocations, boolean highlight) {
        SharedPreferences prefs = Settings.prefs();
        if (prefs == null) return;
        prefs.edit()
                .putStringSet(Settings.FILTER_LOCATIONS, new HashSet<>(newLocations))
                .putString(Settings.FILTER_MODE, highlight ? Settings.MODE_HIGHLIGHT : Settings.MODE_HIDE)
                .apply();
        reload();
    }

    private static void load() {
        if (loaded) return;
        SharedPreferences prefs = Settings.prefs();
        if (prefs == null) return;
        prefs.registerOnSharedPreferenceChangeListener(listener);
        loaded = true;
        reload();
    }

    private static synchronized void reload() {
        SharedPreferences prefs = Settings.prefs();
        if (prefs == null) return;
        Set<String> l = new HashSet<>();
        Set<String> k = new HashSet<>();
        for (String location : prefs.getStringSet(Settings.FILTER_LOCATIONS, Collections.emptySet())) {
            // One entry per country, whichever spelling was saved first.
            if (k.add(Countries.canonicalKey(location))) l.add(location);
        }
        locations = l;
        keys = k;
    }
}
