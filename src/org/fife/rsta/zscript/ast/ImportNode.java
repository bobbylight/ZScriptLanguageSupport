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


public class ImportNode extends AbstractNode {

	private String importedFile;


	public ImportNode(Position start) {
		super(IMPORT, start);
	}


	@Override
	public void accept(ZScriptAstVisitor visitor) {
		visitor.visit(this);
	}


	@Override
	public IconData getIcon() {
		return new IconData(IconFactory.IMPORT_ICON, false);
	}


	public String getImport() {
		return importedFile;
	}


	public void setImport(String importedFile) {
		this.importedFile = importedFile;
	}


	@Override
	public String toString(boolean colored) {
		return importedFile;
	}


}