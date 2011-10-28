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

import org.apache.wicket.settings.IExceptionSettings;
import org.apache.wicket.util.lang.Args;

/**
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 * @author Johan Compagner
 * @author Igor Vaynberg (ivaynberg)
 * @author Martijn Dashorst
 * @author James Carman
 */
public class ExceptionSettings implements IExceptionSettings
{
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
	 * @see org.apache.wicket.settings.IRequestCycleSettings#getUnexpectedExceptionDisplay()
	 */
	public UnexpectedExceptionDisplay getUnexpectedExceptionDisplay()
	{
		return unexpectedExceptionDisplay;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#setUnexpectedExceptionDisplay(org.apache.wicket.settings.Settings.UnexpectedExceptionDisplay)
	 */
	public void setUnexpectedExceptionDisplay(UnexpectedExceptionDisplay unexpectedExceptionDisplay)
	{
		this.unexpectedExceptionDisplay = unexpectedExceptionDisplay;
	}

	/**
	 * @see org.apache.wicket.settings.IExceptionSettings#getAjaxErrorHandlingStrategy()
	 */
	public AjaxErrorStrategy getAjaxErrorHandlingStrategy()
	{
		return errorHandlingStrategyDuringAjaxRequests;
	}

	/**
	 * @see org.apache.wicket.settings.IExceptionSettings#setAjaxErrorHandlingStrategy(org.apache.wicket.settings.IExceptionSettings.AjaxErrorStrategy)
	 */
	public void setAjaxErrorHandlingStrategy(
		AjaxErrorStrategy errorHandlingStrategyDuringAjaxRequests)
	{
		this.errorHandlingStrategyDuringAjaxRequests = errorHandlingStrategyDuringAjaxRequests;
	}

	public void setThreadDumpStrategy(ThreadDumpStrategy strategy)
	{
		threadDumpStrategy = Args.notNull(strategy, "strategy");
	}

	public ThreadDumpStrategy getThreadDumpStrategy()
	{
		return threadDumpStrategy;
	}
}
