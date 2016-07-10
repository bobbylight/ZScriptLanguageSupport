package org.fife.rsta.zscript.ast;

import javax.swing.text.Position;

import org.fife.rsta.zscript.IconFactory.IconData;


/**
 * 
 * @author Robert Futrell
 * @version 1.0
 */
public class DoWhileNode extends AbstractCodeBlockStatementNode {


	public DoWhileNode(Position start) {
		super(STATEMENT_DO_WHILE, start);
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


	@Override
	public String toString(boolean colored) {
		return "do";
	}


}