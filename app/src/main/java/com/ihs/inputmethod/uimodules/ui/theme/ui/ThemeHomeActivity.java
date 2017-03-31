package com.ihs.inputmethod.uimodules.ui.theme.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ihs.actiontrigger.utils.HSPermissionManager;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.iap.HSIAPManager;
import com.ihs.inputmethod.api.HSFloatWindowManager;
import com.ihs.inputmethod.api.HSUIInputMethod;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSToastUtils;
import com.ihs.inputmethod.feature.apkupdate.ApkUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.constants.KeyboardActivationProcessor;
import com.ihs.inputmethod.uimodules.ui.settings.activities.HSAppCompatActivity;
import com.ihs.inputmethod.uimodules.ui.theme.iap.IAPManager;
import com.ihs.inputmethod.uimodules.ui.theme.reward.RewardVideoHelper;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity;
import com.ihs.inputmethod.uimodules.utils.HSAppLockerUtils;
import com.ihs.inputmethod.uimodules.widget.TrialKeyboardDialog;
import com.keyboard.core.themes.custom.KCCustomThemeManager;

import org.json.JSONObject;

import static android.view.View.GONE;

/**
 * Created by jixiang on 16/8/17.
 */
public class ThemeHomeActivity extends HSAppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, KeyboardActivationProcessor.OnKeyboardActivationChangedListener, TrialKeyboardDialog.OnTrialKeyboardStateChanged {
    private static final String SP_LAST_USAGE_ALERT_SESSION_ID = "SP_LAST_USAGE_ALERT_SESSION_ID";

    private static int HANDLER_SHOW_ACTIVE_DIALOG = 101;
    private static int HANDLER_SHOW_UPDATE_DIALOG = 102;

    private AppBarLayout appbarLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FloatingActionButton fab;

    private final static String MY_THEME_FRAGMENT_TAG = "fragment_tag_my_theme";
    private final static String THEME_STORE_FRAGMENT_TAG = "fragment_tag_theme_store";
    private String currentFragmentTag = THEME_STORE_FRAGMENT_TAG;

    public final static String INTENT_KEY_SHOW_TRIAL_KEYBOARD = "SHOW_TRIAL_KEYBOARD";
    private TrialKeyboardDialog trialKeyboardDialog;

    private boolean isFromUsageAccessActivity;
    private View enableTipTV;
    private boolean shouldShowActivationTip;

    private ThemeHomeActivity context = ThemeHomeActivity.this;

    private KeyboardActivationProcessor keyboardActivationProcessor;

    private View apkUpdateTip;
    private View noAds;

    private static final int keyboardActivationFromHome = 11;
    private static final int keyboardActivationFromHomeWithTrial = 12;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HANDLER_SHOW_ACTIVE_DIALOG) {
                if (!HSInputMethod.isCurrentIMESelected()) {
                    keyboardActivationProcessor.showHomePageActivationDialog(ThemeHomeActivity.this);
                }
            } else if (msg.what == HANDLER_SHOW_UPDATE_DIALOG) {
                checkAndShowApkUpdateAlert(false);
            }
        }
    };

    private HSIAPManager.HSIAPListener removeAdsListener = new HSIAPManager.HSIAPListener() {
        @Override
        public void onPurchaseSucceeded(String productId) {
            HSLog.d("onIAPProductPurchaseSucceeded:productId:" + productId);
        }

        @Override
        public void onPurchaseFailed(String productId, int errorCode) {
            HSLog.d("onIAPProductPurchaseFailed:productId:" + productId + ",errorCode:" + errorCode);
            HSIAPManager.getInstance().removeListener(this);
        }

        @Override
        public void onVerifySucceeded(String productId, JSONObject jsonObject) {
            HSLog.d("onIAPProductVerifySucceeded:productId:" + productId);
            Toast.makeText(HSApplication.getContext(), HSApplication.getContext().getString(R.string.purchase_success), Toast.LENGTH_LONG).show();
            IAPManager.getManager().onVerifySuccessed(productId, jsonObject);
            HSIAPManager.getInstance().removeListener(this);
            HSGlobalNotificationCenter.sendNotification(ThemeHomeFragment.NOTIFICATION_REMOVEADS_PURCHASED);
        }

        @Override
        public void onVerifyFailed(String productId, int errorCode) {
            HSLog.d("onVerifyFailed:errorCode:" + errorCode);
            IAPManager.getManager().onVerifyFailed(productId, errorCode);
            HSIAPManager.getInstance().removeListener(this);
            HSGlobalNotificationCenter.sendNotification(HSKeyboardThemeManager.HS_NOTIFICATION_THEME_LIST_CHANGED);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RewardVideoHelper rewardVideoHelper = new RewardVideoHelper(this, null);
        rewardVideoHelper.loadVideo();

        setContentView(R.layout.activity_theme_home);

        // Init custom theme res in case we fail before
        //HSKeyboardThemeManager.initCustomThemeResource();
        KCCustomThemeManager.getInstance();

        appbarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        String themeTitle = getResources().getString(R.string.theme_nav_theme_store);
        toolbar.setTitle(HSLog.isDebugging() ? themeTitle + " (Debug)" : themeTitle);
        setSupportActionBar(toolbar);

        keyboardActivationProcessor = new KeyboardActivationProcessor(ThemeHomeActivity.class, ThemeHomeActivity.this);

        enableTipTV = findViewById(R.id.tv_enable_keyboard);
        enableTipTV.setVisibility(GONE);
        enableTipTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("activate_appbar_clicked");
                keyboardActivationProcessor.activateKeyboard(ThemeHomeActivity.this, false, keyboardActivationFromHome);
            }
        });
        if (getIntent() != null && getIntent().getBooleanExtra(com.ihs.inputmethod.uimodules.constants.Constants.BUNDLE_AUTO_ENABLE_KEYBOARD, false)) {
            keyboardActivationProcessor.activateKeyboard(ThemeHomeActivity.this, false, keyboardActivationFromHome);
        }
        findViewById(R.id.home_create_theme_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();
                String customEntry = THEME_STORE_FRAGMENT_TAG.equals(currentFragmentTag) ? "store_float_button" : "mytheme_float_button";
                bundle.putString(CustomThemeActivity.BUNDLE_KEY_CUSTOMIZE_ENTRY, customEntry);
                IAPManager.getManager().startCustomThemeActivityIfSlotAvailable(ThemeHomeActivity.this, bundle);

                HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("customize_entry_clicked", THEME_STORE_FRAGMENT_TAG.equals(currentFragmentTag) ? "store" : "mythemes");
                HSAnalytics.logEvent("customize_entry_clicked", THEME_STORE_FRAGMENT_TAG.equals(currentFragmentTag) ? "store" : "mythemes");
            }
        });
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                context, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                clearApkUpdateTip();
                HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("sidebar_show", THEME_STORE_FRAGMENT_TAG.equals(currentFragmentTag) ? "store" : "mythemes");
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);//去除自动染色，默认图标灰色，点击后变成colorAccent色
        navigationView.setItemTextColor(null);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        ViewGroup.LayoutParams layoutParams = headerView.getLayoutParams();
        layoutParams.height = (int) (getResources().getDisplayMetrics().widthPixels * 0.48f);

        //remove myThemeFragment if exist
        Fragment myThemeFragment = getFragmentManager().findFragmentByTag(MY_THEME_FRAGMENT_TAG);
        if (myThemeFragment != null) {
            getFragmentManager().beginTransaction().remove(myThemeFragment).commit();
        }

        //create storeFragment only if not exist
        Fragment storeFragment = getFragmentManager().findFragmentByTag(THEME_STORE_FRAGMENT_TAG);
        if (storeFragment == null) {
            storeFragment = new ThemeHomeFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.content_layout, storeFragment, THEME_STORE_FRAGMENT_TAG).commit();
        }
        currentFragmentTag = THEME_STORE_FRAGMENT_TAG;

        //init locker function
        boolean lockerEnable = getResources().getBoolean(R.bool.config_locker_drawer_visiable_enable) && HSAppLockerUtils.isLockerEnabled();
        navigationView.getMenu().findItem(R.id.nav_app_locker).setVisible(lockerEnable);

        // init update function
        navigationView.getMenu().findItem(R.id.nav_update).setVisible(ApkUtils.isUpdateEnabled());
        apkUpdateTip = findViewById(R.id.apk_update_tip);


        //界面被启动 请求 扫描权限
        if (getResources().getBoolean(R.bool.config_ask_for_usage_permission) && !HSPermissionManager.isUsageAccessGranted() && shouldShowUsageAccessAlert()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompactDialogStyle);
            builder.setTitle(getString(R.string.dialog_app_usage_title));
            builder.setMessage(getString(R.string.dialog_app_usage_tips));
            builder.setPositiveButton(getString(R.string.dialog_agree).toUpperCase(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (Build.VERSION.SDK_INT >= 21 && !HSPermissionManager.isUsageAccessGranted()) {
                        isFromUsageAccessActivity = true;
                    }
                    HSPermissionManager.enableUsageAccessPermission();
                    HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("appalert_usageaccess_agree_clicked");
                }
            });
            builder.setNegativeButton(getString(R.string.dialog_disagree).toUpperCase(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            HSPreferenceHelper.getDefault().putInt(SP_LAST_USAGE_ALERT_SESSION_ID, HSSessionMgr.getCurrentSessionId());
            builder.show();
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("appalert_usageaccess_showed");
        }

        HSGlobalNotificationCenter.addObserver(CustomThemeActivity.NOTIFICATION_SHOW_TRIAL_KEYBOARD, notificationObserver);

        //如果是第一次进入页面并且当前键盘没有被选为自己则弹框。
        if (!HSInputMethod.isCurrentIMESelected()) {
            handler.sendEmptyMessageDelayed(HANDLER_SHOW_ACTIVE_DIALOG, 500);
        } else {
            handler.sendEmptyMessageDelayed(HANDLER_SHOW_UPDATE_DIALOG, 500);
        }

        onNewIntent(getIntent());
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        boolean showTrial = intent.getBooleanExtra(INTENT_KEY_SHOW_TRIAL_KEYBOARD, false);
        if (showTrial) {
            handler.removeMessages(HANDLER_SHOW_ACTIVE_DIALOG);
            showTrialKeyboardDialog(keyboardActivationFromHomeWithTrial, false);
            getIntent().putExtra(INTENT_KEY_SHOW_TRIAL_KEYBOARD, false);
        }
        MenuItem item = navigationView.getMenu().findItem(R.id.nav_theme_store);
        onNavigationItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        newConfig.orientation = Configuration.ORIENTATION_PORTRAIT;
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                HSFloatWindowManager.getInstance().removeAccessibilityCover();
            }
        }, 1000);
    }

    @Override
    protected void onResume() {
        super.onResume();

        restoreNavigationView();

        shouldShowActivationTip = true;

        if (isFromUsageAccessActivity) {
            isFromUsageAccessActivity = false;
            if (HSPermissionManager.isUsageAccessGranted()) {
                HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("permission_usage_access");
            }
        }

        refreshApkUpdateViews();
//        UIController.getInstance().getUIHandler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                int currentapiVersion = android.os.Build.VERSION.SDK_INT;
//                if (currentapiVersion > android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                    HSLog.d("should delay rate alert for sdk version between 4.0 and 4.2");
//                    if (PreferenceManager.getDefaultSharedPreferences(ThemeHomeActivity.this).getBoolean("CUSTOM_THEME_SAVE", false)) {
//                        PreferenceManager.getDefaultSharedPreferences(ThemeHomeActivity.this).edit().putBoolean("CUSTOM_THEME_SAVE", false).apply();
//                        HSAlertMgr.showRateAlert();
//                    } else {
//                        HSLog.e("CUSTOM_THEME_SAVE_NULL");
//                    }
//                }
//            }
//        }, 100);

    }

    @Override
    protected void onStop() {
        super.onStop();
        System.gc();
        if (homeKeyTracker.isHomeKeyPressed() && trialKeyboardDialog != null && trialKeyboardDialog.isShowing()) {
            trialKeyboardDialog.dismiss();
        }
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_theme_store) {
            if (!currentFragmentTag.equals(THEME_STORE_FRAGMENT_TAG)) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                Fragment myThemeFragment = getFragmentManager().findFragmentByTag(MY_THEME_FRAGMENT_TAG);
                Fragment themeStoreFragment = getFragmentManager().findFragmentByTag(THEME_STORE_FRAGMENT_TAG);
                if (myThemeFragment != null) {
                    transaction.hide(myThemeFragment);
                }
                if (themeStoreFragment == null) {
                    themeStoreFragment = new ThemeHomeFragment();
                    transaction.add(R.id.content_layout, themeStoreFragment, THEME_STORE_FRAGMENT_TAG);
                }
                transaction.show(themeStoreFragment).commit();
                appbarLayout.setExpanded(true);
                toolbar.setTitle(R.string.theme_nav_theme_store);
                HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("sidebar_store_clicked");
            }
            currentFragmentTag = THEME_STORE_FRAGMENT_TAG;
        } else if (id == R.id.nav_my_themes) {
            if (!currentFragmentTag.equals(MY_THEME_FRAGMENT_TAG)) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                Fragment myThemeFragment = getFragmentManager().findFragmentByTag(MY_THEME_FRAGMENT_TAG);
                Fragment themeStoreFragment = getFragmentManager().findFragmentByTag(THEME_STORE_FRAGMENT_TAG);
                if (themeStoreFragment != null) {
                    transaction.hide(themeStoreFragment);
                }
                if (myThemeFragment == null) {
                    myThemeFragment = new MyThemeFragment();
                    transaction.add(R.id.content_layout, myThemeFragment, MY_THEME_FRAGMENT_TAG);
                }
                transaction.show(myThemeFragment).commit();
                appbarLayout.setExpanded(true);
                toolbar.setTitle(R.string.theme_nav_my_themes);
                HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("sidebar_mythemes_clicked");
            }
            currentFragmentTag = MY_THEME_FRAGMENT_TAG;
        } else if (id == R.id.nav_language) {
            HSUIInputMethod.launchMoreLanguageActivity();
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("sidebar_languages_clicked");
        } else if (id == R.id.nav_setting) {
            HSUIInputMethod.launchSettingsActivity();
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("sidebar_settings_clicked");
        } else if (id == R.id.nav_app_locker) {
            HSGlobalNotificationCenter.sendNotificationOnMainThread(HSUIInputMethod.HS_NOTIFICATION_LOCKER_CLICK);
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("sidebar_applocker_clicked");
        } else if (id == R.id.nav_update) {
            handler.removeMessages(HANDLER_SHOW_UPDATE_DIALOG);
            checkAndShowApkUpdateAlert(true);
            HSGoogleAnalyticsUtils.getInstance().logAppEvent("sidebar_update_clicked");

            handler.post(new Runnable() {
                @Override
                public void run() {
                    restoreNavigationView();
                }
            });
        } else if (id == R.id.nav_no_ads) {
            HSIAPManager.getInstance().addListener(removeAdsListener);
            HSGoogleAnalyticsUtils.getInstance().logAppEvent("removeAds_clicked");
            IAPManager.getManager().purchaseNoAds();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private INotificationObserver notificationObserver = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (CustomThemeActivity
                    .NOTIFICATION_SHOW_TRIAL_KEYBOARD.equals(s)) {
                if (hsBundle != null) {
                    String showTrialKeyboardActivityName = hsBundle.getString(TrialKeyboardDialog.BUNDLE_KEY_SHOW_TRIAL_KEYBOARD_ACTIVITY, "");
                    int activationCode = hsBundle.getInt(KeyboardActivationProcessor.BUNDLE_ACTIVATION_CODE);
                    boolean hasTrialKeyboardShownWhenThemeCreated = hsBundle.getBoolean(TrialKeyboardDialog.BUNDLE_KEY_HAS_TRIAL_KEYBOARD_SHOWN_WHEN_THEME_CREATED, false);
                    if (ThemeHomeActivity.class.getSimpleName().equals(showTrialKeyboardActivityName)) {
                        showTrialKeyboardDialog(activationCode, hasTrialKeyboardShownWhenThemeCreated);
                    }
                }
            }
        }
    };

    private void showTrialKeyboardDialog(int activationCode, boolean hasTrialKeyboardShownWhenThemeCreated) {
        if (trialKeyboardDialog == null) {
            trialKeyboardDialog = new TrialKeyboardDialog.Build(ThemeHomeActivity.class.getName()).create(context, this, hasTrialKeyboardShownWhenThemeCreated);
        }
        trialKeyboardDialog.show(this, activationCode);
    }

    @Override
    protected void onDestroy() {
        if (trialKeyboardDialog != null) {
            trialKeyboardDialog.dismiss();
            trialKeyboardDialog = null;
        }

        HSGlobalNotificationCenter.removeObserver(notificationObserver);
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            finishAffinity();
        }

    }


    private boolean shouldShowUsageAccessAlert() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int currentSessionId = HSSessionMgr.getCurrentSessionId();
            if (HSPreferenceHelper.getDefault().getInt(SP_LAST_USAGE_ALERT_SESSION_ID, 0) == 0) {
                HSPreferenceHelper.getDefault().putInt(SP_LAST_USAGE_ALERT_SESSION_ID, currentSessionId);
                return true;
            } else {
                if (currentSessionId - HSPreferenceHelper.getDefault().getInt(SP_LAST_USAGE_ALERT_SESSION_ID, 0)
                        >= HSConfig.optInteger(0, "Application", "AccessUsageAlertInterval")) {
                    HSPreferenceHelper.getDefault().putInt(SP_LAST_USAGE_ALERT_SESSION_ID, currentSessionId);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void activeDialogShowing() {
        enableTipTV.setVisibility(GONE);
    }

    public void keyboardSelected(int requestCode) {
        if (keyboardActivationFromHomeWithTrial == requestCode || CustomThemeActivity.keyboardActivationFromCustom == requestCode) {
            showTrialKeyboardDialog(requestCode, false);
        }
        enableTipTV.setVisibility(GONE);
    }

    @Override
    public void activeDialogCanceled() {
    }

    @Override
    public void activeDialogDismissed() {
        if (!HSInputMethod.isCurrentIMESelected()) {
            enableTipTV.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTrialKeyShow(int requestCode) {
        enableTipTV.setVisibility(GONE);

        switch (requestCode) {
            case keyboardActivationFromHomeWithTrial:
                HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_theme_try_viewed", "themepackage");
                break;
            case CustomThemeActivity.keyboardActivationFromCustom:
                HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_theme_try_viewed", "customizetheme");
                break;
            default:
                HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_theme_try_viewed", "apply");
                break;
        }
    }

    @Override
    public void onTrailKeyPrevented() {
        if (!HSInputMethod.isCurrentIMESelected()) {
            enableTipTV.setVisibility(View.VISIBLE);
        }
    }

    private void restoreNavigationView() {
        if (THEME_STORE_FRAGMENT_TAG.equals(currentFragmentTag)) {
            navigationView.setCheckedItem(R.id.nav_theme_store);
            if (shouldShowActivationTip && !HSInputMethod.isCurrentIMESelected()) {
                enableTipTV.setVisibility(View.VISIBLE);
            } else {
                enableTipTV.setVisibility(GONE);
            }

        } else if (MY_THEME_FRAGMENT_TAG.equals(currentFragmentTag)) {
            navigationView.setCheckedItem(R.id.nav_my_themes);
        }
    }

    private void resetApkUpdateViews() {
        apkUpdateTip.setVisibility(View.GONE);
        navigationView.getMenu().findItem(R.id.nav_update).getActionView().setVisibility(View.GONE);
    }

    private void refreshApkUpdateViews() {
        resetApkUpdateViews();

        if (ApkUtils.shouldUpdate()) {
            if (shouldShowApkUpdateTip(ApkUtils.getLatestVersionCode())) {
                showApkUpdateTip();
            }

            showApkUpdateMenuItemIndicationIcon();
        }
    }

    private void checkAndShowApkUpdateAlert(final boolean force) {
        if (ApkUtils.checkAndShowUpdateAlert(force)) {
            return;
        }

        if (force) {
            HSToastUtils.toastCenterLong(getResources().getString(R.string.apk_update_to_date_tip));
        }
    }

    private void clearApkUpdateTip() {
        if (apkUpdateTip.getVisibility() == View.VISIBLE) {
            apkUpdateTip.setVisibility(View.GONE);
            ApkUtils.saveUpdateApkVersionCode();
        }
    }

    private void showApkUpdateTip() {
        apkUpdateTip.setVisibility(View.VISIBLE);
        HSGoogleAnalyticsUtils.getInstance().logAppEvent("app_menu_reddot_show");
    }

    private void showApkUpdateMenuItemIndicationIcon() {
        navigationView.getMenu().findItem(R.id.nav_update).getActionView().setVisibility(View.VISIBLE);
        HSGoogleAnalyticsUtils.getInstance().logAppEvent("sidebar_update_icon_show");
    }

    private boolean shouldShowApkUpdateTip(final int versionCode) {
        // Get update apk version code from local storage and remote
        final int versionCodeOfLocalStorage = ApkUtils.getUpdateApkVersionCode();

        HSLog.d("Local storage version code to update: " + versionCodeOfLocalStorage + ", latest version code: " + versionCode);

        // If we have record version code and not less than latest, means we already show the dot
        if (versionCodeOfLocalStorage >= versionCode) {
            return false;
        }

        return true;
    }
}