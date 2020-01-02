/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript;

import java.util.List;

import org.fife.rsta.ac.ShorthandCompletionCache;
import org.fife.rsta.zscript.ast.ZScriptAst;
import org.fife.ui.autocomplete.AbstractCompletionProvider;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.LanguageAwareCompletionProvider;


/**
 * Completion provider for ZScript files.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ZScriptCompletionProvider extends LanguageAwareCompletionProvider {


	/**
	 * Constructor.
	 */
	public ZScriptCompletionProvider() {
		CodeCompletionProvider codeProvider = createCodeCompletionProvider();
		AbstractCompletionProvider commentsProvider =
				createCommentCompletionProvider();
		ShorthandCompletionCache cache = new ZScriptShorthandCompletionCache(
				codeProvider, commentsProvider);
		setDefaultCompletionProvider(createCodeCompletionProvider());
		setStringCompletionProvider(createStringCompletionProvider());
		setCommentCompletionProvider(commentsProvider);
		setShorthandCompletionCache(cache);
	}


	private CodeCompletionProvider createCodeCompletionProvider() {
		return new CodeCompletionProvider(this);
	}


	/**
	 * Returns the provider to use when in a comment.
	 *
	 * @return The provider.
	 * @see #createCodeCompletionProvider()
	 * @see #createStringCompletionProvider()
	 */
	private DefaultCompletionProvider createCommentCompletionProvider() {
		return new DefaultCompletionProvider();
	}


	private CompletionProvider createStringCompletionProvider() {
		return new DefaultCompletionProvider();
	}


	private CodeCompletionProvider getCodeCompletionProvider() {
		return (CodeCompletionProvider)getDefaultCompletionProvider();
	}


	public void setAst(ZScriptAst ast) {
		getCodeCompletionProvider().setAst(ast);
	}


	/**
	 * Set short hand completion cache (template and comment completions).
	 */
	public void setShorthandCompletionCache(ShorthandCompletionCache cache) {

		getCodeCompletionProvider().setShorthandCache(cache);

		//reset comment completions too
		DefaultCompletionProvider dcp = (DefaultCompletionProvider)
				getCommentCompletionProvider();
		List<Completion> commentCompletions = cache.getCommentCompletions();
		for (Completion completion : commentCompletions) {
			dcp.addCompletion(completion);
		}

	}


}
