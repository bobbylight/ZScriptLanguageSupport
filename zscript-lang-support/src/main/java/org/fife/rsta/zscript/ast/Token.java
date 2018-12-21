/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.ast;


/**
 * A token in a ZScript file.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class Token implements TokenTypes {

	private int type;

	/**
	 * The token's text.
	 */
	private String lexeme;

	/**
	 * The line the token is on.
	 */
	private int line;

	/**
	 * The column the token is on.
	 */
	private int column;

	/**
	 * The absolute offset into the source of the token.
	 */
	private int offset;

	/**
	 * Whether the token is invalid (e.g. an invalid char of String).
	 */
	private boolean invalid;


	public Token(int type, String lexeme, int line, int column, int offs) {
		this(type, lexeme, line, column, offs, false);
	}


	public Token(int type, String lexeme, int line, int column, int offs,
					boolean invalid) {
		this.type = type;
		this.lexeme = lexeme;
		this.line = line;
		this.column = column;
		this.offset = offs;
		this.invalid = invalid;
	}


	@Override
	public boolean equals(Object obj) {
		if (obj==this) {
			return true;
		}
		if (obj instanceof Token) {
			Token t2 = (Token)obj;
			return type==t2.getType() && lexeme.equals(t2.getLexeme()) &&
					line==t2.getLine() && column==t2.getColumn() &&
					invalid==t2.isInvalid();
		}
		return false;
	}


	public int getColumn() {
		return column;
	}


	public int getEndOffset() {
		return getOffset() + getLength() - 1;
	}


	public int getLength() {
		return lexeme.length();
	}


	public String getLexeme() {
		return lexeme;
	}


	public int getLine() {
		return line;
	}


	public int getOffset() {
		return offset;
	}


	public int getType() {
		return type;
	}


	@Override
	public int hashCode() {
		return lexeme.hashCode();
	}


	public boolean isBasicType() {
		return (getType()&DATA_TYPE)>0;
	}


	public boolean isIdentifier() {
		return (getType()&IDENTIFIER)>0;
	}


	public boolean isInvalid() {
		return invalid;
	}


	public boolean isOperator() {
		return (getType()&OPERATOR)>0;
	}


	public boolean isType(int type) {
		return this.type==type;
	}


	@Override
	public String toString() {
		return "[Token: " +
			"type=" + type +
			"; lexeme=\"" + lexeme + "\"" +
			"; line=" + getLine() +
			"; col=" + getColumn() +
			"; offs=" + getOffset() +
			"; invalid=" + isInvalid() +
			"]";
	}


}