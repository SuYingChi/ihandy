package com.ihs.inputmethod.utils;

import com.artw.lockscreen.LockerSettings;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;


/**
 * Created by yanxia on 2017/8/31.
 */

public class ScreenLockerConfigUtils {
    private static final String LOG_TAG = ScreenLockerConfigUtils.class.getSimpleName();

    private final static String PREF_KEY_SCREEN_LOCKER_ENABLE_ALERT_SHOW_COUNT = "pref_key_screen_locker_enable_alert_show_count";
    private final static int MAX_SHOW_COUNT = 2;

    private static boolean shouldFocusOnEnableEvent() {
        if (LockerSettings.getLockerEnableStates() == LockerSettings.LOCKER_MUTED) {
            HSLog.i(LOG_TAG, "Screen locker is MUTED");
            return false;
        }

        if (LockerSettings.isUserTouchedLockerSettings()) {
            HSLog.i(LOG_TAG, "Screen locker enabled before");
            return false;
        }

        if (LockerSettings.isLockerEnabled()) {
            HSLog.i(LOG_TAG, "Screen locker opened now");
            return false;
        }

        return true;
    }

    public static void increaseEnableAlertShowCount() {
        int showCount = HSPreferenceHelper.getDefault(HSApplication.getContext()).getInt(PREF_KEY_SCREEN_LOCKER_ENABLE_ALERT_SHOW_COUNT, 0);
        showCount++;
        HSPreferenceHelper.getDefault(HSApplication.getContext()).putInt(PREF_KEY_SCREEN_LOCKER_ENABLE_ALERT_SHOW_COUNT, showCount);
    }

    public static boolean shouldShowScreenLockerAlert(boolean limitShowCount) {
        if (!shouldFocusOnEnableEvent()) {
            return false;
        }

        if (limitShowCount) {
            if (isEnableAlertShowCountAchievedMax()) {
                HSLog.i(LOG_TAG, "Screen locker enable alert achieved max show count");
                return false;
            }
        }

        return true;
    }

    private static boolean isEnableAlertShowCountAchievedMax() {
        return HSPreferenceHelper.getDefault(HSApplication.getContext()).getInt(PREF_KEY_SCREEN_LOCKER_ENABLE_ALERT_SHOW_COUNT, 0) >= MAX_SHOW_COUNT;
    }
}
