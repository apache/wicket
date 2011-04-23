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
package org.apache.wicket.settings.def;

import java.lang.ref.WeakReference;

import org.apache.wicket.Page;
import org.apache.wicket.application.DefaultClassResolver;
import org.apache.wicket.application.IClassResolver;
import org.apache.wicket.settings.IApplicationSettings;
import org.apache.wicket.util.lang.Bytes;

/**
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
public class ApplicationSettings implements IApplicationSettings
{
	private WeakReference<Class<? extends Page>> accessDeniedPage;

	private IClassResolver classResolver = new DefaultClassResolver();

	private WeakReference<Class<? extends Page>> internalErrorPage;

	private WeakReference<Class<? extends Page>> pageExpiredErrorPage;

	private Bytes defaultMaximumUploadSize = Bytes.MAX;

	private boolean uploadProgressUpdatesEnabled = false;

	/**
	 * @see org.apache.wicket.settings.IApplicationSettings#getAccessDeniedPage()
	 */
	public Class<? extends Page> getAccessDeniedPage()
	{
		return accessDeniedPage.get();
	}

	/**
	 * @see org.apache.wicket.settings.IApplicationSettings#getClassResolver()
	 */
	public IClassResolver getClassResolver()
	{
		return classResolver;
	}

	public Bytes getDefaultMaximumUploadSize()
	{
		return defaultMaximumUploadSize;
	}

	/**
	 * @see org.apache.wicket.settings.IApplicationSettings#getInternalErrorPage()
	 */
	public Class<? extends Page> getInternalErrorPage()
	{
		return internalErrorPage.get();
	}

	/**
	 * @see org.apache.wicket.settings.IApplicationSettings#getPageExpiredErrorPage()
	 */
	public Class<? extends Page> getPageExpiredErrorPage()
	{
		return pageExpiredErrorPage.get();
	}

	/**
	 * @see org.apache.wicket.settings.IApplicationSettings#isUploadProgressUpdatesEnabled()
	 */
	public boolean isUploadProgressUpdatesEnabled()
	{
		return uploadProgressUpdatesEnabled;
	}

	/**
	 * @see org.apache.wicket.settings.IApplicationSettings#setAccessDeniedPage(java.lang.Class)
	 */
	public void setAccessDeniedPage(Class<? extends Page> accessDeniedPage)
	{
		if (accessDeniedPage == null)
		{
			throw new IllegalArgumentException("Argument accessDeniedPage may not be null");
		}
		checkPageClass(accessDeniedPage);

		this.accessDeniedPage = new WeakReference<Class<? extends Page>>(accessDeniedPage);
	}

	/**
	 * @see org.apache.wicket.settings.IApplicationSettings#setClassResolver(org.apache.wicket.application.IClassResolver)
	 */
	public void setClassResolver(final IClassResolver defaultClassResolver)
	{
		classResolver = defaultClassResolver;
	}

	/**
	 * @see org.apache.wicket.settings.IApplicationSettings#setDefaultMaximumUploadSize(org.apache.wicket.util.lang.Bytes)
	 */
	public void setDefaultMaximumUploadSize(Bytes defaultMaximumUploadSize)
	{
		this.defaultMaximumUploadSize = defaultMaximumUploadSize;
	}

	/**
	 * @see org.apache.wicket.settings.IApplicationSettings#setInternalErrorPage(java.lang.Class)
	 */
	public void setInternalErrorPage(final Class<? extends Page> internalErrorPage)
	{
		if (internalErrorPage == null)
		{
			throw new IllegalArgumentException("Argument internalErrorPage may not be null");
		}
		checkPageClass(internalErrorPage);

		this.internalErrorPage = new WeakReference<Class<? extends Page>>(internalErrorPage);
	}

	/**
	 * @see org.apache.wicket.settings.IApplicationSettings#setPageExpiredErrorPage(java.lang.Class)
	 */
	public void setPageExpiredErrorPage(final Class<? extends Page> pageExpiredErrorPage)
	{
		if (pageExpiredErrorPage == null)
		{
			throw new IllegalArgumentException("Argument pageExpiredErrorPage may not be null");
		}
		checkPageClass(pageExpiredErrorPage);

		this.pageExpiredErrorPage = new WeakReference<Class<? extends Page>>(pageExpiredErrorPage);
	}

	/**
	 * @see org.apache.wicket.settings.IApplicationSettings#setUploadProgressUpdatesEnabled(boolean)
	 */
	public void setUploadProgressUpdatesEnabled(boolean uploadProgressUpdatesEnabled)
	{
		this.uploadProgressUpdatesEnabled = uploadProgressUpdatesEnabled;
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
}
