/*
 * 08/11/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript;

import java.util.ResourceBundle;

import org.fife.rsta.ac.ShorthandCompletionCache;
import org.fife.ui.autocomplete.AbstractCompletionProvider;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.TemplateCompletion;


/**
 * A cache of basic template and comment completions for ZScript.
 *
 * @author Robert Futrell
 */
class ZScriptShorthandCompletionCache extends ShorthandCompletionCache {

	private static final String MSG_BUNDLE = "org.fife.rsta.zscript.resources";
	private static final ResourceBundle MSG = ResourceBundle.getBundle(MSG_BUNDLE);


	ZScriptShorthandCompletionCache(AbstractCompletionProvider
			templateProvider, AbstractCompletionProvider commentsProvider) {

		super(templateProvider, commentsProvider);
		String template;

		template = "for (int ${i} = 0; ${i} < SizeOfArray(${array}); ${i}++) {\n\t${cursor}\n}";
		addShorthandCompletion(new TemplateCompletion(templateProvider, "for", "for-loop-array", template,
				MSG.getString("for.array.shortDesc"), MSG.getString("for.array.summary")));

        template = "for (int ${i} = 0; ${i} < ${10:int}; ${i}++) {\n\t${cursor}\n}";
        addShorthandCompletion(new TemplateCompletion(templateProvider, "for", "for-loop",
                template, MSG.getString("for.loop.shortDesc"), MSG.getString("for.loop.summary")));

        template = "if (${condition}) {\n\t${cursor}\n}";
        addShorthandCompletion(new TemplateCompletion(templateProvider, "if", "if-cond",
                template, MSG.getString("if.cond.shortDesc"), MSG.getString("if.cond.summary")));

        template = "if (${condition}) {\n\t${cursor}\n}\nelse {\n\t\n}";
        addShorthandCompletion(new TemplateCompletion(templateProvider, "if", "if-else",
                template, MSG.getString("if.else.shortDesc"), MSG.getString("if.else.summary")));

        template = "do {\n\t${cursor}\n} while (${condition});";
		addShorthandCompletion(new TemplateCompletion(templateProvider, "do", "do-loop", template,
				MSG.getString("do.shortDesc"), MSG.getString("do.summary")));

        template = "while (${condition}) {\n\t${cursor}\n}";
        addShorthandCompletion(new TemplateCompletion(templateProvider, "while", "while-cond",
                template, MSG.getString("while.shortDesc"), MSG.getString("while.summary")));

		/* Comments */
        addCommentCompletion(new BasicCompletion(commentsProvider, "TODO:", null, MSG.getString("todo")));
        addCommentCompletion(new BasicCompletion(commentsProvider, "FIXME:", null, MSG.getString("fixme")));

	}


}
