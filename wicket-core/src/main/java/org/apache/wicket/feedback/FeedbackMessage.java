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
package org.apache.wicket.feedback;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.model.IDetachable;


/**
 * Represents a generic message meant for the end-user/ pages.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public class FeedbackMessage implements IDetachable
{
	private static final long serialVersionUID = 1L;

	public static final String UNDEFINED_CSS_CLASS_KEY = "wicket-core-feedback-message-undefined-css-class";

	public static final String DEBUG_CSS_CLASS_KEY = "wicket-core-feedback-message-debug-css-class";

	public static final String INFO_CSS_CLASS_KEY = "wicket-core-feedback-message-info-css-class";

	public static final String SUCCESS_CSS_CLASS_KEY = "wicket-core-feedback-message-success-css-class";

	public static final String WARNING_CSS_CLASS_KEY = "wicket-core-feedback-message-warning-css-class";

	public static final String ERROR_CSS_CLASS_KEY = "wicket-core-feedback-message-error-css-class";

	public static final String FATAL_CSS_CLASS_KEY = "wicket-core-feedback-message-fatal-css-class";

	/**
	 * Constant for an undefined level; note that components might decide not to render anything
	 * when this level is used.
	 */
	public static final int UNDEFINED = 0;

	/** Constant for debug level. */
	public static final int DEBUG = 100;

	/** Constant for info level. */
	public static final int INFO = 200;

	/** Constant for success level (it indicates the outcome of an operation) */
	public static final int SUCCESS = 250;

	/** Constant for warning level. */
	public static final int WARNING = 300;

	/** Constant for error level. */
	public static final int ERROR = 400;

	/** Constant for fatal level. */
	public static final int FATAL = 500;

	/** Levels as strings for debugging. */
	private static final Map<Integer, String> levelStrings = new HashMap<>();

	static
	{
		levelStrings.put(UNDEFINED, "UNDEFINED");
		levelStrings.put(DEBUG, "DEBUG");
		levelStrings.put(INFO, "INFO");
		levelStrings.put(SUCCESS, "SUCCESS");
		levelStrings.put(WARNING, "WARNING");
		levelStrings.put(ERROR, "ERROR");
		levelStrings.put(FATAL, "FATAL");
	}

	/**
	 * The message level; can be used by rendering components. Note that what actually happens with
	 * the level indication is totally up to the components that render messages like these. The
	 * default level is UNDEFINED.
	 */
	private final int level;

	/** The actual message. */
	private final Serializable message;

	/** The reporting component. */
	private Component reporter;

	/** Whether or not this message has been rendered */
	private boolean rendered = false;

	/**
	 * Construct using fields.
	 * 
	 * @param reporter
	 *            The message reporter
	 * @param message
	 *            The actual message. Must not be <code>null</code>.
	 * @param level
	 *            The level of the message
	 */
	public FeedbackMessage(final Component reporter, final Serializable message, final int level)
	{
		if (message == null)
		{
			throw new IllegalArgumentException("Parameter message can't be null");
		}
		this.reporter = reporter;
		this.message = message;
		this.level = level;
	}

	/**
	 * Gets whether or not this message has been rendered
	 * 
	 * @return true if this message has been rendered, false otherwise
	 */
	public final boolean isRendered()
	{
		return rendered;
	}


	/**
	 * Marks this message as rendered.
	 */
	public final void markRendered()
	{
		rendered = true;
	}


	/**
	 * Gets the message level; can be used by rendering components. Note that what actually happens
	 * with the level indication is totally up to the components that render feedback messages.
	 * 
	 * @return The message level indicator.
	 */
	public final int getLevel()
	{
		return level;
	}

	/**
	 * Gets the current level as a String
	 * 
	 * @return The current level as a String
	 */
	public String getLevelAsString()
	{
		return levelStrings.get(getLevel());
	}

	/**
	 * Gets the actual message.
	 * 
	 * @return the message.
	 */
	public final Serializable getMessage()
	{
		return message;
	}

	/**
	 * Gets the reporting component.
	 * 
	 * @return the reporting component.
	 */
	public final Component getReporter()
	{
		return reporter;
	}

	/**
	 * Gets whether the current level is DEBUG or up.
	 * 
	 * @return whether the current level is DEBUG or up.
	 */
	public final boolean isDebug()
	{
		return isLevel(DEBUG);
	}

	/**
	 * Gets whether the current level is INFO or up.
	 * 
	 * @return whether the current level is INFO or up.
	 */
	public final boolean isInfo()
	{
		return isLevel(INFO);
	}

	/**
	 * Gets whether the current level is SUCCESS or up.
	 * 
	 * @return whether the current level is SUCCESS or up.
	 */
	public final boolean isSuccess()
	{
		return isLevel(SUCCESS);
	}

	/**
	 * Gets whether the current level is WARNING or up.
	 * 
	 * @return whether the current level is WARNING or up.
	 */
	public final boolean isWarning()
	{
		return isLevel(WARNING);
	}

	/**
	 * Gets whether the current level is ERROR or up.
	 * 
	 * @return whether the current level is ERROR or up.
	 */
	public final boolean isError()
	{
		return isLevel(ERROR);
	}

	/**
	 * Gets whether the current level is FATAL or up.
	 * 
	 * @return whether the current level is FATAL or up.
	 */
	public final boolean isFatal()
	{
		return isLevel(FATAL);
	}

	/**
	 * Returns whether this level is greater than or equal to the given level.
	 * 
	 * @param level
	 *            the level
	 * @return whether this level is greater than or equal to the given level
	 */
	public final boolean isLevel(int level)
	{
		return (getLevel() >= level);
	}

	/**
	 * Gets whether the current level is UNDEFINED.
	 * 
	 * @return whether the current level is UNDEFINED.
	 */
	public final boolean isUndefined()
	{
		return (getLevel() == UNDEFINED);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[FeedbackMessage message = \"" + getMessage() + "\", reporter = " +
			((getReporter() == null) ? "null" : getReporter().getId()) + ", level = " +
			getLevelAsString() + ']';
	}

	/** {@inheritDoc} */
	@Override
	public void detach()
	{
		// no-op
	}
}
