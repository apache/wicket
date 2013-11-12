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
package org.apache.wicket.jmx;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.lang.Bytes;

/**
 * Application settings.
 * 
 * @author eelcohillenius
 */
public interface ApplicationSettingsMBean
{
	/**
	 * Gets the access denied page class.
	 * 
	 * @return Returns the accessDeniedPage.
	 * @see org.apache.wicket.settings.def.ApplicationSettings#getAccessDeniedPage()
	 */
	String getAccessDeniedPage();

	/**
	 * Gets the default resolver to use when finding classes
	 * 
	 * @return Default class resolver
	 */
	String getClassResolver();

	/**
	 * Gets the default maximum size for uploads. This is used by {@link Form#getMaxSize()} if no
	 * value is explicitly set through {@link Form#setMaxSize(Bytes)}.
	 * 
	 * @return the default maximum size for uploads
	 */
	String getDefaultMaximumUploadSize();

	/**
	 * Gets internal error page class.
	 * 
	 * @return Returns the internalErrorPage.
	 * @see org.apache.wicket.settings.def.ApplicationSettings#getInternalErrorPage()
	 */
	String getInternalErrorPage();

	/**
	 * Gets the page expired page class.
	 * 
	 * @return Returns the pageExpiredErrorPage.
	 * @see org.apache.wicket.settings.def.ApplicationSettings#getPageExpiredErrorPage()
	 */
	String getPageExpiredErrorPage();

	/**
	 * Gets the unexpected exception display.
	 * 
	 * @return the unexpected exception display
	 */
	String getUnexpectedExceptionDisplay();

	/**
	 * Sets the default maximum size for uploads. This is used by {@link Form#getMaxSize()} if no
	 * value is explicitly set through {@link Form#setMaxSize(Bytes)}. The String value should be a
	 * floating point value followed by K, M, G or T for kilobytes, megabytes, gigabytes or
	 * terabytes, respectively. The abbreviations KB, MB, GB and TB are also accepted. Matching is
	 * case insensitive.
	 * 
	 * @param defaultUploadSize
	 *            the default maximum size for uploads
	 */
	void setDefaultMaximumUploadSize(String defaultUploadSize);
}
