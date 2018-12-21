/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.ast;


public interface ZScriptAstVisitor {


	void postVisit(CodeBlock block);


	void postVisit(DoWhileNode doWhileNode);


	void postVisit(ElseNode elseNode);


	void postVisit(ForNode forNode);


	void postVisit(FunctionDecNode functionDec);


	void postVisit(IfNode ifNode);


	void postVisit(ImportNode importNode);


	void postVisit(RootNode root);


	void postVisit(ScriptNode script);


	void postVisit(VariableDecNode varDec);


	void postVisit(WhileNode whileNode);


	boolean visit(CodeBlock block);


	boolean visit(DoWhileNode doWhileNode);


	boolean visit(ElseNode elseNode);


	boolean visit(ForNode forNode);


	boolean visit(FunctionDecNode functionDec);


	boolean visit(IfNode ifNode);


	boolean visit(ImportNode importNode);


	boolean visit(RootNode root);


	boolean visit(ScriptNode script);


	boolean visit(VariableDecNode varDec);


	boolean visit(WhileNode whileNode);


}