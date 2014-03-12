/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript;

import java.awt.Point;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Segment;

import org.fife.rsta.ac.ShorthandCompletionCache;
import org.fife.rsta.zscript.ast.FunctionDecNode;
import org.fife.rsta.zscript.ast.RootNode;
import org.fife.rsta.zscript.ast.VariableDecNode;
import org.fife.rsta.zscript.ast.VariablesInScopeGrabber;
import org.fife.rsta.zscript.ast.ZScriptAst;
import org.fife.ui.autocomplete.AbstractCompletionProvider;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.Util;
import org.fife.ui.autocomplete.VariableCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
import org.fife.ui.rsyntaxtextarea.Token;


/**
 * Completion provider for ZScript code (not comments or strings).
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class CodeCompletionProvider extends AbstractCompletionProvider {

	private ZScriptAst ast;
	private ArrayList globalFunctions;
	private Map globalVariableMembers;
	private Map zhFileToContents;
	private ShorthandCompletionCache shorthandCache;

	private CaseInsensitiveComparator comparator = new CaseInsensitiveComparator();

	/**
	 * Characters that cannot be part of an identifier.
	 */
	private static final String SEQUENCE_POINT_CHARS = "();+-{} \t";


	public CodeCompletionProvider(ZScriptCompletionProvider parent) {
		//this.parent = parent;
		globalVariableMembers = new HashMap();
		try {
			CodeCompletionLoader.load(this);
		} catch (IOException ioe) {
			ioe.printStackTrace(); // Never happens
		}

		zhFileToContents = new HashMap();
		try {
			CodeCompletionLoader.loadZhFileDescription(this, "std.zh");
			CodeCompletionLoader.loadZhFileDescription(this, "string.zh");
		} catch (IOException ioe) {
			ioe.printStackTrace(); // Never happens
		}

		setAutoActivationRules(false, ">");
		setParameterChoicesProvider(new SourceParamChoicesProvider(this));

	}


	private void addGlobalFunctionCompletions(String alreadyEntered, List list) {
		addMembersImpl(globalFunctions, alreadyEntered, list);
		if (ast!=null) {
			RootNode root = ast.getRootNode();
			for (int i=0; i<root.getImportCount(); i++) {
				String zhFile = root.getImport(i).getImport();
				List completions = (List)zhFileToContents.get(zhFile);
				if (completions!=null) {
					addMembersImpl(completions, alreadyEntered, list);
				}
			}
		}
	}


	private void addGlobalMemberCompletions(String member, String alreadyEntered, List retVal) {
		List members = (List)globalVariableMembers.get(member);
		if (members!=null) {
			addMembersImpl(members, alreadyEntered, retVal);
		}
	}


	private void addMembersImpl(List members, String alreadyEntered, List list) {

		int index = Collections.binarySearch(members, alreadyEntered, super.comparator);
		if (index<0) { // No exact match
			index = -index - 1;
		}
		else {
			// If there are several overloads for the function being
			// completed, Collections.binarySearch() will return the index
			// of one of those overloads, but we must return all of them,
			// so search backward until we find the first one.
			int pos = index - 1;
			while (pos>0 &&
					super.comparator.compare(members.get(pos), alreadyEntered)==0) {
				list.add(members.get(pos));
				pos--;
			}
		}

		while (index<members.size()) {
			Completion c = (Completion)members.get(index);
			if (Util.startsWithIgnoreCase(c.getInputText(), alreadyEntered)) {
				list.add(c);
				index++;
			}
			else {
				break;
			}
		}

	}


	private void addShorthandCompletions(String alreadyEntered, List list) {
		List shorthands = shorthandCache.getShorthandCompletions();
		// Not really members, but this method works...
		addMembersImpl(shorthands, alreadyEntered, list);
	}


	private void addVariableCompletions(String alreadyEntered, int dot, List list) {
		SortedSet varCompletions = getVariableCompletions(alreadyEntered, dot);
		if (varCompletions!=null) {
			list.addAll(varCompletions);
		}
	}


	public String getAlreadyEnteredText(JTextComponent comp) {

		RSyntaxDocument doc = (RSyntaxDocument)comp.getDocument();
		int dot = comp.getCaretPosition();

		Segment seg = new Segment();
		Element root = doc.getDefaultRootElement();
		int index = root.getElementIndex(dot);
		Element elem = root.getElement(index);
		int start = elem.getStartOffset();
		int len = dot-start;
		try {
			doc.getText(start, len, seg);
		} catch (BadLocationException ble) {
			ble.printStackTrace();
			return EMPTY_STRING;
		}

		int segEnd = seg.offset + len;
		start = segEnd - 1;
OUTER:
		while (start>=seg.offset) {
			char ch = seg.array[start];
			switch (ch) {
				case '>':
					start--;
					if (start<seg.offset || seg.array[start]!='-') {
						break OUTER;
					}
					break;
				// TODO: array brackets
				default:
					if (isSequencePointChar(ch)) {
						break OUTER;
					}
					break;
			}
			start--;
		}
		start++;

		len = segEnd - start;
		return len==0 ? EMPTY_STRING : new String(seg.array, start, len);

	}


	private void getCompletionsArrowed(JTextComponent comp, String text, List retVal) {

		String[] sections = text.split("\\->");
		String first = sections[0];
		boolean endsWithArrow = sections.length==1;

		// First, check if it's a global variable thing
		final String[] GLOBALS = { "Link", "Screen", "Game" };
		for (int i=0; i<GLOBALS.length; i++) {
			if (GLOBALS[i].equals(first)) {
				if (sections.length>2) {
					System.err.println("No members of '" + GLOBALS[i] + "' have members themselves (or do they?)");
					return;
				}
				text = sections.length==2 ? sections[1] : "";
				addGlobalMemberCompletions(GLOBALS[i], text, retVal);
				return;
			}
		}

		// Must be a variable (lweapon, eweapon, etc.)
		int possibleBracket = first.indexOf('[');
		if (possibleBracket>-1) {
			first = first.substring(0, possibleBracket);
		}
		SortedSet vars = getVariableCompletions(first, comp.getCaretPosition());
		List completionsForType = Collections.EMPTY_LIST;
		if (!vars.isEmpty()) {
			// If > 1, then might have one var name that's also a prefix for
			// another var's name, such as "foo" and "foobar"
			VariableCompletion vc = (VariableCompletion)vars.first();
			String type = vc.getType();
			completionsForType = getCompletionsForType(type);
		}
		if (!completionsForType.isEmpty()) {
			int end = endsWithArrow ? sections.length : sections.length-1;
			for (int i=1; i<end; i++) {
				//String item = sections[i];
				// TODO: Get the matched item from completionsForType and keep looping
				System.out.println("Not yet implemented: going multiple arrows deep!");
			}
		}

		if (endsWithArrow) {
			retVal.addAll(completionsForType);
		}
		else {
			String last = sections[sections.length-1];
			SortedSet varCompletions = new TreeSet(comparator);
			varCompletions.addAll(completionsForType);

			// Get only those that match what's typed
			if (varCompletions.size()>0) {
				varCompletions = varCompletions.subSet(last, last+'{');
			}

			retVal.addAll(varCompletions);
		}

	}


	public List getCompletionsAt(JTextComponent tc, Point p) {

		int offset = tc.viewToModel(p);
		if (offset<0 || offset>=tc.getDocument().getLength()) {
//			lastCompletionsAtText = null;
//			return lastParameterizedCompletionsAt = null;
return null;
		}

		List completionsAt = new ArrayList();

		RSyntaxTextArea rsta = (RSyntaxTextArea)tc;
		int line = 0;
		try {
			line = rsta.getLineOfOffset(offset);
		} catch (BadLocationException ble) {
			ble.printStackTrace();
			return completionsAt;
		}
		Token token = rsta.getTokenListForLine(line);
		Token t = RSyntaxUtilities.getTokenAtOffset(token, offset);

		if (t!=null) {
			switch (t.getType()) {
				case Token.FUNCTION:
				case Token.VARIABLE:
					Token source = getSourceToken(token, offset);
					List sourceList = null;
					if (source==null) {
						sourceList = globalFunctions;
					}
					else {
						sourceList = getGlobalVariableMembers(source.getLexeme());
					}
					if (sourceList!=null) {
						Completion fc = getMatchingCompletion(sourceList, t.getLexeme());
						if (fc!=null) {
							completionsAt.add(fc);
						}
					}
					break;
				case Token.IDENTIFIER:
					if (ast!=null) {
						source = getSourceToken(token, offset);
						// Check for "foo->bar" first, e.g. e/lweapon properties
						if (source!=null && source.getType()==Token.IDENTIFIER) {
							VariableDecNode varDec = ZScriptUtils.
									getVariableDeclaration(source.getLexeme(), rsta, ast, offset);
							if (varDec!=null) {
								String type = varDec.getType();
								List completions = getCompletionsForType(type);
								String property = t.getLexeme();
								int index = Collections.binarySearch(completions, property, comparator);
								if (index>-1) {
									completionsAt.add(completions.get(index));
								}
							}
						}
						// Then just check for regular local variables.
						else {
							String varName = t.getLexeme();
							VariableDecNode varDec = ZScriptUtils.getVariableDeclaration(varName, rsta, ast, offset);
							if (varDec!=null) {
								completionsAt.add(new ZScriptVariableCompletion(this, varDec));
							}
						}
					}
					break;
			}
		}

		return completionsAt;

	}
private Completion getMatchingCompletion(List sourceList, String name) {
	Comparator c = new Comparator() {
		public int compare(Object o1, Object o2) {
			Completion c = (Completion)o1;
			String name1 = c.getInputText();
			if (name1.endsWith("[]")) {
				name1 = name1.substring(0, name1.length()-2);
			}
			String name2 = (String)o2;
			return name1.compareToIgnoreCase(name2);
		}
	};
	int index = Collections.binarySearch(sourceList, name, c);
	return index>=0 ? ((Completion)sourceList.get(index)) : null;
}
private static final char[] ARROW = { '-', '>' };
private Token getSourceToken(Token tokenList, int offset) {
	Token source = null;
	Token t = tokenList;
	while (!t.containsPosition(offset)) {
		if (t.is(Token.OPERATOR, ARROW)) {
			Token next = t.getNextToken();
			if (next!=null && next.containsPosition(offset)) {
				return source;
			}
			source = null;
		}
		source = t;
		t = t.getNextToken();
	}
	return null;
}


	private List getCompletionsForType(String type) {
		List completions = (List)globalVariableMembers.get(type);
		if (completions==null) {
			completions = Collections.EMPTY_LIST;
		}
		return completions;
	}


	@Override
	protected List getCompletionsImpl(JTextComponent comp) {

		//List completions = super.getCompletionsImpl(comp);

		List retVal = new ArrayList();
		String text = getAlreadyEnteredText(comp);

		if (text!=null) {

			int arrowIndex = text.indexOf("->");
			if (arrowIndex==-1) {
				addGlobalFunctionCompletions(text, retVal);
				addVariableCompletions(text, comp.getCaretPosition(), retVal);
				addShorthandCompletions(text, retVal);
			}
			else {
				getCompletionsArrowed(comp, text, retVal);
				text = text.substring(arrowIndex+2);
			}
		}

		Collections.sort(retVal);

		// Only return stuff that starts with what the user has entered.
		int start = Collections.binarySearch(retVal, text, comparator);
		if (start<0) {
			start = -(start+1);
		}
		else {
			// There might be multiple entries with the same input text.
			while (start>0 && comparator.compare(retVal.get(start-1), text)==0) {
				start--;
			}
		}

		int end = Collections.binarySearch(retVal, text+'{', comparator);
		end = -(end+1);

		return retVal.subList(start, end);

	}


	public List getParameterizedCompletions(JTextComponent tc) {
		return null;
	}


	@Override
	public char getParameterListEnd() {
		return ')';
	}


	@Override
	public String getParameterListSeparator() {
		return ", ";
	}


	@Override
	public char getParameterListStart() {
		return '(';
	}


	/**
	 * Does a crude search for variables up to the caret position.
	 *
	 * @param comp The text area.
	 * @return The completions for variables, or <code>null</code> if there
	 *         were none.
	 */
	private SortedSet getVariableCompletions(String text, int dot) {

		if (text==null) {
			return null;
		}

		SortedSet varCompletions = new TreeSet(comparator);

		// Go through all code blocks in scope and look for variables
		// declared before the caret.
		VariablesInScopeGrabber varGrabber = new VariablesInScopeGrabber(dot);
		ast.getRootNode().accept(varGrabber);
		List varList = varGrabber.getVariableList();
		for (Iterator i=varList.iterator(); i.hasNext(); ) {
			VariableDecNode varDec = (VariableDecNode)i.next();
			VariableCompletion vc = new ZScriptVariableCompletion(this, varDec);
			varCompletions.add(vc);
		}
		List funcList = varGrabber.getFunctionList();
		for (Iterator i=funcList.iterator(); i.hasNext(); ) {
			FunctionDecNode funcDec = (FunctionDecNode)i.next();
			FunctionCompletion fc = new ZScriptFunctionCompletion(this, funcDec);
			varCompletions.add(fc);
		}

		// Get only those that match what's typed
		if (varCompletions.size()>0) {
			varCompletions = varCompletions.subSet(text, text+'{');
		}

		return varCompletions;

	}


	/**
	 * Returns whether a character is a sequence point character (i.e., cannot
	 * be part of an identifier).
	 *
	 * @param ch The character.
	 * @return Whether that character can be part of an identifier.
	 */
	private static final boolean isSequencePointChar(char ch) {
		return SEQUENCE_POINT_CHARS.indexOf(ch)>-1;
	}


	public void putZhFileContents(String zhFileName, SortedSet contents) {
		zhFileToContents.put(zhFileName, new ArrayList(contents));
	}


	public void setAst(ZScriptAst ast) {
		this.ast = ast;
	}


	void setGlobalFunctions(SortedSet globalFunctions) {
		this.globalFunctions = new ArrayList(globalFunctions);
	}


	void setGlobalVariableMembers(String global, SortedSet members) {
		globalVariableMembers.put(global, new ArrayList(members));
	}


	void setShorthandCache(ShorthandCompletionCache cache) {
		this.shorthandCache = cache;
	}


public List getGlobalVariableMembers(String global) {
	return (List)globalVariableMembers.get(global);
}

	/**
	 * A comparator that compares the input text of a {@link Completion}
	 * against a String lexicographically, ignoring case.
	 */
	private static class CaseInsensitiveComparator implements Comparator,
														Serializable {

		public int compare(Object o1, Object o2) {
			String s1 = o1 instanceof String ? (String)o1 :
							((Completion)o1).getInputText();
			String s2 = o2 instanceof String ? (String)o2 :
							((Completion)o2).getInputText();
			return String.CASE_INSENSITIVE_ORDER.compare(s1, s2);
		}

	}


}