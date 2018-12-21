/*
 * 02/16/2013
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript;

import java.util.List;

import org.fife.rsta.zscript.ast.VariableDecNode;
import org.fife.rsta.zscript.ast.VariablesInScopeGrabber;
import org.fife.rsta.zscript.ast.ZScriptAst;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;


/**
 * Utility methods for ZScript code completion.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ZScriptUtils {


	/**
	 * Private constructor to prevent instantiation.
	 */
	private ZScriptUtils() {
	}


	public static VariableDecNode getVariableDeclaration(String varName,
                                                         RSyntaxTextArea textArea, ZScriptAst ast, int offs) {

		VariableDecNode varDec = null;

		VariablesInScopeGrabber grabber = new VariablesInScopeGrabber(offs);
		ast.getRootNode().accept(grabber);

		// TODO: Check for '(' token and check for matching function first?

		// Find matching variable declaration in "deepest" scope.
		List<VariableDecNode> varList = grabber.getVariableList();
		for (VariableDecNode vdn : varList) {
			if (varName.equals(vdn.getName())) {
				varDec = vdn;
				break;
			}
		}

		return varDec;

	}


}
