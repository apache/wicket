package org.apache.wicket.atmosphere;

import com.google.common.base.Objects;

/**
 * Identifies a page by its id and the session it belongs to.
 * 
 * @author papegaaij
 */
public class PageKey
{
	private Integer pageId;

	private String sessionId;

	/**
	 * Construct.
	 * 
	 * @param pageId
	 * @param sessionId
	 */
	public PageKey(Integer pageId, String sessionId)
	{
		this.pageId = pageId;
		this.sessionId = sessionId;
	}

	/**
	 * @return The id of the page
	 */
	public Integer getPageId()
	{
		return pageId;
	}

	/**
	 * @return The id of the session
	 */
	public String getSessionId()
	{
		return sessionId;
	}

	/**
	 * @param sessionId
	 * @return true if this {@code PageKey} is for the same session
	 */
	public boolean isForSession(String sessionId)
	{
		return getSessionId().equals(sessionId);
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(pageId, sessionId);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof PageKey)
		{
			PageKey other = (PageKey)obj;
			return Objects.equal(pageId, other.pageId) && Objects.equal(sessionId, other.sessionId);
		}
		return false;
	}
}
