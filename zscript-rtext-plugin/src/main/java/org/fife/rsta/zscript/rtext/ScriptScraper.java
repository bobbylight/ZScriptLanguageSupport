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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Scrapes the scripts off of the PureZC forums.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ScriptScraper {

	public static final String SITE = "http://www.purezc.net/";
	public static final String FORUM = SITE + "index.php?page=scripts";

//	private static final Pattern HTML_BR = Pattern.compile("<[bB][rR]\\s*/?>");
//	private static final Pattern HTML_COMMENT = Pattern.compile("<!--.*?-->", Pattern.DOTALL);


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
	private static String getPageContent(String address) throws IOException {

		StringBuilder sb = new StringBuilder();

		URL url = new URL(address);

        try (BufferedReader r = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }

		return sb.toString();

	}


	private int getPageCount(String content) {
		Pattern pageCountP = Pattern.compile("<a href='#'>Page 1 of (\\d+)\\s*<!--");
		Matcher m = pageCountP.matcher(content);
		if (m.find()) {
			return Integer.parseInt(m.group(1));
		}
		return -1;
	}


	private int getPinnedPostCount(String content) {
		int count = 0;
		int index = 0;
		final String lookFor = "'>Pinned</span>";
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

		List<ScriptInfo> scripts = new ArrayList<>();
		boolean loggedIn = false; // TODO

		String content = getPageContent(FORUM);
		int pageCount = getPageCount(content);
		int pinnedPostCount = getPinnedPostCount(content);
		content = content.substring(content.indexOf("<!-- BEGIN TOPICS -->"));

		Pattern ratingP = Pattern.compile(SITE + "Images/rating(\\d)\\.png");
		Pattern nameP = Pattern.compile("<span itemprop=\"name\">([^<]+)</span>");
		Pattern dateCreatedP = Pattern.compile("dateCreated\">([^<]+)</span>");
		Pattern authorP = null;
		if (loggedIn) {
			// If logged in, we can get the user name and ID to link to their profile page
			authorP = Pattern.compile("showuser=(\\d+)' title='View Profile'><span itemprop=\"name\">([^<]+)</span>");
		}
		else {
			authorP = Pattern.compile("Started by[\\s\\r\\n]+([^\\s\\r\\n].+[^\\s\\r\\n])[\\s\\r\\n]+, <span");
		}
		Pattern searchTagsP = Pattern.compile("search_tags=([^&]+)&amp;search_app=forums");

		Pattern p = Pattern.compile("data-tid=\"(\\d+)\">(.+?)<!\\-\\-<tr itemscope itemtype=", Pattern.DOTALL);
		int page = 0;

		// Pinned posts are all on the first page, and count towards the total
		// number of posts on a page.
		while (page < pageCount) {

			Matcher m = p.matcher(content);

			// Skip the pinned topics on the first page
			if (page==0) {
				for (int i=0; i<pinnedPostCount; i++) {
					m.find();
				}
			}

			while (m.find()) {

				String id = m.group(1);
				String temp = m.group(2);
				Matcher dateCreatedM = dateCreatedP.matcher(temp);
				String dateCreated = dateCreatedM.find() ? dateCreatedM.group(1) : "";
				String author = null;
				int authorId = 0;
				Matcher authorM = authorP.matcher(temp);
				if (authorM.find()) {
					if (loggedIn) {
						authorId = Integer.parseInt(authorM.group(1));
						author = authorM.group(2);
					}
					else {
						author = authorM.group(1);
					}
				}
				Matcher ratingM = ratingP.matcher(temp);
				int rating = ratingM.find() ? Integer.parseInt(ratingM.group(1)) : -1;
				Matcher nameM = nameP.matcher(temp);
				String name = nameM.find() ? nameM.group(1) :
					Messages.getString("ScriptSearchDialog.Unknown");
				name = ZScriptUIUtils.replaceEntities(name);

				Set<String> searchTags = new TreeSet<>();
				Matcher searchTagM = searchTagsP.matcher(temp);
				while (searchTagM.find()) {
					searchTags.add(searchTagM.group(1));
				}

				ScriptInfo info = new ScriptInfo(this);
				info.setAuthor(authorId, author);
				info.setId(id);
				info.setName(name);
				info.setDateCreated(dateCreated);
				info.setRating(rating);
				info.setSearchTags(searchTags);
				scripts.add(info);

			}

			page++;
			if (page<pageCount) {
				String url = FORUM + "&page=" + (page+1);
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

		String pattern = "<pre class=['\"]prettyprint(?:\\s+(?:lang\\-auto\\s+)?linenums:0)?['\"]>([^<]+)</pre>";
		Pattern p = Pattern.compile(pattern, Pattern.DOTALL);
		Matcher m = p.matcher(content);
		if (!m.find()) {
			info.setContent("// " + Messages.getString("SearchScriptDialog.SourceNotFound") + 
					"\n// (Content regex not found: " + pattern + ")");
			return;
		}

		String script = m.group(1);

		// Regexes for manipulating HTML, argh!!

		// Replace escapes with the actual chars
		script = ZScriptUIUtils.replaceEntities(script);

//		// Replace line breaks with newlines
//		m = HTML_BR.matcher(script);
//		script = m.replaceAll("\n");
//
//		// Remove HTML comments
//		m = HTML_COMMENT.matcher(script);
//		script = m.replaceAll("");

		info.setContent(script.trim());

	}


	public static int fetchRating(ScriptInfo script) {

		String address = script.getScriptAddress();
		String content = null;
		try {
			content = getPageContent(address);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return -1;
		}

		Pattern p = Pattern.compile("<meta itemprop=['\"]ratingValue['\"] content=['\"](\\d+)['\"]\\s*/>");
		Matcher m = p.matcher(content);
		return m.find() ? Integer.parseInt(m.group(1)) : -1;

	}


	/**
	 * For testing purposes.
	 */
	public static void main(String[] args) throws IOException {
		List<ScriptInfo> scripts = new ScriptScraper().getScripts();
		for (ScriptInfo script : scripts) {
			System.out.println(script.toString(true));
		}
	}


}
