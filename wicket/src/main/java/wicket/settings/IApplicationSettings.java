package wicket.settings;

import java.util.Locale;

import wicket.Application;
import wicket.Page;
import wicket.application.IClassResolver;
import wicket.protocol.http.WebApplication;
import wicket.protocol.http.WebRequest;
import wicket.util.convert.IConverterLocatorFactory;

/**
 * Settings interface for application settings.
 * <p>
 * <i>internalErrorPage </i>- You can override this with your own page class to
 * display internal errors in a different way.
 * <p>
 * <i>pageExpiredErrorPage </i>- You can override this with your own
 * bookmarkable page class to display expired page errors in a different way.
 * You can set property homePageRenderStrategy to choose from different ways the
 * home page url shows up in your browser.
 * <p>
 * <b>A CoverterLocator Factory </b>- By overriding getConverterFactory(), you
 * can provide your own factory which creates locale sensitive CoverterLocator
 * instances.
 * 
 * @author Jonathan Locke
 */
public interface IApplicationSettings
{
	/**
	 * Gets the access denied page class.
	 * 
	 * @return Returns the accessDeniedPage.
	 * @see IApplicationSettings#setAccessDeniedPage(Class)
	 */
	Class<? extends Page> getAccessDeniedPage();

	/**
	 * Gets the default resolver to use when finding classes
	 * 
	 * @return Default class resolver
	 */
	IClassResolver getClassResolver();

	/**
	 * Gets the converter supplier factory.
	 * 
	 * @return the converter factory
	 */
	IConverterLocatorFactory getConverterSupplierFactory();

	/**
	 * Gets context path to use for absolute path generation. For example an
	 * Application Server that is used as a virtual server on a Webserver:
	 * 
	 * <pre>
	 *        appserver.com/context mapped to webserver/ (context path should be '/')
	 * </pre>
	 * 
	 * @return The context path
	 * 
	 * @see IApplicationSettings#setContextPath(String) what the possible values
	 *      can be.
	 */
	String getContextPath();

	/**
	 * @return Returns the defaultLocale.
	 */
	Locale getDefaultLocale();

	/**
	 * Gets internal error page class.
	 * 
	 * @return Returns the internalErrorPage.
	 * @see IApplicationSettings#setInternalErrorPage(Class)
	 */
	Class<? extends Page> getInternalErrorPage();

	/**
	 * Gets the page expired page class.
	 * 
	 * @return Returns the pageExpiredErrorPage.
	 * @see IApplicationSettings#setPageExpiredErrorPage(Class)
	 */
	Class<? extends Page> getPageExpiredErrorPage();

	/**
	 * Sets the access denied page class. The class must be bookmarkable and
	 * must extend Page.
	 * 
	 * @param accessDeniedPage
	 *            The accessDeniedPage to set.
	 */
	void setAccessDeniedPage(final Class<? extends Page> accessDeniedPage);

	/**
	 * Sets the default class resolver to use when finding classes.
	 * 
	 * @param defaultClassResolver
	 *            The default class resolver
	 */
	void setClassResolver(final IClassResolver defaultClassResolver);


	/**
	 * Sets the CoverterLocatorFactory
	 * 
	 * @param factory
	 */
	public void setConverterSupplierFactory(IConverterLocatorFactory factory);

	/**
	 * Sets context path to use for absolute path generation. For example an
	 * Application Server that is used as a virtual server on a Webserver:
	 * 
	 * <pre>
	 *        appserver.com/context mapped to webserver/ (context path should be '/')
	 * </pre>
	 * 
	 * This method can be called in the init phase of the application with the
	 * servlet init parameter {@link Application#CONTEXTPATH} if it is specified
	 * or by the developer itself in the {@link WebApplication} init() method.
	 * If it is not set in the init phase of the application it will be set
	 * automatically on the context path of the request
	 * {@link WebRequest#getContextPath()}
	 * 
	 * @param contextPath
	 *            The context path to use.
	 */
	void setContextPath(String contextPath);

	/**
	 * @param defaultLocale
	 *            The defaultLocale to set.
	 */
	void setDefaultLocale(Locale defaultLocale);

	/**
	 * Sets internal error page class. The class must be bookmarkable and must
	 * extend Page.
	 * 
	 * @param internalErrorPage
	 *            The internalErrorPage to set.
	 */
	void setInternalErrorPage(final Class<? extends Page> internalErrorPage);

	/**
	 * Sets the page expired page class. The class must be bookmarkable and must
	 * extend Page.
	 * 
	 * @param pageExpiredErrorPage
	 *            The pageExpiredErrorPage to set.
	 */
	void setPageExpiredErrorPage(final Class<? extends Page> pageExpiredErrorPage);

}
