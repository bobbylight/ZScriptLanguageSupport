/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript;

import javax.swing.Icon;

import org.fife.rsta.zscript.IconFactory.IconData;
import org.fife.rsta.zscript.ast.VariableDecNode;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.VariableCompletion;


public class ZScriptVariableCompletion extends VariableCompletion {

	private IconData iconData;


	public ZScriptVariableCompletion(CompletionProvider provider, VariableDecNode node) {
		this(provider, node.getName(), node.getType(), node.isConstant());
	}


	public ZScriptVariableCompletion(CompletionProvider provider,
			String name, String type, boolean constant) {
		super(provider, name, type);
		iconData = new IconData(IconFactory.FIELD_PUBLIC_ICON, constant);
	}


	@Override
	public String getDefinitionString() {
		String def = super.getDefinitionString();
		if (isConstant()) {
			def = "const " + def;
		}
		return def;
	}


	@Override
	public Icon getIcon() {
		return IconFactory.get().getIcon(iconData);
	}


	@Override
	public String getToolTipText() {
		String url = IconFactory.get().getIconUrl(iconData);
		return "<html><img src='" + url + "'/>" + super.getToolTipText();
	}


	protected boolean isConstant() {
		return iconData.isConstant();
	}


}