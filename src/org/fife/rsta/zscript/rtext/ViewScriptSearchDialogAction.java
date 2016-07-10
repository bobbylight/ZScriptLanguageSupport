/*
 * 11/03/2012
 *
 * ViewScriptSearchDialogAction - Displays the dialog for searching scripts.
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.rtext;

import java.awt.event.ActionEvent;

import org.fife.rtext.RText;
import org.fife.ui.app.StandardAction;


/**
 * Displays a dialog for searching for scripts on PureZC.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ViewScriptSearchDialogAction extends StandardAction {

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
	public ViewScriptSearchDialogAction(RText owner, Plugin plugin) {
		super(owner, Messages.getBundle(), "Action.ViewScriptSearchDialog");
		this.plugin = plugin;
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		plugin.showScriptSearchDialog();
	}


}