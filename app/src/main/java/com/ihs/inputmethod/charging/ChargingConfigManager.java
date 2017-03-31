package com.ihs.inputmethod.charging;


import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.utils.PreferenceUtils;

import java.util.Date;

/**
 * Created by jixiang on 16/4/20.
 *
 * charging 相关配置
 */
public class ChargingConfigManager {

    private final static String PREF_KEY_SHOULD_HIDE_CHARGING_HINT = "display_charging_hint";
    public final static String PREF_KEY_USER_SET_CHARGING_TOGGLE = "user_set_charging_toggle";
    public final static String PREF_KEY_CHARGING_NEW_USER = "charging_new_user";
    public final static String PREF_KEY_LOCK_VIEW_CLICKED = "pref_key_lock_view_clicked";

    private final boolean CHARGING_TOGGLE_DEFAULT_VALUE = false;

    private static ChargingConfigManager instance;
    public static ChargingConfigManager getManager() {
        if (instance == null) {
            synchronized (ChargingConfigManager.class) {
                if (instance == null) {
                    instance = new ChargingConfigManager();
                }
            }
        }
        return instance;
    }

    private ChargingConfigManager() {

    }

    /**
     * 是否显示过Charging的提示框
     * @return
     */
    public boolean isDisplayChargingHint() {
        return PreferenceUtils.getBoolean(PREF_KEY_SHOULD_HIDE_CHARGING_HINT);
    }

    public void setDisplayChargingHint(boolean display) {
        PreferenceUtils.setBoolean(PREF_KEY_SHOULD_HIDE_CHARGING_HINT, display);
    }


    /**
     * 是否该开启Charging功能
     * @return
     */
    public boolean shouldOpenChargingFunction(){
        //总控制，如果关了则直接返回false
        boolean chargingEnable =
                HSConfig.optBoolean(false, "Application", "ChargeLocker",  "enable");
        if(chargingEnable) {
            HSPreferenceHelper prefs = HSPreferenceHelper.getDefault(HSApplication.getContext());

            if(prefs.contains(PREF_KEY_USER_SET_CHARGING_TOGGLE)){
                boolean userSetValue = prefs.getBoolean(HSApplication.getContext().getResources().getString(R.string.config_charge_switchpreference_key), CHARGING_TOGGLE_DEFAULT_VALUE);
                HSLog.d("jx,用户设置过charging的开关，用户设置的结果为:"+userSetValue);
                return userSetValue;
            }

            // 如果未发现remote config变化，则默认打开
            if (!prefs.contains(PREF_KEY_CHARGING_NEW_USER)) {
                HSLog.d("jx,未发现remote config变化 shouldOpenChargingFunction");
                return true;
            }
            boolean newUserConfig = prefs.getBoolean(PREF_KEY_CHARGING_NEW_USER, false);
            boolean firstLaunchShowedUserConfig =
                    HSConfig.optBoolean(false, "Application", "ChargeLocker", "FirstLaunchShowedUser");
            Date configDate =
                    HSConfig.optDate(new Date(), "Application", "ChargeLocker", "AfterInstallDateUser");
            long currentSessionId = HSSessionMgr.getCurrentSessionId();
            if (newUserConfig && currentSessionId == 1) {
                HSLog.d("jx,newUser为true且是第一次打开");
                return true;
            }
            if (newUserConfig && currentSessionId > 1 && firstLaunchShowedUserConfig) {
                HSLog.d("jx,newUser为true且是第2次及以上打开");
                return true;
            }
            long firstSessionStartTime = HSSessionMgr.getFirstSessionStartTime();
            HSLog.d("jx,firstSessionStartTime:"+firstSessionStartTime+",configDate:"+configDate.getTime());
            if (firstSessionStartTime > configDate.getTime()) {
                HSLog.d("jx,首次打开时间大于configDate");
                return true;
            }
        }else {
            HSLog.d("jx,chargingEnable 为false");
        }
        HSLog.d("jx,不满足开启charging要求，返回false");
        return false;
    }

    /**
     * 是否应开启charging功能
     * @return
     */
    public boolean enableChargingFunction(){
        return  HSConfig.optBoolean(false, "Application", "ChargeLocker",  "enable");
    }

    /**
     * 是否用户设置过charging的开关
     * @return
     */
    public boolean isUserSetChargingToggle(){
        return HSPreferenceHelper.getDefault(HSApplication.getContext()).contains(PREF_KEY_USER_SET_CHARGING_TOGGLE);
    }

    /**
     * 用户设置过charging的开关
     */
    public void setUserChangeChargingToggle(){
        HSPreferenceHelper.getDefault(HSApplication.getContext()).putBoolean(PREF_KEY_USER_SET_CHARGING_TOGGLE,true);
    }

    /**
     * 设置lockView被点击过，则不现实任何出厂动画
     */
    public void setLockViewClicked(){
        boolean lockViewClicked = HSPreferenceHelper.getDefault(HSApplication.getContext()).getBoolean(PREF_KEY_LOCK_VIEW_CLICKED, false);
        if(!lockViewClicked){
            HSPreferenceHelper.getDefault(HSApplication.getContext()).putBoolean(PREF_KEY_LOCK_VIEW_CLICKED,true);
        }
    }

    /**
     * 判断是否lockView被点击过
     * @return
     */
    public boolean isLockViewClicked(){
        return HSPreferenceHelper.getDefault(HSApplication.getContext()).getBoolean(PREF_KEY_LOCK_VIEW_CLICKED, false);
    }
}