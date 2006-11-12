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
package wicket.jmx;

import wicket.settings.IApplicationSettings;

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
	 * @see IApplicationSettings#setAccessDeniedPage(Class)
	 */
	String getAccessDeniedPage();

	/**
	 * Gets the default resolver to use when finding classes
	 * 
	 * @return Default class resolver
	 */
	String getClassResolver();

	/**
	 * Gets context path to use for absolute path generation. For example an
	 * Application Server that is used as a virtual server on a Webserver:
	 * 
	 * <pre>
	 *           appserver.com/context mapped to webserver/ (context path should be '/')
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
	String getConverterLocatorFactory();

	/**
	 * Gets internal error page class.
	 * 
	 * @return Returns the internalErrorPage.
	 * @see IApplicationSettings#setInternalErrorPage(Class)
	 */
	String getInternalErrorPage();

	/**
	 * Gets the page expired page class.
	 * 
	 * @return Returns the pageExpiredErrorPage.
	 * @see IApplicationSettings#setPageExpiredErrorPage(Class)
	 */
	String getPageExpiredErrorPage();

	/**
	 * Gets the unexpected exception display.
	 * 
	 * @return the unexpected exception display
	 */
	String getUnexpectedExceptionDisplay();
}
