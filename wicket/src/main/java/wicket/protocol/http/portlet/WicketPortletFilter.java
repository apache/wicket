package wicket.protocol.http.portlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.html.pages.AccessDeniedPage;
import wicket.protocol.http.IWebApplicationFactory;
import wicket.protocol.http.WebApplication;
import wicket.protocol.http.WicketFilter;

/**
 * 
 *  Filter to support dynamic resources with portlets
 * 
 * See:
 * 
 * http://weblogs.java.net/blog/wholder/archive/2005/02/session_session.html
 * http://issues.apache.org/jira/browse/PLUTO-53
 * 
 * @author Janne Hietam&auml;ki
 */
public class WicketPortletFilter extends WicketFilter
{
	private static final Log log = LogFactory.getLog(WicketPortletServlet.class);

	private static final long serialVersionUID = 1L;

	protected final IWebApplicationFactory getApplicationFactory()
	{
		return new IWebApplicationFactory()
		{

			public WebApplication createApplication(WicketFilter filter)
			{
				return new WebApplication()
				{

					public Class getHomePage()
					{
						return AccessDeniedPage.class;
					}
				};
			}

		};
	}
}