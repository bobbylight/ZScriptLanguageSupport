/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.ast;


public interface VariableContainer {


	ShadowedVarInfo addVariableDec(VariableDecNode variable);


	int getStartOffset();


	int getVariableCount();


	VariableDecNode getVariableDec(int index);


	VariableDecNode getVariableDecByName(String name);


}