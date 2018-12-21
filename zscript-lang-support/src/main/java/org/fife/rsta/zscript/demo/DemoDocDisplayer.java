/*
 * 02/17/2013
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.demo;

import java.awt.Frame;
import java.io.BufferedReader;
import java.io.IOException;
import javax.swing.JDialog;
import javax.swing.UIManager;

import org.fife.rsta.zscript.DocDisplayer;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;


/**
 * Displays documentation for tokens Ctrl+clicked by the user in a popup
 * window.  Real applications would probably open such documentation in a new
 * tab.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class DemoDocDisplayer implements DocDisplayer {


	@Override
	public void display(String title, BufferedReader r, String toFocus) {

		RSyntaxTextArea textArea = new RSyntaxTextArea(40, 80);
		try {
			textArea.read(r, null);
			r.close();
			textArea.setCaretPosition(0);
		} catch (IOException ioe) {
			UIManager.getLookAndFeel().provideErrorFeedback(null);
			ioe.printStackTrace();
			return;
		}
		textArea.setEditable(false);
		textArea.setSyntaxEditingStyle("text/zscript");
		textArea.setCodeFoldingEnabled(true);

		Frame owner = Frame.getFrames()[0];
		JDialog dialog = new JDialog(owner);
		dialog.setContentPane(new RTextScrollPane(textArea));
		dialog.setTitle(title + " (read-only)");
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);

		SearchContext context = new SearchContext(toFocus, true);
		SearchEngine.find(textArea, context);

	}


}