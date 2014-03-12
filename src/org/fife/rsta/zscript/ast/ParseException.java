/*
 * 08/20/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.ast;

import java.io.IOException;


/**
 * An exception thrown when an unexpected token is found.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ParseException extends IOException {

	private Token token;


	/**
	 * Constructor.
	 *
	 * @param token The unexpected token.  This should not be
	 *        <code>null</code>.
	 * @param msg The error message.
	 */
	public ParseException(Token token, String msg) {
		super(msg);
		this.token = token;
	}


	/**
	 * Returns the token that was unexpected.
	 *
	 * @return The token that was unexpected.  This should never be
	 *         <code>null</code>.
	 */
	public Token getToken() {
		return token;
	}


}