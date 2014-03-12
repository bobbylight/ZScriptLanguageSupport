/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.tree;

import javax.swing.Icon;

import org.fife.rsta.ac.SourceTreeNode;
import org.fife.rsta.zscript.IconFactory;
import org.fife.rsta.zscript.ast.AbstractNode;


/**
 * Base class for nodes in the Java outline tree.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ZScriptTreeNode extends SourceTreeNode {

	private AbstractNode node;

	/**
	 * Only used for tree nodes not representing AST nodes.
	 */
	private Icon icon;

	protected static final int PRIORITY_METHOD = 0;
	protected static final int PRIORITY_VAR = 1;
	protected static final int PRIORITY_VAR_CONST = 1;


	protected ZScriptTreeNode(AbstractNode node) {
		this(node, false);
	}


	protected ZScriptTreeNode(AbstractNode node, boolean sorted) {
		super(node, sorted);
		this.node = node;
	}


	public ZScriptTreeNode(String text, String iconName, boolean sorted) {
		super(text, sorted);
		if (iconName!=null) {
			icon = IconFactory.get().getIcon(iconName);
		}
	}


	/**
	 * Overridden to compare tree text without HTML.
	 */
	@Override
	public int compareTo(SourceTreeNode obj) {
		int res = -1;
		if (obj instanceof ZScriptTreeNode) {
			ZScriptTreeNode ztn2 = (ZScriptTreeNode)obj;
			res = getSortPriority() - ztn2.getSortPriority();
			if (res==0 && ((SourceTreeNode)getParent()).isSorted()) {
				res = getText(false).compareToIgnoreCase(ztn2.getText(false));
			}
		}
		return res;
	}


	public AbstractNode getNode() {
		return node;
	}


	public Icon getIcon() {
		return icon!=null ? icon : IconFactory.get().getIcon(node.getIcon());
	}


	public String getText(boolean selected) {
		Object obj = getUserObject();
		if (obj instanceof AbstractNode) { // Always true?
			AbstractNode node = (AbstractNode)obj;
			return node.toString(!selected);
		}
		return obj!=null ? obj.toString() : null;
	}


	/**
	 * Overridden to return the same thing as <tt>getText(false)</tt>, so
	 * we look nice with <tt>ToolTipTree</tt>s.
	 *
	 * @return A string representation of this tree node.
	 */
	@Override
	public String toString() {
		return getText(false);
	}


}