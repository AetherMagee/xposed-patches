package app.xposed.extension;

import android.util.Log;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Keeps passive engagement with filtered posts (impressions, dwell time, video views) out of
 * X's client events. Active engagement such as likes, replies, reposts and taps is untouched.
 */
final class Engagement {
    /** Event actions that only record that something was on screen or played. */
    private static final Set<String> PASSIVE_ACTIONS = new HashSet<>(Arrays.asList(
            "impression", "results", "linger", "dwell", "view", "show", "visible", "render",
            "heartbeat", "video_view", "view_threshold", "playback_start", "playback_25",
            "playback_50", "playback_75", "playback_95", "playback_complete"
    ));

    private static Class<?> logClass, itemClass;
    private static Field eventName, items, tweetDetails, authorId;
    private static volatile boolean ready, broken;

    private Engagement() {
    }

    /** True when the event only concerned filtered posts and should not be reported at all. */
    static boolean shouldDrop(Object event) {
        if (broken || event == null || !Settings.zeroEngagement()) return false;
        try {
            if (!ready) resolve();
            if (!logClass.isInstance(event)) return false;
            String name = (String) eventName.get(event);
            if (name == null || !isPassive(name)) return false;
            List<?> list = (List<?>) items.get(event);
            if (list == null || list.isEmpty()) return false;
            // Mixed events (e.g. a list of shown posts) keep their other items.
            if (!list.removeIf(Engagement::isFilteredItem)) return false;
            Log.d(Hooks.TAG, "Withheld " + name + (list.isEmpty() ? "" : " (partially)"));
            return list.isEmpty();
        } catch (Exception e) {
            broken = true;
            Log.e(Hooks.TAG, "Client event fields are unavailable", e);
            return false;
        }
    }

    /** Event names are "page:section:component:element:action". */
    private static boolean isPassive(String name) {
        String action = name.substring(name.lastIndexOf(':') + 1).toLowerCase(Locale.ROOT);
        return PASSIVE_ACTIONS.contains(action) || action.endsWith("_impression") || action.startsWith("linger");
    }

    private static boolean isFilteredItem(Object item) {
        if (!itemClass.isInstance(item)) return false;
        try {
            Object details = tweetDetails.get(item);
            if (details == null) return false;
            String handle = Users.handleFor(authorId.getLong(details));
            return handle != null && Filters.actionFor(Lookup.peek(handle)) != Filters.Action.NONE;
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    /** "logClass|eventNameField|itemsField|itemClass|tweetDetailsField|authorIdField", from the patch. */
    private static synchronized void resolve() throws Exception {
        if (ready) return;
        String[] spec = Hooks.scribeFields().split("\\|");
        logClass = Class.forName(spec[0]);
        eventName = field(logClass, spec[1]);
        items = field(logClass, spec[2]);
        itemClass = Class.forName(spec[3]);
        tweetDetails = field(itemClass, spec[4]);
        authorId = field(tweetDetails.getType(), spec[5]);
        ready = true;
    }

    private static Field field(Class<?> c, String name) throws NoSuchFieldException {
        for (Class<?> k = c; k != null; k = k.getSuperclass()) {
            try {
                Field f = k.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new NoSuchFieldException(c.getName() + "." + name);
    }
}
