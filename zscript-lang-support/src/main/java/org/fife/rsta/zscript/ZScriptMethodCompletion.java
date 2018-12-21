/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript;

import javax.swing.text.JTextComponent;

import org.fife.ui.autocomplete.CompletionProvider;


/**
 * Completion for a method of a variable.
 * 
 * @author Robert Futrell
 * @version 1.0
 * @see ZScriptPropertyCompletion
 */
public class ZScriptMethodCompletion extends ZScriptFunctionCompletion {


	public ZScriptMethodCompletion(CompletionProvider p, String name,
								String returnType) {
		super(p, name, returnType);
	}


	@Override
	public String getAlreadyEntered(JTextComponent comp) {
		String temp = getProvider().getAlreadyEnteredText(comp);
		int lastArrow = temp.lastIndexOf("->");
		if (lastArrow>-1) {
			temp = temp.substring(lastArrow+2);
		}
		return temp;
	}


}