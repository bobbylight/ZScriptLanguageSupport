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
 * Completion for a property of a variable.
 *
 * @author Robert Futrell
 * @version 1.0
 * @see ZScriptMethodCompletion
 */
public class ZScriptPropertyCompletion extends ZScriptVariableCompletion {

	/**
	 * The type of the "parent" variable of this property.
	 */
	private String parentType;


	public ZScriptPropertyCompletion(CompletionProvider p, String parentType,
					String name, String returnType) {
		super(p, name, returnType, false);
		this.parentType = parentType;
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


	@Override
	public String getDefinitionString() {

		StringBuffer sb = new StringBuffer();
		sb.append(getType()).append(' ');

		// Add the item being described's name.
		sb.append(parentType).append("->").append(getName());

		String def = sb.toString();
		if (isConstant()) {
			def = "const " + def;
		}
		return def;
	}


	@Override
	public String getToolTipText() {
		String text = getSummary();
		if (text==null) {
			text = getDefinitionString();
		}
		return text;
	}


}