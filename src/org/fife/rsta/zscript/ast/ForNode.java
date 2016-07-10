/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.ast;

import javax.swing.text.Position;

import org.fife.rsta.zscript.IconFactory.IconData;


/**
 * We don't do specific validation of the init, expression, or update
 * expressions.
 *
 * <pre>
 * ForNode  -&gt; "for" "(" Expression? ";" Expression? ";" Expression? ")" StatementNode
 * </pre>
 * 
 * @author Robert Futrell
 * @version 1.0
 */
public class ForNode extends AbstractCodeBlockStatementNode {

	//private List counterVars;
	private VariableDecNode varDec;


	public ForNode(Position start) {
		super(STATEMENT_FOR, start);
		//counterVars = new ArrayList();
	}


	@Override
	public void accept(ZScriptAstVisitor visitor) {

		boolean visitChildren = visitor.visit(this);

		if (visitChildren) {

			CodeBlock block = getCodeBlock();
			if (block!=null) {
				block.accept(visitor);
			}

		}

		visitor.postVisit(this);

	}


//	public void addCounterVariable(String var) {
//		counterVars.add(var);
//	}


//	public String getCounterVariable(int index) {
//		return (String)counterVars.get(index);
//	}
//
//
//	public int getCounterVariableCount() {
//		return counterVars.size();
//	}


	@Override
	public IconData getIcon() {
		return null;
	}


	public VariableDecNode getVariableDeclaration() {
		return varDec;
	}


	public void setVariableDeclaration(VariableDecNode varDec) {
		this.varDec = varDec;
	}


	@Override
	public String toString(boolean colored) {
		StringBuffer sb = new StringBuffer("for (");
if (varDec!=null) {
	sb.append(varDec.toString());
}
//		int count = getCounterVariableCount();
//		for (int i=0; i<count; i++) {
//			sb.append(getCounterVariable(i));
//			if (i<count-1) {
//				sb.append(", ");
//			}
//		}
		sb.append(")");
		return sb.toString();
	}


}