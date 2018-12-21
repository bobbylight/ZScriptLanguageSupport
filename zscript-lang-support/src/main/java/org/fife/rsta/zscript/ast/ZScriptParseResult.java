/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.ast;

import java.util.ArrayList;
import java.util.List;

import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.Parser;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;


public class ZScriptParseResult {

	private Parser parser;
	private ZScriptAst ast;
	private List<ParserNotice> notices;


	public ZScriptParseResult(Parser parser) {
		this.parser = parser;
		notices = new ArrayList<>();
	}


	public void addNotice(String msg, int line) {
		notices.add(new DefaultParserNotice(parser, msg, line));
	}


	public void addNotice(Token t, String msg) {
		addNotice(t, msg, ParserNotice.Level.ERROR);
	}


	public void addNotice(Token t, String msg, ParserNotice.Level level) {
		DefaultParserNotice notice = new DefaultParserNotice(parser, msg,
				t.getLine(), t.getOffset(), t.getLength());
		notice.setLevel(level);
		notices.add(notice);
	}


	public List<ParserNotice> getNotices() {
		return notices;
	}


	public ZScriptAst getAst() {
		return ast;
	}


	public void setAst(ZScriptAst ast) {
		this.ast = ast;
	}


}
