/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.ast;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Position;

import org.fife.rsta.zscript.IconFactory.IconData;


/**
 * A code block contains a list of statements.  It also caches variables
 * and statements containing child code blocks for performance reasons.
 *
 * <pre>
 * CodeBlock          -&gt; "{" BlockStatement* "}"
 * BlockStatement     -&gt; VariableDecNode
 *                    |  {@link StatementNode}
 * </pre>
 * 
 * @author Robert Futrell
 * @version 1.0
 */
public class CodeBlock extends AbstractNode implements VariableContainer,
		StatementParent {

	private CodeBlockParent parent;
	private List<AbstractCodeBlockStatementNode> childBlockParentStatements;
	private List<VariableDecNode> vars;
	private List<StatementNode> statements;


	public CodeBlock(CodeBlockParent parent, Position start) {
		super(CODE_BLOCK, start);
		this.parent = parent;
		childBlockParentStatements = new ArrayList<AbstractCodeBlockStatementNode>();
		vars = new ArrayList<VariableDecNode>();
		statements = new ArrayList<StatementNode>();
	}


	public void accept(ZScriptAstVisitor visitor) {

		boolean visitChildren = visitor.visit(this);

		if (visitChildren) {
			for (int i=0; i<getStatementCount(); i++) {
				getStatement(i).accept(visitor);
			}
		}

		visitor.postVisit(this);

	}


	public void addStatement(StatementNode statement) {
		statements.add(statement);
		statement.setParentCodeBlock(this);
	}


	public ShadowedVarInfo addVariableDec(VariableDecNode variable) {

		ShadowedVarInfo info = null;
		String varName = variable.getName();

		// Check for a shadowed variable
		VariableDecNode prevVar = getVariableDecByName(varName);
		if (prevVar!=null) {
			info = new ShadowedVarInfo(prevVar);
		}

		vars.add(variable);

		return info;

	}


	/**
	 * Optimization; cache what statements have child code blocks for fast
	 * location of variables in scope, etc.
	 */
	public void cacheStatementsWithChildBlocks() {
		for (int i=0; i<getStatementCount(); i++) {
			StatementNode statement = getStatement(i);
			if (statement instanceof AbstractCodeBlockStatementNode) {
				AbstractCodeBlockStatementNode acbsn =
						(AbstractCodeBlockStatementNode)statement;
				if (acbsn.hasCodeBlock()) {
					childBlockParentStatements.add(acbsn);
				}
			}
		}
	}


	public CodeBlock getChildCodeBlock(int index) {
		return getChildCodeBlockParentStatement(index).getCodeBlock();
	}


	public CodeBlockParent getChildCodeBlockParentStatement(int index) {
		return childBlockParentStatements.get(index);
	}


	/**
	 * @return The number of child code blocks in this code block.
	 */
	public int getCodeBlockCount() {
		return childBlockParentStatements.size();
	}


	/**
	 * Returns the deepest child code block containing a specific offset.  If
	 * no child code blocks exist, or none contain the offset, this code block
	 * itself is returned.<p>
	 * It is assumed that the caller has already verified that the offset is
	 * indeed contained in this code block.
	 *
	 * @param offs The offset.
	 * @return The deepest code block containing the offset.
	 */
	public CodeBlock getDeepestCodeBlockContaining(int offs) {
		if (!containsOffset(offs)) {
			return null;
		}
		for (int i=0; i<getCodeBlockCount(); i++) {
			CodeBlock child = getChildCodeBlock(i);
			if (child.containsOffset(offs)) {
				return child.getDeepestCodeBlockContaining(offs);
			}
		}
		return this;
	}


	@Override
	public IconData getIcon() {
		return null;
	}


	public CodeBlockParent getParent() {
		return parent;
	}


	public StatementNode getStatement(int index) {
		return statements.get(index);
	}


	public int getStatementCount() {
		return statements.size();
	}


	public int getVariableCount() {
		return vars.size();
	}


	public VariableDecNode getVariableDec(int index) {
		return vars.get(index);
	}


	public VariableDecNode getVariableDecByName(String name) {
		for (int i=0; i<getVariableCount(); i++) {
			VariableDecNode var = getVariableDec(i);
			if (name.equals(var.getName())) {
				return var;
			}
		}
		if (parent instanceof VariableContainer) {
			return ((VariableContainer)parent).getVariableDecByName(name);
		}
		else if (parent instanceof StatementNode) {
			return ((StatementNode)parent).getParentCodeBlock().getVariableDecByName(name);
		}
		else if (parent instanceof FunctionDecNode) {
			FunctionDecNode func = (FunctionDecNode)parent;
			VariableDecNode arg = func.getArgumentByName(name);
			if (arg!=null) {
				return arg;
			}
		}

		// Don't check script-level args above FunctionDecNodes, as this
		// method is used to check for variable shadowing, and we can shadow
		// global variables without a warning.

		return null;
	}


	@Override
	public String toString(boolean colored) {
		return "[CodeBlock: " +
				"statementCount: " + getStatementCount() +
				", childBlockCount: " + getCodeBlockCount() +
				", vars: " + vars +
				"]";
	}


}