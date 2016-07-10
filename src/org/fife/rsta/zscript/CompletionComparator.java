/*
 * 03/12/2014
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript;

import java.util.Comparator;

import org.fife.ui.autocomplete.Completion;


/**
 * Compares two {@link Completion}s by a string representation, ignoring case.
 * The default implementation compares by input text, but this can be
 * overridden by passing a custom <code>CompletionStringer</code> into the
 * constructor.
 *
 * @author Robert Futrell
 * @version 1.0
 */
// TODO: Move this into AutoComplete library.
public class CompletionComparator implements Comparator<Completion> {

	private CompletionStringer stringer;


	public CompletionComparator() {
		this(new InputTextCompletionStringer());
	}


	/**
	 * Constructor.
	 *
	 * @param stringer Used to stringify the completions to compare.
	 */
	public CompletionComparator(CompletionStringer stringer) {
		this.stringer = stringer;
	}


	/**
	 * Compares two completions lexicographically.
	 *
	 * @param c1 The first <code>Completion</code>  Guaranteed to not be
	 *        <code>null</code>.
	 * @param c2 The second <code>Completion</code>  Guaranteed to not be
	 *        <code>null</code>.
	 */
	@Override
	public int compare(Completion c1, Completion c2) {
		String s1 = c1==null ? "" : stringer.getCompareValue(c1);
		String s2 = c2==null ? "" : stringer.getCompareValue(c2);
		return String.CASE_INSENSITIVE_ORDER.compare(s1, s2);
	}


	/**
	 * An interface that stringifies <code>Completion</code>s for instances
	 * of {@link CompletionComparator}.
	 */
	public static interface CompletionStringer {

		public String getCompareValue(Completion c);

	}


	/**
	 * A {@link CompletionStringer} implementation that returns the
	 * completion's input text.
	 */
	public static class InputTextCompletionStringer
			implements CompletionStringer {

		@Override
		public String getCompareValue(Completion c) {
			return c.getInputText();
		}

	}


}