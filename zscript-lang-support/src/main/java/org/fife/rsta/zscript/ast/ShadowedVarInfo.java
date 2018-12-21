/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.ast;


public class ShadowedVarInfo {

	private String type;
	private String name;


	public ShadowedVarInfo(VariableDecNode shadowedVar) {
		this.type = shadowedVar.getType();
		this.name = shadowedVar.getName();
	}


	public String getName() {
		return name;
	}


	public String getType() {
		return type;
	}


}