package app.xposed.extension;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.style.StyleSpan;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Account details shown when a badge is tapped. */
final class DetailsDialog {
    private DetailsDialog() {
    }

    static void show(Context context, String handle) {
        AccountInfo info = Lookup.peek(handle);
        if (info == null) return;

        SpannableStringBuilder text = new SpannableStringBuilder();
        if (info.location != null) {
            String flag = Countries.flagFor(info.location);
            row(text, "Account based in", (flag != null ? flag : Countries.UNKNOWN_FLAG) + " " + info.location);
        }
        if (info.source != null) row(text, "Connected via", info.source);
        if (info.affiliation != null) {
            // X gives the affiliate's handle; the community cache only has its name.
            boolean isHandle = AccountInfo.ORIGIN_X.equals(info.origin);
            row(text, "Affiliated with", isHandle ? "@" + info.affiliation : info.affiliation);
        }
        if (info.hasLocationWarning()) {
            text.append("⚠ X reports that this account's location may not be accurate. "
                    + "This is not proof of VPN use.\n\n");
        }
        String origin = AccountInfo.ORIGIN_CLOUD.equals(info.origin) ? "X-Posed community cache" : "X";
        long now = System.currentTimeMillis();
        CharSequence age = now - info.observedAt < DateUtils.MINUTE_IN_MILLIS
                ? "just now"
                : DateUtils.getRelativeTimeSpanString(info.observedAt, now, DateUtils.MINUTE_IN_MILLIS);
        text.append("From ").append(origin).append(", observed ").append(age).append(".\n");
        text.append("This is what X reports, not a live location.");

        new AlertDialog.Builder(context, theme(context))
                .setTitle("@" + handle)
                .setMessage(text)
                .setPositiveButton(android.R.string.ok, null)
                .setNeutralButton("Refresh from X", (d, w) -> Lookup.refresh(handle))
                .setNegativeButton("Filters…", (d, w) -> showFilters(context, info.location))
                .show();
    }

    /**
     * Country filters as a checklist: the saved ones plus this account's location.
     * One mode applies to all of them, like the extension's Hide/Highlight choice.
     */
    private static void showFilters(Context context, String location) {
        List<String> items = Filters.locations();
        if (location != null && !Filters.isFiltered(location)) items.add(0, location);
        if (items.isEmpty()) {
            new AlertDialog.Builder(context, theme(context))
                    .setTitle("Filters")
                    .setMessage("This account has no location to filter by.")
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return;
        }

        String[] labels = new String[items.size()];
        boolean[] checked = new boolean[items.size()];
        for (int i = 0; i < labels.length; i++) {
            String flag = Countries.flagFor(items.get(i));
            labels[i] = (flag != null ? flag : Countries.UNKNOWN_FLAG) + "  " + items.get(i);
            checked[i] = Filters.isFiltered(items.get(i));
        }
        boolean highlight = Filters.isHighlightMode();

        new AlertDialog.Builder(context, theme(context))
                .setTitle(highlight ? "Highlight posts from" : "Hide posts from")
                .setMultiChoiceItems(labels, checked, (d, which, isChecked) -> checked[which] = isChecked)
                .setPositiveButton("Save", (d, w) -> saveFilters(items, checked, highlight))
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(highlight ? "Hide instead" : "Highlight instead",
                        (d, w) -> saveFilters(items, checked, !highlight))
                .show();
    }

    private static void saveFilters(List<String> items, boolean[] checked, boolean highlight) {
        Set<String> selected = new HashSet<>();
        for (int i = 0; i < checked.length; i++) {
            if (checked[i]) selected.add(items.get(i));
        }
        Filters.save(selected, highlight);
        HeaderBadges.refreshAll();
    }

    private static int theme(Context context) {
        boolean dark = (context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        return dark
                ? android.R.style.Theme_DeviceDefault_Dialog_Alert
                : android.R.style.Theme_DeviceDefault_Light_Dialog_Alert;
    }

    private static void row(SpannableStringBuilder text, String label, String value) {
        int start = text.length();
        text.append(label).append('\n');
        text.setSpan(new StyleSpan(Typeface.BOLD), start, text.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.append(value).append("\n\n");
    }
}
