/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.tree;

import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;


/**
 * Renderer for the AST tree in the UI.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class AstTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 1L;


	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
							boolean sel, boolean expanded, boolean leaf,
							int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
											row, hasFocus);
		if (value instanceof ZScriptTreeNode) { // Should always be true
			ZScriptTreeNode node = (ZScriptTreeNode)value;
			setText(node.getText(sel));
			setIcon(node.getIcon());
		}
		return this;
	}


}