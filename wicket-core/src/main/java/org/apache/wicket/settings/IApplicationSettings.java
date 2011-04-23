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

import org.apache.wicket.Page;
import org.apache.wicket.application.IClassResolver;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.lang.Bytes;

/**
 * Settings interface for application settings.
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
	 * Gets the default resolver to use when finding classes and resources.
	 * 
	 * @return Default class resolver
	 */
	IClassResolver getClassResolver();

	/**
	 * Gets the default maximum size for uploads. This is used by {@link Form#getMaxSize()} if no
	 * value is explicitly set through {@link Form#setMaxSize(Bytes)}.
	 * 
	 * @return the default maximum size for uploads
	 */
	Bytes getDefaultMaximumUploadSize();

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
	 * Gets whether wicket is providing updates about the upload progress or not.
	 * 
	 * @return if true upload progress monitoring is enabled
	 */
	boolean isUploadProgressUpdatesEnabled();

	/**
	 * Sets the access denied page class. The class must be bookmarkable and must extend Page.
	 * 
	 * @param accessDeniedPage
	 *            The accessDeniedPage to set.
	 */
	void setAccessDeniedPage(final Class<? extends Page> accessDeniedPage);

	/**
	 * Sets the default class resolver to use when finding classes and resources.
	 * 
	 * @param defaultClassResolver
	 *            The default class resolver
	 */
	void setClassResolver(final IClassResolver defaultClassResolver);

	/**
	 * Sets the default maximum size for uploads. This is used by {@link Form#getMaxSize()} if no
	 * value is explicitly set through {@link Form#setMaxSize(Bytes)}.
	 * 
	 * @param defaultUploadSize
	 *            the default maximum size for uploads
	 */
	void setDefaultMaximumUploadSize(Bytes defaultUploadSize);

	/**
	 * Sets internal error page class. The class must be bookmarkable and must extend Page.
	 * 
	 * @param internalErrorPage
	 *            The internalErrorPage to set.
	 */
	void setInternalErrorPage(final Class<? extends Page> internalErrorPage);

	/**
	 * Sets the page expired page class. The class must be bookmarkable and must extend Page.
	 * 
	 * @param pageExpiredErrorPage
	 *            The pageExpiredErrorPage to set.
	 */
	void setPageExpiredErrorPage(final Class<? extends Page> pageExpiredErrorPage);

	/**
	 * Sets whether wicket should provide updates about the upload progress or not.
	 * 
	 * @param uploadProgressUpdatesEnabled
	 *            if true upload progress monitoring is enabled
	 */
	void setUploadProgressUpdatesEnabled(boolean uploadProgressUpdatesEnabled);
}
