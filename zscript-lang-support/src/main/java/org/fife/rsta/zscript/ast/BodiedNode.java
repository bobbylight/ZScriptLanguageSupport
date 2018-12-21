/*
 * 08/24/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.ast;


/**
 * A node with a "body;" that is, a code block or other content in curly
 * braces.  Thus, the range of offsets inside the body is likely a subset of
 * the entire range of offsets this node covers.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public interface BodiedNode extends Node {


	/**
	 * Returns whether the body of this node contains a specific offset.
	 *
	 * @param offs The offset to check.
	 * @return Whether the body of this node contains the offset.
	 */
	boolean bodyContainsOffset(int offs);


	int getBodyEndOffset();


	int getBodyStartOffset();


	/**
	 * Returns the deepest bodied node containing an offset.  This method
	 * should be refactored and live elsewhere.
	 */
	BodiedNode getDeepestBodiedNodeContaining(int offs);


}
