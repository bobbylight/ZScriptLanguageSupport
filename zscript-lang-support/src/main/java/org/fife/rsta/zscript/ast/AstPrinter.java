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


	@Override
	public void postVisit(CodeBlock block) {
	}


	@Override
	public void postVisit(DoWhileNode doWhileNode) {
		decreaseIndent();
		System.out.println(indentLevel + "} while ();");
	}


	@Override
	public void postVisit(ElseNode elseNode) {
		blockStatementEnd();
	}


	@Override
	public void postVisit(ForNode forNode) {
		blockStatementEnd();
	}


	@Override
	public void postVisit(IfNode ifNode) {
		blockStatementEnd();
	}


	@Override
	public void postVisit(FunctionDecNode functionDec) {
		blockStatementEnd();
	}


	@Override
	public void postVisit(ImportNode importNode) {
	}


	@Override
	public void postVisit(RootNode root) {
	}


	@Override
	public void postVisit(ScriptNode script) {
		blockStatementEnd();
	}


	@Override
	public void postVisit(VariableDecNode varDec) {
	}


	@Override
	public void postVisit(WhileNode whileNode) {
		blockStatementEnd();
	}


	@Override
	public boolean visit(CodeBlock block) {
		return true;
	}


	@Override
	public boolean visit(DoWhileNode doWhileNode) {
		return blockStatementBegin(doWhileNode.toString());
	}


	@Override
	public boolean visit(ElseNode elseNode) {
		blockStatementEnd();
		return blockStatementBegin(elseNode.toString());
	}


	@Override
	public boolean visit(ForNode forNode) {
		return blockStatementBegin(forNode.toString());
	}


	@Override
	public boolean visit(FunctionDecNode functionDec) {

		if (firstMember) {
			System.out.println();
			firstMember = false;
		}

		StringBuilder sb = new StringBuilder(functionDec.getType());
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


	@Override
	public boolean visit(IfNode ifNode) {
		return blockStatementBegin(ifNode.toString());
	}


	@Override
	public boolean visit(ImportNode importNode) {
		System.out.println("import \"" + importNode.getImport() + "\"");
		return true;
	}


	@Override
	public boolean visit(RootNode root) {
		return true;
	}


	@Override
	public boolean visit(ScriptNode script) {

		if (firstMember) {
			System.out.println();
			firstMember = false;
		}

        System.out.println(indentLevel + script.getType() + " script " + script.getName() + " {");
		increaseIndent();

		return true;

	}


	@Override
	public boolean visit(VariableDecNode varDec) {

		if (firstMember) {
			System.out.println();
			firstMember = false;
		}

		StringBuilder sb = new StringBuilder();
		if (varDec.isConstant()) {
			sb.append("const ");
		}
		sb.append(varDec.getType());
		sb.append(' ').append(varDec.getName()).append(';');

		System.out.println(indentLevel + sb.toString());
		return true;

	}


	@Override
	public boolean visit(WhileNode whileNode) {
		return blockStatementBegin(whileNode.toString());
	}


}
