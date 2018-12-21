/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.ast;

import org.fife.rsta.zscript.IconFactory.IconData;


public interface Node {

	public static final int ROOT = 0;
	public static final int IMPORT = 1;
	public static final int SCRIPT_DEC = 2;
	public static final int FUNCTION_DEC = 3;
	public static final int VARIABLE_DEC = 4;
	public static final int CODE_BLOCK = 5;

	public static final int STATEMENT = 1<<8;
	public static final int STATEMENT_LOCAL_VAR	= STATEMENT|1;
	public static final int STATEMENT_FOR		= STATEMENT|2;
	public static final int STATEMENT_IF		= STATEMENT|3;
	public static final int STATEMENT_ELSE		= STATEMENT|4;
	public static final int STATEMENT_WHILE		= STATEMENT|5;
	public static final int STATEMENT_DO_WHILE	= STATEMENT|6;


	void accept(ZScriptAstVisitor visitor);


	int getEndOffset();


	IconData getIcon();


	int getStartOffset();


	int getNodeType();


	String toString(boolean colored);


}