package org.fife.rsta.zscript.ast;

import javax.swing.text.Position;

import org.fife.rsta.zscript.IconFactory.IconData;


/**
 * 
 * @author Robert Futrell
 * @version 1.0
 */
public class ElseNode extends AbstractCodeBlockStatementNode {

	private IfNode parentIf;
	//private Expression conditionStatement;
	private boolean conditional;


	public ElseNode(Position start, IfNode parentIf) {
		super(STATEMENT_ELSE, start);
		this.parentIf = parentIf;
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


	@Override
	public IconData getIcon() {
		return null;
	}


	public IfNode getParentIfNode() {
		return parentIf;
	}


	public boolean isConditional() {
		return conditional;
	}


	public void setConditional(boolean conditional) {
		this.conditional = conditional;
	}


	@Override
	public String toString(boolean colored) {
		return "else";
	}


}