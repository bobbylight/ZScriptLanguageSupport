/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.VariableCompletion;
import org.fife.ui.autocomplete.ParameterizedCompletion.Parameter;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;


/**
 * Loads all completions from zscript.txt.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class CodeCompletionLoader {


	private static String getDescription(BufferedReader r) throws IOException {
		StringBuffer desc = new StringBuffer();
		String line = null;
		while ((line=r.readLine())!=null) {
			if (line.startsWith(" *")) {
				int nextSpace = line.indexOf(' ', 1);
				line = line.substring(nextSpace+1).trim();
				desc.append(line).append(' ');
			}
			else {
				break;
			}
		}
		String temp = desc.toString();
		if (temp.startsWith("<html>")) {
			return temp.substring("<html>".length());
		}
		return RSyntaxUtilities.escapeForHtml(temp, "<br>", false);
	}


	/**
	 * Returns the next global function completion.
	 */
	private static FunctionCompletion getFunctionCompletion(BufferedReader r,
								CompletionProvider p) throws IOException {

		String sig = getNextSignature(r);
		List<Parameter> params = null;
		int lparen = sig.indexOf('(');
		int rparen = sig.indexOf(')');
		if (rparen>lparen+1) {
			params = new ArrayList<Parameter>();
			String[] args = sig.substring(lparen+1, sig.indexOf(')')).split(",\\s+");
			for (int i=0; i<args.length; i++) {
				int lastSpace = args[i].lastIndexOf(' ');
				String type = args[i].substring(0, lastSpace);
				String name = args[i].substring(lastSpace+1);
				params.add(new Parameter(type, name));
			}
		}
		String temp = sig.substring(0, lparen);
		int lastSpace = temp.lastIndexOf(' ');
		String returnType = temp.substring(0, lastSpace);
		String name = temp.substring(lastSpace+1);

		FunctionCompletion fc = new ZScriptFunctionCompletion(p, name, returnType);
		fc.setParams(params);

		String desc = getDescription(r);
		if (desc!=null) {
			fc.setShortDescription(desc);
		}

		return fc;

	}


	/**
	 * Returns the next method or property description for a data type.
	 */
	private static Completion getMethodOrPropertyCompletion(BufferedReader r,
			CompletionProvider p, String parentType) throws IOException {
		return getMethodOrPropertyCompletion(r, p, parentType, null);
	}


	/**
	 * Returns the next method or property description for a data type.
	 */
	private static Completion getMethodOrPropertyCompletion(BufferedReader r,
			CompletionProvider p, String parentType, String definedIn)
					throws IOException {

		String sig = getNextSignature(r);
		List<Parameter> params = null;
		int lparen = sig.indexOf('(');
		int rparen = sig.indexOf(')');
		if (rparen > lparen + 1) {
			params = new ArrayList<Parameter>();
			String[] args = sig.substring(lparen + 1, sig.indexOf(')')).
					trim().split(",\\s+");
			for (int i = 0; i < args.length; i++) {
				int lastSpace = args[i].lastIndexOf(' ');
if (lastSpace==-1) {
	System.out.println(sig);
	Thread.dumpStack();
	System.exit(0);
}
				String type = args[i].substring(0, lastSpace);
				String name = args[i].substring(lastSpace + 1);
				params.add(new Parameter(type, name));
			}
		}
		String temp = sig;
		if (lparen>-1) {
			temp = temp.substring(0, lparen);
		}

		// string.zh defines constant values, e.g. "const int FOO = 55"
		// TODO: Remember value
		int equals = temp.indexOf('=');
		if (equals>-1) {
			temp = temp.substring(0, equals).trim();
		}

		int lastSpace = temp.lastIndexOf(' ');

		String returnType = temp.substring(0, lastSpace);
		String name = temp.substring(lastSpace + 1);

		BasicCompletion c = null;
		if (lparen>-1 && rparen>lparen) { // parens => function
			FunctionCompletion fc = new ZScriptMethodCompletion(p, name, returnType);
			fc.setParams(params);
			fc.setDefinedIn(definedIn);
			c = fc;
		}
		else { // Variable
			VariableCompletion vc = new ZScriptPropertyCompletion(p, parentType, name, returnType);
			vc.setDefinedIn(definedIn);
			c = vc;
		}

		String desc = getDescription(r);
		if (desc != null) {
			c.setShortDescription(desc);
		}

		return c;

	}


	private static String getNextSignature(BufferedReader r) throws IOException {
		String sig = null;
		String line = null;
		while ((line=r.readLine())!=null) {
			line = line.trim();
			if (line.length()>0 && line.charAt(0)!=' ') {
				sig = line;
				if (sig.indexOf('(')>-1) { // Some function signatures span multiple lines
					while (!sig.endsWith(")")) {
						sig += r.readLine().replaceAll("  +", " ");
					}
				}
				break;
			}
		}
		sig = sig.replaceAll("\t+", "");
		return sig;
	}


	public static void load(CodeCompletionProvider p) throws IOException {

		InputStream in = p.getClass().getResourceAsStream("/data/zscript.txt");
		BufferedReader r = new BufferedReader(new InputStreamReader(in));
		String line;

		try {

			// Global functions
			TreeSet<FunctionCompletion> globalFunctions = new TreeSet<FunctionCompletion>();
			do {
				line = r.readLine();
			} while (!line.startsWith("--- Global Functions"));
			line = r.readLine();

			do {
				FunctionCompletion fc = getFunctionCompletion(r, p);
				globalFunctions.add(fc);
			} while (!startingNewSection(r));
			p.setGlobalFunctions(globalFunctions);

			// FFC stuff
			TreeSet<Completion> members = new TreeSet<Completion>();
			do {
				Completion c = getMethodOrPropertyCompletion(r, p, "ffc");
				members.add(c);
			} while (!startingNewSection(r));
			p.setGlobalVariableMembers("ffc", members);

			// Link stuff
			members = new TreeSet<Completion>();
			do {
				Completion c = getMethodOrPropertyCompletion(r, p, "Link");
				members.add(c);
			} while (!startingNewSection(r));
			p.setGlobalVariableMembers("Link", members);

			// Screen stuff
			members = new TreeSet<Completion>();
			do {
				Completion c = getMethodOrPropertyCompletion(r, p, "Screen");
				members.add(c);
			} while (!startingNewSection(r));
			p.setGlobalVariableMembers("Screen", members);

			// Item stuff
			members = new TreeSet<Completion>();
			do {
				Completion c = getMethodOrPropertyCompletion(r, p, "item");
				members.add(c);
			} while (!startingNewSection(r));
			p.setGlobalVariableMembers("item", members);

			// Weapon stuff (lweapon and eweapon)
			members = new TreeSet<Completion>();
			do {
				Completion c = getMethodOrPropertyCompletion(r, p, "*weapon");
				members.add(c);
			} while (!startingNewSection(r));
			p.setGlobalVariableMembers("lweapon", members);
			p.setGlobalVariableMembers("eweapon", members);

			// Itemdata stuff
			members = new TreeSet<Completion>();
			do {
				Completion c = getMethodOrPropertyCompletion(r, p, "Itemdata");
				members.add(c);
			} while (!startingNewSection(r));
			p.setGlobalVariableMembers("itemdata", members);

			// Game stuff
			members = new TreeSet<Completion>();
			do {
				Completion c = getMethodOrPropertyCompletion(r, p, "Game");
				members.add(c);
			} while (!startingNewSection(r));
			p.setGlobalVariableMembers("Game", members);

			// Npc stuff
			members = new TreeSet<Completion>();
			do {
				Completion c = getMethodOrPropertyCompletion(r, p, "npc");
				members.add(c);
			} while (!startingNewSection(r));
			p.setGlobalVariableMembers("npc", members);

		} finally {
			r.close();
		}

	}


	public static void loadZhFileDescription(CodeCompletionProvider p,
					String zhFileName) throws IOException {

		String res = "/data/" + zhFileName.substring(0, zhFileName.length()-3) + ".txt";
		InputStream in = p.getClass().getResourceAsStream(res);
		BufferedReader r = new BufferedReader(new InputStreamReader(in));
		String line = null;

		TreeSet<Completion> contents = new TreeSet<Completion>();

		try {

			// Constants
			do {
				line = r.readLine();
			} while (!line.startsWith("--- Constants"));
			line = r.readLine();

			do {
				Completion c = getMethodOrPropertyCompletion(r, p, zhFileName);
				contents.add(c);
			} while (!startingNewSection(r));

			// Functions
			do {
				Completion c = getMethodOrPropertyCompletion(r, p, zhFileName);
				contents.add(c);
			} while (anotherCompletionExists(r));

		} finally {
			r.close();
		}

		p.putZhFileContents(zhFileName, contents);

	}


	/**
	 * Ghetto way to read until either the next function signature or section
	 * start.
	 */
	private static boolean startingNewSection(BufferedReader r) throws IOException {
		String line = null;
		r.mark(65535);
		while ((line=r.readLine())!=null) {
			if (line.length()>0) {
				if (line.startsWith("---")) {
					return true;
				}
				else if (line.startsWith("//")) {
					continue;
				}
				r.reset(); // Read the next function desc, put it back
				return false;
			}
			else {
				r.mark(65535);
			}
		}
		return true;
	}


	/**
	 * Ghetto way to read until either the next completion signature, skipping
	 * section/subsection starts (as seen in string.txt and std.txt).
	 */
	private static boolean anotherCompletionExists(BufferedReader r) throws IOException {
		String line = null;
		r.mark(65535);
		while ((line=r.readLine())!=null) {
			if (line.length()>0) {
				if (line.startsWith("--") || line.startsWith("==") ||
						line.startsWith("//")) {
					continue;
				}
				r.reset(); // Read start of the next completion, put it back
				return true;
			}
			else {
				r.mark(65535);
			}
		}
		return false;
	}


}
