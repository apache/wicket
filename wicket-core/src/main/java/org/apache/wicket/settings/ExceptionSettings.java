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

import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.EnumeratedType;

/**
 *
 * Settings interface for configuring exception handling related settings.
 * <p>
 * <i>unexpectedExceptionDisplay </i> (defaults to SHOW_EXCEPTION_PAGE) - Determines how exceptions
 * are displayed to the developer or user
 * <p>
 * <i>throwExceptionOnMissingResource </i> (defaults to true) - Set to true to throw a runtime
 * exception if a required string resource is not found. Set to false to return the requested
 * resource key surrounded by pairs of question mark characters (e.g. "??missingKey??")
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
public class ExceptionSettings
{
	/**
	 * Enumerated type for different ways of displaying unexpected exceptions.
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
	 * Indicates that an exception page appropriate to development should be shown when an
	 * unexpected exception is thrown.
	 */
	public static final UnexpectedExceptionDisplay SHOW_EXCEPTION_PAGE = new UnexpectedExceptionDisplay(
			"SHOW_EXCEPTION_PAGE");
	/**
	 * Indicates a generic internal error page should be shown when an unexpected exception is
	 * thrown.
	 */
	public static final UnexpectedExceptionDisplay SHOW_INTERNAL_ERROR_PAGE = new UnexpectedExceptionDisplay(
			"SHOW_INTERNAL_ERROR_PAGE");

	/**
	 * Indicates that no exception page should be shown when an unexpected exception is thrown.
	 */
	public static final UnexpectedExceptionDisplay SHOW_NO_EXCEPTION_PAGE = new UnexpectedExceptionDisplay(
			"SHOW_NO_EXCEPTION_PAGE");

	/**
	 * How to handle errors while processing an Ajax request
	 *
	 * @author igor
	 */
	public static enum AjaxErrorStrategy {
		/** redirect to error page, just like a normal requset */
		REDIRECT_TO_ERROR_PAGE,
		/** invoke client side failure handler */
		INVOKE_FAILURE_HANDLER
	}

	/**
	 * Which threads' stacktrace to dump when a page lock timeout occurs
	 *
	 * @author papegaaij
	 */
	public static enum ThreadDumpStrategy {
		/** Do not dump any stacktraces */
		NO_THREADS,
		/** Dump the stacktrace of the thread holding the lock */
		THREAD_HOLDING_LOCK,
		/** Dump stacktraces of all threads of the application */
		ALL_THREADS
	}

	/** Type of handling for unexpected exceptions */
	private UnexpectedExceptionDisplay unexpectedExceptionDisplay = SHOW_EXCEPTION_PAGE;

	private AjaxErrorStrategy errorHandlingStrategyDuringAjaxRequests = AjaxErrorStrategy.REDIRECT_TO_ERROR_PAGE;

	/**
	 * Strategy to use for dumping stack traces of live threads in the JVM.
	 * <p>
	 * By default will dump the stacktrace of the thread that holds the lock on the page.
	 * </p>
	 */
	private ThreadDumpStrategy threadDumpStrategy = ThreadDumpStrategy.THREAD_HOLDING_LOCK;

	/**
	 * @return Returns the unexpectedExceptionDisplay.
	 */
	public UnexpectedExceptionDisplay getUnexpectedExceptionDisplay()
	{
		return unexpectedExceptionDisplay;
	}

	/**
	 * The exception display type determines how the framework displays exceptions to you as a
	 * developer or user.
	 * <p>
	 * The default value for exception display type is SHOW_EXCEPTION_PAGE. When this value is set
	 * and an unhandled runtime exception is thrown by a page, a redirect to a helpful exception
	 * display page will occur.
	 * <p>
	 * This is a developer feature, however, and you may want to instead show an internal error page
	 * without developer details that allows a user to start over at the application's home page.
	 * This can be accomplished by setting the exception display type to SHOW_INTERNAL_ERROR_PAGE.
	 * <p>
	 * Finally, if you are having trouble with the exception display pages themselves, you can
	 * disable exception displaying entirely with the value SHOW_NO_EXCEPTION_PAGE. This will cause
	 * the framework to re-throw any unhandled runtime exceptions after wrapping them in a
	 * ServletException wrapper.
	 *
	 * @param unexpectedExceptionDisplay
	 *            The unexpectedExceptionDisplay to set.
	 * @return {@code this} object for chaining
	 */
	public ExceptionSettings setUnexpectedExceptionDisplay(UnexpectedExceptionDisplay unexpectedExceptionDisplay)
	{
		this.unexpectedExceptionDisplay = unexpectedExceptionDisplay;
		return this;
	}

	/**
	 * @return strategy used to handle errors during Ajax request processing
	 */
	public AjaxErrorStrategy getAjaxErrorHandlingStrategy()
	{
		return errorHandlingStrategyDuringAjaxRequests;
	}

	/**
	 * Sets strategy used to handle errors during Ajax request processing
	 *
	 * @param errorHandlingStrategyDuringAjaxRequests
	 * @return {@code this} object for chaining
	 */
	public ExceptionSettings setAjaxErrorHandlingStrategy(
		AjaxErrorStrategy errorHandlingStrategyDuringAjaxRequests)
	{
		this.errorHandlingStrategyDuringAjaxRequests = errorHandlingStrategyDuringAjaxRequests;
		return this;
	}

	/**
	 * Sets the strategy to use for dumping stack traces of live threads in the JVM.
	 *
	 * @param strategy
	 * @return {@code this} object for chaining
	 */
	public ExceptionSettings setThreadDumpStrategy(ThreadDumpStrategy strategy)
	{
		threadDumpStrategy = Args.notNull(strategy, "strategy");
		return this;
	}

	/**
	 * @return strategy to use for dumping stack traces of live threads in the JVM.
	 */
	public ThreadDumpStrategy getThreadDumpStrategy()
	{
		return threadDumpStrategy;
	}
}
