/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.demo;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.plaf.FileChooserUI;
import javax.swing.plaf.basic.BasicFileChooserUI;

public class FileChooser extends JFileChooser {


	@Override
	public void setCurrentDirectory(File dir) {
		super.setCurrentDirectory(dir);
		// WindowsFileChooserUI unfortunately doesn't clear file name field!
		FileChooserUI ui = getUI();
		if (ui instanceof BasicFileChooserUI) {
			((BasicFileChooserUI)ui).setFileName(null);
		}
	}


}