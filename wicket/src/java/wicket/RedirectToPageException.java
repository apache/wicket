package wicket;

/**
 * Causes wicket to interrupt current request processing and immediately respond
 * to the specified page.
 * <p>
 * Notice that throwing this exception <strong>does not</strong> cause an http
 * redirect to be issued.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
// FIXME General: Why have this exception at all?  Instead you could do the setResponsePage yourself and then throw a more generic AbortAndRespondException.
public class RedirectToPageException extends AbortAndRespondException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * Redirects to the specified bookmarkable page
	 * 
	 * @param pageClass
	 *            class of bookmarkable page
	 */
	public RedirectToPageException(Class pageClass)
	{
		RequestCycle.get().setResponsePage(pageClass);
	}

	/**
	 * Redirects to the specified bookmarkable page with the given page
	 * parameters
	 * 
	 * @param pageClass
	 *            class of bookmarkable page
	 * @param params
	 *            bookmarkable page parameters
	 */
	public RedirectToPageException(Class pageClass, PageParameters params)
	{
		RequestCycle.get().setResponsePage(pageClass, params);
	}

	/**
	 * Redirects to the specified page
	 * 
	 * @param page
	 *            redirect page
	 */
	public RedirectToPageException(Page page)
	{
		RequestCycle.get().setResponsePage(page);
	}


}
