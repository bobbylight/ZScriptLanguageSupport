/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.ast;

import java.util.List;

import org.fife.rsta.zscript.EnclosingScriptNodeGrabber;


/**
 * Abstract syntax tree for ZScript.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ZScriptAst {

	private RootNode root;


	public BodiedNode getDeepestBodiedNodeContaining(int offs) {
		List<MemberNode> members = root.getAllMembers();
		for (MemberNode member : members) {
			if (member instanceof BodiedNode) {
				BodiedNode bn = (BodiedNode)member;
				if (bn.bodyContainsOffset(offs)) {
					return bn.getDeepestBodiedNodeContaining(offs);
				}
			}
		}
		return null;
	}


	public RootNode getRootNode() {
		return root;
	}


	/**
	 * Returns the deepest enclosing script node for a given offset.
	 *
	 * @param offs The offset.
	 * @return The deepest enclosing script node.  This will be
	 *         <code>null</code> if the offset is not in a script node.
	 */
	public ScriptNode getScriptNodeContaining(int offs) {
		EnclosingScriptNodeGrabber scriptNodeGrabber =
				new EnclosingScriptNodeGrabber(offs);
		root.accept(scriptNodeGrabber);
		return scriptNodeGrabber.getEnclosingScriptNode();
	}


	public void setRootNode(RootNode root) {
		this.root = root;
	}


}