package wicket.protocol.http;

/**
 * A factory interface used by wicket servlet to create application objects
 * 
 * @author Igor Vaynberg ( ivaynberg@privesec.com )
 * 
 */
public interface IWebApplicationFactory
{
	/**
	 * Create application object
	 * 
	 * @param servlet
	 *            the wicket servlet
	 * @return application object instance
	 */
	WebApplication createApplication(WicketServlet servlet);
}
