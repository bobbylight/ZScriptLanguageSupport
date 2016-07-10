/*
 * 08/19/2012
 *
 * ZScriptDockableWindow.java - Shows parser notices for ZScript editors.
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.rtext;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JTable;

import org.fife.rsta.zscript.IconFactory;
import org.fife.rsta.zscript.ZScriptParser;
import org.fife.rtext.AbstractMainView;
import org.fife.rtext.AbstractParserNoticeWindow;
import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;
import org.fife.ui.RScrollPane;
import org.fife.ui.dockablewindows.DockableWindow;
import org.fife.ui.dockablewindows.DockableWindowScrollPane;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;


/**
 * Dockable window that displays the error notices for all open editors that
 * are editing ZScript.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ZScriptDockableWindow extends AbstractParserNoticeWindow
		implements PropertyChangeListener {

	private JTable table;
	private ZScriptErrorTableModel model;
	private boolean installed;
	private Map<ParserNotice.Level, Icon> noticeIcons;


	public ZScriptDockableWindow(RText app, Plugin plugin) {

		super(app);

		model = new ZScriptErrorTableModel(Messages.getString(
				"Plugin.NoticeDescription"));
		table = createTable(model);
		RScrollPane sp = new DockableWindowScrollPane(table);
		setLayout(new BorderLayout());
		add(sp);

		setDockableWindowName(Messages.getString("DockableWindow.Title"));
		setIcon(plugin.getPluginIcon());
		setPosition(DockableWindow.BOTTOM);
		setActive(true);

		applyComponentOrientation(app.getComponentOrientation());

		noticeIcons = new HashMap<ParserNotice.Level, Icon>();
		noticeIcons.put(ParserNotice.Level.INFO, getIcon());
		noticeIcons.put(ParserNotice.Level.WARNING, IconFactory.get().getIcon(IconFactory.WARNING_ICON));
		noticeIcons.put(ParserNotice.Level.ERROR, IconFactory.get().getIcon(IconFactory.ERROR_ICON));

	}


	/**
	 * Registers listeners on the application view and any already-opened
	 * editors.
	 *
	 * @see #uninstallListeners()
	 */
	private void installListeners() {
		if (!installed) {
			RText rtext = getRText();
			AbstractMainView mainView = rtext.getMainView();
			mainView.addPropertyChangeListener(AbstractMainView.TEXT_AREA_ADDED_PROPERTY, this);
			mainView.addPropertyChangeListener(AbstractMainView.TEXT_AREA_REMOVED_PROPERTY, this);
			for (int i=0; i<mainView.getNumDocuments(); i++) {
				RTextEditorPane textArea = mainView.getRTextEditorPaneAt(i);
				List<ParserNotice> notices = textArea.getParserNotices();
				model.update(textArea, notices);
				textArea.addPropertyChangeListener(
							RSyntaxTextArea.PARSER_NOTICES_PROPERTY, this);
			}
			installed = true;
		}
	}


	/**
	 * Callback for text areas being added to or removed from RText.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {

		String prop = e.getPropertyName();

		if (RSyntaxTextArea.PARSER_NOTICES_PROPERTY.equals(prop)) {
			RTextEditorPane source = (RTextEditorPane)e.getSource();
			List<ParserNotice> notices = source.getParserNotices();//e.getNewValue();
			model.update(source, notices);
		}

		if (AbstractMainView.TEXT_AREA_ADDED_PROPERTY.equals(prop)) {
			RTextEditorPane textArea = (RTextEditorPane)e.getNewValue();
			textArea.addPropertyChangeListener(
							RSyntaxTextArea.PARSER_NOTICES_PROPERTY, this);
		}

		else if (AbstractMainView.TEXT_AREA_REMOVED_PROPERTY.equals(prop)) {
			RTextEditorPane textArea = (RTextEditorPane)e.getNewValue();
			textArea.removePropertyChangeListener(
							RSyntaxTextArea.PARSER_NOTICES_PROPERTY, this);
		}

	}


	/**
	 * Overridden to remove our listeners when this window isn't active
	 * (visible).
	 *
	 * @param active Whether this window should be active.
	 */
	@Override
	public void setActive(boolean active) {
		if (active!=isActive()) {
			super.setActive(active);
			if (active && !installed) {
				installListeners();
			}
			else if (!active && installed) {
				uninstallListeners();
			}
		}
	}


	/**
	 * Removes listeners from the application view and any open editors.
	 *
	 * @see #installListeners()
	 */
	private void uninstallListeners() {
		if (installed) {
			RText rtext = getRText();
			AbstractMainView mainView = rtext.getMainView();
			mainView.removePropertyChangeListener(AbstractMainView.TEXT_AREA_ADDED_PROPERTY, this);
			mainView.removePropertyChangeListener(AbstractMainView.TEXT_AREA_REMOVED_PROPERTY, this);
			for (int i=0; i<mainView.getNumDocuments(); i++) {
				RTextEditorPane textArea = mainView.getRTextEditorPaneAt(i);
				textArea.removePropertyChangeListener(
							RSyntaxTextArea.PARSER_NOTICES_PROPERTY, this);
			}
			model.setRowCount(0);
			installed = false;
		}
	}


	/**
	 * Table model for the error table.
	 */
	private class ZScriptErrorTableModel extends ParserNoticeTableModel {

		public ZScriptErrorTableModel(String lastColHeader) {
			super(lastColHeader);
		}

		@Override
		protected void addNoticesImpl(RTextEditorPane textArea,
				List<ParserNotice> notices) {
			for (ParserNotice notice : notices) {
				// Unfortunately, ZScriptParsers aren't shared.
				if (notice.getParser() instanceof ZScriptParser) {
					Object[] data = {	getIconFor(notice), textArea,
							Integer.valueOf(notice.getLine()+1),
							notice.getMessage() };
					addRow(data);
				}
			}
		}

		private Icon getIconFor(ParserNotice notice) {
			return noticeIcons.get(notice.getLevel());
		}

	}


}