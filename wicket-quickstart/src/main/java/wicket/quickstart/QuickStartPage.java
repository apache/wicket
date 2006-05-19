package wicket.quickstart;

import wicket.markup.html.WebPage;

/**
 * Base class for all pages in the QuickStart application. Any page which
 * subclasses this page can get session properties from QuickStartSession via
 * getQuickStartSession().
 */
public abstract class QuickStartPage extends WebPage
{
	/**
	 * Get downcast session object for easy access by subclasses
	 * 
	 * @return The session
	 */
	public QuickStartSession getQuickStartSession()
	{
		return (QuickStartSession)getSession();
	}
}