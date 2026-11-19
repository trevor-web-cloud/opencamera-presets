package net.sourceforge.opencamera.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import net.sourceforge.opencamera.MainActivity;
import net.sourceforge.opencamera.MyDebug;
import android.util.Log;
import net.sourceforge.opencamera.PreferenceKeys;
import net.sourceforge.opencamera.R;
import net.sourceforge.opencamera.cameracontroller.CameraController;

/** PRESET-1: on-screen preset bar for quickly applying ISO + exposure time presets
 *  （控箱1, 控箱2, ...).
 *
 *  - Tap a preset button: applies its stored ISO and exposure time by writing the
 *    standard Open Camera preferences (preference_iso / preference_exposure_time)
 *    and calling MainActivity.updateForSettings(), i.e. exactly the same path as
 *    changing ISO from the exposure popup menu.
 *  - Long-press a preset button: stores the currently set manual ISO and exposure
 *    time into that preset, so presets can be calibrated on site without editing
 *    any configuration files.
 *  - The "+" button appends a new preset （控箱N+1). Presets are stored in
 *    SharedPreferences; the count defaults to 4.
 *  - PRESET-2: after a preset is applied the bar auto-collapses to a small
 *    "預設 ▶" pill so it never blocks the preview while framing; the "◀"
 *    button collapses it manually. Tap the pill to expand again. The
 *    collapsed state is persisted.
 */
public class PresetBar {
    private static final String TAG = "PresetBar";

    private static final int DEFAULT_PRESET_COUNT = 4;

    public static int getPresetCount(SharedPreferences prefs) {
        return prefs.getInt(PreferenceKeys.PresetCountPreferenceKey, DEFAULT_PRESET_COUNT);
    }

    public static String getPresetName(Context context, SharedPreferences prefs, int index) {
        // PRESET-4: the default name comes from string resources, so it follows the system language
        return prefs.getString(PreferenceKeys.PresetNamePreferenceKeyPrefix + index, context.getString(R.string.preset_box_name, index + 1));
    }

    private static boolean hasPreset(SharedPreferences prefs, int index) {
        return prefs.contains(PreferenceKeys.PresetISOPreferenceKeyPrefix + index);
    }

    private static String describePreset(MainActivity activity, SharedPreferences prefs, int index) {
        if( !hasPreset(prefs, index) )
            return activity.getString(R.string.preset_not_set_label);
        String iso = prefs.getString(PreferenceKeys.PresetISOPreferenceKeyPrefix + index, CameraController.ISO_DEFAULT);
        long exposure_time = prefs.getLong(PreferenceKeys.PresetExposureTimePreferenceKeyPrefix + index, CameraController.EXPOSURE_TIME_DEFAULT);
        String exp_string = activity.getPreview() != null ? activity.getPreview().getExposureTimeString(exposure_time) : "";
        return "ISO " + iso + " ·" + exp_string;
    }

    private static Button makeBarButton(MainActivity activity, String text, float scale, boolean active) {
        Button button = new Button(activity);
        button.setAllCaps(false);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(13);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        int pad_x = (int)(10 * scale + 0.5f);
        int pad_y = (int)(3 * scale + 0.5f);
        button.setPadding(pad_x, pad_y, pad_x, pad_y);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(8 * scale);
        bg.setColor(active ? 0xCC1565C0 : 0xB3000000);
        button.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int margin = (int)(4 * scale + 0.5f);
        lp.setMargins(margin, margin, margin, margin);
        button.setLayoutParams(lp);
        return button;
    }

    /** (Re)builds all preset buttons inside R.id.preset_container. Safe to call repeatedly. */
    public static void build(final MainActivity activity) {
        LinearLayout container = activity.findViewById(R.id.preset_container);
        if( container == null )
            return;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        container.removeAllViews();
        final float scale = activity.getResources().getDisplayMetrics().density;
        // PRESET-2/3: collapsed state - the bar itself is hidden (see
        // refreshVisibility) and only the top-docked "預設 ▶" pill is shown, so
        // nothing blocks the preview while framing
        if( prefs.getBoolean(PreferenceKeys.PresetBarCollapsedPreferenceKey, false) ) {
            refreshVisibility(activity);
            return;
        }
        // PRESET-2: collapse button at the head of the bar
        Button collapse_button = makeBarButton(activity, "◀", scale, false);
        collapse_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(PreferenceKeys.PresetBarCollapsedPreferenceKey, true);
                editor.apply();
                build(activity);
            }
        });
        container.addView(collapse_button);
        int count = getPresetCount(prefs);
        int active = prefs.getInt(PreferenceKeys.PresetActivePreferenceKey, -1);
        for( int i = 0; i < count; i++ ) {
            final int index = i;
            Button button = makeBarButton(activity, getPresetName(activity, prefs, index) + "\n" + describePreset(activity, prefs, index), scale, index == active);
            button.setMinWidth((int)(84 * scale + 0.5f));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    applyPreset(activity, index);
                }
            });
            button.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    saveCurrentToPreset(activity, index);
                    return true;
                }
            });
            container.addView(button);
        }
        // "+" button to append a new preset
        Button add_button = makeBarButton(activity, activity.getString(R.string.preset_add), scale, false);
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(PreferenceKeys.PresetCountPreferenceKey, getPresetCount(prefs) + 1);
                editor.apply();
                if( MyDebug.LOG )
                    Log.d(TAG, "added preset, new count: " + getPresetCount(prefs));
                build(activity);
            }
        });
        container.addView(add_button);
        refreshVisibility(activity);
    }

    /** PRESET-3: wires up the top-docked pill (R.id.preset_pill) and syncs the
     *  visibility of bar vs pill with the persisted collapsed state. The pill
     *  lives outside the scrollable bar so it can sit up in the top toolbar
     *  area, clear of the preview. Safe to call repeatedly. */
    public static void refreshVisibility(final MainActivity activity) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        boolean collapsed = prefs.getBoolean(PreferenceKeys.PresetBarCollapsedPreferenceKey, false);
        View bar = activity.findViewById(R.id.preset_bar);
        if( bar != null )
            bar.setVisibility(collapsed ? View.GONE : View.VISIBLE);
        Button pill = activity.findViewById(R.id.preset_pill);
        if( pill != null ) {
            float scale = activity.getResources().getDisplayMetrics().density;
            pill.setAllCaps(false);
            pill.setTextColor(Color.WHITE);
            pill.setTextSize(13);
            pill.setMinHeight(0);
            pill.setMinimumHeight(0);
            int pad_x = (int)(10 * scale + 0.5f);
            int pad_y = (int)(3 * scale + 0.5f);
            pill.setPadding(pad_x, pad_y, pad_x, pad_y);
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(8 * scale);
            bg.setColor(0xB3000000);
            pill.setBackground(bg);
            pill.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(PreferenceKeys.PresetBarCollapsedPreferenceKey, false);
                    editor.apply();
                    build(activity);
                }
            });
            pill.setVisibility(collapsed ? View.VISIBLE : View.GONE);
        }
    }

    private static void applyPreset(MainActivity activity, int index) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        if( !hasPreset(prefs, index) ) {
            if( activity.getPreview() != null )
                activity.getPreview().showToast(null, activity.getString(R.string.preset_not_set_toast, getPresetName(activity, prefs, index)), 0, true);
            return;
        }
        String iso = prefs.getString(PreferenceKeys.PresetISOPreferenceKeyPrefix + index, CameraController.ISO_DEFAULT);
        long exposure_time = prefs.getLong(PreferenceKeys.PresetExposureTimePreferenceKeyPrefix + index, CameraController.EXPOSURE_TIME_DEFAULT);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PreferenceKeys.ISOPreferenceKey, iso);
        editor.putLong(PreferenceKeys.ExposureTimePreferenceKey, exposure_time);
        editor.putInt(PreferenceKeys.PresetActivePreferenceKey, index);
        // PRESET-2: auto-hide the bar after a preset is chosen, so it never
        // blocks the preview while framing; tap the "預設 ▶" pill to reopen
        editor.putBoolean(PreferenceKeys.PresetBarCollapsedPreferenceKey, true);
        editor.apply();
        if( MyDebug.LOG )
            Log.d(TAG, "apply preset " + index + ": iso=" + iso + " exposure_time=" + exposure_time);
        activity.updateForSettings(true, ""); // empty toast: we show our own below
        if( activity.getPreview() != null )
            activity.getPreview().showToast(null, activity.getString(R.string.preset_applied_toast, getPresetName(activity, prefs, index), describePreset(activity, prefs, index)), 0, true);
        build(activity);
    }

    private static void saveCurrentToPreset(MainActivity activity, int index) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        String iso = prefs.getString(PreferenceKeys.ISOPreferenceKey, CameraController.ISO_DEFAULT);
        if( iso.equals(CameraController.ISO_DEFAULT) ) {
            if( activity.getPreview() != null )
                activity.getPreview().showToast(null, activity.getString(R.string.preset_auto_iso_toast), 0, true);
            return;
        }
        long exposure_time = prefs.getLong(PreferenceKeys.ExposureTimePreferenceKey, CameraController.EXPOSURE_TIME_DEFAULT);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PreferenceKeys.PresetISOPreferenceKeyPrefix + index, iso);
        editor.putLong(PreferenceKeys.PresetExposureTimePreferenceKeyPrefix + index, exposure_time);
        editor.apply();
        if( MyDebug.LOG )
            Log.d(TAG, "saved preset " + index + ": iso=" + iso + " exposure_time=" + exposure_time);
        if( activity.getPreview() != null )
            activity.getPreview().showToast(null, activity.getString(R.string.preset_saved_toast, getPresetName(activity, prefs, index), describePreset(activity, prefs, index)), 0, true);
        build(activity);
    }
}
