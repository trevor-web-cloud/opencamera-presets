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
        // PRESET-5: calibration wizard entry point (guided re-shoot after a phone change)
        Button calib_button = makeBarButton(activity, activity.getString(R.string.preset_calibrate), scale, false);
        calib_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCalibration(activity);
            }
        });
        container.addView(calib_button);
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
        refreshCalibBanner(activity);
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

    // ------------------------------------------------------------------
    // PRESET-6: guided calibration re-shoot wizard (revised from PRESET-5
    // after on-device feedback).
    //
    // Purpose: after changing phones, the previously used ISO + exposure
    // values no longer guarantee the same OCR photo quality (different
    // sensor, lens and image processing). The wizard brackets the
    // exposure time around an adjustable base so the resulting batch can
    // be compared against the old phone's golden set.
    //
    // Feedback-driven design (PRESET-6):
    //  - No preset (box) name is designated: the wizard is a single run,
    //    not a walk through named presets.
    //  - The base is adjustable: the wizard starts from the CURRENT
    //    manual ISO + exposure time, and at any point the user can dial
    //    new values with the app's normal exposure controls and tap the
    //    "set as base" button to re-base the remaining steps.
    // Steps: saved base, +1/2 EV, +1 EV, -1/2 EV, -1 EV (exposure-time
    // factors x1, x1.41, x2, x0.71, x0.5); ISO stays at the base value.
    // The user frames and shoots one photo per step, then taps the top
    // banner to advance. Capture is deliberately manual. State lives in
    // SharedPreferences so it survives rotation and restarts. Leaving the
    // wizard restores the settings as they were when it was started.
    // ------------------------------------------------------------------

    private static final double[] CALIB_FACTORS = {1.0, 1.41421356, 2.0, 0.70710678, 0.5};
    private static final int CALIB_STEPS = CALIB_FACTORS.length;

    private static void startCalibration(MainActivity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        String iso = prefs.getString(PreferenceKeys.ISOPreferenceKey, CameraController.ISO_DEFAULT);
        if( iso.equals(CameraController.ISO_DEFAULT) ) {
            if( activity.getPreview() != null )
                activity.getPreview().showToast(null, activity.getString(R.string.preset_auto_iso_toast), 0, true);
            return;
        }
        long exposure_time = prefs.getLong(PreferenceKeys.ExposureTimePreferenceKey, CameraController.EXPOSURE_TIME_DEFAULT);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PreferenceKeys.PresetCalibActivePreferenceKey, true);
        editor.putInt(PreferenceKeys.PresetCalibStepPreferenceKey, 0);
        editor.putString(PreferenceKeys.PresetCalibBaseISOPreferenceKey, iso);
        editor.putLong(PreferenceKeys.PresetCalibBaseExposurePreferenceKey, exposure_time);
        editor.putString(PreferenceKeys.PresetCalibOrigISOPreferenceKey, iso);
        editor.putLong(PreferenceKeys.PresetCalibOrigExposurePreferenceKey, exposure_time);
        // collapse the bar so only the banner and the preview stay visible
        editor.putBoolean(PreferenceKeys.PresetBarCollapsedPreferenceKey, true);
        editor.apply();
        if( MyDebug.LOG )
            Log.d(TAG, "calibration started: base iso=" + iso + " exposure_time=" + exposure_time);
        applyCalibStep(activity);
        build(activity);
    }

    private static void applyCalibStep(MainActivity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        int step = prefs.getInt(PreferenceKeys.PresetCalibStepPreferenceKey, 0);
        String iso = prefs.getString(PreferenceKeys.PresetCalibBaseISOPreferenceKey, CameraController.ISO_DEFAULT);
        long base_exposure = prefs.getLong(PreferenceKeys.PresetCalibBaseExposurePreferenceKey, CameraController.EXPOSURE_TIME_DEFAULT);
        long exposure_time = Math.max(1L, Math.round(base_exposure * CALIB_FACTORS[step]));
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PreferenceKeys.ISOPreferenceKey, iso);
        editor.putLong(PreferenceKeys.ExposureTimePreferenceKey, exposure_time);
        editor.apply();
        if( MyDebug.LOG )
            Log.d(TAG, "calibration step " + step + ": iso=" + iso + " exposure_time=" + exposure_time);
        activity.updateForSettings(true, ""); // empty toast: the banner shows the step
        refreshCalibBanner(activity);
    }

    private static void advanceCalibration(MainActivity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        int step = prefs.getInt(PreferenceKeys.PresetCalibStepPreferenceKey, 0) + 1;
        if( step >= CALIB_STEPS ) {
            endCalibration(activity, true);
            return;
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PreferenceKeys.PresetCalibStepPreferenceKey, step);
        editor.apply();
        applyCalibStep(activity);
    }

    /** PRESET-6: re-base the wizard on the values currently dialed in with
     *  the app's normal exposure controls, so the base stays adjustable. */
    private static void rebaseCalibration(MainActivity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        String iso = prefs.getString(PreferenceKeys.ISOPreferenceKey, CameraController.ISO_DEFAULT);
        if( iso.equals(CameraController.ISO_DEFAULT) ) {
            if( activity.getPreview() != null )
                activity.getPreview().showToast(null, activity.getString(R.string.preset_auto_iso_toast), 0, true);
            return;
        }
        long exposure_time = prefs.getLong(PreferenceKeys.ExposureTimePreferenceKey, CameraController.EXPOSURE_TIME_DEFAULT);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PreferenceKeys.PresetCalibBaseISOPreferenceKey, iso);
        editor.putLong(PreferenceKeys.PresetCalibBaseExposurePreferenceKey, exposure_time);
        editor.apply();
        if( MyDebug.LOG )
            Log.d(TAG, "calibration re-based: iso=" + iso + " exposure_time=" + exposure_time);
        applyCalibStep(activity);
        if( activity.getPreview() != null ) {
            String exp_string = activity.getPreview().getExposureTimeString(exposure_time);
            activity.getPreview().showToast(null, activity.getString(R.string.preset_calib_rebased_toast, iso, exp_string), 0, true);
        }
    }

    private static void endCalibration(MainActivity activity, boolean finished) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        // restore the settings as they were when the wizard was started
        String iso = prefs.getString(PreferenceKeys.PresetCalibOrigISOPreferenceKey, CameraController.ISO_DEFAULT);
        long exposure_time = prefs.getLong(PreferenceKeys.PresetCalibOrigExposurePreferenceKey, CameraController.EXPOSURE_TIME_DEFAULT);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PreferenceKeys.ISOPreferenceKey, iso);
        editor.putLong(PreferenceKeys.ExposureTimePreferenceKey, exposure_time);
        editor.putBoolean(PreferenceKeys.PresetCalibActivePreferenceKey, false);
        editor.apply();
        activity.updateForSettings(true, "");
        if( MyDebug.LOG )
            Log.d(TAG, "calibration ended, finished=" + finished);
        if( activity.getPreview() != null )
            activity.getPreview().showToast(null, activity.getString(finished ? R.string.preset_calib_done_toast : R.string.preset_calib_ended_toast), 0, true);
        refreshCalibBanner(activity);
        build(activity);
    }

    /** PRESET-6: syncs the top calibration banner (R.id.calib_banner) and the
     *  "set as base" button (R.id.calib_rebase) with the persisted wizard
     *  state. Banner tap = next step, banner long-press = end and restore. */
    public static void refreshCalibBanner(final MainActivity activity) {
        Button banner = activity.findViewById(R.id.calib_banner);
        Button rebase = activity.findViewById(R.id.calib_rebase);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        boolean active = prefs.getBoolean(PreferenceKeys.PresetCalibActivePreferenceKey, false);
        if( !active ) {
            if( banner != null )
                banner.setVisibility(View.GONE);
            if( rebase != null )
                rebase.setVisibility(View.GONE);
            return;
        }
        int step = prefs.getInt(PreferenceKeys.PresetCalibStepPreferenceKey, 0);
        String iso = prefs.getString(PreferenceKeys.PresetCalibBaseISOPreferenceKey, CameraController.ISO_DEFAULT);
        long base_exposure = prefs.getLong(PreferenceKeys.PresetCalibBaseExposurePreferenceKey, CameraController.EXPOSURE_TIME_DEFAULT);
        long exposure_time = Math.max(1L, Math.round(base_exposure * CALIB_FACTORS[step]));
        String exp_string = activity.getPreview() != null ? activity.getPreview().getExposureTimeString(exposure_time) : "";
        String[] step_labels = activity.getResources().getStringArray(R.array.preset_calib_step_labels);
        String step_label = step < step_labels.length ? step_labels[step] : "";
        float scale = activity.getResources().getDisplayMetrics().density;
        if( banner != null ) {
            banner.setAllCaps(false);
            banner.setTextColor(Color.WHITE);
            banner.setTextSize(13);
            banner.setMinHeight(0);
            banner.setMinimumHeight(0);
            int pad_x = (int)(10 * scale + 0.5f);
            int pad_y = (int)(5 * scale + 0.5f);
            banner.setPadding(pad_x, pad_y, pad_x, pad_y);
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(8 * scale);
            bg.setColor(0xCC1565C0);
            banner.setBackground(bg);
            banner.setText(activity.getString(R.string.preset_calib_banner,
                    step + 1, CALIB_STEPS, iso, exp_string, step_label));
            banner.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    advanceCalibration(activity);
                }
            });
            banner.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    endCalibration(activity, false);
                    return true;
                }
            });
            banner.setVisibility(View.VISIBLE);
        }
        if( rebase != null ) {
            rebase.setAllCaps(false);
            rebase.setTextColor(Color.WHITE);
            rebase.setTextSize(13);
            rebase.setMinHeight(0);
            rebase.setMinimumHeight(0);
            int pad_x = (int)(10 * scale + 0.5f);
            int pad_y = (int)(3 * scale + 0.5f);
            rebase.setPadding(pad_x, pad_y, pad_x, pad_y);
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(8 * scale);
            bg.setColor(0xB3000000);
            rebase.setBackground(bg);
            rebase.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rebaseCalibration(activity);
                }
            });
            rebase.setVisibility(View.VISIBLE);
        }
    }
}
