/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.ast;


public interface CodeBlockParent extends BodiedNode {


	CodeBlock getCodeBlock();


	void setCodeBlock(CodeBlock block);


}