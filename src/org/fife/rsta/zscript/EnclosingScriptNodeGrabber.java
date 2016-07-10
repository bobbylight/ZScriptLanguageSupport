/*
 * 01/30/2016
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript;

import org.fife.rsta.zscript.ast.CodeBlock;
import org.fife.rsta.zscript.ast.DoWhileNode;
import org.fife.rsta.zscript.ast.ElseNode;
import org.fife.rsta.zscript.ast.ForNode;
import org.fife.rsta.zscript.ast.FunctionDecNode;
import org.fife.rsta.zscript.ast.IfNode;
import org.fife.rsta.zscript.ast.ImportNode;
import org.fife.rsta.zscript.ast.RootNode;
import org.fife.rsta.zscript.ast.ScriptNode;
import org.fife.rsta.zscript.ast.VariableDecNode;
import org.fife.rsta.zscript.ast.WhileNode;
import org.fife.rsta.zscript.ast.ZScriptAstVisitor;


/**
 * Returns the deepest script node enclosing a given offset.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class EnclosingScriptNodeGrabber implements ZScriptAstVisitor {

	private ScriptNode scriptNode;
	private int offs;
	
	public EnclosingScriptNodeGrabber(int offs) {
		this.offs = offs;
	}


	/**
	 * Returns the deepest enclosing script node.
	 *
	 * @return The deepest enclosing script node.  This will be
	 *         <code>null</code> if the offset is not in a script node.
	 */
	public ScriptNode getEnclosingScriptNode() {
		return scriptNode;
	}


	@Override
	public void postVisit(CodeBlock block) {}


	@Override
	public void postVisit(DoWhileNode doWhileNode) {}


	@Override
	public void postVisit(ElseNode elseNode) {}


	@Override
	public void postVisit(ForNode forNode) {}


	@Override
	public void postVisit(FunctionDecNode functionDec) {}


	@Override
	public void postVisit(IfNode ifNode) {}


	@Override
	public void postVisit(ImportNode importNode) {}


	@Override
	public void postVisit(RootNode root) {
	}


	@Override
	public void postVisit(ScriptNode script) {
	}


	@Override
	public void postVisit(VariableDecNode varDec) {}


	@Override
	public void postVisit(WhileNode whileNode) {}


	@Override
	public boolean visit(CodeBlock block) {
		return false;
	}


	@Override
	public boolean visit(DoWhileNode doWhileNode) {
		return false;
	}


	@Override
	public boolean visit(ElseNode elseNode) {
		return false;
	}


	@Override
	public boolean visit(ForNode forNode) {
		return false;
	}


	@Override
	public boolean visit(IfNode ifNode) {
		return false;
	}


	@Override
	public boolean visit(FunctionDecNode functionDec) {
		return false;
	}


	@Override
	public boolean visit(ImportNode importNode) {
		return false;
	}


	@Override
	public boolean visit(RootNode root) {
		return true;
	}


	@Override
	public boolean visit(ScriptNode script) {
		if (script.bodyContainsOffset(offs)) {
			if (scriptNode == null || script.getBodyStartOffset() >
					scriptNode.getBodyStartOffset()) {
				scriptNode = script;
			}
		}
		return true;
	}


	@Override
	public boolean visit(VariableDecNode varDec) {
		return false;
	}


	@Override
	public boolean visit(WhileNode whileNode) {
		return false;
	}


}