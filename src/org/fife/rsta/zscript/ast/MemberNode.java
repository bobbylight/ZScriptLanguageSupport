/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.ast;

import javax.swing.text.Position;


/**
 * A "member" of a ZScript file is a variable, function, or script.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public abstract class MemberNode extends AbstractNode {

	private String name;
	private String type;


	protected MemberNode(int type, Position start) {
		super(type, start);
	}


	public String getName() {
		return name;
	}


	public String getType() {
		return type;
	}


	public void setName(String name) {
		this.name = name;
	}


	public void setType(String type) {
		this.type = type;
	}


}