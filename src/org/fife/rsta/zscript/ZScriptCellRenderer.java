/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript;

import javax.swing.Icon;
import javax.swing.JList;

import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionCellRenderer;
import org.fife.ui.autocomplete.EmptyIcon;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.TemplateCompletion;
import org.fife.ui.autocomplete.VariableCompletion;


/**
 * The cell renderer for ZScript.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ZScriptCellRenderer extends CompletionCellRenderer {

	private Icon emptyIcon;


	/**
	 * Constructor.
	 */
	public ZScriptCellRenderer() {
		emptyIcon = new EmptyIcon(16); // Should be done first
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void prepareForFunctionCompletion(JList list,
			FunctionCompletion fc, int index, boolean selected,
			boolean hasFocus) {
		super.prepareForFunctionCompletion(list, fc, index, selected, hasFocus);
		Icon icon = fc.getIcon();
		setIcon(icon!=null ? icon : emptyIcon);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void prepareForOtherCompletion(JList list,
			Completion c, int index, boolean selected, boolean hasFocus) {
		super.prepareForOtherCompletion(list, c, index, selected, hasFocus);
		setIcon(emptyIcon);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void prepareForTemplateCompletion(JList list,
			TemplateCompletion tc, int index, boolean selected,
			boolean hasFocus) {
		super.prepareForTemplateCompletion(list, tc, index, selected, hasFocus);
		setIcon(IconFactory.get().getIcon(IconFactory.TEMPLATE_ICON));
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void prepareForVariableCompletion(JList list,
			VariableCompletion vc, int index, boolean selected,
			boolean hasFocus) {
		super.prepareForVariableCompletion(list, vc, index, selected, hasFocus);
		Icon icon = vc.getIcon();
		setIcon(icon!=null ? icon : emptyIcon);
	}


}