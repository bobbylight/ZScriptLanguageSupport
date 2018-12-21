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


public class RootNode extends AbstractNode implements MemberContainer {

	private List<ImportNode> imports;
	private List<MemberNode> allMembers;
	private List<VariableDecNode> vars;
	private List<FunctionDecNode> functions;
	private List<ScriptNode> scripts;


	public RootNode(Position start) {
		super(ROOT, start);
		imports = new ArrayList<>();
		allMembers = new ArrayList<>();
		vars = new ArrayList<>();
		functions = new ArrayList<>();
		scripts = new ArrayList<>();
	}


	@Override
	public void accept(ZScriptAstVisitor visitor) {

		boolean visitChildren = visitor.visit(this);

		if (visitChildren) {

			for (int i=0; i<getImportCount(); i++) {
				getImport(i).accept(visitor);
			}

			List<MemberNode> sortedMembers = getAllMembers();
			for (MemberNode member : sortedMembers) {
				member.accept(visitor);
			}

		}

		visitor.postVisit(this);

	}


	@Override
	public void addFunctionDec(FunctionDecNode function) {
		functions.add(function);
		allMembers.add(function);
	}


	public void addImport(ImportNode importNode) {
		imports.add(importNode);
	}


	public void addScript(ScriptNode script) {
		scripts.add(script);
		allMembers.add(script);
	}


	@Override
	public ShadowedVarInfo addVariableDec(VariableDecNode variable) {
		VariableDecNode prev = getVariableDecByName(variable.getName());
		vars.add(variable);
		allMembers.add(variable);
		return prev==null ? null : new ShadowedVarInfo(prev);
	}


	public List<MemberNode> getAllMembers() {
		return allMembers;
	}


	@Override
	public FunctionDecNode getFunction(int index) {
		return functions.get(index);
	}


	@Override
	public int getFunctionCount() {
		return functions.size();
	}


	@Override
	public IconData getIcon() {
		return null;
	}


	public ImportNode getImport(int index) {
		return imports.get(index);
	}


	public int getImportCount() {
		return imports.size();
	}


	public ScriptNode getScript(int index) {
		return scripts.get(index);
	}


	public int getScriptCount() {
		return scripts.size();
	}


	@Override
	public int getVariableCount() {
		return vars.size();
	}


	@Override
	public VariableDecNode getVariableDec(int index) {
		return vars.get(index);
	}


	@Override
	public VariableDecNode getVariableDecByName(String name) {
		for (int i=0; i<getVariableCount(); i++) {
			VariableDecNode var = getVariableDec(i);
			if (name.equals(var.getName())) {
				return var;
			}
		}
		return null;
	}


	@Override
	public String toString(boolean colored) {
		return "Root!";
	}


}
