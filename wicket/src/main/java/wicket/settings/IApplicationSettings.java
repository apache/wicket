/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	 * Enumerated type for different ways of displaying unexpected exceptions.
	 */
	public static enum UnexpectedExceptionDisplay {
		/**
		 * Indicates that an exception page appropriate to development should be
		 * shown when an unexpected exception is thrown.
		 */
		SHOW_EXCEPTION_PAGE,
		/**
		 * Indicates a generic internal error page should be shown when an
		 * unexpected exception is thrown.
		 */
		SHOW_INTERNAL_ERROR_PAGE,
		/**
		 * Indicates that no exception page should be shown when an unexpected
		 * exception is thrown.
		 */
		SHOW_NO_EXCEPTION_PAGE
	}

	/**
	 * Sets the CoverterLocatorFactory
	 * 
	 * @param factory
	 */
	public void setConverterSupplierFactory(IConverterLocatorFactory factory);

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
	 * Gets context path to use for absolute path generation. For example an
	 * Application Server that is used as a virtual server on a Webserver:
	 * 
	 * <pre>
	 *         appserver.com/context mapped to webserver/ (context path should be '/')
	 * </pre>
	 * 
	 * @return The context path
	 * 
	 * @see IApplicationSettings#setContextPath(String) what the possible values
	 *      can be.
	 */
	String getContextPath();

	/**
	 * Gets the converter locator factory.
	 * 
	 * @return the converter locator factory
	 */
	IConverterLocatorFactory getConverterLocatorFactory();

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
	 * @return Returns the unexpectedExceptionDisplay.
	 */
	UnexpectedExceptionDisplay getUnexpectedExceptionDisplay();

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
	 * Sets context path to use for absolute path generation. For example an
	 * Application Server that is used as a virtual server on a Webserver:
	 * 
	 * <pre>
	 *         appserver.com/context mapped to webserver/ (context path should be '/')
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

	/**
	 * The exception display type determines how the framework displays
	 * exceptions to you as a developer or user.
	 * <p>
	 * The default value for exception display type is SHOW_EXCEPTION_PAGE. When
	 * this value is set and an unhandled runtime exception is thrown by a page,
	 * a redirect to a helpful exception display page will occur.
	 * <p>
	 * This is a developer feature, however, and you may want to instead show an
	 * internal error page without developer details that allows a user to start
	 * over at the application's home page. This can be accomplished by setting
	 * the exception display type to SHOW_INTERNAL_ERROR_PAGE.
	 * <p>
	 * Finally, if you are having trouble with the exception display pages
	 * themselves, you can disable exception displaying entirely with the value
	 * SHOW_NO_EXCEPTION_PAGE. This will cause the framework to re-throw any
	 * unhandled runtime exceptions after wrapping them in a ServletException
	 * wrapper.
	 * 
	 * @param unexpectedExceptionDisplay
	 *            The unexpectedExceptionDisplay to set.
	 */
	void setUnexpectedExceptionDisplay(UnexpectedExceptionDisplay unexpectedExceptionDisplay);

}
