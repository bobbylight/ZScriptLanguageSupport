/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.demo;

import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;


/**
 * A table cell renderer for icons.  Ripped off from RText.
 */
class IconTableCellRenderer extends DefaultTableCellRenderer {

	private static final Border b = BorderFactory.createEmptyBorder(0, 5, 0, 5);


	@Override
	public Component getTableCellRendererComponent(JTable table,
		Object value, boolean selected, boolean focus, int row, int col) {
		super.getTableCellRendererComponent(table, value, selected, focus,
				row, col);
		setText(null);
		setIcon((Icon)value);
		setBorder(b);
		return this;
	}


}