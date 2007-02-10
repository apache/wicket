/*
 * Copyright Teachscape
 */
package wicket.threadtest.apps.app2;

import wicket.Session;

public class Connection {

	private final String id;

	public Connection(String id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		return ((Connection) obj).id.equals(id);
	}

	public String getData() {
		return "data[Connection=" + id + ",Session=" + Session.get() + ",Thread=" + Thread.currentThread() + "]";
	}

	/**
	 * Gets id.
	 * 
	 * @return id
	 */
	public String getId() {
		return id;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return "Connection[" + id + "]";
	}
}