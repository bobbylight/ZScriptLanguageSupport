/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.ast;

import javax.swing.text.Position;

import org.fife.rsta.zscript.IconFactory;
import org.fife.rsta.zscript.IconFactory.IconData;


/**
 * A variable declaration.  Note that the parent <code>CodeBlock</code> may
 * be <code>null</code>, as this class is also used to denote function
 * arguments.
 *
 * <pre>
 * VarDecNode          -&gt; Type VariableDeclarator
 * Type                -&gt; "int" | "float" | "bool" | "void" | "eweapon" | "lweapon"
 * VariableDeclarator  -&gt; Identifier Dim* "=" VariableInitializer
 * Dim                 -&gt; ( "[" Integer "]" )
 * VariableInitializer -&gt; Expression | ArrayInitializer
 * ArrayInitializer    -&gt; "{" VariableInitializer ( "," VariableInitializer )* "}"
 * </pre>
 *
 * TODO: Keep and evaluate VariableInitializer (at least if it's an ArrayInitializer).
 *
 * @author Robert Futrell
 * @version 1.0
 *
 */
public class VariableDecNode extends MemberNode implements StatementNode {

	private CodeBlock parentBlock;
	private boolean constant;


	public VariableDecNode(Position start) {
		super(VARIABLE_DEC, start);
	}


	@Override
	public void accept(ZScriptAstVisitor visitor) {
		visitor.visit(this);
	}


	public int compareTo(Object obj) {
		if (obj instanceof VariableDecNode) {
			return getName().compareTo(((VariableDecNode)obj).getName());
		}
		return -1;
	}


	@Override
	public boolean equals(Object obj) {
		return compareTo(obj)==0;
	}


	@Override
	public IconData getIcon() {
		return new IconData(IconFactory.FIELD_PUBLIC_ICON, constant);
	}


	@Override
	public CodeBlock getParentCodeBlock() {
		return parentBlock;
	}


	@Override
	public int hashCode() {
		return getName().hashCode();
	}


	public void incTypeArrayDepth() {
		setType(getType() + "[]");
	}


	public boolean isConstant() {
		return constant;
	}


	public void setConstant(boolean constant) {
		this.constant = constant;
	}


	@Override
	public void setParentCodeBlock(CodeBlock parent) {
		parentBlock = parent;
	}


	@Override
	public String toString(boolean colored) {

		StringBuilder sb = new StringBuilder();
		if (colored) {
			sb.append("<html>");
		}

		sb.append(getName()).append(" : ");
		if (colored) {
			sb.append("<font color='#808080'>");
		}
		sb.append(getType());

		return sb.toString();

	}


}
