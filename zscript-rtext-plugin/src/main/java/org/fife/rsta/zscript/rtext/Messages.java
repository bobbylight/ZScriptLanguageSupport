/*
 * 08/19/2012
 *
 * Messages.java - Resource bundle wrapper for the plugin.
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.rtext;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * Contains localized text for this plugin.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class Messages {

	private static final String MSG = "rtext.ZScriptPlugin";

	private static final ResourceBundle msg = ResourceBundle.getBundle(MSG);


	public static ResourceBundle getBundle() {
		return msg;
	}


	public static int getMnemonic(String keyRoot) {
		try {
			return getString(keyRoot + ".Mnemonic").charAt(0);
		} catch (MissingResourceException mre) {
			// TODO: Replace with containsString() or whatever when we drop
			// 1.4 support.
			return -1;
		}
	}


	public static String getString(String key) {
		return msg.getString(key);
	}


	public static String getString(String key, Object arg) {
		String text = getString(key);
		return MessageFormat.format(text, arg);
	}


}
