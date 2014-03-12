/*
 * 02/17/2013
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.BadLocationException;

import org.fife.rsta.ac.LanguageSupport;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.zscript.ast.VariableDecNode;
import org.fife.rsta.zscript.ast.ZScriptAst;
import org.fife.ui.rsyntaxtextarea.LinkGenerator;
import org.fife.ui.rsyntaxtextarea.LinkGeneratorResult;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
import org.fife.ui.rsyntaxtextarea.SelectRegionLinkGeneratorResult;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenImpl;
import org.fife.ui.rsyntaxtextarea.TokenTypes;


/**
 * Identifies tokens in the document that should become clickable links via
 * Ctrl/Cmd+clicks by the mouse.  When the user clicks on one of these links,
 * they are taken to the declaration of that member (variable, etc.).<p>
 * 
 * Currently this only identifies local
 * variables.  In the future it could also identify scripts, as well as provide
 * links from global variables such as Link, Script, etc., into their
 * respective members.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ZScriptLinkGenerator implements LinkGenerator {

	private ZScriptLanguageSupport zls;

	private static final char[] ARROW = "->".toCharArray();
	private static final char[] EWEAPON = "eweapon".toCharArray();
	private static final char[] FFC = "ffc".toCharArray();
	private static final char[] ITEM = "item".toCharArray();
	private static final char[] ITEMDATA = "itemdata".toCharArray();
	private static final char[] LWEAPON = "lweapon".toCharArray();
	private static final char[] NPC = "npc".toCharArray();


	ZScriptLinkGenerator(ZScriptLanguageSupport zls) {
		this.zls = zls;
	}


	/**
	 * Checks if the token at the specified offset is possibly a "click-able"
	 * region.
	 *
	 * @param textArea The text area.
	 * @param offs The offset, presumably at the mouse position.
	 * @return A result object.
	 */
	private Token checkForVarDereference(RSyntaxTextArea textArea, int offs) {

		if (offs>=0) {

			RSyntaxDocument doc = (RSyntaxDocument)textArea.getDocument();

			try {

				int line = textArea.getLineOfOffset(offs);
				Token first = textArea.getTokenListForLine(line);
				Token prev = null;
				Token prev2 = null;

				for (Token t=first; t!=null && t.isPaintable(); t=t.getNextToken()) {
					if (t.containsPosition(offs)) {
						if (prev==null) {
							prev = RSyntaxUtilities.getPreviousImportantToken(
									doc, line-1);
						}
						break;
					}
					else if (!t.isCommentOrWhitespace()) {
						prev2 = prev;
						prev = t;
					}
				}

				// Sigh, if only Tokens could be a doubly-linked list...
				// Scan back again to get what variable is dereferenced.
				if (prev!=null && prev.is(Token.OPERATOR, ARROW)) {

					// Common case - user had it all on one line, e.g.
					// "Link->PressA"
					if (prev2!=null) {
						System.out.println("Common case!");
						return prev2;
					}

					// Uncommon case - user had the "->" on one line and e.g.
					// "PressA" on another line.
					int arrowOffs = prev.getOffset();
					line = textArea.getLineOfOffset(arrowOffs);
					first = textArea.getTokenListForLine(line);
					prev = null;

					for (Token t=first; t!=null && t.isPaintable(); t=t.getNextToken()) {
						if (t.getOffset()==arrowOffs) {
							if (prev==null) {
								prev = RSyntaxUtilities.getPreviousImportantToken(
										doc, line-1);
							}
							if (prev!=null) {
								return prev;
							}
						}
						else if (!t.isCommentOrWhitespace()) {
							prev = t;
						}
					}

				}

			} catch (BadLocationException ble) {
				ble.printStackTrace(); // Never happens
			}

		}

		return null;

	}


	private LinkGeneratorResult handleDereferenceLink(Token parent, Token main,
			RSyntaxTextArea textArea, int offs) {
//		return new SelectRegionLinkGeneratorResult(textArea,
//				main.offset, 1, 2);
// TODO: Implement me right!  Need actual property definition to be able to
// look it up in the doc
String searchFor = null;//main.getLexeme();
if (isGlobalVariable(parent.getLexeme())) {
	searchFor = "namespace " + parent.getLexeme();
}
else if ("lweapon".equalsIgnoreCase(searchFor) || "eweapon".equalsIgnoreCase(searchFor)) {
	searchFor = null;//"Weapon Functions and Variables";
}
return new OpenBuiltInStuffLinkGeneratorResult(textArea, main.getOffset(), searchFor);
	}


	private static final boolean isGlobalVariable(String text) {
		return "Link".equals(text) || "Game".equals(text) || "Screen".equals(text);
	}


	/**
	 * {@inheritDoc}
	 */
	public LinkGeneratorResult isLinkAtOffset(RSyntaxTextArea textArea, int offs) {

		Token t = textArea.modelToToken(offs);
		if (t==null) {
			return null;
		}

		if (t.isIdentifier()) {

			// First, check for variable dereferences (e.g.
			// "eweapon e; e->SomeField = ...;").  This also catches
			// "multi-line" global dereference stuff (uncommon), such as
			// "Link->" on one line and "PressA" on the next line.
			Token main = new TokenImpl(t);
			Token parent = checkForVarDereference(textArea, offs);
			if (parent!=null) {
				return handleDereferenceLink(parent, main, textArea, offs);
			}

			// Then just check for a local variable 
			String varName = main.getLexeme();
			ZScriptParser parser = zls.getParser(textArea);
			ZScriptAst ast = parser.getAst();
			if (ast!=null) {
				VariableDecNode varDec = ZScriptUtils.
					getVariableDeclaration(varName, textArea, ast, offs);
				if (varDec!=null) {
					int start = varDec.getStartOffset();
					int end = varDec.getEndOffset();
					return new SelectRegionLinkGeneratorResult(textArea,
							main.getOffset(), start, end);
				}
			}

		}

		// Global functions and variables
		else if (t.getType()==TokenTypes.FUNCTION || t.getType()==TokenTypes.VARIABLE) {

			// Global variable dereferences, such as "Screen->NumLWeapons()".
			// We must clone our original token due to RSTA's token pooling!
			Token main = new TokenImpl(t);
			Token parent = checkForVarDereference(textArea, offs);
			if (parent!=null) {
				return handleDereferenceLink(parent, main, textArea, offs);
			}

			// Just a plain old global function or variable
			String res = "/data/unmodified/std_" +
				(main.getType()==TokenTypes.FUNCTION ? "functions.zh" : "constants.zh");
			return new OpenResourceLinkGeneratorResult(textArea, main.getOffset(), res,
					main.getLexeme());
		}

		// Complex data types link to information about their members.
		else if (t.getType()==TokenTypes.DATA_TYPE) {
			String searchFor = null;
			if (t.is(LWEAPON) || t.is(EWEAPON)) {
				searchFor = "Weapon Functions and Variables";
			}
			else if (t.is(FFC)) {
				searchFor = "FFC Functions and Variables";
			}
			else if (t.is(ITEM)) {
				searchFor = "Item Functions and Variables";
			}
			else if (t.is(ITEMDATA)) {
				searchFor = "Itemdata Functions and Variables";
			}
			else if (t.is(NPC)) {
				searchFor = "NPC Functions and Variables";
			}
			if (searchFor!=null) {
				return new OpenBuiltInStuffLinkGeneratorResult(textArea, t.getOffset(), searchFor);
			}
		}

		// Game, Link, and Screen link to doc about their members
		else if (t.getType()==TokenTypes.RESERVED_WORD) {
			String lexeme = t.getLexeme();
			if (isGlobalVariable(lexeme)) {
				return new OpenBuiltInStuffLinkGeneratorResult(textArea, t.getOffset(), "namespace " + lexeme);
			}
		}

		return null;

	}


	/**
	 * Opens links to stuff in zscript.txt.  This can probably be merged with
	 * the other LinkGeneratorResult class below, but first I need to figure
	 * out a way to determine what the target file should be based on what
	 * was clicked.  For example, a "Rand" function is defined in both
	 * zscript.txt (a built-in function) and in std_functions.zh.
	 */
	private static class OpenBuiltInStuffLinkGeneratorResult
		implements LinkGeneratorResult {

		private RSyntaxTextArea textArea;
		private int sourceOffs;
		private String res;
		private String searchFor;

		public OpenBuiltInStuffLinkGeneratorResult(RSyntaxTextArea textArea,
				int sourceOffs, String searchFor) {
			this.textArea = textArea;
			this.sourceOffs = sourceOffs;
			this.res = "/data/unmodified/zscript.txt";
			this.searchFor = searchFor;
		}

		public HyperlinkEvent execute() {
			InputStream in = getClass().getResourceAsStream(res);
			BufferedReader r = new BufferedReader(new InputStreamReader(in));
			
			LanguageSupport ls = LanguageSupportFactory.get().getSupportFor("text/zscript");
			ZScriptLanguageSupport zsls = (ZScriptLanguageSupport)ls;
			DocDisplayer docDisplayer = zsls.getDocDisplayer();
			if (docDisplayer!=null) {
				String title = res.substring(res.lastIndexOf('/')+1);
				docDisplayer.display(title, r, searchFor);
			}
			else {
				UIManager.getLookAndFeel().provideErrorFeedback(textArea);
			}

			return null;
		}

		public int getSourceOffset() {
			return sourceOffs;
		}

	}


	/**
	 * This result isn't used until I can figure out a way to determine the
	 * appropriate target resource for what was clicked.  Probably need to
	 * examine the argument count of functions.
	 */
	private static class OpenResourceLinkGeneratorResult
			implements LinkGeneratorResult {

//		private RSyntaxTextArea textArea;
		private int sourceOffs;
//		private String res;
//		private String token;

		public OpenResourceLinkGeneratorResult(RSyntaxTextArea textArea,
				int sourceOffs, String res, String token) {
//			this.textArea = textArea;
//			this.sourceOffs = sourceOffs;
//			this.res = res;
//			this.token = token;
		}

		public HyperlinkEvent execute() {
// TODO: Determine whether we can open a *.zh file (std_*.zh, string.zh), or have
// to fall back onto .txt description (zscript.txt).
//			InputStream in = getClass().getResourceAsStream(res);
//			BufferedReader r = new BufferedReader(new InputStreamReader(in));
//			try {
//
//				textArea.read(r, null);
//				r.close();
//
//				textArea.setCaretPosition(0);
//				if (token!=null) {
//					SearchContext context = new SearchContext(token, true);
//					SearchEngine.find(textArea, context);
//				}
//
//			} catch (IOException ioe) {
//				UIManager.getLookAndFeel().provideErrorFeedback(null);
//			}
			return null;
		}

		public int getSourceOffset() {
			return sourceOffs;
		}

	}


}