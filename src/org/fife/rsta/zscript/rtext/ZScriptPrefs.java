/*
 * 11/04/2012
 *
 * ZScriptPrefs - Preferences for the ZScript support plugin.
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.rtext;

import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

import org.fife.ui.app.Prefs;
import org.fife.ui.dockablewindows.DockableWindow;


/**
 * Preferences for this plugin.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ZScriptPrefs extends Prefs {

	/**
	 * Whether the GUI plugin window is active (visible).
	 */
	public boolean windowVisible;

	/**
	 * The location of the dockable console output window.
	 */
	public int windowPosition;

	/**
	 * Key stroke that toggles the console window's visibility.
	 */
	public KeyStroke windowVisibilityAccelerator;

	/**
	 * Shortcut for the PureZC script dialog.
	 */
	public KeyStroke scriptSearchDialogAccelerator;


	@Override
	public void setDefaults() {
		windowVisible = true;
		windowPosition = DockableWindow.BOTTOM;
		windowVisibilityAccelerator = null;
		int ctrlShift = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() |
				InputEvent.SHIFT_MASK;
		scriptSearchDialogAccelerator = KeyStroke.getKeyStroke(KeyEvent.VK_Z, ctrlShift);
	}


}