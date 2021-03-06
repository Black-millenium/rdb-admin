/*
 * LnFHelper.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2015, Thomas Kellerer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.lnf;

import workbench.gui.components.TabbedPaneUIFactory;
import workbench.log.LogMgr;
import workbench.resource.GuiSettings;
import workbench.resource.Settings;
import workbench.util.CollectionUtil;
import workbench.util.PlatformHelper;
import workbench.util.StringUtil;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import java.awt.*;
import java.util.Set;

/**
 * Initialize some gui elements during startup.
 *
 * @author Thomas Kellerer
 */
public class LnFHelper {
  public static final String MENU_FONT_KEY = "MenuItem.font";
  public static final String LABEL_FONT_KEY = "Label.font";
  public static final String TREE_FONT_KEY = "Tree.font";
  // Font properties that are automatically scaled by Java
  private final Set<String> noScale = CollectionUtil.treeSet(
      "Menu.font",
      "MenuBar.font",
      "MenuItem.font",
      "PopupMenu.font",
      "CheckBoxMenuItem.font");
  private final Set<String> fontProperties = CollectionUtil.treeSet(
      "Button.font",
      "CheckBox.font",
      "CheckBoxMenuItem.font",
      "ColorChooser.font",
      "ComboBox.font",
      "EditorPane.font",
      "FileChooser.font",
      LABEL_FONT_KEY,
      "List.font",
      "Menu.font",
      "MenuBar.font",
      MENU_FONT_KEY,
      "OptionPane.font",
      "Panel.font",
      "PasswordField.font",
      "PopupMenu.font",
      "ProgressBar.font",
      "RadioButton.font",
      "RadioButtonMenuItem.font",
      "ScrollPane.font",
      "Slider.font",
      "Spinner.font",
      "TabbedPane.font",
      "TextArea.font",
      "TextField.font",
      "TextPane.font",
      "TitledBorder.font",
      "ToggleButton.font",
      "ToolBar.font",
      "ToolTip.font",
      TREE_FONT_KEY,
      "ViewPort.font");
  private boolean isWindowsClassic;

  public static int getMenuFontHeight() {
    return getFontHeight(MENU_FONT_KEY);
  }

  public static int getLabelFontHeight() {
    return getFontHeight(LABEL_FONT_KEY);
  }

  private static int getFontHeight(String key) {
    UIDefaults def = UIManager.getDefaults();
    double factor = Toolkit.getDefaultToolkit().getScreenResolution() / 72.0;
    Font font = def.getFont(key);
    if (font == null) return 18;
    return (int) Math.ceil((double) font.getSize() * factor);
  }

  public static boolean isJGoodies() {
    String lnf = UIManager.getLookAndFeel().getClass().getName();
    return lnf.startsWith("com.jgoodies.looks.plastic");
  }

  public boolean isWindowsClassic() {
    return isWindowsClassic;
  }

  public void initUI() {
    initializeLookAndFeel();

    Settings settings = Settings.getInstance();
    UIDefaults def = UIManager.getDefaults();

    Font stdFont = settings.getStandardFont();
    if (stdFont != null) {
      for (String property : fontProperties) {
        def.put(property, stdFont);
      }
    } else if (isWindowsLookAndFeel()) {
      // The default Windows look and feel does not scale the fonts properly
      scaleDefaultFonts();
    }

    Font dataFont = settings.getDataFont();
    if (dataFont != null) {
      def.put("Table.font", dataFont);
      def.put("TableHeader.font", dataFont);
    }

    String cls = TabbedPaneUIFactory.getTabbedPaneUIClass();
    if (cls != null) def.put("TabbedPaneUI", cls);

    if (settings.getBoolProperty("workbench.gui.adjustgridcolor", true)) {
      Color c = settings.getColor("workbench.table.gridcolor", new Color(215, 215, 215));
      def.put("Table.gridColor", c);
    }

    def.put("Button.showMnemonics", Boolean.valueOf(GuiSettings.getShowMnemonics()));
  }

  private boolean isWindowsLookAndFeel() {
    String lnf = UIManager.getLookAndFeel().getClass().getName();
    return lnf.contains("plaf.windows");
  }

  private void scaleDefaultFonts() {
    FontScaler scaler = new FontScaler();
    scaler.logSettings();
    if (!Settings.getInstance().getScaleFonts()) return;

    LogMgr.logDebug("LnFHelper.scaleDefaultFonts()", "Scaling default fonts by: " + scaler.getScaleFactor());

    UIDefaults def = UIManager.getDefaults();
    for (String property : fontProperties) {
      if (!noScale.contains(property)) {
        Font base = def.getFont(property);
        if (base != null) {
          Font scaled = scaler.scaleFont(base);
          def.put(property, scaled);
        }
      }
    }
  }

  protected void initializeLookAndFeel() {
    String className = GuiSettings.getLookAndFeelClass();
    try {
      if (StringUtil.isEmptyString(className)) {
        className = UIManager.getSystemLookAndFeelClassName();
      }
      LnFManager mgr = new LnFManager();
      LnFDefinition def = mgr.findLookAndFeel(className);

      if (def == null) {
        LogMgr.logError("LnFHelper.initializeLookAndFeel()", "Specified Look & Feel " + className + " not available!", null);
        setSystemLnF();
      } else {
        // JGoodies Looks settings
        UIManager.put("jgoodies.useNarrowButtons", Boolean.FALSE);
        UIManager.put("FileChooser.useSystemIcons", Boolean.TRUE);

        // I hate the bold menu font in the Metal LnF
        UIManager.put("swing.boldMetal", Boolean.FALSE);

        // Remove Synthetica's own window decorations
        UIManager.put("Synthetica.window.decoration", Boolean.FALSE);

        // Remove the extra icons for read only text fields and
        // the "search bar" in the main menu for the Substance Look & Feel
        System.setProperty("substancelaf.noExtraElements", "");

        LnFLoader loader = new LnFLoader(def);
        LookAndFeel lnf = loader.getLookAndFeel();

        if (className.startsWith("com.jgoodies.looks.plastic")) {
          String theme = Settings.getInstance().getProperty("workbench.gui.lnf.jgoodies.theme", "Silver");
          setJGoodiesTheme(loader, theme);
        }

        UIManager.setLookAndFeel(lnf);
        PlatformHelper.installGtkPopupBugWorkaround();
      }
    } catch (Throwable e) {
      LogMgr.logError("LnFHelper.initializeLookAndFeel()", "Could not set look and feel to [" + className + "]. Look and feel will be ignored", e);
      setSystemLnF();
    }

    checkWindowsClassic(UIManager.getLookAndFeel().getClass().getName());
  }

  private void setJGoodiesTheme(LnFLoader loader, String themeName) {
    if (StringUtil.isBlank(themeName)) return;
    try {
      String className = "com.jgoodies.looks.plastic.theme." + themeName;
      LogMgr.logDebug("LnFHelper.setJGoodiesTheme()", "Trying to set theme: " + className);

      Class themeClass = loader.loadClass(className);
      Object themeInstance = themeClass.newInstance();

			/*
        for some reason using reflection does not work any longer with
			  newer JGoodies versions. But using MetalLookAndFeel.setCurrentTheme() seems
			  to work just as good
			Class lnf = loader.loadClass("com.jgoodies.looks.plastic.PlasticLookAndFeel");
			Class baseThemeClass = loader.loadClass("com.jgoodies.looks.plastic.PlasticTheme");
			Method setTheme = lnf.getDeclaredMethod("setPlasticTheme", baseThemeClass);
			setTheme.invoke(null, themeInstance);
			*/

      if (themeInstance instanceof MetalTheme) {
        // PlasticLookAndFeel.setPlasticTheme() simply calls MetalLookAndFeel.setCurrentTheme()
        MetalLookAndFeel.setCurrentTheme((MetalTheme) themeInstance);
      }
    } catch (Throwable th) {
      LogMgr.logError("LnFHelper.setJGoodiesTheme()", "Could not set theme", th);
    }
  }

  private void setSystemLnF() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception ex) {
      // should not ahppen
    }
  }

  private void checkWindowsClassic(String clsname) {
    try {
      if (clsname.contains("com.sun.java.swing.plaf.windows")) {
        String osVersion = System.getProperty("os.version", "1.0");
        Float version = Float.valueOf(osVersion);
        if (version <= 5.0) {
          isWindowsClassic = true;
        } else {
          isWindowsClassic = clsname.contains("WindowsClassicLookAndFeel");
          if (!isWindowsClassic) {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Boolean themeActive = (Boolean) toolkit.getDesktopProperty("win.xpstyle.themeActive");
            if (themeActive != null) {
              isWindowsClassic = !themeActive;
            } else {
              isWindowsClassic = true;
            }
          }
        }
      }
    } catch (Throwable e) {
      isWindowsClassic = false;
    }

  }

}
