package org.fife.rsta.zscript.ast;

import javax.swing.text.Position;

import org.fife.rsta.zscript.IconFactory.IconData;


/**
 * A "while" statement.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class WhileNode extends AbstractCodeBlockStatementNode {


	public WhileNode(Position start) {
		super(STATEMENT_WHILE, start);
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
		return "while ()";
	}


}
