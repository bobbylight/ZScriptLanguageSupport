/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.ast;

import javax.swing.text.Position;

import org.fife.rsta.zscript.IconFactory.IconData;


public abstract class AbstractNode implements Node {

	private int type;
	private Position start;
	private Position end;

	protected AbstractNode(int type, Position start) {
		this.type = type;
		this.start = start;
	}


	public boolean containsOffset(int offs) {
		return offs>=getStartOffset() && offs<getEndOffset();
	}


	@Override
	public int getEndOffset() {
		return end==null ? Integer.MAX_VALUE : end.getOffset();
	}


	@Override
	public abstract IconData getIcon();


	@Override
	public int getStartOffset() {
		return start.getOffset();
	}


	@Override
	public int getNodeType() {
		return type;
	}


	public String getRange() {
		return "[" + getStartOffset() + ", " + getEndOffset() + ")";
	}


	public void setEndOffset(Position end) {
		this.end = end;
	}


	@Override
	public String toString() {
		return toString(false);
	}


	@Override
	public abstract String toString(boolean colored);


}