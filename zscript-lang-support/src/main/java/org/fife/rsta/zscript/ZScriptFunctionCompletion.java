/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;

import org.fife.rsta.zscript.IconFactory.IconData;
import org.fife.rsta.zscript.ast.FunctionDecNode;
import org.fife.rsta.zscript.ast.VariableDecNode;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;


public class ZScriptFunctionCompletion extends FunctionCompletion {

	private IconData iconData;


	public ZScriptFunctionCompletion(CompletionProvider provider,
			FunctionDecNode node) {

		this(provider, node.getName(), node.getType());

		List<VariableDecNode> args = node.getArguments();
		if (!args.isEmpty()) {
			List<Parameter> params = new ArrayList<>(args.size());
			for (VariableDecNode arg : args) {
				params.add(new Parameter(arg.getType(), arg.getName()));
			}
			setParams(params);
		}

	}


	public ZScriptFunctionCompletion(CompletionProvider provider,
			String name, String type) {
		super(provider, name, type);
		iconData = new IconData(IconFactory.METHOD_PUBLIC_ICON, false);
	}


	@Override
	public Icon getIcon() {
		return IconFactory.get().getIcon(iconData);
	}


}
