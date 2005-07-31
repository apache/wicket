package wicket.protocol.http;

import wicket.WicketRuntimeException;

/**
 * Factory that creates application objects based on the class name specified in
 * the APP_CLASS_PARAM context variable.
 * 
 * @author Igor Vaynberg ( ivaynberg@privesec.com )
 * 
 */
public class ContextParamWebApplicationFactory implements IWebApplicationFactory
{
	/**
	 * context parameter name that must contain the class name of the
	 * application
	 */
	public final String APP_CLASS_PARAM = "applicationClassName";

	/** @see IWebApplicationFactory#createApplication(WicketServlet) */
	public WebApplication createApplication(WicketServlet servlet)
	{
		final String applicationClassName = servlet.getInitParameter(APP_CLASS_PARAM);
		try
		{
			final Class applicationClass = getClass().getClassLoader().loadClass(
					applicationClassName);
			if (WebApplication.class.isAssignableFrom(applicationClass))
			{
				// Construct WebApplication subclass
				return (WebApplication)applicationClass.newInstance();
			}
			else
			{
				throw new WicketRuntimeException("Application class " + applicationClassName
						+ " must be a subclass of WebApplication");
			}
		}
		catch (ClassNotFoundException e)
		{
			throw new WicketRuntimeException("Unable to create application of class "
					+ applicationClassName, e);
		}
		catch (InstantiationException e)
		{
			throw new WicketRuntimeException("Unable to create application of class "
					+ applicationClassName, e);
		}
		catch (IllegalAccessException e)
		{
			throw new WicketRuntimeException("Unable to create application of class "
					+ applicationClassName, e);
		}
		catch (SecurityException e)
		{
			throw new WicketRuntimeException("Unable to create application of class "
					+ applicationClassName, e);
		}

	}

}
