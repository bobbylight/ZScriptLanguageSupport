/*
 * 11/03/2012
 *
 * ScriptInfo - Information about a script on PureZC.
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.rtext;


/**
 * Information about a script on PureZC.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ScriptInfo implements Comparable {

	private ScriptScraper parent;
	private String id;
	private String name;
	private String author;
	private int authorId;
	private int rating;
	private String content;


	public ScriptInfo(ScriptScraper parent) {
		this.parent = parent;
	}


	public int compareTo(Object o) {
		if (o instanceof ScriptInfo) {
			ScriptInfo script2 = (ScriptInfo)o;
			// Note that id is probably a more logical thing to compare, but
			// script (forum topic) names are always unique also, and that's
			// what we want to sort on in the table in the UI, so...
			return getName().compareTo(script2.getName());
		}
		return -1;
	}


	@Override
	public boolean equals(Object o) {
		return compareTo(o)==0;
	}


	public void fetchContent() {
		parent.loadScriptContent(this);
	}


	public String getName() {
		return name;
	}


	public String getAuthor() {
		return author;
	}


	public String getAuthorAddress() {
		return parent.getAuthorAddress(this);
	}


	public int getAuthorId() {
		return authorId;
	}


	public int getRating() {
		return rating;
	}


	public String getContent() {
		return content;
	}


	public String getId() {
		return id;
	}


	public String getScriptAddress() {
		return parent.getScriptAddress(this);
	}


	@Override
	public int hashCode() {
		return getId().hashCode();
	}


	public void setName(String name) {
		this.name = name;
	}


	public void setAuthor(int authorId, String author) {
		this.authorId = authorId;
		this.author = author;
	}


	public void setRating(int rating) {
		this.rating = rating;
	}


	public void setContent(String content) {
		this.content = content;
	}


	public void setId(String id) {
		this.id = id;
	}


	/**
	 * Returns the name of this script, so that the cell renderer and filter
	 * behave properly.
	 *
	 * @return A string representation of the script.
	 */
	@Override
	public String toString() {
		return getName();
	}


}