package org.fife.rsta.zscript.ast;

import javax.swing.text.Position;


/**
 * Base class for statement nodes that contain child code blocks.
 *
 * @author Robert Futrell
 * @version 1.0
 */
abstract class AbstractCodeBlockStatementNode extends AbstractNode
		implements StatementNode, CodeBlockParent {

	private CodeBlock parentBlock;
	private CodeBlock codeBlock;


	public AbstractCodeBlockStatementNode(int type, Position start) {
		super(type, start);
	}


	public boolean bodyContainsOffset(int offs) {
		return codeBlock!=null && codeBlock.containsOffset(offs);
	}


	public int getBodyEndOffset() {
		return codeBlock!=null ? codeBlock.getEndOffset() : Integer.MAX_VALUE;
	}


	public int getBodyStartOffset() {
		return codeBlock!=null ? codeBlock.getStartOffset() : 0;
	}


	/**
	 * @return The child code block, or <code>null</code> for none.
	 */
	public CodeBlock getCodeBlock() {
		return codeBlock;
	}


	public boolean getCodeBlockContains(int offs) {
		return codeBlock==null ? false : codeBlock.containsOffset(offs);
	}


	public BodiedNode getDeepestBodiedNodeContaining(int offs) {
		if (bodyContainsOffset(offs)) { // Should always be true
			for (int i=0; i<codeBlock.getCodeBlockCount(); i++) {
				CodeBlockParent cbp = codeBlock.getChildCodeBlockParentStatement(i);
				if (cbp.bodyContainsOffset(offs)) {
					return cbp.getDeepestBodiedNodeContaining(offs);
				}
			}
			return this;
		}
		return null;
	}


	public CodeBlock getParentCodeBlock() {
		return parentBlock;
	}


	/**
	 * Some statements may have child code blocks or single child statements.
	 * In the latter case, for performance, we may  not remember the child,
	 * in which case the "code block" for this statement is <code>null</code>.
	 *
	 * @return Whether this statement has a child code block.
	 */
	public boolean hasCodeBlock() {
		return codeBlock!=null;
	}


	public void setCodeBlock(CodeBlock codeBlock) {
		this.codeBlock = codeBlock;
	}


	public void setParentCodeBlock(CodeBlock parent) {
		parentBlock = parent;
	}


}