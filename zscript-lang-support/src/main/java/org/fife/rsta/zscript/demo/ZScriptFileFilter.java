/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.demo;

import java.io.File;

import javax.swing.filechooser.FileFilter;


public class ZScriptFileFilter extends FileFilter {


	@Override
	public boolean accept(File f) {
		return f.isDirectory() || hasZScriptExtension(f);
	}


	@Override
	public String getDescription() {
		return "ZScript Files (*.z, *.zh)";
	}


	private boolean hasZScriptExtension(File file) {
		String name = file.getName();
		return name.endsWith(".z") || name.endsWith(".zh");
	}


}