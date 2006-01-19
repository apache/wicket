package wicket;

/**
 * Causes wicket to interrupt current request processing and immediately
 * redirect to an intercept page.
 * <p>
 * Similar to calling session.redirectToInteceptPage(Page) with the difference
 * that this exception will interrupt processing of the current request
 * 
 * @see wicket.Session#redirectToInterceptPage(Page)
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class RedirectToInterceptPageException extends AbortAndRespondException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * Redirects to the specified intercept page
	 * 
	 * @param interceptPage
	 *            redirect page
	 */
	public RedirectToInterceptPageException(Page interceptPage)
	{
		Session.get().redirectToInterceptPage(interceptPage);
	}


}
