/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.text.JTextComponent;

import org.fife.rsta.ac.LanguageSupport;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.zscript.ast.RootNode;
import org.fife.rsta.zscript.ast.VariableDecNode;
import org.fife.rsta.zscript.ast.VariablesInScopeGrabber;
import org.fife.rsta.zscript.ast.ZScriptAst;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.ParameterChoicesProvider;
import org.fife.ui.autocomplete.ParameterizedCompletion.Parameter;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;


/**
 * Offers completion choices for method and global function parameters.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class SourceParamChoicesProvider implements ParameterChoicesProvider {

	private CompletionProvider provider;

	private static final Map constantValueMap;


	public SourceParamChoicesProvider(CompletionProvider provider) {
		this.provider = provider;
	}


	public List getParameterChoices(JTextComponent tc, Parameter param) {

		String type = param.getType();
		if (type==null) { // e.g. a template
			return null;
		}
		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		LanguageSupport support = lsf.getSupportFor("text/zscript");
		ZScriptLanguageSupport zsls = (ZScriptLanguageSupport)support;

		RSyntaxTextArea textArea = (RSyntaxTextArea)tc;
		ZScriptParser parser = zsls.getParser(textArea);
		if (parser==null) {
			return null;
		}
		ZScriptAst ast = parser.getAst();
		if (ast==null) {
			return null;
		}
		RootNode root = ast.getRootNode();

		List choices = new ArrayList();

		int dot = textArea.getCaretPosition();
		VariablesInScopeGrabber grabber = new VariablesInScopeGrabber(dot);
		root.accept(grabber);
		List vars = grabber.getVariableList();
		for (Iterator i=vars.iterator(); i.hasNext(); ) {
			VariableDecNode varDec = (VariableDecNode)i.next();
			if (type.equals(varDec.getType())) {
				choices.add(new ZScriptVariableCompletion(provider, varDec));
			}
		}

		List constants = (List)constantValueMap.get(type);
		if (constants!=null) {
			choices.addAll(constants);
		}

		return choices;

	}


	/**
	 * Creates a low-relevance constant value completion.
	 *
	 * @param value The value of the completion.
	 * @return The completion.
	 */
	private static final Completion createConstantCompletion(String value) {
		BasicCompletion c = new BasicCompletion(null, value);
		c.setRelevance(-1);
		return c;
	}


	/**
	 * Creates completions for commonly-used constant values for various types.
	 */
	static {

		constantValueMap = new HashMap();

		List completions = new ArrayList();
		Completion ZERO = createConstantCompletion("0");
		completions.add(ZERO);
		constantValueMap.put("int", completions);
		constantValueMap.put("float", completions);

		completions = new ArrayList();
		completions.add(createConstantCompletion("false")); 
		completions.add(createConstantCompletion("true"));
		constantValueMap.put("bool", completions);

	}


}