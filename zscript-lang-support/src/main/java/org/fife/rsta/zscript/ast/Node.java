/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.ast;

import org.fife.rsta.zscript.IconFactory.IconData;


public interface Node {

	int ROOT = 0;
	int IMPORT = 1;
	int SCRIPT_DEC = 2;
	int FUNCTION_DEC = 3;
	int VARIABLE_DEC = 4;
	int CODE_BLOCK = 5;

	int STATEMENT = 1<<8;
	int STATEMENT_LOCAL_VAR	= STATEMENT|1;
	int STATEMENT_FOR		= STATEMENT|2;
	int STATEMENT_IF		= STATEMENT|3;
	int STATEMENT_ELSE		= STATEMENT|4;
	int STATEMENT_WHILE		= STATEMENT|5;
	int STATEMENT_DO_WHILE	= STATEMENT|6;


	void accept(ZScriptAstVisitor visitor);


	int getEndOffset();


	IconData getIcon();


	int getStartOffset();


	int getNodeType();


	String toString(boolean colored);


}
