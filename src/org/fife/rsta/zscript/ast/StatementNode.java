/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.ast;


/**
 * A statement is a snippet of code in a <code>CodeBlock</code>.
 *
 * <pre>
 * StatementNode -&gt; {@link IfNode}
 *               |  {@link DoWhileNode}
 *               |  {@link ForNode}
 *               |  {@link WhileNode}
 *               |  StatementWithoutTrailingSubstatement
 * </pre>
 * 
 * @author Robert Futrell
 * @version 1.0
 * @see CodeBlock
 */
public interface StatementNode extends Node {


	CodeBlock getParentCodeBlock();


	void setParentCodeBlock(CodeBlock parent);


}