/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.ast;

import java.util.ArrayList;
import java.util.List;


/**
 * Generates a list of all local variables in scope at an offset.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class VariablesInScopeGrabber implements ZScriptAstVisitor {

	private List<VariableDecNode> varList;
	private List<FunctionDecNode> functionList;
	private int offs;
	
	private static final boolean DEBUG = false;


	public VariablesInScopeGrabber(int offs) {
		varList = new ArrayList<VariableDecNode>();
		functionList = new ArrayList<FunctionDecNode>();
		this.offs = offs;
	}


	private void addFunctionsFromFuncContainer(FunctionContainer fc) {
		if (fc.getStartOffset()<offs) {
			for (int i=0; i<fc.getFunctionCount(); i++) {
				FunctionDecNode funcDec = fc.getFunction(i);
				//if (funcDec.getEndOffset()<offs) {
					functionList.add(funcDec);
				//}
				//else {
				//	return;
				//}
			}
		}
	}


	private void addVariablesFromVarContainer(VariableContainer vc, boolean addAll) {
		if (vc.getStartOffset()<offs) {
			for (int i=0; i<vc.getVariableCount(); i++) {
				VariableDecNode varDec = vc.getVariableDec(i);
				if (addAll || varDec.getEndOffset()<offs) {
					if (!varList.contains(varDec)) {
						varList.add(varDec);
					}
				}
				else {
					return;
				}
			}
		}
	}


	public List<FunctionDecNode> getFunctionList() {
		return functionList;
	}


	public List<VariableDecNode> getVariableList() {
		return varList;
	}


	public void postVisit(CodeBlock block) {}


	public void postVisit(DoWhileNode doWhileNode) {}


	public void postVisit(ElseNode elseNode) {}


	public void postVisit(ForNode forNode) {}


	public void postVisit(FunctionDecNode functionDec) {}


	public void postVisit(IfNode ifNode) {}


	public void postVisit(ImportNode importNode) {}


	public void postVisit(RootNode root) {
		// Add these last, so they don't shadow function-scoped vars, etc.
		addFunctionsFromFuncContainer(root);
		addVariablesFromVarContainer(root, true);
	}


	public void postVisit(ScriptNode script) {
		// Add these last, so they don't shadow function-scoped vars, etc.
		if (script.bodyContainsOffset(offs)) {
			addVariablesFromVarContainer(script, true);
			addFunctionsFromFuncContainer(script);
		}
	}


	public void postVisit(VariableDecNode varDec) {}


	public void postVisit(WhileNode whileNode) {}


	public boolean visit(CodeBlock block) {
		return false;
	}


	public boolean visit(DoWhileNode doWhileNode) {
		return false;
	}


	public boolean visit(ElseNode elseNode) {
		return false;
	}


	public boolean visit(ForNode forNode) {
		return false;
	}


	public boolean visit(IfNode ifNode) {
		return false;
	}


	public boolean visit(FunctionDecNode functionDec) {

		if (functionDec.bodyContainsOffset(offs)) {

			CodeBlock cb = functionDec.getCodeBlock();
			cb = cb.getDeepestCodeBlockContaining(offs);
			if (DEBUG) {
				System.out.println("DEBUG: In function: " + functionDec);
				System.out.println("DEBUG: ... deepst CB: " + cb);
				System.out.println("DEBUG: ... ... offs: " + offs + ", cb range: " + cb.getRange());
			}

			while (cb!=null) {
				addVariablesFromVarContainer(cb, false);
				CodeBlockParent parent = cb.getParent();
				// TODO: Clean this up!
				if (parent instanceof StatementNode) {
					cb = ((AbstractCodeBlockStatementNode)parent).getParentCodeBlock();
				}
				else {
					cb = null;
				}
			}

			// Function arguments last
			for (int i=0; i<functionDec.getArgumentCount(); i++) {
				VariableDecNode arg = functionDec.getArgument(i);
				if (!varList.contains(arg)) {
					varList.add(arg);
				}
			}

		}

		return false;

	}


	public boolean visit(ImportNode importNode) {
		return false;
	}


	public boolean visit(RootNode root) {
		return true;
	}


	public boolean visit(ScriptNode script) {
		return true;
	}


	public boolean visit(VariableDecNode varDec) {
		return false;
	}


	public boolean visit(WhileNode whileNode) {
		return false;
	}


}