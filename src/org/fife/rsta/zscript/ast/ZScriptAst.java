/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.ast;

import java.util.Iterator;
import java.util.List;


/**
 * Abstract syntax tree for ZScript.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ZScriptAst {

	private RootNode root;


	public BodiedNode getDeepestBodiedNodeContaining(int offs) {
		List members = root.getAllMembers();
		for (Iterator i=members.iterator(); i.hasNext(); ) {
			MemberNode member = (MemberNode)i.next();
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


	public void setRootNode(RootNode root) {
		this.root = root;
	}


}