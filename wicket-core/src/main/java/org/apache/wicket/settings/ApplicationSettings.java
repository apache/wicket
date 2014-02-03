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
package org.apache.wicket.settings;

import java.lang.ref.WeakReference;

import org.apache.wicket.Page;
import org.apache.wicket.application.DefaultClassResolver;
import org.apache.wicket.application.IClassResolver;
import org.apache.wicket.feedback.DefaultCleanupFeedbackMessageFilter;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;

/**
 * * Settings interface for application settings.
 * <p>
 * <i>internalErrorPage </i>- You can override this with your own page class to display internal
 * errors in a different way.
 * <p>
 * <i>pageExpiredErrorPage </i>- You can override this with your own bookmarkable page class to
 * display expired page errors in a different way. You can set property homePageRenderStrategy to
 * choose from different ways the home page url shows up in your browser.
 * <p>
 * <b>A Converter Factory </b>- By overriding getConverterFactory(), you can provide your own
 * factory which creates locale sensitive Converter instances.
 *
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 * @author Johan Compagner
 * @author Igor Vaynberg (ivaynberg)
 * @author Martijn Dashorst
 * @author James Carman
 */
public class ApplicationSettings
{
	private WeakReference<Class<? extends Page>> accessDeniedPage;

	private IClassResolver classResolver = new DefaultClassResolver();

	private WeakReference<Class<? extends Page>> internalErrorPage;

	private WeakReference<Class<? extends Page>> pageExpiredErrorPage;

	private Bytes defaultMaximumUploadSize = Bytes.MAX;

	private boolean uploadProgressUpdatesEnabled = false;

	private IFeedbackMessageFilter feedbackMessageCleanupFilter = new DefaultCleanupFeedbackMessageFilter();

	/**
	 * Gets the access denied page class.
	 *
	 * @return Returns the accessDeniedPage.
	 */
	public Class<? extends Page> getAccessDeniedPage()
	{
		return accessDeniedPage.get();
	}

	/**
	 * Gets the default resolver to use when finding classes and resources.
	 *
	 * @return Default class resolver
	 */
	public IClassResolver getClassResolver()
	{
		return classResolver;
	}

	/**
	 * Gets the default maximum size for uploads. This is used by {@link org.apache.wicket.markup.html.form.Form#getMaxSize()} if no
	 * value is explicitly set through {@link org.apache.wicket.markup.html.form.Form#setMaxSize(Bytes)}.
	 *
	 * @return the default maximum size for uploads
	 */
	public Bytes getDefaultMaximumUploadSize()
	{
		return defaultMaximumUploadSize;
	}

	/**
	 * Gets internal error page class.
	 *
	 * @return Returns the internalErrorPage.
	 */
	public Class<? extends Page> getInternalErrorPage()
	{
		return internalErrorPage.get();
	}

	/**
	 * Gets the page expired page class.
	 *
	 * @return Returns the pageExpiredErrorPage.
	 */
	public Class<? extends Page> getPageExpiredErrorPage()
	{
		return pageExpiredErrorPage.get();
	}

	/**
	 * Gets whether wicket is providing updates about the upload progress or not.
	 *
	 * @return if true upload progress monitoring is enabled
	 */
	public boolean isUploadProgressUpdatesEnabled()
	{
		return uploadProgressUpdatesEnabled;
	}

	/**
	 * Sets the access denied page class. The class must be bookmarkable and must extend Page.
	 *
	 * @param accessDeniedPage
	 *            The accessDeniedPage to set.
	 * @return {@code this} object for chaining
	 */
	public ApplicationSettings setAccessDeniedPage(Class<? extends Page> accessDeniedPage)
	{
		if (accessDeniedPage == null)
		{
			throw new IllegalArgumentException("Argument accessDeniedPage may not be null");
		}
		checkPageClass(accessDeniedPage);

		this.accessDeniedPage = new WeakReference<Class<? extends Page>>(accessDeniedPage);
		return this;
	}

	/**
	 * Sets the default class resolver to use when finding classes and resources.
	 *
	 * @param defaultClassResolver
	 *            The default class resolver
	 * @return {@code this} object for chaining
	 */
	public ApplicationSettings setClassResolver(final IClassResolver defaultClassResolver)
	{
		classResolver = defaultClassResolver;
		return this;
	}

	/**
	 * Sets the default maximum size for uploads. This is used by {@link org.apache.wicket.markup.html.form.Form#getMaxSize()} if no
	 * value is explicitly set through {@link org.apache.wicket.markup.html.form.Form#setMaxSize(Bytes)}.
	 *
	 * @param defaultMaximumUploadSize
	 *            the default maximum size for uploads
	 * @return {@code this} object for chaining
	 */
	public ApplicationSettings setDefaultMaximumUploadSize(Bytes defaultMaximumUploadSize)
	{
		this.defaultMaximumUploadSize = defaultMaximumUploadSize;
		return this;
	}

	/**
	 * Sets internal error page class. The class must be bookmarkable and must extend Page.
	 *
	 * @param internalErrorPage
	 *            The internalErrorPage to set.
	 * @return {@code this} object for chaining
	 */
	public ApplicationSettings setInternalErrorPage(final Class<? extends Page> internalErrorPage)
	{
		Args.notNull(internalErrorPage, "internalErrorPage");
		checkPageClass(internalErrorPage);

		this.internalErrorPage = new WeakReference<Class<? extends Page>>(internalErrorPage);
		return this;
	}

	/**
	 * Sets the page expired page class. The class must be bookmarkable and must extend Page.
	 *
	 * @param pageExpiredErrorPage
	 *            The pageExpiredErrorPage to set.
	 * @return {@code this} object for chaining
	 */
	public ApplicationSettings setPageExpiredErrorPage(final Class<? extends Page> pageExpiredErrorPage)
	{
		if (pageExpiredErrorPage == null)
		{
			throw new IllegalArgumentException("Argument pageExpiredErrorPage may not be null");
		}
		checkPageClass(pageExpiredErrorPage);

		this.pageExpiredErrorPage = new WeakReference<Class<? extends Page>>(pageExpiredErrorPage);
		return this;
	}

	/**
	 * Sets whether wicket should provide updates about the upload progress or not.
	 *
	 * @param uploadProgressUpdatesEnabled
	 *            if true upload progress monitoring is enabled
	 * @return {@code this} object for chaining
	 */
	public ApplicationSettings setUploadProgressUpdatesEnabled(boolean uploadProgressUpdatesEnabled)
	{
		this.uploadProgressUpdatesEnabled = uploadProgressUpdatesEnabled;
		return this;
	}

	/**
	 * Throws an IllegalArgumentException if the given class is not a subclass of Page.
	 * 
	 * @param <C>
	 * @param pageClass
	 *            the page class to check
	 */
	private <C extends Page> void checkPageClass(final Class<C> pageClass)
	{
		// NOTE: we can't really check on whether it is a bookmarkable page
		// here, as - though the default is that a bookmarkable page must
		// either have a default constructor and/or a constructor with a
		// PageParameters object, this could be different for another
		// IPageFactory implementation
		if (!Page.class.isAssignableFrom(pageClass))
		{
			throw new IllegalArgumentException("argument " + pageClass +
				" must be a subclass of Page");
		}
	}

	/**
	 * Sets the cleanup feedback message filter. see {@link #getFeedbackMessageCleanupFilter()} for
	 * more details.
	 *
	 * @param filter
	 * @return {@code this} object for chaining
	 */
	public ApplicationSettings setFeedbackMessageCleanupFilter(IFeedbackMessageFilter filter)
	{
		Args.notNull(filter, "filter");
		feedbackMessageCleanupFilter = filter;
		return this;
	}

	/**
	 * Returns the cleanup feedack message filter. At the end of request all messages are ran
	 * through this filter, and the ones accepted are removed. The default implementation accepts
	 * (and therefore remkoves) all rendered messages.
	 *
	 * @return feedback message filter
	 */
	public IFeedbackMessageFilter getFeedbackMessageCleanupFilter()
	{
		return feedbackMessageCleanupFilter;
	}
}
