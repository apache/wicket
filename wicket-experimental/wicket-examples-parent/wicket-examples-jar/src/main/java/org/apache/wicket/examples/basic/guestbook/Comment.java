package org.apache.wicket.examples.basic.guestbook;

import java.util.Date;

public class Comment {
	private final String author;
	private final String text;
	private final Date createdAt;

	public Comment(String author, String text) {
		this.author = author;
		this.text = text;
		this.createdAt = new Date();
	}

	public String getAuthor() {
		return author;
	}

	public String getText() {
		return text;
	}

	public Date getCreatedAt() {
		return createdAt;
	}
}
