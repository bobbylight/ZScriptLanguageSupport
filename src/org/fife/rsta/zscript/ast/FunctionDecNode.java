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

import org.fife.rsta.zscript.IconFactory;
import org.fife.rsta.zscript.IconFactory.IconData;


/**
 * A function declaration.  Its "variables" are copies of all variables in
 * child blocks.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FunctionDecNode extends MemberNode implements CodeBlockParent {

	private List args;
	private CodeBlock codeBlock;


	public FunctionDecNode(Position start) {
		super(FUNCTION_DEC, start);
		args = new ArrayList();
	}


	public void accept(ZScriptAstVisitor visitor) {

		boolean visitChildren = visitor.visit(this);

		if (visitChildren && codeBlock!=null) {
			codeBlock.accept(visitor);
		}

		visitor.postVisit(this);

	}


	public void addArgument(VariableDecNode argNode) {
		args.add(argNode);
	}


	public boolean bodyContainsOffset(int offs) {
		return codeBlock!=null && codeBlock.containsOffset(offs);
	}


	public VariableDecNode getArgument(int index) {
		return (VariableDecNode)args.get(index);
	}


	public VariableDecNode getArgumentByName(String name) {
		for (int i=0; i<getArgumentCount(); i++) {
			VariableDecNode arg = getArgument(i);
			if (name.equals(arg.getName())) {
				return arg;
			}
		}
		return null;
	}


	public int getArgumentCount() {
		return args.size();
	}


	public List getArguments() {
		return args;
	}


	public int getBodyEndOffset() {
		return codeBlock!=null ? codeBlock.getEndOffset() : Integer.MAX_VALUE;
	}


	public int getBodyStartOffset() {
		return codeBlock!=null ? codeBlock.getStartOffset() : 0;
	}


	public CodeBlock getCodeBlock() {
		return codeBlock;
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


	@Override
	public IconData getIcon() {
		return new IconData(IconFactory.METHOD_PUBLIC_ICON, false);
	}


	public void setCodeBlock(CodeBlock block) {
		this.codeBlock = block;
	}


	@Override
	public String toString(boolean colored) {

		StringBuffer sb = new StringBuffer();
		if (colored) {
			sb.append("<html>");
		}

		sb.append(getName()).append('(');
		for (int i=0; i<getArgumentCount(); i++) {
			VariableDecNode arg = getArgument(i);
			sb.append(arg.getType());
			if (i<getArgumentCount()-1) {
				sb.append(", ");
			}
		}
		sb.append(") : ");
		if (colored) {
			sb.append("<font color='#808080'>");
		}
		sb.append(getType());

		return sb.toString();

	}

/*
	public static class FunctionArg {

		private String type;
		private String name;

		public FunctionArg(String type, String name) {
			this.type = type;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public String getType() {
			return type;
		}

	}
*/

}