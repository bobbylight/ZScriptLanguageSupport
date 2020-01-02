/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.demo;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.AccessControlException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;

import org.fife.rsta.ac.LanguageSupport;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.ui.search.FindDialog;
import org.fife.rsta.ui.search.ReplaceDialog;
import org.fife.rsta.ui.search.SearchEvent;
import org.fife.rsta.ui.search.SearchListener;
import org.fife.rsta.zscript.IconFactory;
import org.fife.rsta.zscript.ZScriptLanguageSupport;
import org.fife.rsta.zscript.ZScriptParser;
import org.fife.rsta.zscript.demo.ErrorTable.ErrorTableModel;
import org.fife.rsta.zscript.tree.ZScriptOutlineTree;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.ErrorStrip;
import org.fife.ui.rsyntaxtextarea.FileLocation;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.TextEditorPane;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.CurlyFoldParser;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.IconGroup;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;


/**
 * Root pane for the demo.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class DemoRootPane extends JRootPane implements SyntaxConstants,
				HyperlinkListener, SearchListener {

	private JScrollPane treeSP;
	private RTextScrollPane scrollPane;
	private RSyntaxTextArea textArea;
	private boolean webDemo;
	private JTable errorTable;
	private ErrorTableModel errorTableModel;
	private FileChooser chooser;

	private FindDialog findDialog;
	private ReplaceDialog replaceDialog;
	private SearchContext searchContext;


	public DemoRootPane() {

		JPanel cp = new JPanel(new BorderLayout());

		textArea = createTextArea();
		setText("ZScriptExample.txt");
		scrollPane = new RTextScrollPane(textArea, true);
		scrollPane.setIconRowHeaderEnabled(true);

		JPanel topRightPanel = new JPanel(new BorderLayout());
		topRightPanel.add(scrollPane);
		ErrorStrip errorStrip = new ErrorStrip(textArea);
		topRightPanel.add(errorStrip, BorderLayout.LINE_END);

		// Dummy tree keeps JViewport's "background" looking right initially
		ZScriptOutlineTree tree = new ZScriptOutlineTree();
		tree.listenTo(textArea);
		treeSP = new JScrollPane(tree);

		String[] columnNames = { "", "Line", "Error" };
		errorTableModel = new ErrorTableModel(columnNames, 0);
		errorTable = new ErrorTable(errorTableModel, textArea);
		JScrollPane errorTableScrollPane = new JScrollPane(errorTable);

		final JSplitPane rhsSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				topRightPanel, errorTableScrollPane);
		rhsSplitPane.setUI(new CleanSplitPaneUI());
		rhsSplitPane.setContinuousLayout(true);
		rhsSplitPane.setResizeWeight(1);

		final JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				treeSP, rhsSplitPane);
		sp.setUI(new CleanSplitPaneUI());
		sp.setContinuousLayout(true);
		sp.setResizeWeight(0);
		cp.add(sp);

		setJMenuBar(createMenuBar());
		setContentPane(cp);

		SwingUtilities.invokeLater(() -> {
			rhsSplitPane.setDividerLocation(0.70);
			sp.setDividerLocation(0.25);
			textArea.requestFocusInWindow();
		});

		textArea.addPropertyChangeListener(RSyntaxTextArea.PARSER_NOTICES_PROPERTY,
                e -> {
                    List<ParserNotice> notices = textArea.getParserNotices();
                    refreshErrorTable(notices);
                }
        );

		searchContext = new SearchContext();

	}


	private void addThemeItem(String name, String themeXml, ButtonGroup bg,
			JMenu menu) {
		JRadioButtonMenuItem item = new JRadioButtonMenuItem(
				new ThemeAction(name, themeXml));
		bg.add(item);
		menu.add(item);
	}


	private void createFileChooser() {
		chooser = new FileChooser();
		chooser.setFileFilter(new ZScriptFileFilter());
	}


	private JMenuBar createMenuBar() {

		JMenuBar mb = new JMenuBar();

		JMenu menu = new JMenu("File");
		menu.setMnemonic('F');
		menu.add(new JMenuItem(new OpenAction()));
		menu.add(new JMenuItem(new SaveAction()));
		menu.add(new JMenuItem(new SaveAsAction()));
		menu.addSeparator();
		menu.add(new JMenuItem(new ExitAction()));
		mb.add(menu);

		menu = new JMenu("Edit");
		menu.setMnemonic('E');
		menu.add(new JMenuItem(new GoToMemberDelegateAction()));
		menu.addSeparator();
		menu.add(new JMenuItem(new FindAction()));
		menu.add(new JMenuItem(new ReplaceAction()));
		mb.add(menu);

		menu = new JMenu("View");
		menu.setMnemonic('V');
		JCheckBoxMenuItem cbItem = new JCheckBoxMenuItem(new CodeFoldingAction());
		cbItem.setSelected(true);
		menu.add(cbItem);
		cbItem = new JCheckBoxMenuItem(new ViewLineNumbersAction());
		cbItem.setSelected(true);
		menu.add(cbItem);
		cbItem = new JCheckBoxMenuItem(new WordWrapAction());
		menu.add(cbItem);
		cbItem = new JCheckBoxMenuItem(new TabLinesAction());
		menu.add(cbItem);
		mb.add(menu);
		menu.addSeparator();
		cbItem = new JCheckBoxMenuItem(new AutoActivationAction());
		cbItem.setSelected(true);
		menu.add(cbItem);

		ButtonGroup bg = new ButtonGroup();
		menu = new JMenu("Themes");
		addThemeItem("Default", "default.xml", bg, menu);
		addThemeItem("Default (System Selection)", "default-alt.xml", bg, menu);
		addThemeItem("Dark", "dark.xml", bg, menu);
		addThemeItem("Eclipse", "eclipse.xml", bg, menu);
		addThemeItem("IDEA", "idea.xml", bg, menu);
		addThemeItem("Visual Studio", "vs.xml", bg, menu);
		mb.add(menu);

		menu = new JMenu("Help");
		menu.setMnemonic('H');
		JMenuItem item = new JMenuItem(new AboutAction());
		menu.add(item);
		mb.add(menu);

		return mb;

	}


	/**
	 * Creates the text area for this application.
	 *
	 * @return The text area.
	 */
	private RSyntaxTextArea createTextArea() {
		RSyntaxTextArea textArea;
		try {
			textArea = new TextEditorPane();
		} catch (AccessControlException ace) { // Web demo, no file system access
			textArea = new RSyntaxTextArea();
			webDemo = true;
		}
		textArea.setRows(25);
		textArea.setColumns(100);
		textArea.setSyntaxEditingStyle("text/zscript");
		textArea.addHyperlinkListener(this);
		textArea.requestFocusInWindow();
		textArea.setMarkOccurrences(true);
		textArea.setCodeFoldingEnabled(true);
		textArea.setClearWhitespaceLinesEnabled(false);
		textArea.setTabSize(4);
		textArea.setTabsEmulated(true);
		textArea.setAnimateBracketMatching(false);
		textArea.setPaintMatchedBracketPair(true);
		TextEditorPane.setIconGroup(new IconGroup("Eclipse", "org/fife/rsta/zscript/demo"));
		LanguageSupportFactory.get().register(textArea);
		return textArea;
	}


	/**
	 * Focuses the text area.
	 */
	void focusTextArea() {
		textArea.requestFocusInWindow();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSelectedText() {
		return textArea.getSelectedText();
	}


	/**
	 * Returns the editor component.
	 *
	 * @return The editor component.
	 */
	public RSyntaxTextArea getTextArea() {
		return textArea;
	}


	@Override
	public void searchEvent(SearchEvent e) {

		SearchEvent.Type type = e.getType();
		SearchContext context = e.getSearchContext();
		SearchResult result;

		switch (type) {
			case MARK_ALL:
				result = SearchEngine.markAll(textArea, context);
				break;
			case FIND:
				result = SearchEngine.find(textArea, context);
				if (!result.wasFound()) {
					UIManager.getLookAndFeel().provideErrorFeedback(textArea);
				}
				break;
			case REPLACE:
				result = SearchEngine.replace(textArea, context);
				if (!result.wasFound()) {
					UIManager.getLookAndFeel().provideErrorFeedback(textArea);
				}
				break;
			case REPLACE_ALL:
				result = SearchEngine.replaceAll(textArea, context);
				JOptionPane.showMessageDialog(null, result.getCount() +
						" occurrences replaced.");
				break;
		}

//		String text = null;
//		if (result.wasFound()) {
//			text = "Text found; occurrences marked: " + result.getMarkedCount();
//		}
//		else if (type==SearchEvent.Type.MARK_ALL) {
//			if (result.getMarkedCount()>0) {
//				text = "Occurrences marked: " + result.getMarkedCount();
//			}
//			else {
//				text = "";
//			}
//		}
//		else {
//			text = "Text not found";
//		}
//		statusBar.setLabel(text);

	}


	/**
	 * Called when a hyperlink is clicked in the text area.
	 *
	 * @param e The event.
	 */
	@Override
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {
			URL url = e.getURL();
			if (url!=null) { // CodeEditorHyperlinkListener doesn't use URLs.
				JOptionPane.showMessageDialog(this,
									"URL clicked:\n" + url.toString());
			}
		}
	}


	private static boolean isOSX() {
		return System.getProperty("os.name").toLowerCase().contains("os x");
	}


	/**
	 * Opens a file.  Prompts the user to save if the existing file is dirty.
	 *
	 * @param file The file to open.
	 */
	private void openFile(File file) {

		if (webDemoCheck()) {
			return;
		}

		if (!saveIfDirty()) {
			return;
		}

		FileLocation loc = FileLocation.create(file);
		try {
			((TextEditorPane)textArea).load(loc);
			refreshTitle();
		} catch (IOException ioe) {
			String message = "Error loading file " + loc.getFileName() + ":\n" +
					ioe.getMessage();
			String title = "Could not load file";
			JOptionPane.showMessageDialog(this, message, title,
					JOptionPane.ERROR_MESSAGE);
			ioe.printStackTrace();
		}

	}


	private void refreshErrorTable(List<ParserNotice> notices) {

		Gutter gutter = scrollPane.getGutter();
		gutter.removeAllTrackingIcons();
		errorTableModel.setRowCount(0);

		LanguageSupport ls = LanguageSupportFactory.get().getSupportFor("text/zscript");
		ZScriptLanguageSupport zsls = (ZScriptLanguageSupport)ls;
		ZScriptParser parser = zsls.getParser(textArea);
		for (ParserNotice notice : notices) {
			if (notice.getParser()==parser) {
				boolean error = notice.getLevel()==ParserNotice.Level.ERROR;
				String iconName = error ? IconFactory.ERROR_ICON : IconFactory.WARNING_ICON;
				Icon icon = IconFactory.get().getIcon(iconName);
				int line = notice.getLine();
				Object[] data = { icon, line + 1, notice };
				errorTableModel.addRow(data);
				try {
					gutter.addLineTrackingIcon(line, icon, notice.getToolTipText());
				} catch (BadLocationException ble) {
					ble.printStackTrace();
				}
			}
		}

	}


	private void refreshTitle() {
		if (webDemo) {
			return;
		}
		String file = ((TextEditorPane)textArea).getFileFullPath();
		Window parent = SwingUtilities.getWindowAncestor(this);
		if (parent instanceof Frame) {
			((Frame)parent).setTitle("ZScript Demo - " + file);
		}
	}


	/**
	 * Set up general stuff for our new language.  This doesn't really belong
	 * here, but was put here anywhere to share between the applet and
	 * stand-alone demos.
	 */
	static void registerZScript() {

		// Set up general stuff for our new language.
		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		lsf.addLanguageSupport("text/zscript", "org.fife.rsta.zscript.ZScriptLanguageSupport");
		TokenMakerFactory tmf = TokenMakerFactory.getDefaultInstance();
		((AbstractTokenMakerFactory)tmf).putMapping("text/zscript", "org.fife.rsta.zscript.ZScriptTokenMaker");
		FoldParserManager fpm = FoldParserManager.get();
		fpm.addFoldParserMapping("text/zscript", new CurlyFoldParser(false, false));

		LanguageSupport ls = LanguageSupportFactory.get().getSupportFor("text/zscript");
		ZScriptLanguageSupport zsls = (ZScriptLanguageSupport)ls;
		zsls.setDocDisplayer(new DemoDocDisplayer());

	}


	private boolean saveIfDirty() {

		boolean proceed = true;

		if (!webDemo && ((TextEditorPane)textArea).isDirty()) {
			String message = "File " + ((TextEditorPane)textArea).getFileName() +
					" has unsaved changes.\n" +
					"Do you want to save them?";
			int rc = JOptionPane.showConfirmDialog(this, message);
			switch (rc) {
				case JOptionPane.YES_OPTION:
					proceed = saveImpl(null);
					break;
				case JOptionPane.CANCEL_OPTION:
					proceed = false;
					break;
			}
		}

		return proceed;

	}


	private boolean saveImpl(FileLocation newLoc) {

		boolean success = true;

		if (!webDemo) {
			TextEditorPane textArea = (TextEditorPane)this.textArea;
			try {
				if (newLoc!=null) {
					textArea.saveAs(newLoc);
					refreshTitle();
				}
				else {
					textArea.save();
				}
			} catch (IOException ioe) {
				String message = "Error saving file:\n" + ioe.getMessage();
				String title = "Could not save file";
				JOptionPane.showMessageDialog(this, message, title,
						JOptionPane.ERROR_MESSAGE);
				ioe.printStackTrace();
				success = false;
			}
		}

		return success;

	}


	/**
	 * Sets the content in the text area to that in the specified resource.
	 *
	 * @param resource The resource to load.
	 */
	private void setText(String resource) {
		BufferedReader r;
		try {
			r = new BufferedReader(new InputStreamReader(
					getClass().getResourceAsStream(resource), StandardCharsets.UTF_8));
			textArea.read(r, null);
			r.close();
			textArea.setCaretPosition(0);
			textArea.discardAllEdits();
		} catch (RuntimeException re) {
			throw re; // FindBugs
		} catch (Exception e) { // Never happens
			textArea.setText("Type here to see syntax highlighting");
		}
	}


	private boolean webDemoCheck() {
		if (webDemo) {
			String msg = "You cannot open and save files in the web demo, use the native demo.";
			String title = "Sorry!";
			JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
		}
		return webDemo;
	}


	private class AboutAction extends AbstractAction {

		AboutAction() {
			putValue(NAME, "About ZScript Editor Demo...");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JOptionPane.showMessageDialog(DemoRootPane.this,
					"<html><b>ZScript Editor Support for RSyntaxTextArea</b>" +
					"<br>Version " + getVersion() +
					"<br>Licensed under a modified BSD license",
					"About ZScript Editor Support Demo",
					JOptionPane.INFORMATION_MESSAGE);
		}

		public String getVersion() {
			String version = "3.0.0-SNAPSHOT";
			String buildDate = ZScriptLanguageSupport.getBuildDate();
			if (buildDate!=null) {
				version += " build " + buildDate;
			}
			return version;
		}

	}


	private static class AutoActivationAction extends AbstractAction {

		AutoActivationAction() {
			putValue(NAME, "Automatically show completions after typing \"->\"");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LanguageSupport ls = LanguageSupportFactory.get().getSupportFor("text/zscript");
			ZScriptLanguageSupport zsls = (ZScriptLanguageSupport)ls;
			zsls.setAutoActivationEnabled(!zsls.isAutoActivationEnabled());
		}

	}


	private class CodeFoldingAction extends AbstractAction {

		CodeFoldingAction() {
			putValue(NAME, "Code Folding");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			textArea.setCodeFoldingEnabled(!textArea.isCodeFoldingEnabled());
		}

	}


	private class ExitAction extends AbstractAction {

		ExitAction() {
			putValue(NAME, "Exit");
			putValue(MNEMONIC_KEY, (int)'x');
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (!saveIfDirty()) {
				return;
			}
			System.exit(0);
		}

	}


	private class FindAction extends AbstractAction {

		FindAction() {
			putValue(NAME, "Find...");
			String ks = isOSX() ? "meta F" : "ctrl F";
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(ks));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (replaceDialog!=null && replaceDialog.isVisible()) {
				replaceDialog.setVisible(false);
			}
			if (findDialog==null) {
				Frame parent = (Frame)SwingUtilities.getWindowAncestor(DemoRootPane.this);
				findDialog = new FindDialog(parent, DemoRootPane.this);
				findDialog.setSearchContext(searchContext);
			}
			if (findDialog.isVisible()) {
				findDialog.requestFocus();
			}
			else {
				findDialog.setVisible(true);
			}
		}

	}


	private class GoToMemberDelegateAction extends AbstractAction {

		GoToMemberDelegateAction() {
			putValue(NAME, "Go to Member...");
			String ks = isOSX() ? "meta shift O" : "ctrl shift O";
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(ks));
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			int c = textArea.getToolkit().getMenuShortcutKeyMaskEx();
			int shift = InputEvent.SHIFT_DOWN_MASK;
			KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_O, c|shift);
			boolean success = false;

			InputMap im = textArea.getInputMap();
			Object key = im.get(ks);
			if (key!=null) {
				ActionMap am = textArea.getActionMap();
				Action a = am.get(key);
				if (a instanceof org.fife.rsta.ac.GoToMemberAction) {
					a.actionPerformed(e);
					success = true;
				}
			}

			// Shouldn't happen unless they've mapped a different action to
			// C+Shift+O.
			if (!success) {
				UIManager.getLookAndFeel().provideErrorFeedback(textArea);
			}

		}

	}


	private class OpenAction extends AbstractAction {

		OpenAction() {
			putValue(NAME, "Open File...");
			String ks = isOSX() ? "meta O" : "ctrl O";
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(ks));
			putValue(SMALL_ICON, new ImageIcon(getClass().getResource("open.gif")));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (webDemoCheck()) {
				return;
			}
			if (chooser==null) {
				createFileChooser();
			}
			int rc = chooser.showOpenDialog(DemoRootPane.this);
			if (rc==JFileChooser.APPROVE_OPTION) {
				openFile(chooser.getSelectedFile());
			}
		}

	}


	private class ReplaceAction extends AbstractAction {

		ReplaceAction() {
			putValue(NAME, "Replace...");
			String ks = isOSX() ? "meta H" : "ctrl H";
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(ks));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (findDialog!=null && findDialog.isVisible()) {
				findDialog.setVisible(false);
			}
			if (replaceDialog==null) {
				Frame parent = (Frame)SwingUtilities.getWindowAncestor(DemoRootPane.this);
				replaceDialog = new ReplaceDialog(parent, DemoRootPane.this);
				replaceDialog.setSearchContext(searchContext);
			}
			if (replaceDialog.isVisible()) {
				replaceDialog.requestFocus();
			}
			else {
				replaceDialog.setVisible(true);
			}
		}

	}


	private class SaveAction extends AbstractAction {

		SaveAction() {
			putValue(NAME, "Save");
			String ks = isOSX() ? "meta S" : "ctrl S";
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(ks));
			putValue(SMALL_ICON, new ImageIcon(getClass().getResource("save.gif")));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (!webDemoCheck()) {
				saveImpl(null);
			}
		}

	}


	private class SaveAsAction extends AbstractAction {

		SaveAsAction() {
			putValue(NAME, "Save As...");
			putValue(SMALL_ICON, new ImageIcon(getClass().getResource("saveas.gif")));
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			if (webDemoCheck()) {
				return;
			}

			if (chooser==null) {
				createFileChooser();
			}

			int rc = chooser.showSaveDialog(DemoRootPane.this);
			if (rc!=JFileChooser.APPROVE_OPTION) {
				return;
			}

			File file = chooser.getSelectedFile();
			if (file.exists()) {
				String msg = "File already exists:\n" +
						file.getAbsolutePath() + "\n" +
						"Do you wish to overwrite it?";
				rc = JOptionPane.showConfirmDialog(DemoRootPane.this, msg);
				if (rc!=JOptionPane.YES_OPTION) {
					return;
				}
			}

			FileLocation loc = FileLocation.create(file);
			saveImpl(loc);

		}

	}


	private class TabLinesAction extends AbstractAction {

		private boolean selected;

		TabLinesAction() {
			putValue(NAME, "Tab Lines");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			selected = !selected;
			textArea.setPaintTabLines(selected);
		}

	}


	private class ThemeAction extends AbstractAction {

		private String xml;

		ThemeAction(String name, String xml) {
			putValue(NAME, name);
			this.xml = xml;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			InputStream in = getClass().getResourceAsStream(
					"/org/fife/ui/rsyntaxtextarea/themes/" + xml);
			try {
				Theme theme = Theme.load(in);
				theme.apply(textArea);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

	}


	private class ViewLineNumbersAction extends AbstractAction {

		ViewLineNumbersAction() {
			putValue(NAME, "Line Numbers");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			scrollPane.setLineNumbersEnabled(!scrollPane.getLineNumbersEnabled());
		}

	}


	private class WordWrapAction extends AbstractAction {

		WordWrapAction() {
			putValue(NAME, "Word Wrap");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			textArea.setLineWrap(!textArea.getLineWrap());
		}

	}


}
