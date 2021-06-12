/*
 * 08/19/2012
 *
 * Plugin.java - Entry point for an RText plugin for ZScript language support.
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.rtext;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.zscript.ZScriptLanguageSupport;
import org.fife.rtext.AbstractMainView;
import org.fife.rtext.FileTypeIconManager;
import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;
import org.fife.rtext.RTextMenuBar;
import org.fife.rtext.RTextUtilities;
import org.fife.rtext.SyntaxFilters;
import org.fife.ui.app.AbstractPluggableGUIApplication;
import org.fife.ui.app.GUIPlugin;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.StandardAction;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.CurlyFoldParser;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;


/**
 * Plugin for RText that adds language support for ZScript, as well as other
 * ZScript-specific features.  Depends on the RSTALanguageSupport plugin.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class Plugin extends GUIPlugin {

	/**
	 * Constant representing ZScript in RSyntaxTextArea.
	 */
	public static final String SYNTAX_STYLE_ZSCRIPT = "text/zscript";

	private static final String PLUGIN_VERSION = "3.0.3";

	private static final String ICON = "/org/fife/rsta/zscript/img/link.png";
	private static final String VIEW_ZSCRIPT_DOCKED_WIDNOW_ACTION =
			"viewZScriptDockedWindow";
	private static final String VIEW_ZSCRIPT_SEARCH_DIALOG_ACTION =
			"viewScriptSearchDialog";

	private RText rtext;
	private String version;
	private Icon icon;
	private ZScriptDockableWindow window;
	private int initialDockableWindowPosition;
	private boolean initialDockableWindowVisible;
	private ScriptSearchDialog scriptSearchDialog;


	public Plugin(AbstractPluggableGUIApplication<?> app) {

		this.rtext = (RText)app;
		ZScriptPrefs prefs = loadPrefs();

		StandardAction a = new ViewDockedWindowAction(rtext, this);
		a.setAccelerator(prefs.windowVisibilityAccelerator);
		rtext.addAction(VIEW_ZSCRIPT_DOCKED_WIDNOW_ACTION, a);
		initialDockableWindowPosition = prefs.windowPosition;
		initialDockableWindowVisible = prefs.windowVisible;

		a = new ViewScriptSearchDialogAction(rtext, this);
		a.setAccelerator(prefs.scriptSearchDialogAccelerator);
		rtext.addAction(VIEW_ZSCRIPT_SEARCH_DIALOG_ACTION, a);

	}


	/**
	 * Adds a new submenu to the View menu for ZScript-specific stuff.
	 *
	 * @param mb The RText menu bar.
	 */
	private void addZScriptSubMenu(RTextMenuBar mb) {

		JMenu viewMenu = mb.getMenuByName(RTextMenuBar.MENU_VIEW);
		int index = viewMenu.getMenuComponentCount() - 2;

		JMenu zscriptMenu = new JMenu("ZScript"); // No localization worries
		zscriptMenu.setIcon(getPluginIcon());
		viewMenu.add(zscriptMenu, index);
		viewMenu.add(new JPopupMenu.Separator(), index);

		Action a = rtext.getAction(VIEW_ZSCRIPT_SEARCH_DIALOG_ACTION);
		zscriptMenu.add(new JMenuItem(a));

	}


	/**
	 * Returns the dockable window's position, even if it has not yet been
	 * created.
	 *
	 * @return The dockable window's position.
	 */
	private int getDockableWindowPosition() {
		return window==null ? initialDockableWindowPosition : window.getPosition();
	}


	/**
	 * Returns the options dialog panel for this plugin.
	 *
	 * @return The options dialog panel.
	 */
	@Override
	public PluginOptionsDialogPanel<Plugin> getOptionsDialogPanel() {
		return new ZScriptOptionPanel(this);
	}


	/**
	 * Returns the ID of the option panel that should be the parent of this
	 * one.
	 *
	 * @return The parent option panel ID.
	 */
	@Override
	public String getOptionsDialogPanelParentPanelID() {
		return /*LanguageOptionPanel.OPTION_PANEL_ID*/"LanguageSupportOptionPanel";
	}


	@Override
	public String getPluginAuthor() {
		return "Robert Futrell";
	}


	@Override
	public Icon getPluginIcon(boolean darkLookAndFeel) {
		if (icon==null) {
			URL url = getClass().getResource(ICON);
			icon = new ImageIcon(url);
		}
		return icon;
	}


	@Override
	public String getPluginName() {
		return Messages.getString("Plugin.Name");
	}


	/**
	 * Returns the version number to display in the UI.
	 *
	 * @return The version number.
	 */
	@Override
	public String getPluginVersion() {
		if (version==null) {
			version = PLUGIN_VERSION;
			String buildDate = ZScriptLanguageSupport.getBuildDate();
			if (buildDate!=null) {
				version += "." + buildDate;
			}
		}
		return version;
	}


	/**
	 * Returns the file preferences for this plugin are saved in.
	 *
	 * @return The file.
	 */
	private File getPrefsFile() {
		return new File(RTextUtilities.getPreferencesDirectory(),
						"zscript_support.properties");
	}


	@Override
	public void install(AbstractPluggableGUIApplication app) {

		RText rtext = (RText)app;
		RTextMenuBar mb = (RTextMenuBar)app.getJMenuBar();
		registerZScript();

		// Make RText open ZScript files properly and enable code folding.
		SyntaxFilters filters = rtext.getMainView().getSyntaxFilters();
		filters.addFileFilter(SYNTAX_STYLE_ZSCRIPT, "*.z");
		filters.addFileFilter(SYNTAX_STYLE_ZSCRIPT, "*.zh");
		rtext.getMainView().setCodeFoldingEnabledFor(SYNTAX_STYLE_ZSCRIPT, true);
		FileTypeIconManager.get().setIconFor(SYNTAX_STYLE_ZSCRIPT, getPluginIcon());
		setDockableWindowVisible(initialDockableWindowVisible);

		String sbpPrefix = "sbp.customHandler.";/*SourceBrowserPlugin.CUSTOM_HANDLER_PREFIX*/
		System.setProperty(
				sbpPrefix + SYNTAX_STYLE_ZSCRIPT,
				"org.fife.rsta.zscript.rtext.ZScriptSourceBrowserTreeConstructor");

		// Add an item to the "View" menu to toggle console visibility
		JMenu menu = mb.getMenuByName(RTextMenuBar.MENU_DOCKED_WINDOWS);
		Action a = rtext.getAction(VIEW_ZSCRIPT_DOCKED_WIDNOW_ACTION);
		final JCheckBoxMenuItem item = new JCheckBoxMenuItem(a);
		item.setToolTipText(null);
		item.applyComponentOrientation(app.getComponentOrientation());
		menu.add(item);
		JPopupMenu popup = menu.getPopupMenu();
		popup.pack();
		// Only needed for pre-1.6 support
		popup.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				item.setSelected(isDockableWindowVisible());
			}
		});

		addZScriptSubMenu(mb);

		// Since plugins are loaded on a delay, we must manualy check for
		// ZScript files already opened.
		AbstractMainView view = rtext.getMainView();
		for (int i=0; i<view.getNumDocuments(); i++) {
			RTextEditorPane textArea = view.getRTextEditorPaneAt(i);
			String fileName = textArea.getFileName();
			String style = filters.getSyntaxStyleForFile(fileName, false);
			if (SYNTAX_STYLE_ZSCRIPT.equals(style)) {
				textArea.setSyntaxEditingStyle(style);
			}
		}

	}


	/**
	 * Returns whether the dockable window is visible.
	 *
	 * @return Whether the dockable window is visible.
	 * @see #setDockableWindowVisible(boolean)
	 */
	boolean isDockableWindowVisible() {
		return window!=null && window.isActive();
	}


	/**
	 * Loads saved preferences.  If this is the first time through, default
	 * values will be returned.
	 *
	 * @return The preferences.
	 */
	private ZScriptPrefs loadPrefs() {
		ZScriptPrefs prefs = new ZScriptPrefs();
		File prefsFile = getPrefsFile();
		if (prefsFile.isFile()) {
			try {
				prefs.load(prefsFile);
			} catch (IOException ioe) {
				rtext.displayException(ioe);
				// (Some) defaults will be used
			}
		}
		return prefs;
	}


	/**
	 * Overridden to update our cached dialog's appearance also.
	 */
	@Override
	protected void lookAndFeelChanged(LookAndFeel laf) {
		super.lookAndFeelChanged(laf);
		if (scriptSearchDialog!=null) {
			SwingUtilities.updateComponentTreeUI(scriptSearchDialog);
		}
	}


	/**
	 * Set up general stuff for our new language.
	 */
	private void registerZScript() {
		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		lsf.addLanguageSupport(SYNTAX_STYLE_ZSCRIPT,
				"org.fife.rsta.zscript.ZScriptLanguageSupport");
		TokenMakerFactory tmf = TokenMakerFactory.getDefaultInstance();
		((AbstractTokenMakerFactory)tmf).putMapping(SYNTAX_STYLE_ZSCRIPT,
				"org.fife.rsta.zscript.ZScriptTokenMaker",
				getClass().getClassLoader());
		FoldParserManager fpm = FoldParserManager.get();
		fpm.addFoldParserMapping(SYNTAX_STYLE_ZSCRIPT,
				new CurlyFoldParser(false, false));
	}


	@Override
	public void savePreferences() {

		// General GUI plugin options
		ZScriptPrefs prefs = new ZScriptPrefs();
		prefs.windowPosition = getDockableWindowPosition();
		StandardAction a = (StandardAction)rtext.getAction(VIEW_ZSCRIPT_DOCKED_WIDNOW_ACTION);
		prefs.windowVisibilityAccelerator = a.getAccelerator();
		prefs.windowVisible = isDockableWindowVisible();

		// Plugin-specific stuff
		a = (StandardAction)rtext.getAction(VIEW_ZSCRIPT_SEARCH_DIALOG_ACTION);
		prefs.scriptSearchDialogAccelerator = a.getAccelerator();

		File prefsFile = getPrefsFile();
		try {
			prefs.save(prefsFile);
		} catch (IOException ioe) {
			rtext.displayException(ioe);
		}

	}


	/**
	 * Toggles the visibility of the dockable window.
	 *
	 * @param visible Whether the dockable window should be visible.
	 * @see #isDockableWindowVisible()
	 */
	void setDockableWindowVisible(boolean visible) {
		if (visible!=isDockableWindowVisible()) {
			if (visible && window==null) {
				window = new ZScriptDockableWindow(rtext, this);
				window.setPosition(initialDockableWindowPosition);
				// Must manually add since we are lazily created
				rtext.addDockableWindow(window);
				putDockableWindow("zscriptDockableWindow", window);
			}
			window.setActive(visible);
		}
	}


	/**
	 * Displays the dialog allowing the searching of PureZC scripts.
	 */
	void showScriptSearchDialog() {
		if (scriptSearchDialog==null) {
			scriptSearchDialog = new ScriptSearchDialog(rtext);
		}
		if (scriptSearchDialog.isShowing()) {
			scriptSearchDialog.toFront();
		}
		else {
			scriptSearchDialog.setVisible(true);
		}
	}


	@Override
	public boolean uninstall() {
		// TODO Auto-generated method stub
		return false;
	}


}
