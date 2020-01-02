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
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
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
import org.fife.ui.CleanSplitPaneUI;
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
	private Container filterPanel;
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


	ScriptSearchDialog(RText rtext) {

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
		Dimension size = getPreferredSize();
		if (size.width<1024) {
			size.width = 1024;
		}
		setSize(size);
		//pack();
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
		temp.add(panel, BorderLayout.NORTH);
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
				return parent instanceof JViewport && parent.getHeight() > getPreferredSize().height;
			}
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		org.fife.ui.UIUtil.possiblyFixGridColor(table);

		FileExplorerTableModel fetm = new FileExplorerTableModel(model,
				table.getTableHeader());
		fetm.setColumnComparator(String.class, String.CASE_INSENSITIVE_ORDER);
		fetm.setSortingStatus(0, FileExplorerTableModel.ASCENDING);
		table.setModel(fetm);

		TableColumnModel tcm = table.getColumnModel();
		tcm.getColumn(0).setPreferredWidth(130);
		tcm.getColumn(0).setWidth(130);
		tcm.getColumn(1).setPreferredWidth(90); // Slightly larger than images
		tcm.getColumn(1).setWidth(90);
		tcm.getColumn(1).setCellRenderer(new RatingCellRenderer());
		tcm.getColumn(2).setPreferredWidth(90);
		tcm.getColumn(2).setWidth(90);

		return table;

	}


	/**
	 * Creates the top panel, containing the dialog's description and filter
	 * fields.
	 *
	 * @return The top panel of the dialog.
	 */
	private Container createTopPanel() {

		JPanel box = new JPanel(new BorderLayout());
		box.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

		SelectableLabel desc = new SelectableLabel(Messages.getString("ScriptSearchDialog.Desc"));
		desc.addHyperlinkListener(listener);
		box.add(desc, BorderLayout.LINE_START);

		filterPanel = new JPanel(new BorderLayout(5, 0));
		filterField = new JTextField(25);
		// OS X-specific property (requires Java 5+).  Causes the text field
		// to be painted in OS X's "Search field" style.
		filterField.putClientProperty("JTextField.variant", "search");
		filterField.getDocument().addDocumentListener(listener);
		JLabel labelFilter = createLabel("Filter", filterField);
		filterPanel.add(labelFilter, BorderLayout.LINE_START);
		filterPanel.add(filterField, BorderLayout.LINE_END);
		JPanel temp = new JPanel(new BorderLayout());
		temp.add(filterPanel, BorderLayout.NORTH);
		box.add(temp, BorderLayout.LINE_END);

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
	 */
	private synchronized void scriptContentLoaded(ScriptInfo info) {
		scriptTextArea.setText(info.getContent());
		scriptTextArea.setCaretPosition(0);
		scriptLoader = null;
	}


	private void scriptListLoaded(List<ScriptInfo> scripts) {

		JSplitPane cp = new JSplitPane();
		cp.setUI(new CleanSplitPaneUI());

		Vector<String> colNames = new Vector<>();
		colNames.add(Messages.getString("ScriptSearchDialog.TableColumn.Script"));
		colNames.add(Messages.getString("ScriptSearchDialog.TableColumn.Rating"));
		colNames.add(Messages.getString("ScriptSearchDialog.TableColumn.Author"));
		colNames.add(Messages.getString("ScriptSearchDialog.TableColumn.Tags"));
		colNames.add(Messages.getString("ScriptSearchDialog.TableColumn.StartedDate"));

		model = new FilterableTableModel(colNames, 0) {
			@Override
			public Class<?> getColumnClass(int colIndex) {
				switch (colIndex) {
					case 0:
						return ScriptInfo.class;
					case 1:
						return Integer.class;
					default:
						return String.class;
				}
			}
		};
		model.addFilterColumn(0);
		model.addFilterColumn(2);
		model.addFilterColumn(3);
		model.addFilterColumn(4);
		for (ScriptInfo script : scripts) {
			Vector<Object> row = new Vector<>();
			row.add(script);
			row.add(script.getRating());
			row.add(script.getAuthor());
			row.add(ZScriptUIUtils.prettyPrint(script.getSearchTags()));
			row.add(script.getDateCreated());
			model.addRow(row);
		}

		table = createTable(model);
		table.getSelectionModel().addListSelectionListener(listener);
		RScrollPane sp = new RScrollPane(table);
		JPanel temp = new JPanel(new BorderLayout());
		temp.add(sp);
		cp.setLeftComponent(temp);
		if (getComponentOrientation().isLeftToRight()) {
			temp.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
		}
		else {
			temp.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		}

		JPanel descPanel = new JPanel(new BorderLayout(0, 5));
		descPanel.add(createScriptDetailsPanel(), BorderLayout.NORTH);
		scriptTextArea = new RSyntaxTextArea();//30, 80);
		scriptTextArea.setSyntaxEditingStyle(Plugin.SYNTAX_STYLE_ZSCRIPT);
		scriptTextArea.setCodeFoldingEnabled(true);
		scriptTextArea.setEditable(false);
		//scriptTextArea.setUseSelectedTextColor(true);
		descPanel.add(new RTextScrollPane(scriptTextArea));
		cp.setRightComponent(descPanel);

		replaceMainContentWith(cp);
		setChildrenEnabled(filterPanel, true);
		Dimension size = getPreferredSize();
		if (size.width<1024) {
			size.width = 1024;
		}
		setSize(size);
		//pack();

		new ScriptRatingFetcher(scripts).start();

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
	private static class FilterableTableModel extends DefaultTableModel {

		private String filter;
		private List<Integer> filterColumns;
		private List allRows;

		FilterableTableModel(Vector<String> colNames, int rowCount) {
			super(colNames, rowCount);
			allRows = new ArrayList<>();
			filterColumns = new ArrayList<>();
			setFilter(null); // initialize!
		}

		public void addFilterColumn(int col) {
			filterColumns.add(col);
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
            for (Object allRow : allRows) {
                Vector rowData = (Vector)allRow;
                if (rowMatchesFilter(rowData)) {
                    super.addRow(rowData);
                }
            }
		}

		private boolean rowMatchesFilter(Vector rowData) {
            for (Integer filterColumn : filterColumns) {
                int col = filterColumn;
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

		private int lastSelectedIndex = -1;

		@Override
		public void actionPerformed(ActionEvent e) {

			String command = e.getActionCommand();

			if ("Close".equals(command)) {
				escapePressed();
			}

		}

		@Override
		public void changedUpdate(DocumentEvent e) {
		}

		private void handleDocumentEvent(DocumentEvent e) {
			if (filterField.getDocument()==e.getDocument()) {
				model.setFilter(filterField.getText());
			}
		}

		@Override
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {
				URL url = e.getURL();
				if (url==null) {
					String msg = Messages.getString("SearchScriptDialog.Error.NotImplemented");
					String title = Messages.getString("Error.DialogTitle");
					JOptionPane.showMessageDialog(ScriptSearchDialog.this, msg,
							title, JOptionPane.ERROR_MESSAGE);
				}
				else {
                    UIUtil.browse(url.toString());
                }
			}
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			handleDocumentEvent(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			handleDocumentEvent(e);
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			// Minimize chances of multiple fast HTTP requests if they scroll
			// quickly with the mouse (holding down arrow keys still a problem).
			if (!e.getValueIsAdjusting()) {
				int index = table.getSelectedRow();
				ScriptInfo selected = index==-1 ? null :
					(ScriptInfo)table.getValueAt(index, 0);
				if (selected!=null && index!=lastSelectedIndex) {
					lastSelectedIndex = index;
					refreshScriptInfo(selected);
				}
			}
		}

	}


	/**
	 * Renderer for the "rating" table column.
	 */
	private static class RatingCellRenderer extends DefaultTableCellRenderer {

		private Icon[] icons;

		RatingCellRenderer() {
			loadIcons();
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean selected, boolean focused,
				int row, int col) {
	    	super.getTableCellRendererComponent(table, value, selected, focused, row, col);
	    	if (value instanceof Integer) { // Always true
	    		setText(null);
	    		int i = Math.max(0,  Math.min((Integer)value, 5));
	    		setIcon(icons[i]);
	    	}
	    	return this;
	    }

		private void loadIcons() {

			Icon star = new ImageIcon(getClass().getResource("star.png"));
			Icon starOff = new ImageIcon(getClass().getResource("star_off.png"));

			int width = 16*5 + 2*4;
			int height = 16;
			icons = new Icon[6];
			for (int i=0; i<icons.length; i++) {
				BufferedImage img = new BufferedImage(width, height,
						BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = img.createGraphics();
				for (int j=0; j<i; j++) {
					star.paintIcon(null, g, j*(16+2), 0);
				}
				for (int j=i; j<icons.length; j++) {
					starOff.paintIcon(null, g, j*(16+2), 0);
				}
				g.dispose();
				icons[i] = new ImageIcon(img);
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

		ScriptListLoaderThread(ScriptScraper scraper) {
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
			else {
				@SuppressWarnings("unchecked")
				List<ScriptInfo> result2 = (List<ScriptInfo>)result;
				scriptListLoaded(result2);
			}
		}

	}


	/**
	 * Loads the content of a single script.
	 */
	private final class ScriptLoaderThread extends GUIWorkerThread {

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


	private final class ScriptRatingFetcher extends Thread {

		private List<ScriptInfo> scripts;

		private ScriptRatingFetcher(List<ScriptInfo> scripts) {
			this.scripts = scripts;
		}

		@Override
		public void run() {
			for (ScriptInfo script : scripts) {
				int rating = ScriptScraper.fetchRating(script);
				if (rating>-1) {
					updateRating(script, rating);
					try {
						Thread.sleep(100);
					} catch (InterruptedException ie) {
						ie.printStackTrace();
					}
				}
			}
		}

		private void updateRating(final ScriptInfo script, final int rating) {
			//System.out.println(script + ": " + rating);
			SwingUtilities.invokeLater(() -> {
				script.setRating(rating);
				for (int row=0; row<model.getRowCount(); row++) {
					ScriptInfo s2 = (ScriptInfo)model.getValueAt(row, 0);
					if (s2.equals(script)) {
						// Note JTable drops its selection when
						// DefaultTableModel.setValueAt() is called
						int selection = table.getSelectedRow();
						model.setValueAt(rating, row, 1);
						table.getSelectionModel().setSelectionInterval(
								selection, selection);
						break;
					}
				}
			});
		}

	}


}
