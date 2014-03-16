/*
 * 11/03/2012
 *
 * ScriptInfo - Information about a script on PureZC.
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.rtext;

import java.util.Set;


/**
 * Information about a script on PureZC.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ScriptInfo implements Comparable<ScriptInfo> {

	private ScriptScraper parent;
	private String id;
	private String name;
	private String author;
	private int authorId;
	private int rating;
	private String dateCreated;
	private Set<String> searchTags;
	private String content;


	public ScriptInfo(ScriptScraper parent) {
		this.parent = parent;
	}


	public int compareTo(ScriptInfo script2) {
		if (script2!=null) {
			// Note that id is probably a more logical thing to compare, but
			// script (forum topic) names are always unique also, and that's
			// what we want to sort on in the table in the UI, so...
			return getName().compareToIgnoreCase(script2.getName());
		}
		return 1;
	}


	@Override
	public boolean equals(Object o) {
		return (o instanceof ScriptInfo) && compareTo((ScriptInfo)o)==0;
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


	public String getDateCreated() {
		return dateCreated;
	}


	public String getId() {
		return id;
	}


	public Set<String> getSearchTags() {
		return searchTags;
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


	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
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


	public void setSearchTags(Set<String> searchTags) {
		this.searchTags = searchTags;
	}


	/**
	 * Returns the name of this script, so that the cell renderer and filter
	 * behave properly.
	 *
	 * @return A string representation of the script.
	 */
	@Override
	public String toString() {
		return toString(false);
	}


	public String toString(boolean detailed) {

		if (!detailed) {
			return getName();
		}

		StringBuilder sb = new StringBuilder("[ScriptInfo: ").
			append("name=").append(getName()).
			append(", id=").append(getId()).
			append(", author=").append(getAuthor());

		if (searchTags!=null && !searchTags.isEmpty()) {
			sb.append(", tags=");
			int i = 0;
			for (String searchTag : searchTags) {
				sb.append(searchTag);
				if (i<searchTags.size()-1) {
					sb.append(",");
				}
				i++;
			}
		}

		sb.append("]");
		return sb.toString();

	}


}