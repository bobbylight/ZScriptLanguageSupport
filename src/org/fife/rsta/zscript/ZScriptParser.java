/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript;

import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.event.SwingPropertyChangeSupport;

import org.fife.rsta.zscript.ast.ZScriptAst;
import org.fife.rsta.zscript.ast.ZScriptParseResult;
import org.fife.rsta.zscript.ast.AstFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;


/**
 * Creates a very basic AST from a ZScript file.  This parser cannot be
 * shared across multiple editors.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ZScriptParser extends AbstractParser {

	/**
	 * Property fired when an editor is re-parsed.  The new value will be the
	 * new {@link ZScriptAst}.
	 */
	public static final String PROPERTY_AST = "org.fife.rsta.zscript.AST";

	private ZScriptAst ast;
	private SwingPropertyChangeSupport support;


	public ZScriptParser() {
		support = new SwingPropertyChangeSupport(this);
	}


	public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
		support.addPropertyChangeListener(property, listener);
	}


	/**
	 * Returns the most recent AST generated from source.
	 *
	 * @return The most recent AST.
	 */
	public ZScriptAst getAst() {
		return ast;
	}


	public ParseResult parse(RSyntaxDocument doc, String style) {
		ZScriptAst old = ast;
		AstFactory parser2 = new AstFactory(doc, this, null);
		ZScriptParseResult zspr = parser2.parse();
		DefaultParseResult result = new DefaultParseResult(this);
		ast = zspr.getAst();
		List<ParserNotice> notices = zspr.getNotices();
		for (ParserNotice notice : notices) {
			//System.out.println(">>> " + notice);
			result.addNotice(notice);
		}
		support.firePropertyChange(PROPERTY_AST, old, ast);
//System.out.println("----------");
//ast.getRootNode().accept(new org.fife.rsta.zscript.ast.AstPrinter());
//System.out.println("----------");
		return result;
	}


	public void removePropertyChangeListener(String property, PropertyChangeListener listener) {
		support.removePropertyChangeListener(property, listener);
	}


}