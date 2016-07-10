/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.tree;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Stack;
import javax.swing.BorderFactory;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.fife.rsta.ac.AbstractSourceTree;
import org.fife.rsta.ac.LanguageSupport;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.zscript.IconFactory;
import org.fife.rsta.zscript.ZScriptLanguageSupport;
import org.fife.rsta.zscript.ZScriptParser;
import org.fife.rsta.zscript.ast.CodeBlock;
import org.fife.rsta.zscript.ast.DoWhileNode;
import org.fife.rsta.zscript.ast.ElseNode;
import org.fife.rsta.zscript.ast.ForNode;
import org.fife.rsta.zscript.ast.FunctionDecNode;
import org.fife.rsta.zscript.ast.AbstractNode;
import org.fife.rsta.zscript.ast.IfNode;
import org.fife.rsta.zscript.ast.RootNode;
import org.fife.rsta.zscript.ast.ScriptNode;
import org.fife.rsta.zscript.ast.VariableDecNode;
import org.fife.rsta.zscript.ast.WhileNode;
import org.fife.rsta.zscript.ast.ZScriptAst;
import org.fife.rsta.zscript.ast.ImportNode;
import org.fife.rsta.zscript.ast.ZScriptAstVisitor;
import org.fife.ui.rsyntaxtextarea.DocumentRange;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;


/**
 * A tree view showing the outline of ZScript source, similar to the "Outline"
 * view in the Eclipse JDT.
 * 
 * @author Robert Futrell
 * @version 1.0
 */
public class ZScriptOutlineTree extends AbstractSourceTree {

	private DefaultTreeModel model;
	private RSyntaxTextArea textArea;
	private ZScriptParser parser;
	private Listener listener;

	private static final int SORT_PRIORITY_IMPORTS		= 0;
	private static final int SORT_PRIORITY_SCRIPT		= 1;
	private static final int SORT_PRIORITY_FUNCTION		= 2;
	private static final int SORT_PRIORITY_VARIABLE		= 3;


	/**
	 * Constructor.  The tree created will not have its elements sorted
	 * alphabetically.
	 */
	public ZScriptOutlineTree() {
		this(false);
	}


	/**
	 * Constructor.
	 *
	 * @param sorted Whether the tree should sort its elements alphabetically.
	 *        Note that outline trees will likely group nodes by type before
	 *        sorting (i.e. methods will be sorted in one group, fields in
	 *        another group, etc.).
	 */
	public ZScriptOutlineTree(boolean sorted) {
		setSorted(sorted);
		setBorder(BorderFactory.createEmptyBorder(0,8,0,8));
		setRootVisible(false);
//		setCellRenderer(new AstTreeCellRenderer());
		model = new DefaultTreeModel(new DefaultMutableTreeNode("Nothing"));
		setModel(model);
		listener = new Listener();
		addTreeSelectionListener(listener);
	}


	/**
	 * Refreshes this tree.
	 *
	 * @param ast The parsed compilation unit.  If this is <code>null</code>
	 *        then the tree is cleared.
	 */
	private void update(ZScriptAst ast) {

		ZScriptTreeNode root = new ZScriptTreeNode("Remove me!",
									IconFactory.SOURCE_FILE_ICON, false);
		root.setSortable(true);
		if (ast==null) {
			model.setRoot(root);
			return;
		}

		NodeGenerator generator = new NodeGenerator(root);
		generator.generate(ast);

		model.setRoot(root);
		root.setSorted(isSorted());
		refresh();

	}


	/**
	 * Refreshes listeners on the text area when its syntax style changes.
	 */
	private void checkForZScriptParsing() {

		// Remove possible listener on old Java parser (in case they're just
		// changing syntax style AWAY from Java)
		if (parser!=null) {
			parser.removePropertyChangeListener(
					ZScriptParser.PROPERTY_AST, listener);
			parser = null;
		}

		// Get the Java language support (shared by all RSTA instances editing
		// Java that were registered with the LanguageSupportFactory).
		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		LanguageSupport support = lsf.getSupportFor("text/zscript");
		ZScriptLanguageSupport zls = (ZScriptLanguageSupport)support;

		// Listen for re-parsing of the editor, and update the tree accordingly
		parser = zls.getParser(textArea);
		if (parser!=null) { // Should always be true
			parser.addPropertyChangeListener(
					ZScriptParser.PROPERTY_AST, listener);
			// Populate with any already-existing AST.
			update(parser.getAst());
		}
		else {
			update((ZScriptAst)null); // Clear the tree
		}

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void expandInitialNodes() {
		fastExpandAll(new TreePath(getModel().getRoot()), true);
	}


	private void gotoElementAtPath(TreePath path) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.
													getLastPathComponent();
		Object obj = node.getUserObject();
		if (obj instanceof AbstractNode) {
			AbstractNode astNode = (AbstractNode)obj;
			int start = astNode.getStartOffset();
			int end = astNode.getEndOffset();
			DocumentRange range = new DocumentRange(start, end);
			RSyntaxUtilities.selectAndPossiblyCenter(textArea, range, true);
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean gotoSelectedElement() {
		TreePath path = getLeadSelectionPath();//e.getNewLeadSelectionPath();
		if (path != null) {
			gotoElementAtPath(path);
			return true;
		}
		return false;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void listenTo(RSyntaxTextArea textArea) {

		if (this.textArea!=null) {
			uninstall();
		}

		// Nothing new to listen to
		if (textArea==null) {
			return;
		}

		// Listen for future language changes in the text editor
		this.textArea = textArea;
		textArea.addPropertyChangeListener(
							RSyntaxTextArea.SYNTAX_STYLE_PROPERTY, listener);

		// Check whether we're currently editing Java
		checkForZScriptParsing();

	}


	/**
	 *{@inheritDoc}
	 */
	@Override
	public void uninstall() {

		if (parser!=null) {
			parser.removePropertyChangeListener(
					ZScriptParser.PROPERTY_AST, listener);
			parser = null;
		}

		if (textArea!=null) {
			textArea.removePropertyChangeListener(
					RSyntaxTextArea.SYNTAX_STYLE_PROPERTY, listener);
			textArea = null;
		}

	}


	/**
	 * Overridden to also update the UI of the child cell renderer.
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		// DefaultTreeCellRenderer caches colors, so we can't just call
		// ((JComponent)getCellRenderer()).updateUI()...
		setCellRenderer(new AstTreeCellRenderer());
	}


	/**
	 * Listens for events this tree is interested in (events in the associated
	 * editor, for example), as well as events in this tree.
	 */
	private class Listener implements PropertyChangeListener,
							TreeSelectionListener {

		/**
		 * Called whenever the text area's syntax style changes, as well as
		 * when it is re-parsed.
		 */
		@Override
		public void propertyChange(PropertyChangeEvent e) {

			String name = e.getPropertyName();

			// If the text area is changing the syntax style it is editing
			if (RSyntaxTextArea.SYNTAX_STYLE_PROPERTY.equals(name)) {
				checkForZScriptParsing();
			}

			else if (ZScriptParser.PROPERTY_AST.equals(name)) {
				update((ZScriptAst)e.getNewValue());
			}

		}

		/**
		 * Selects the corresponding element in the text editor when a user
		 * clicks on a node in this tree.
		 */
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			if (getGotoSelectedElementOnClick()) {
				//gotoSelectedElement();
				TreePath newPath = e.getNewLeadSelectionPath();
				if (newPath!=null) {
					gotoElementAtPath(newPath);
				}
			}
		}

	}


	private class NodeGenerator implements ZScriptAstVisitor {

		private ZScriptTreeNode treeRoot;
		private ZScriptTreeNode importRoot;
		private Stack<ZScriptTreeNode> functionContainers;
		private Stack<ZScriptTreeNode> variableContainers;

		public NodeGenerator(ZScriptTreeNode treeRoot) {
			this.treeRoot = treeRoot;
			functionContainers = new Stack<ZScriptTreeNode>();
			variableContainers = new Stack<ZScriptTreeNode>();
		}

		public void generate(ZScriptAst ast) {
			ast.getRootNode().accept(this);
		}

		public ZScriptTreeNode getFunctionContainer() {
			return functionContainers.peek();
		}

		public ZScriptTreeNode getVariableContainer() {
			return variableContainers.peek();
		}

		@Override
		public void postVisit(CodeBlock block) {}

		@Override
		public void postVisit(DoWhileNode doWhileNode) {}

		@Override
		public void postVisit(ElseNode elseNode) {}

		@Override
		public void postVisit(ForNode forNode) {}

		@Override
		public void postVisit(IfNode ifNode) {}

		@Override
		public void postVisit(FunctionDecNode functionDec) {
			popAndVerify("variable", variableContainers, functionDec);
		}

		@Override
		public void postVisit(ImportNode importNode) {}

		@Override
		public void postVisit(RootNode root) {}

		@Override
		public void postVisit(ScriptNode script) {
			popAndVerify("function", functionContainers, script);
			popAndVerify("variable", variableContainers, script);
		}

		@Override
		public void postVisit(VariableDecNode varDec) {}

		@Override
		public void postVisit(WhileNode whileNode) {}

		private void popAndVerify(String type, Stack<ZScriptTreeNode> stack, Object top) {
			ZScriptTreeNode node = stack.pop();
			if (node.getNode()!=top) {
				throw new InternalError(type + " stack: " + top + " not on top!");
			}
		}

		@Override
		public boolean visit(CodeBlock block) {
			return true;
		}

		@Override
		public boolean visit(DoWhileNode doWhileNode) {
			return false;
		}

		@Override
		public boolean visit(ElseNode elseNode) {
			return false;
		}

		@Override
		public boolean visit(ForNode forNode) {
			return false;
		}

		@Override
		public boolean visit(FunctionDecNode functionDec) {
			ZScriptTreeNode funcNode = new ZScriptTreeNode(functionDec);
			funcNode.setSortPriority(SORT_PRIORITY_FUNCTION);
			getFunctionContainer().add(funcNode);
			variableContainers.push(funcNode);
			return true;
		}

		@Override
		public boolean visit(IfNode ifNode) {
			return false;
		}

		@Override
		public boolean visit(ImportNode importNode) {
			if (!getShowMajorElementsOnly()) {
				importRoot.add(new ZScriptTreeNode(importNode));
			}
			return false;
		}

		@Override
		public boolean visit(RootNode root) {
			if (!getShowMajorElementsOnly()) {
				importRoot = new ZScriptTreeNode("Imports",
									IconFactory.IMPORT_ROOT_ICON, false);
				importRoot.setSortPriority(SORT_PRIORITY_IMPORTS);
				treeRoot.add(importRoot);
			}
			functionContainers.push(treeRoot);
			variableContainers.push(treeRoot);
			return true;
		}

		@Override
		public boolean visit(ScriptNode script) {
			ZScriptTreeNode scriptNode = new ZScriptTreeNode(script);
			scriptNode.setSortPriority(SORT_PRIORITY_SCRIPT);
			treeRoot.add(scriptNode);
			functionContainers.push(scriptNode);
			variableContainers.push(scriptNode);
			return true;
		}

		@Override
		public boolean visit(VariableDecNode varDec) {
			ZScriptTreeNode varNode = new ZScriptTreeNode(varDec);
			varNode.setSortPriority(SORT_PRIORITY_VARIABLE);
			getVariableContainer().add(varNode);
			return false;
		}

		@Override
		public boolean visit(WhileNode whileNode) {
			return false;
		}

	}


}