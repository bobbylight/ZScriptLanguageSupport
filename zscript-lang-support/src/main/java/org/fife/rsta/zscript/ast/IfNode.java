package org.fife.rsta.zscript.ast;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Position;

import org.fife.rsta.zscript.IconFactory.IconData;


/**
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class IfNode extends AbstractCodeBlockStatementNode {

	private List<ElseNode> elseNodes;


	public IfNode(Position start) {
		super(STATEMENT_IF, start);
	}


	@Override
	public void accept(ZScriptAstVisitor visitor) {

		boolean visitChildren = visitor.visit(this);

		if (visitChildren) {

			CodeBlock block = getCodeBlock();
			if (block!=null) {
				block.accept(visitor);
			}

			for (int i=0; i<getElseNodeCount(); i++) {
				getElseNode(i).accept(visitor);
			}

		}

		visitor.postVisit(this);

	}


	public void addElse(ElseNode elseNode) {
		if (elseNodes==null) {
			elseNodes = new ArrayList<>();
		}
		elseNodes.add(elseNode);
	}


	public ElseNode getElseNode(int index) {
		return elseNodes.get(index);
	}


	public int getElseNodeCount() {
		return elseNodes.size();
	}


	@Override
	public IconData getIcon() {
		return null;
	}


	@Override
	public String toString(boolean colored) {
		return "if ()";
	}


}
