/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.demo;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;


/**
 * Displays a list of errors in the editor.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ErrorTable extends JTable {

	private RSyntaxTextArea textArea;


	public ErrorTable(ErrorTableModel model, RSyntaxTextArea textArea) {

		super(model);
		this.textArea = textArea;

		Dimension size = getPreferredScrollableViewportSize();
		size.height = 100;
		setPreferredScrollableViewportSize(size);

		Listener listener = new Listener();
		addMouseListener(listener);

		possiblyFixGridColor();
		fixTableModel();

	}


	private void fixTableModel() {

		JTableHeader old = getTableHeader();
		setTableHeader(new JTableHeader(old.getColumnModel()));

		IconTableCellRenderer itcr = new IconTableCellRenderer();
		ParserNoticeCellRenderer pncr = new ParserNoticeCellRenderer();

		TableColumnModel tcm = getColumnModel();
		tcm.getColumn(0).setPreferredWidth(32);
		tcm.getColumn(0).setWidth(32);
		tcm.getColumn(0).setCellRenderer(itcr);
		tcm.getColumn(1).setPreferredWidth(100);
		tcm.getColumn(1).setWidth(200);
		tcm.getColumn(2).setPreferredWidth(800);
		tcm.getColumn(2).setWidth(800);
		tcm.getColumn(2).setCellRenderer(pncr);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

	}


	private ParserNotice getNoticeForRow(int row) {
		return (ParserNotice)getModel().getValueAt(row, 2);
	}


	/**
	 * Overridden to ensure the table completely fills the JViewport it
	 * is sitting in.  Note in Java 6 this could be taken care of by the
	 * method JTable#setFillsViewportHeight(boolean).
	 * 1.6: Remove this and replace it with the method call.
	 */
	@Override
	public boolean getScrollableTracksViewportHeight() {
		Component parent = getParent();
		return parent instanceof JViewport ?
			parent.getHeight()>getPreferredSize().height : false;
	}


	/**
	 * Hack to look a little better in Windows LAF with standard color scheme.
	 */
	private void possiblyFixGridColor() {
		String laf = UIManager.getLookAndFeel().getClass().getName();
		if (laf.endsWith("WindowsLookAndFeel")) {
			if (Color.white.equals(getBackground())) {
				Color gridColor = getGridColor();
				if (gridColor!=null && gridColor.getRGB()<=0x808080) {
					setGridColor(new Color(0xe3e3e3));
				}
			}
		}
	}


	@Override
	public void scrollRectToVisible(Rectangle r) {
		r.x = 0; r.width = 0;
		super.scrollRectToVisible(r);
	}


	/**
	 * The model for data in this table.
	 */
	public static class ErrorTableModel extends DefaultTableModel {

		public ErrorTableModel(String[] columnNames, int rowCount) {
			super(columnNames, rowCount);
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return false;
		}

	}


	/**
	 * Listens for events in this table.
	 */
	private class Listener extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount()==2) {
				Point p = e.getPoint();
				int row = rowAtPoint(p);
				if (row==-1) {
					return;
				}
				ParserNotice notice = getNoticeForRow(row);
				int offs = notice.getOffset();
				int len = notice.getLength();
				if (offs==-1) { // "Unexpected end of input"
					offs = textArea.getDocument().getLength();
					len = 0;
				}
				textArea.setCaretPosition(offs);
				textArea.moveCaretPosition(offs+len);
				textArea.requestFocusInWindow();
			}
		}

	}


	private static class ParserNoticeCellRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean selected, boolean focus, int row, int col) {
			super.getTableCellRendererComponent(table, value, selected, focus,
					row, col);
			setText(((ParserNotice)value).getMessage());
			return this;
		}

	}


}