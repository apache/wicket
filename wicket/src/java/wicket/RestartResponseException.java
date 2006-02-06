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
public class RestartResponseException extends AbstractRestartResponseException
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
	public RestartResponseException(Class pageClass)
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
	public RestartResponseException(Class pageClass, PageParameters params)
	{
		RequestCycle.get().setResponsePage(pageClass, params);
	}

	/**
	 * Redirects to the specified page
	 * 
	 * @param page
	 *            redirect page
	 */
	public RestartResponseException(Page page)
	{
		RequestCycle.get().setResponsePage(page);
	}


}
