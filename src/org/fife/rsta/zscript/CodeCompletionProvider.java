/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import org.fife.rsta.zscript.ast.ScriptNode;
import org.fife.rsta.zscript.ast.VariableDecNode;
import org.fife.rsta.zscript.ast.VariablesInScopeGrabber;
import org.fife.rsta.zscript.ast.ZScriptAst;
import org.fife.ui.autocomplete.AbstractCompletionProvider;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.ParameterizedCompletion;
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
	private ArrayList<FunctionCompletion> globalFunctions;
	private Map<String, List<Completion>> globalVariableMembers;
	private Map<String, List<Completion>> zhFileToContents;
	private ShorthandCompletionCache shorthandCache;

	private CompletionComparator completionComparator = new CompletionComparator();

	/**
	 * Characters that cannot be part of an identifier.
	 */
	private static final String SEQUENCE_POINT_CHARS = "();+-{} \t";

	private static final char[] ARROW = { '-', '>' };


	public CodeCompletionProvider(ZScriptCompletionProvider parent) {
		//this.parent = parent;
		globalVariableMembers = new HashMap<String, List<Completion>>();
		try {
			CodeCompletionLoader.load(this);
		} catch (IOException ioe) {
			ioe.printStackTrace(); // Never happens
		}

		zhFileToContents = new HashMap<String, List<Completion>>();
		try {
			CodeCompletionLoader.loadZhFileDescription(this, "std.zh");
			CodeCompletionLoader.loadZhFileDescription(this, "string.zh");
		} catch (IOException ioe) {
			ioe.printStackTrace(); // Never happens
		}

		setAutoActivationRules(false, ">");
		setParameterChoicesProvider(new SourceParamChoicesProvider(this));

	}


	private void addGlobalFunctionCompletions(String alreadyEntered,
			List<Completion> list) {
		addMembersImpl(globalFunctions, alreadyEntered, list);
		if (ast!=null) {
			RootNode root = ast.getRootNode();
			for (int i=0; i<root.getImportCount(); i++) {
				String zhFile = root.getImport(i).getImport();
				List<Completion> completions = zhFileToContents.get(zhFile);
				if (completions!=null) {
					addMembersImpl(completions, alreadyEntered, list);
				}
			}
		}
	}


	private void addGlobalMemberCompletions(String member, String alreadyEntered,
			List<Completion> retVal) {
		List<Completion> members = globalVariableMembers.get(member);
		if (members!=null) {
			addMembersImpl(members, alreadyEntered, retVal);
		}
	}


	private void addMembersImpl(List<? extends Completion> members,
			String alreadyEntered, List<Completion> list) {

		BasicCompletion alreadyEnteredC = new BasicCompletion(null, alreadyEntered);
		int index = Collections.binarySearch(members, alreadyEnteredC, completionComparator);
		if (index<0) { // No exact match
			index = -index - 1;
		}
		else {
			// If there are several overloads for the function being
			// completed, Collections.binarySearch() will return the index
			// of one of those overloads, but we must return all of them,
			// so search backward until we find the first one.
			int pos = index - 1;
			while (pos>0 && completionComparator.compare(
							members.get(pos), alreadyEnteredC)==0) {
				list.add(members.get(pos));
				pos--;
			}
		}

		while (index<members.size()) {
			Completion c = members.get(index);
			if (Util.startsWithIgnoreCase(c.getInputText(), alreadyEntered)) {
				list.add(c);
				index++;
			}
			else {
				break;
			}
		}

	}


	private void addShorthandCompletions(String alreadyEntered,
			List<Completion> list) {
		List<Completion> shorthands = shorthandCache.getShorthandCompletions();
		// Not really members, but this method works...
		addMembersImpl(shorthands, alreadyEntered, list);
	}


	private void addVariableCompletions(String alreadyEntered, int dot,
			List<Completion> list) {
		SortedSet<Completion> varCompletions = getVariableCompletions(alreadyEntered, dot);
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


	private void getCompletionsArrowed(JTextComponent comp, String text,
			List<Completion> retVal) {

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

		// "item" scripts display itemdata fields for "this->"
		int dot = comp.getCaretPosition();
		if ("this".equals(first)) {
			ScriptNode script = ast.getScriptNodeContaining(dot);
			if (script != null) {
				text = sections.length==2 ? sections[1] : "";
				addGlobalMemberCompletions("itemdata", text, retVal);
				return;
			}
		}

		// Must be a variable (lweapon, eweapon, etc.)
		int possibleBracket = first.indexOf('[');
		if (possibleBracket>-1) {
			first = first.substring(0, possibleBracket);
		}
		SortedSet<Completion> vars = getVariableCompletions(first, dot);
		List<Completion> completionsForType = Collections.emptyList();
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
			SortedSet<Completion> varCompletions = new TreeSet<Completion>(completionComparator);
			varCompletions.addAll(completionsForType);

			// Get only those that match what's typed
			if (varCompletions.size()>0) {
				Completion start = new BasicCompletion(null, last);
				Completion end = new BasicCompletion(null, last + '{');
				varCompletions = varCompletions.subSet(start, end);
			}

			retVal.addAll(varCompletions);
		}

	}


	public List<Completion> getCompletionsAt(JTextComponent tc, Point p) {

		int offset = tc.viewToModel(p);
		if (offset<0 || offset>=tc.getDocument().getLength()) {
//			lastCompletionsAtText = null;
//			return lastParameterizedCompletionsAt = null;
return null;
		}

		List<Completion> completionsAt = new ArrayList<Completion>();

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
					List<? extends Completion> sourceList = null;
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
								List<Completion> completions = getCompletionsForType(type);
								String property = t.getLexeme();
								BasicCompletion c = new BasicCompletion(null, property);
								int index = Collections.binarySearch(completions, c, completionComparator);
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


	private Completion getMatchingCompletion(List<? extends Completion> sourceList, String name) {
		CompletionComparator comparator = new CompletionComparator(new CompletionComparator.CompletionStringer() {
			public String getCompareValue(Completion c) {
				String name = c.getInputText();
				if (name.endsWith("[]")) {
					name = name.substring(0, name.length()-2);
				}
				return name;
			}
		});
		BasicCompletion bc = new BasicCompletion(null, name);
		int index = Collections.binarySearch(sourceList, bc, comparator);
		return index>=0 ? ((Completion)sourceList.get(index)) : null;
	}


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


	private List<Completion> getCompletionsForType(String type) {
		List<Completion> completions = globalVariableMembers.get(type);
		if (completions==null) {
			completions = Collections.emptyList();
		}
		return completions;
	}


	@Override
	protected List<Completion> getCompletionsImpl(JTextComponent comp) {

		//List completions = super.getCompletionsImpl(comp);

		List<Completion> retVal = new ArrayList<Completion>();
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
		Completion startComp = new BasicCompletion(null, text);
		int start = Collections.binarySearch(retVal, startComp, completionComparator);
		if (start<0) {
			start = -(start+1);
		}
		else {
			// There might be multiple entries with the same input text.
			while (start>0 && completionComparator.compare(retVal.get(start-1), startComp)==0) {
				start--;
			}
		}

		Completion endComp = new BasicCompletion(null, text + '{');
		int end = Collections.binarySearch(retVal, endComp, completionComparator);
		end = -(end+1);

		return retVal.subList(start, end);

	}


	public List<ParameterizedCompletion> getParameterizedCompletions(JTextComponent tc) {
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
	private SortedSet<Completion> getVariableCompletions(String text, int dot) {

		if (text==null) {
			return null;
		}

		SortedSet<Completion> varCompletions = new TreeSet<Completion>(completionComparator);

		// Go through all code blocks in scope and look for variables
		// declared before the caret.
		VariablesInScopeGrabber varGrabber = new VariablesInScopeGrabber(dot);
		ast.getRootNode().accept(varGrabber);
		List<VariableDecNode> varList = varGrabber.getVariableList();
		for (VariableDecNode varDec : varList) {
			VariableCompletion vc = new ZScriptVariableCompletion(this, varDec);
			varCompletions.add(vc);
		}
		List<FunctionDecNode> funcList = varGrabber.getFunctionList();
		for (FunctionDecNode funcDec : funcList) {
			FunctionCompletion fc = new ZScriptFunctionCompletion(this, funcDec);
			varCompletions.add(fc);
		}

		// Get only those that match what's typed
		if (varCompletions.size()>0) {
			Completion start = new BasicCompletion(null, text);
			Completion end = new BasicCompletion(null, text + '{');
			varCompletions = varCompletions.subSet(start, end);
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


	public void putZhFileContents(String zhFileName, SortedSet<Completion> contents) {
		zhFileToContents.put(zhFileName, new ArrayList<Completion>(contents));
	}


	public void setAst(ZScriptAst ast) {
		this.ast = ast;
	}


	void setGlobalFunctions(SortedSet<FunctionCompletion> globalFunctions) {
		this.globalFunctions = new ArrayList<FunctionCompletion>(globalFunctions);
	}


	void setGlobalVariableMembers(String global, SortedSet<Completion> members) {
		globalVariableMembers.put(global, new ArrayList<Completion>(members));
	}


	void setShorthandCache(ShorthandCompletionCache cache) {
		this.shorthandCache = cache;
	}


public List<Completion> getGlobalVariableMembers(String global) {
	return globalVariableMembers.get(global);
}

}