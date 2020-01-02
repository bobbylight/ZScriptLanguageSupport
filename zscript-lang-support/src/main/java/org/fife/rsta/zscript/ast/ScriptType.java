/*
 * 01/30/2016
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.ast;


/**
 * The type-qualifier of a script.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public enum ScriptType {

	GLOBAL,
	FFC,
	ITEM;


	/**
	 * Returns the script type for a corresponding script-type token.
	 *
	 * @param tokenType The script-type token's type.
	 * @return The script type.  This will be <code>null</code> if the
	 *         specified token type is invalid for a script type.
	 */
	public static ScriptType forTokenType(int tokenType) {

		ScriptType scriptType = null;

		switch (tokenType) {
			case TokenTypes.DATA_TYPE_FFC:
				scriptType = ScriptType.FFC;
				break;
			case TokenTypes.DATA_TYPE_ITEM:
				scriptType = ScriptType.ITEM;
				break;
			case TokenTypes.KEYWORD_GLOBAL:
				scriptType = ScriptType.GLOBAL;
				break;
		}

		return scriptType;
	}


}
