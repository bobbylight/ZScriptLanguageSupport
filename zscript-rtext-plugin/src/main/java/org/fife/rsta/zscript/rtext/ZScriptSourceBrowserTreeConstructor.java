/*
 * 08/19/2012
 *
 * ZScriptSourceBrowserTreeConstructor.java - Uses an intelligent source
 * browser tree for ZScript.
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.rtext;

import javax.swing.JTree;

import org.fife.rsta.zscript.tree.ZScriptOutlineTree;
import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;


/**
 * Constructs the source browser tree for ZScript files.
 * 
 * @author Robert Futrell
 * @version 1.0
 */
public class ZScriptSourceBrowserTreeConstructor {


	public JTree constructSourceBrowserTree(RText rtext) {
		RTextEditorPane textArea = rtext.getMainView().getCurrentTextArea();
		ZScriptOutlineTree tree = new ZScriptOutlineTree();
		tree.listenTo(textArea);
		return tree;
	}


}