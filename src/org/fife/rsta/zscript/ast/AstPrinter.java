/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.ast;

import java.util.List;


public class AstPrinter implements ZScriptAstVisitor {

	private String indentLevel;
	private boolean firstMember;


	public AstPrinter() {
		indentLevel = "";
		firstMember = true;
	}


	private boolean blockStatementBegin(String text) {
		System.out.println(indentLevel + text + " {");
		increaseIndent();
		return false;
	}


	private void blockStatementEnd() {
		decreaseIndent();
		System.out.println(indentLevel + "}");
	}


	private void decreaseIndent() {
		indentLevel = indentLevel.substring(0, Math.max(indentLevel.length()-3, 0));
	}


	private void increaseIndent() {
		indentLevel += "   ";
	}


	public void postVisit(CodeBlock block) {}


	public void postVisit(DoWhileNode doWhileNode) {
		decreaseIndent();
		System.out.println(indentLevel + "} while ();");
	}


	public void postVisit(ElseNode elseNode) {
		blockStatementEnd();
	}


	public void postVisit(ForNode forNode) {
		blockStatementEnd();
	}


	public void postVisit(IfNode ifNode) {
		blockStatementEnd();
	}


	public void postVisit(FunctionDecNode functionDec) {
		blockStatementEnd();
	}


	public void postVisit(ImportNode importNode) {}


	public void postVisit(RootNode root) {}


	public void postVisit(ScriptNode script) {
		blockStatementEnd();
	}


	public void postVisit(VariableDecNode varDec) {}


	public void postVisit(WhileNode whileNode) {
		blockStatementEnd();
	}


	public boolean visit(CodeBlock block) {
		return true;
	}


	public boolean visit(DoWhileNode doWhileNode) {
		return blockStatementBegin(doWhileNode.toString());
	}


	public boolean visit(ElseNode elseNode) {
		blockStatementEnd();
		return blockStatementBegin(elseNode.toString()); 
	}


	public boolean visit(ForNode forNode) {
		return blockStatementBegin(forNode.toString());
	}


	public boolean visit(FunctionDecNode functionDec) {

		if (firstMember) {
			System.out.println();
			firstMember = false;
		}

		StringBuffer sb = new StringBuffer(functionDec.getType());
		sb.append(' ').append(functionDec.getName()).append('(');
		List<VariableDecNode> args = functionDec.getArguments();
		for (int i=0; i<args.size(); i++) {
			VariableDecNode arg = args.get(i);
			sb.append(arg.getType()).append(' ').append(arg.getName());
			if (i<args.size()-1) {
				sb.append(", ");
			}
		}
		sb.append(") {");

		System.out.println(indentLevel + sb.toString());
		increaseIndent();
		return true;

	}


	public boolean visit(IfNode ifNode) {
		return blockStatementBegin(ifNode.toString());
	}


	public boolean visit(ImportNode importNode) {
		System.out.println("import \"" + importNode.getImport() + "\"");
		return true;
	}


	public boolean visit(RootNode root) {
		return true;
	}


	public boolean visit(ScriptNode script) {

		if (firstMember) {
			System.out.println();
			firstMember = false;
		}

		StringBuffer sb = new StringBuffer(script.getType());
		sb.append(" script ").append(script.getName()).append(" {");

		System.out.println(indentLevel + sb.toString());
		increaseIndent();

		return true;

	}


	public boolean visit(VariableDecNode varDec) {

		if (firstMember) {
			System.out.println();
			firstMember = false;
		}

		StringBuffer sb = new StringBuffer();
		if (varDec.isConstant()) {
			sb.append("const ");
		}
		sb.append(varDec.getType());
		sb.append(' ').append(varDec.getName()).append(';');

		System.out.println(indentLevel + sb.toString());
		return true;

	}


	public boolean visit(WhileNode whileNode) {
		return blockStatementBegin(whileNode.toString());
	}


}