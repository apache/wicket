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
public class RedirectException extends RuntimeException
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
	public RedirectException(Class pageClass)
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
	public RedirectException(Class pageClass, PageParameters params)
	{
		RequestCycle.get().setResponsePage(pageClass, params);
	}

	/**
	 * Redirects to the specified page
	 * 
	 * @param page
	 *            redirect page
	 */
	public RedirectException(Page page)
	{
		RequestCycle.get().setResponsePage(page);
	}


}
