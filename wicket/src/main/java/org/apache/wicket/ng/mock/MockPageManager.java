package org.apache.wicket.ng.mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.ng.page.ManageablePage;
import org.apache.wicket.ng.page.PageManagerContext;
import org.apache.wicket.ng.page.common.AbstractPageManager;
import org.apache.wicket.ng.page.common.RequestAdapter;

public class MockPageManager extends AbstractPageManager
{

	public MockPageManager()
	{
	}

	private Map<String, Map<Integer, ManageablePage>> sessionMap = new ConcurrentHashMap<String, Map<Integer, ManageablePage>>();

	private class MockRequestAdapter extends RequestAdapter
	{
		public MockRequestAdapter(PageManagerContext context)
		{
			super(context);
		}

		@Override
		protected ManageablePage getPage(int id)
		{
			Map<Integer, ManageablePage> pages = sessionMap.get(getSessionId());
			return pages != null ? pages.get(id) : null;
		}

		@Override
		protected void newSessionCreated()
		{
			
		}

		@Override
		protected void storeTouchedPages(List<ManageablePage> touchedPages)
		{
			bind();
			Map<Integer, ManageablePage> pages = sessionMap.get(getSessionId());
			if (pages == null)
			{
				pages = new HashMap<Integer, ManageablePage>();
				sessionMap.put(getSessionId(), pages);
			}
			for (ManageablePage page : touchedPages)
			{
				pages.put(page.getPageId(), page);
			}
		}
	};

	@Override
	protected RequestAdapter newRequestAdapter(PageManagerContext context)
	{
		return new MockRequestAdapter(context);
	}

	@Override
	public void sessionExpired(String sessionId)
	{
		sessionMap.remove(sessionId);
	}

	@Override
	public boolean supportsVersioning()
	{
		return false;
	}

	public void destroy()
	{
		sessionMap.clear();
	}
}
