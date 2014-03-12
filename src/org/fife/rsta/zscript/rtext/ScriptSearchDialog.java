/*
 * 11/03/2012
 *
 * ScriptSearchDialog.java - Allows you to search for scripts on PureZC.
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.rtext;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.fife.rsta.ui.EscapableDialog;
import org.fife.rsta.ui.ResizableFrameContentPane;
import org.fife.rsta.ui.UIUtil;
import org.fife.rtext.RText;
import org.fife.ui.FileExplorerTableModel;
import org.fife.ui.GUIWorkerThread;
import org.fife.ui.Hyperlink;
import org.fife.ui.RScrollPane;
import org.fife.ui.SelectableLabel;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;


/**
 * A dialog that lets you search for scripts on PureZC.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ScriptSearchDialog extends EscapableDialog {

	//private RText rtext;
	private Box filterPanel;
	private JTextField filterField;
	private JTable table;
	private FilterableTableModel model;
	private Hyperlink scriptLink;
	private SelectableLabel authorField;
	private RSyntaxTextArea scriptTextArea;
	private JButton okButton;
	private Listener listener;
	private JPanel mainContentPane;
	private ScriptScraper scriptScraper;
	private ScriptLoaderThread scriptLoader;


	public ScriptSearchDialog(RText rtext) {

		super(rtext);
		//this.rtext = rtext;
		listener = new Listener();

		JPanel cp = new ResizableFrameContentPane(new BorderLayout());
		cp.setBorder(UIUtil.getEmpty5Border());

		cp.add(createTopPanel(), BorderLayout.NORTH);

		// The "center" of our BorderLayout content pane; we swap out its
		// contents when things load.
		mainContentPane = new JPanel(new BorderLayout());
		cp.add(mainContentPane);

		// Message to display while the scripts are loading.
		SelectableLabel label = new SelectableLabel(Messages.getString(
				"ScriptSearchDialog.LoadingScripts", ScriptScraper.FORUM));
		label.addHyperlinkListener(listener);
		label.setPreferredSize(new Dimension(640, 480));
		mainContentPane.add(label);

		// Make a panel for OK and cancel buttons.
		okButton = createButton("Close");
		okButton.setActionCommand("Close");
		okButton.addActionListener(listener);
		Container buttons = org.fife.ui.UIUtil.createButtonFooter(okButton);
		cp.add(buttons, BorderLayout.SOUTH);

		setContentPane(cp);
		setTitle(Messages.getString("ScriptSearchDialog.Title"));
		setResizable(true);
		pack();
		setLocationRelativeTo(rtext);

	}


	private JButton createButton(String keyRoot) {
		String title = Messages.getString(keyRoot);
		JButton button = new JButton(title);
		int mnemonic = Messages.getMnemonic(keyRoot);
		if (mnemonic>-1) {
			button.setMnemonic(mnemonic);
		}
		return button;
	}


	private JLabel createLabel(String keyRoot, Component labelFor) {
		keyRoot = "ScriptSearchDialog." + keyRoot;
		String title = Messages.getString(keyRoot);
		JLabel label = new JLabel(title);
		int mnemonic = Messages.getMnemonic(keyRoot);
		if (mnemonic>-1) {
			label.setDisplayedMnemonic(mnemonic);
		}
		label.setLabelFor(labelFor);
		return label;
	}


	/**
	 * Creates the panel with extra details about the script.
	 */
	private Container createScriptDetailsPanel() {

		JPanel panel = new JPanel(new SpringLayout());

		scriptLink = new Hyperlink("");
		JLabel linkLabel = createLabel("Link", scriptLink);
		authorField = new SelectableLabel("...");
		authorField.addHyperlinkListener(listener);
		JLabel authorLabel = createLabel("Author", authorField);

		if (getComponentOrientation().isLeftToRight()) {
			panel.add(linkLabel);			panel.add(scriptLink);
			panel.add(authorLabel);			panel.add(authorField);
		}
		else {
			panel.add(scriptLink);			panel.add(linkLabel);
			panel.add(authorField);			panel.add(authorLabel);
		}

		UIUtil.makeSpringCompactGrid(panel, 2, 2, 5, 5, 5, 5);

		JPanel temp = new JPanel(new BorderLayout());
		temp.add(panel);
		return temp;

	}


	private JTable createTable(TableModel model) {

		JTable table = new JTable(model) {
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
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		org.fife.ui.UIUtil.possiblyFixGridColor(table);

		FileExplorerTableModel fetm = new FileExplorerTableModel(model,
				table.getTableHeader());
		fetm.setSortingStatus(0, FileExplorerTableModel.ASCENDING);
		table.setModel(fetm);

		TableColumnModel tcm = table.getColumnModel();
		tcm.getColumn(1).setPreferredWidth(90); // Slightly larger than images
		tcm.getColumn(1).setWidth(90);
		tcm.getColumn(1).setCellRenderer(new RatingCellRenderer());
		tcm.getColumn(2).setPreferredWidth(130);
		tcm.getColumn(2).setWidth(130);

		return table;

	}


	/**
	 * Creates the top panel, containing the dialog's description and filter
	 * fields.
	 *
	 * @return The top panel of the dialog.
	 */
	private Container createTopPanel() {

		Box box = new Box(BoxLayout.LINE_AXIS);
		box.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

		SelectableLabel desc = new SelectableLabel(Messages.getString("ScriptSearchDialog.Desc"));
		desc.addHyperlinkListener(listener);
		box.add(desc);
		box.add(Box.createHorizontalGlue());

		filterPanel = new Box(BoxLayout.LINE_AXIS);
		filterField = new JTextField(25);
		// OS X-specific property (requires Java 5+).  Causes the text field
		// to be painted in OS X's "Search field" style.
		filterField.putClientProperty("JTextField.variant", "search");
		filterField.getDocument().addDocumentListener(listener);
		JLabel labelFilter = createLabel("Filter", filterField);
		filterPanel.add(labelFilter);
		filterPanel.add(Box.createHorizontalStrut(5));
		filterPanel.add(filterField);
		JPanel temp = new JPanel(new BorderLayout());
		temp.add(filterPanel, BorderLayout.NORTH);
		box.add(temp);

		setChildrenEnabled(filterPanel, false);
		return box;

	}


	private void displayError(Throwable t) {
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		RTextArea textArea = new RTextArea();
		textArea.setText(sw.toString());
		textArea.setEditable(false);
		RTextScrollPane sp = new RTextScrollPane(textArea);
		replaceMainContentWith(sp);
	}


	/**
	 * Spawns a worker thread to load the content of a script.  If an existing
	 * thread is working on another script, it is cancelled.
	 *
	 * @param info The script whose content to load.
	 */
	private synchronized void loadScriptContent(final ScriptInfo info) {
		if (scriptLoader!=null) {
			scriptLoader.interrupt();
		}
		scriptLoader = new ScriptLoaderThread(info);
		scriptLoader.start();
	}


	private void refreshScriptInfo(ScriptInfo info) {

		if (info==null) {
			scriptLink.setAddress(null);
			scriptLink.setText("");
			authorField.setText("");
			scriptTextArea.setText(null);
			return;
		}

		scriptLink.setAddress(info.getScriptAddress());
		scriptLink.setText(info.getScriptAddress());
		scriptLink.revalidate();

		String authorText = info.getAuthor();
		if (authorText!=null) {
			authorText = "<html><a href='" + info.getAuthorAddress() + "'>" +
					info.getAuthor() + "</a>";
		}
		else {
			authorText = Messages.getString("ScriptSearchDialog.UnknownAuthor");
		}
		authorField.setText(authorText);
		authorField.revalidate();

		String content = info.getContent();
		if (content!=null) {
			scriptTextArea.setText(content);
			scriptTextArea.setCaretPosition(0);
		}
		else {
			scriptTextArea.setText(Messages.getString("ScriptSearchDialog.Loading"));
			loadScriptContent(info);
		}

	}


	private void replaceMainContentWith(Component c) {
		mainContentPane.removeAll();
		mainContentPane.add(c);
		mainContentPane.revalidate();
	}


	/**
	 * Called when a <code>ScriptLoaderThread</code> completes.  This will be
	 * called on the EDT, but we synchronize it since it accesses our script
	 * loader field, just for "completeness."
	 *
	 * @param info The script whose content was loaded.
	 * @param content The content of the script.
	 */
	private synchronized void scriptContentLoaded(ScriptInfo info) {
		scriptTextArea.setText(info.getContent());
		scriptTextArea.setCaretPosition(0);
		scriptLoader = null;
	}


	private void scriptListLoaded(List scripts) {

		JPanel cp = new JPanel(new BorderLayout());

		Vector colNames = new Vector();
		colNames.add(Messages.getString("ScriptSearchDialog.TableColumn.Script"));
		colNames.add(Messages.getString("ScriptSearchDialog.TableColumn.Rating"));
		colNames.add(Messages.getString("ScriptSearchDialog.TableColumn.Author"));

		model = new FilterableTableModel(colNames, 0) {
			@Override
			public Class getColumnClass(int colIndex) {
				switch (colIndex) {
					case 1:
						return Integer.class;
					default:
						return String.class;
				}
			}
		};
		model.addFilterColumn(0);
		model.addFilterColumn(2);
		for (Iterator i=scripts.iterator(); i.hasNext(); ) {
			ScriptInfo script = (ScriptInfo)i.next();
			Vector row = new Vector();
			row.add(script);
			// TODO: Integer.valueOf(int) or whatever when move to Java 5+
			row.add(new Integer(script.getRating()));
			row.add(script.getAuthor());
			model.addRow(row);
		}

		table = createTable(model);
		table.getSelectionModel().addListSelectionListener(listener);
		RScrollPane sp = new RScrollPane(table);
		JPanel temp = new JPanel(new BorderLayout());
		temp.add(sp);
		cp.add(temp, BorderLayout.LINE_START);
		if (getComponentOrientation().isLeftToRight()) {
			temp.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
		}
		else {
			temp.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		}

		Box descPanel = Box.createVerticalBox();
		descPanel.add(createScriptDetailsPanel());
		descPanel.add(Box.createVerticalStrut(5));
		scriptTextArea = new RSyntaxTextArea(30, 80);
		scriptTextArea.setSyntaxEditingStyle(Plugin.SYNTAX_STYLE_ZSCRIPT);
		scriptTextArea.setCodeFoldingEnabled(true);
		scriptTextArea.setEditable(false);
		descPanel.add(new RTextScrollPane(scriptTextArea));
		descPanel.add(Box.createVerticalGlue());
		cp.add(descPanel);

		replaceMainContentWith(cp);
		setChildrenEnabled(filterPanel, true);
		pack();

	}


	/**
	 * Recursively enables or disables all components in a container.
	 *
	 * @param parent The container.
	 * @param enabled Whether to enable or disable all components.
	 */
	private void setChildrenEnabled(Container parent, boolean enabled) {
		for (int i=0; i<parent.getComponentCount(); i++) {
			Component c = parent.getComponent(i);
			c.setEnabled(enabled);
			if (c instanceof Container) {
				Container container = (Container)c;
				if (container.getComponentCount()>0) {
					setChildrenEnabled(container, enabled);
				}
			}
		}
	}


	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			if (scriptScraper==null) {
				scriptScraper = new ScriptScraper();
				new ScriptListLoaderThread(scriptScraper).start();
			}
		}
		super.setVisible(visible);
	}


	/**
	 * A filterable table model.  This is by no means complete, but is just
	 * enough for this class's use case.<p>
	 * A lot of unsafe-ness is going on since Swing wasn't genericized until
	 * Java 7.
	 */
	@SuppressWarnings("rawtypes")
	private class FilterableTableModel extends DefaultTableModel {

		private String filter;
		private List<Integer> filterColumns;
		private List allRows;
	
		public FilterableTableModel(Vector<String> colNames, int rowCount) {
			super(colNames, rowCount);
			allRows = new ArrayList<Object>();
			filterColumns = new ArrayList<Integer>();
			setFilter(null); // initialize!
		}

		public void addFilterColumn(int col) {
			filterColumns.add(Integer.valueOf(col));
		}

		@Override
		@SuppressWarnings("unchecked")
		public void addRow(Vector rowData) {
			allRows.add(rowData);
			if (rowMatchesFilter(rowData)) {
				super.addRow(rowData);
			}
		}

		private void refilter() {
			setRowCount(0);
			for (int i=0; i<allRows.size(); i++) {
				Vector rowData = (Vector)allRows.get(i);
				if (rowMatchesFilter(rowData)) {
					super.addRow(rowData);
				}
			}
		}

		private boolean rowMatchesFilter(Vector rowData) {
			for (int i=0; i<filterColumns.size(); i++) {
				int col = filterColumns.get(i).intValue();
				String cell = rowData.get(col).toString().toLowerCase();
				if (cell.contains(filter)) {
					return true;
				}
			}
			return false;
		}

		public void setFilter(String filter) {
			if (filter==null) {
				filter = "";
			}
			else {
				filter = filter.toLowerCase();
			}
			this.filter = filter;
			refilter();
		}

	}


	/**
	 * Listens for events in this dialog.
	 */
	private class Listener implements ActionListener, DocumentListener,
			ListSelectionListener, HyperlinkListener {

		public void actionPerformed(ActionEvent e) {

			String command = e.getActionCommand();

			if ("Close".equals(command)) {
				escapePressed();
			}

		}

		public void changedUpdate(DocumentEvent e) {
		}

		private void handleDocumentEvent(DocumentEvent e) {
			if (filterField.getDocument()==e.getDocument()) {
				model.setFilter(filterField.getText());
			}
		}

		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {
				URL url = e.getURL();
				UIUtil.browse(url.toString());
			}
		}

		public void insertUpdate(DocumentEvent e) {
			handleDocumentEvent(e);
		}

		public void removeUpdate(DocumentEvent e) {
			handleDocumentEvent(e);
		}

		public void valueChanged(ListSelectionEvent e) {
			// Minimize chances of multiple fast HTTP requests if they scroll
			// quickly with the mouse (holding down arrow keys still a problem).
			if (!e.getValueIsAdjusting()) {
				int index = table.getSelectedRow();
				ScriptInfo selected = index==-1 ? null :
					(ScriptInfo)table.getValueAt(index, 0);
				refreshScriptInfo(selected);
			}
		}

	}


	/**
	 * Renderer for the "rating" table column.
	 */
	private static class RatingCellRenderer extends DefaultTableCellRenderer {

		private Icon[] icons;

		public RatingCellRenderer() {
			loadIcons();
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean selected, boolean focused,
				int row, int col) {
	    	super.getTableCellRendererComponent(table, value, selected, focused, row, col);
	    	if (value instanceof Integer) { // Always true
	    		setText(null);
	    		int i = Math.max(0,  Math.min(((Integer)value).intValue(), 5));
	    		setIcon(icons[i]);
	    	}
	    	return this;
	    }

		private void loadIcons() {
			icons = new Icon[6];
			for (int i=0; i<icons.length; i++) {
				URL url = getClass().getResource("rating" + i + ".png");
				icons[i] = new ImageIcon(url);
			}
		}

	}


	/**
	 * Loads scripts from PureZC in a background thread, and returns either
	 * a list of metadata about those scripts, or an error if something bad
	 * happened.
	 */
	private class ScriptListLoaderThread extends GUIWorkerThread {

		private ScriptScraper scraper;

		public ScriptListLoaderThread(ScriptScraper scraper) {
			this.scraper = scraper;
		}

		@Override
		public Object construct() {
			try {
				return scraper.getScripts();
			} catch (/*IOException*/Throwable t) {
				return t;
			}
		}

		@Override
		public void finished() {
			Object result = get();
			if (result instanceof Throwable) {
				displayError((Throwable)result);
			}
			scriptListLoaded((List)result);
		}

	}


	/**
	 * Loads the content of a single script.
	 */
	private class ScriptLoaderThread extends GUIWorkerThread {

		private ScriptInfo scriptToLoad;

		private ScriptLoaderThread(ScriptInfo scriptToLoad) {
			this.scriptToLoad = scriptToLoad;
		}

		@Override
		public Object construct() {
			scriptToLoad.fetchContent();
			return scriptToLoad;
		}

		@Override
		public void finished() {
			scriptContentLoaded(scriptToLoad);
		}

	}


}