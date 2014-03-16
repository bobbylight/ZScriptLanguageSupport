/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.Timer;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;

import org.fife.rsta.ac.AbstractLanguageSupport;
import org.fife.rsta.ac.GoToMemberAction;
import org.fife.rsta.zscript.ast.BodiedNode;
import org.fife.rsta.zscript.ast.ZScriptAst;
import org.fife.rsta.zscript.tree.ZScriptOutlineTree;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;


/**
 * LanguageSupport for ZScript.
 *
 * @author Robert Futrell
 * @vesrion 1.0
 */
public class ZScriptLanguageSupport extends AbstractLanguageSupport {

	/**
	 * Maps parsers to <code>Info</code> instances about them.
	 */
	private Map<ZScriptParser, Info> parserToInfoMap;

	private DocDisplayer docDisplayer;


	public ZScriptLanguageSupport() {
		setParameterAssistanceEnabled(true);
		setShowDescWindow(true);
		setAutoActivationEnabled(true);
		setAutoActivationDelay(0);
		parserToInfoMap = new HashMap<ZScriptParser, Info>();
	}


	@Override
	protected ListCellRenderer createDefaultCompletionCellRenderer() {
		return new ZScriptCellRenderer();
	}


	/**
	 * Returns the build date of this library.
	 *
	 * @return The build date, or <code>null</code> if we are not running from
	 *         a compiled jar (e.g. debugging in Eclipse).
	 */
	public static String getBuildDate() {

		String buildDate = null;

		// This file is injected by the Ant build script's compile task.
		InputStream in = ZScriptLanguageSupport.class.
				getResourceAsStream("build.date");
		if (in!=null) {
			BufferedReader r = new BufferedReader(new InputStreamReader(in));
			try {
				buildDate = r.readLine();
				r.close();
			} catch (IOException ioe) {
				ioe.printStackTrace(); // Never happens
			}
		}

		return buildDate;

	}


	public DocDisplayer getDocDisplayer() {
		return docDisplayer;
	}


	/**
	 * Returns the ZScript parser running on a text area with this ZScript
	 * language support installed.
	 *
	 * @param textArea The text area.
	 * @return The ZScript parser.  This will be <code>null</code> if the text
	 *         area does not have this <tt>ZSCriptLanguageSupport</tt>
	 *         installed.
	 */
	public ZScriptParser getParser(RSyntaxTextArea textArea) {
		// Could be a parser for another language.
		Object parser = textArea.getClientProperty(PROPERTY_LANGUAGE_PARSER);
		if (parser instanceof ZScriptParser) {
			return (ZScriptParser)parser;
		}
		return null;
	}


	public void install(RSyntaxTextArea textArea) {

		ZScriptCompletionProvider provider = new ZScriptCompletionProvider();
		// We use a custom auto-completion.
		//AutoCompletion ac = createAutoCompletion(provider);
		AutoCompletion ac = new ZScriptAutoCompletion(provider, textArea);
		ac.setListCellRenderer(getDefaultCompletionCellRenderer());
		ac.setAutoCompleteEnabled(isAutoCompleteEnabled());
		ac.setAutoActivationEnabled(isAutoActivationEnabled());
		ac.setAutoActivationDelay(getAutoActivationDelay());
		ac.setParameterAssistanceEnabled(isParameterAssistanceEnabled());
		ac.setShowDescWindow(getShowDescWindow());
//		ac.setParamChoicesRenderer(ac.getListCellRenderer());
		ac.install(textArea);
		installImpl(textArea, ac);

		textArea.setToolTipSupplier(provider);
		ZScriptScopeListener scopeListener = new ZScriptScopeListener(textArea);

		ZScriptParser parser = new ZScriptParser();
		textArea.addParser(parser);
		textArea.putClientProperty(PROPERTY_LANGUAGE_PARSER, parser);

		Info info = new Info(textArea, provider, parser, scopeListener);
		parserToInfoMap.put(parser, info);

		installKeyboardShortcuts(textArea);

		textArea.setLinkGenerator(new ZScriptLinkGenerator(this));

	}


	/**
	 * Installs extra keyboard shortcuts supported by this language support.
	 *
	 * @param textArea The text area to install the shortcuts into.
	 */
	private void installKeyboardShortcuts(RSyntaxTextArea textArea) {

		InputMap im = textArea.getInputMap();
		ActionMap am = textArea.getActionMap();
		int c = textArea.getToolkit().getMenuShortcutKeyMask();
		int shift = InputEvent.SHIFT_MASK;

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, c|shift), "GoToType");
		am.put("GoToType", new GoToMemberAction(ZScriptOutlineTree.class));

	}


	public void setDocDisplayer(DocDisplayer displayer) {
		this.docDisplayer = displayer;
	}


	public void uninstall(RSyntaxTextArea textArea) {

		uninstallImpl(textArea);

		ZScriptParser parser = getParser(textArea);
		Info info = parserToInfoMap.remove(parser);
		if (info!=null) { // Should always be true
			parser.removePropertyChangeListener(
					ZScriptParser.PROPERTY_AST, info);
			info.scopeListener.uninstall();
		}
		if (parser!=null) {
			textArea.removeParser(parser);
		}
		textArea.putClientProperty(PROPERTY_LANGUAGE_PARSER, null);

		textArea.setLinkGenerator(null);

	}


	/**
	 * Manages information about the parsing/auto-completion for a single text
	 * area.  Unlike many simpler language supports,
	 * <tt>ZScriptLanguageSupport</tt> cannot share any information amongst
	 * instances of <tt>RSyntaxTextArea</tt>.
	 */
	private static class Info implements PropertyChangeListener {

		public ZScriptCompletionProvider provider;
		public ZScriptScopeListener scopeListener;

		public Info(RSyntaxTextArea textArea, ZScriptCompletionProvider provider,
					ZScriptParser parser, ZScriptScopeListener scopeListener) {
			this.provider = provider;
			this.scopeListener = scopeListener;
			parser.addPropertyChangeListener(ZScriptParser.PROPERTY_AST, this);
		}

		/**
		 * Called when a text area is re-parsed.
		 *
		 * @param e The event.
		 */
		public void propertyChange(PropertyChangeEvent e) {

			String name = e.getPropertyName();

			if (ZScriptParser.PROPERTY_AST.equals(name)) {
				provider.setAst((ZScriptAst)e.getNewValue());
			}

		}

	}


	/**
	 * A hack of <code>AutoCompletion</code> that forces the parser to
	 * re-parse the document when the user presses Ctrl+space.
	 */
	private class ZScriptAutoCompletion extends AutoCompletion {

		private RSyntaxTextArea textArea;

		public ZScriptAutoCompletion(ZScriptCompletionProvider provider,
				RSyntaxTextArea textArea) {
			super(provider);
			this.textArea = textArea;
		}

		@Override
		protected int refreshPopupWindow() {
			// Force the parser to re-parse
			ZScriptParser parser = getParser(textArea);
			RSyntaxDocument doc = (RSyntaxDocument) textArea.getDocument();
			String style = textArea.getSyntaxEditingStyle();
			parser.parse(doc, style);
			return super.refreshPopupWindow();
		}

	}


	/**
	 * Listens for various events in a text area editing ZScript (in particular,
	 * caret events, so we can track the "active" code block).
	 */
	private class ZScriptScopeListener implements CaretListener, ActionListener {

		private RSyntaxTextArea textArea;
		private Timer t;

		public ZScriptScopeListener(RSyntaxTextArea textArea) {
			this.textArea = textArea;
			textArea.addCaretListener(this);
			t = new Timer(650, this);
			t.setRepeats(false);
		}

		public void actionPerformed(ActionEvent e) {

			ZScriptParser parser = getParser(textArea);
			if (parser==null) {
				return; // Shouldn't happen
			}
			ZScriptAst ast = parser.getAst();

			// Highlight the line range of the function being edited in the
			// gutter.
			if (ast != null) { // Should always be true
				int dot = textArea.getCaretPosition();
				BodiedNode node = ast.getDeepestBodiedNodeContaining(dot);
				if (node != null) {
					int startOffs = node.getBodyStartOffset();
					int endOffs = node.getBodyEndOffset();
					try {
						int startLine = textArea.getLineOfOffset(startOffs);
						// Unterminated blocks can end in Integer.MAX_VALUE
						endOffs = Math.min(endOffs,
								textArea.getDocument().getLength());
						int endLine = textArea.getLineOfOffset(endOffs);
						textArea.setActiveLineRange(startLine, endLine);
					} catch (BadLocationException ble) {
						ble.printStackTrace();
					}
				}
				else {
					textArea.setActiveLineRange(-1, -1);
				}
			}

		}

		public void caretUpdate(CaretEvent e) {
			t.restart();
		}

		/**
		 * Should be called whenever Java language support is removed from a
		 * text area.
		 */
		public void uninstall() {
			textArea.removeCaretListener(this);
		}

	}


}