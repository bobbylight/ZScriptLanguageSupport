/*
 * 11/03/2012
 *
 * ScriptScraper - Scrapes scripts off of PureZC.
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.rtext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Scrapes the scripts off of the PureZC forums.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ScriptScraper {

	public static final String SITE = "http://www.purezc.com/";
	public static final String FORUM = SITE + "forums/index.php?showforum=179";

	private Map<String, String> replacementMap;

	private static final Pattern HTML_BR = Pattern.compile("<[bB][rR]\\s*/?>");
	private static final Pattern HTML_COMMENT = Pattern.compile("<!--.*?-->", Pattern.DOTALL);
	private static final Pattern HTML_ESCAPE = Pattern.compile("&([^;]+);");


	public ScriptScraper() {
		replacementMap = createReplacementMap();
	}


	/**
	 * Returns a map from common HTML escapes to their characters.
	 *
	 * @return The map.
	 */
	private Map<String, String> createReplacementMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("nbsp", " ");
		map.put("amp",  "&");
		map.put("lt",   "<");
		map.put("gt",   ">");
		map.put("quot", "\"");
		map.put("euro", "\u20ac");
		map.put("copy", "\u00a9");
		return map;
	}


	/**
	 * Returns the URL for information about the author of a script.
	 *
	 * @param info The script information, or <code>null</code>.
	 * @return The URL for information about the script's author, or
	 *         <code>null</code> if <code>info</code> was <code>null</code>.
	 */
	public String getAuthorAddress(ScriptInfo info) {
		int authorId = info.getAuthorId();
		return authorId<=0 ? null : (SITE + "forums/index.php?showuser=" + authorId);
	}


	/**
	 * Reads the full content of a web page.
	 *
	 * @param address The web page.
	 * @return The content.
	 * @throws IOException If an IO error occurs.
	 */
	private static final String getPageContent(String address) throws IOException {

		StringBuffer sb = new StringBuffer();

		URL url = new URL(address);
		BufferedReader r = new BufferedReader(new InputStreamReader(url.openStream()));

		String line = null;
		try {
			while ((line=r.readLine())!=null) {
				sb.append(line).append('\n');
			}
		} finally {
			r.close();
		}

		return sb.toString();

	}


	private int getPageCount(String content) {
		Pattern pageCountP = Pattern.compile(">(\\d+) Pages\\s*<img");
		Matcher m = pageCountP.matcher(content);
		if (m.find()) {
			return Integer.parseInt(m.group(1));
		}
		return -1;
	}


	private int getPinnedPostCount(String content) {
		int count = 0;
		int index = 0;
		final String lookFor = "<strong>Pinned: </strong>";
		while ((index=content.indexOf(lookFor, index))>-1) {
			count++;
			index += lookFor.length();
		}
		return count;
	}


	/**
	 * Returns the URL for more information about a script.
	 *
	 * @param info The script, or <code>null</code>.
	 * @return The URL for information about the script, or <code>null</code>
	 *         if <code>info</code> is <code>null</code>.
	 */
	public String getScriptAddress(ScriptInfo info) {
		String id = info.getId();
		return id==null ? null : (SITE + "forums/index.php?showtopic=" + id);
	}


	/**
	 * Returns the scripts at the given location.
	 *
	 * @return The scripts, or an empty list if none were found.
	 * @throws IOException If an IO error occurred getting the scripts.
	 */
	public List<ScriptInfo> getScripts() throws IOException {

		List<ScriptInfo> scripts = new ArrayList<ScriptInfo>();

		String content = getPageContent(FORUM);
		int pageCount = getPageCount(content);
		int pinnedPostCount = getPinnedPostCount(content);
		content = content.substring(content.indexOf("Forum Topics"));

		Pattern ratingP = Pattern.compile(SITE + "Images/rating(\\d)\\.png");
		Pattern titleP = Pattern.compile("This topic was started: [^>]+>([^<]+)</a></span>");
		Pattern authorP = Pattern.compile("showuser=(\\d+)'>([^<]+)</a></td>");

		Pattern p = Pattern.compile("Begin Topic Entry (\\d+) \\-\\->(.+)<!\\-\\- End Topic Entry \\1", Pattern.DOTALL);

		int page = 0;
		// Pinned posts are all on the first page, and count towards the total
		// number of posts on a page.
		int itemsPerPage = pinnedPostCount;
		while (page < pageCount) {

			Matcher m = p.matcher(content);

			while (m.find()) {

				String id = m.group(1);
				String temp = m.group(2);
				Matcher titleM = titleP.matcher(temp);
				String title = titleM.find() ? titleM.group(1) : "No title found!";
				String author = null;
				int authorId = 0;
				Matcher authorM = authorP.matcher(temp);
				if (authorM.find()) {
					authorId = Integer.parseInt(authorM.group(1));
					author = authorM.group(2);
				}
				Matcher ratingM = ratingP.matcher(temp);
				int rating = ratingM.find() ? Integer.parseInt(ratingM.group(1)) : -1;

				ScriptInfo info = new ScriptInfo(this);
				info.setAuthor(authorId, author);
				info.setId(id);
				info.setName(title);
				info.setRating(rating);
				scripts.add(info);

				if (page==0) {
					itemsPerPage++;
				}

			}

			page++;
			if (page<pageCount) {
				String url = FORUM + "&st=" + (itemsPerPage*page);
				//System.out.println("DEBUG: " + url + ": Fetching page " + (page+1) + " of " + pageCount);
				content = getPageContent(url);
			}

		}

		return scripts;

	}


	/**
	 * Fetches a script's content from the appropriate URL.  This method is
	 * currently sloppy with error handling, and makes the script's "content"
	 * be the stack trace if an IO error occurs.<p>
	 *
	 * This should be called off the EDT, since it is making a remote call.
	 *
	 * @param info The script whose content should be loaded.
	 */
	public void loadScriptContent(ScriptInfo info) {

		String content = null;

		String address = info.getScriptAddress();
		try {
			content = getPageContent(address);
		} catch (IOException ioe) {
			StringWriter sw = new StringWriter();
			ioe.printStackTrace(new PrintWriter(sw));
			info.setContent(sw.toString());
			return;
		}

		final String startString = "<div class='codemain'>";
		int start = content.indexOf(startString);
		if (start==-1) {
			info.setContent("startString not found: " + startString);
			return;
		}

		final String endString = "</div>";
		int end = content.indexOf(endString, start);
		String script = content.substring(start+startString.length(), end);

		// Regexes for manipulating HTML, argh!!

		// Replace escapes with the actual chars
		Matcher m = HTML_ESCAPE.matcher(script);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String escape = m.group(1);
			String replacement = null;
			if (escape.charAt(0)=='#') {
				try {
					int value = Integer.parseInt(escape.substring(1));
					replacement = Character.toString((char)value);
				} catch (NumberFormatException nfe) {
					replacement = m.group(0);
				}
			}
			else {
				replacement = (String)replacementMap.get(escape);
				if (replacement==null) { // ???
					replacement = m.group(0);
				}
			}
			// PureZC escapes backslashes as &#092;
			replacement = replacement.replaceAll("\\\\", "\\\\\\\\");
			m.appendReplacement(sb, replacement);
		}
		m.appendTail(sb);
		script = sb.toString();

		// Replace line breaks with newlines
		m = HTML_BR.matcher(script);
		script = m.replaceAll("\n");

		// Remove HTML comments
		m = HTML_COMMENT.matcher(script);
		script = m.replaceAll("");

		info.setContent(script);

	}


}