package org.apache.wicket.atmosphere;

import com.google.common.base.Objects;

public class PageKey
{
	private Integer pageId;

	private String sessionId;

	public PageKey(Integer pageId, String sessionId)
	{
		this.pageId = pageId;
		this.sessionId = sessionId;
	}

	public Integer getPageId()
	{
		return pageId;
	}

	public String getSessionId()
	{
		return sessionId;
	}

	public boolean isForSession(String sessionId)
	{
		return sessionId.equals(sessionId);
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
			PageKey other = (PageKey) obj;
			return Objects.equal(pageId, other.pageId) && Objects.equal(sessionId, other.sessionId);
		}
		return false;
	}
}
