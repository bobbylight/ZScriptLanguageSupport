/*
 * 02/17/2013
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript;

import java.io.BufferedReader;


/**
 * Called to display read-only documentation for something the user
 * Ctrl+clicked on in the editor.  Applications can pass one of these to the
 * <code>ZScriptLanguageSupport</code> to open and display the resource
 * appropriately.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public interface DocDisplayer {


	/**
	 * Called when documentation should be displayed.
	 *
	 * @param title The title of the documentation.
	 * @param r The documentation itself.
	 * @param toFocus Text to scan for and jump to after the documentation is
	 *        loaded.
	 */
	// TODO: "toFocus" should really be a callback so we can have sophisticated
	// logic to identify what to jump to.
	public void display(String title, BufferedReader r, String toFocus);


}