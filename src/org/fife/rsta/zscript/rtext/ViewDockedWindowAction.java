/*
 * 08/19/2012
 *
 * ViewDockedWindowAction.java - Toggles visibility of the dockable window.
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.rtext;

import java.awt.event.ActionEvent;

import org.fife.rtext.RText;
import org.fife.ui.app.StandardAction;


/**
 * Toggles visibility of the ZScript dockable window.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ViewDockedWindowAction extends StandardAction {

	/**
	 * The parent plugin.
	 */
	private Plugin plugin;


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param plugin The plugin.
	 */
	public ViewDockedWindowAction(RText owner, Plugin plugin) {
		super(owner, Messages.getBundle(), "Action.ViewDockedWindow");
		this.plugin = plugin;
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		plugin.setDockableWindowVisible(!plugin.isDockableWindowVisible());
	}


}