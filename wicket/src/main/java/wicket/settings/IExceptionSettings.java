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

import wicket.util.lang.EnumeratedType;


/**
 * Settings interface for configuring exception handling related settings.
 * <p>
 * <i>unexpectedExceptionDisplay </i> (defaults to SHOW_EXCEPTION_PAGE) -
 * Determines how exceptions are displayed to the developer or user
 * <p>
 * <i>throwExceptionOnMissingResource </i> (defaults to true) - Set to true to
 * throw a runtime exception if a required string resource is not found. Set to
 * false to return the requested resource key surrounded by pairs of question
 * mark characters (e.g. "??missingKey??")
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 * @deprecated will be removed in 2.0. See methods for details
 */
public interface IExceptionSettings
{
	/**
	 * Enumerated type for different ways of displaying unexpected exceptions.
	 * 
	 * @deprecated will be moved to {@link IApplicationSettings} in 2.0
	 */
	public static final class UnexpectedExceptionDisplay extends EnumeratedType
	{
		private static final long serialVersionUID = 1L;

		UnexpectedExceptionDisplay(final String name)
		{
			super(name);
		}
	}

	/**
	 * Indicates that an exception page appropriate to development should be
	 * shown when an unexpected exception is thrown.
	 */
	public static final UnexpectedExceptionDisplay SHOW_EXCEPTION_PAGE = new UnexpectedExceptionDisplay(
			"SHOW_EXCEPTION_PAGE");
	/**
	 * Indicates a generic internal error page should be shown when an
	 * unexpected exception is thrown.
	 */
	public static final UnexpectedExceptionDisplay SHOW_INTERNAL_ERROR_PAGE = new UnexpectedExceptionDisplay(
			"SHOW_INTERNAL_ERROR_PAGE");

	/**
	 * Indicates that no exception page should be shown when an unexpected
	 * exception is thrown.
	 */
	public static final UnexpectedExceptionDisplay SHOW_NO_EXCEPTION_PAGE = new UnexpectedExceptionDisplay(
			"SHOW_NO_EXCEPTION_PAGE");

	/**
	 * @return Whether to throw an exception when a missing resource is
	 *         requested
	 * @deprecated use
	 *             {@link IResourceSettings#getThrowExceptionOnMissingResource()}
	 *             instead
	 */
	boolean getThrowExceptionOnMissingResource();

	/**
	 * @return Returns the unexpectedExceptionDisplay.
	 * @deprecated will be moved to {@link IApplicationSettings} in 2.0
	 */
	UnexpectedExceptionDisplay getUnexpectedExceptionDisplay();

	/**
	 * @param throwExceptionOnMissingResource
	 *            Whether to throw an exception when a missing resource is
	 *            requested
	 * @deprecated use
	 *             {@link IResourceSettings#setThrowExceptionOnMissingResource(boolean)}
	 *             instead
	 */
	void setThrowExceptionOnMissingResource(final boolean throwExceptionOnMissingResource);

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
	 * 
	 * @deprecated will be moved to {@link IApplicationSettings} in 2.0
	 */
	void setUnexpectedExceptionDisplay(UnexpectedExceptionDisplay unexpectedExceptionDisplay);
}
