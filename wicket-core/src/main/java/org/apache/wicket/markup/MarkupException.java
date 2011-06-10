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
package org.apache.wicket.markup;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.resource.IResourceStream;

/**
 * Runtime exception that is thrown when markup parsing fails.
 * 
 * @author Jonathan Locke
 */
public final class MarkupException extends WicketRuntimeException
{
	private static final long serialVersionUID = 1L;

	/** The markup stream that was being parsed when the exception was thrown */
	private transient MarkupStream markupStream;

	/**
	 * Constructor
	 * 
	 * @param message
	 *            The problem description
	 */
	public MarkupException(final String message)
	{
		super(message);
		markupStream = null;
	}

	/**
	 * @param resource
	 *            The markup resource where this exception occurred
	 * @param message
	 *            The message
	 */
	public MarkupException(final IResourceStream resource, final String message)
	{
		super(resource.toString() + ": " + message);
		markupStream = null;
	}

	/**
	 * @param resource
	 *            The markup where this exception occurred
	 * @param message
	 *            The message
	 * @param cause
	 *            The causing exception
	 */
	public MarkupException(final IResourceStream resource, final String message,
		final Throwable cause)
	{
		super(resource.toString() + ": " + message, cause);
		markupStream = null;
	}

	/**
	 * @param markupStream
	 *            The markup stream where this exception occurred
	 * @param message
	 *            The message
	 */
	public MarkupException(final MarkupStream markupStream, final String message)
	{
		super(message);
		this.markupStream = markupStream;
	}

	/**
	 * @param markupStream
	 *            The markup stream where this exception occurred
	 * @param message
	 *            The message
	 * @param cause
	 *            The causing exception
	 */
	public MarkupException(final MarkupStream markupStream, final String message,
		final Throwable cause)
	{
		super(message, cause);
		this.markupStream = markupStream;
	}

	/**
	 * Construct.
	 * 
	 * @param markup
	 * @param message
	 * @param cause
	 */
	public MarkupException(final Markup markup, final String message, final Throwable cause)
	{
		this(new MarkupStream(markup).setCurrentIndex(markup.size() - 1), message, cause);
	}

	/**
	 * @return Returns the MarkupStream.
	 */
	public MarkupStream getMarkupStream()
	{
		return markupStream;
	}

	/**
	 * Set the markup stream which caused the exception
	 * 
	 * @param markupStream
	 */
	public void setMarkupStream(final MarkupStream markupStream)
	{
		this.markupStream = markupStream;
	}

	@Override
	public String toString()
	{
		return getMessage() + "\n MarkupStream: " +
			(markupStream == null ? "[unknown]" : markupStream.toString());
	}
}
