package com.keyboard.inputmethod.panels;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.HSInputMethod;
import com.ihs.inputmethod.api.HSInputMethodPanel;
import com.keyboard.inputmethod.panels.fonts.HSFontSelectPanel;
import com.keyboard.inputmethod.panels.settings.HSSettingsPanel;
import com.keyboard.inputmethod.panels.theme.HSThemeSelectPanel;


/**
 * Created by xu.zhang on 10/30/15.
 */
public class KeyboardExtensionUtils {

    private static final String PANEL_NAME_FONTS = "fonts";
    private static final String PANEL_NAME_THEME = "theme";
    private static final String PANEL_NAME_SETTINGS = "settings";

    public static void loadPanels() {
        final String[] panelNames = HSApplication.getContext().getResources().getStringArray(com.ihs.inputmethod.R.array.addtional_panel_names);
        for (String name : panelNames) {
            HSInputMethod.addPanel(createPanel(name));
        }
    }


    private static HSInputMethodPanel createPanel(String name) {
        if (name.equals(PANEL_NAME_FONTS))
            return new HSFontSelectPanel();
        else if (name.equals(PANEL_NAME_THEME))
            return new HSThemeSelectPanel();
         else if (name.equals(PANEL_NAME_SETTINGS))
            return new HSSettingsPanel();
        return null;
    }
}
