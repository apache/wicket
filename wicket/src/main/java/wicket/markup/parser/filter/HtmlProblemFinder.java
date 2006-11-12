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
package wicket.markup.parser.filter;

import java.text.ParseException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupElement;
import wicket.markup.parser.AbstractMarkupFilter;

/**
 * This is a markup inline filter which by default is not added to the list of
 * markup filter. It can be added by means of subclassing
 * Application.newMarkupParser() like
 * 
 * <pre>
 *        public class MyApplication extends Application
 *        {
 *            ...
 *            public IMarkupFilter[] getAdditionalMarkupHandler()
 *            {
 *                return new IMarkupFilter[] { new HtmlProblemFinder(HtmlProblemFinder.ERR_THROW_EXCEPTION) };
 *            }
 * </pre>
 * 
 * The purpose of the filter is to find possible HTML issues and to log a
 * warning.
 * 
 * @author Juergen Donnerstag
 */
public final class HtmlProblemFinder extends AbstractMarkupFilter
{
	/** Logging */
	private static final Log log = LogFactory.getLog(HtmlProblemFinder.class);

	/** Ignore the issue detected */
	public static final int ERR_INGORE = 3;

	/** Log a warning on the issue detected */
	public static final int ERR_LOG_WARN = 2;

	/** Log an error on the issue detected */
	public static final int ERR_LOG_ERROR = 1;

	/** Throw an exception on the issue detected */
	public static final int ERR_THROW_EXCEPTION = 0;

	/** Default behavior in case of a potential HTML issue detected */
	private final int problemEscalation;

	/**
	 * Construct.
	 * 
	 * @param problemEscalation
	 *            How to escalate the issue found.
	 */
	public HtmlProblemFinder(final int problemEscalation)
	{
		this.problemEscalation = problemEscalation;
	}

	/**
	 * Get the next MarkupElement from the parent MarkupFilter and handle it if
	 * the specific filter criteria are met. Depending on the filter, it may
	 * return the MarkupElement unchanged, modified or it remove by asking the
	 * parent handler for the next tag.
	 * 
	 * @see wicket.markup.parser.IMarkupFilter#nextTag()
	 * @return Return the next eligible MarkupElement
	 */
	public MarkupElement nextTag() throws ParseException
	{
		// Get the next tag. If null, no more tags are available
		final ComponentTag tag = nextComponentTag();
		if (tag == null)
		{
			return tag;
		}

		// Make sure some typical and may be tricky problems are detected and
		// logged.
		if ("img".equals(tag.getName()) && (tag.isOpen() || tag.isOpenClose()))
		{
			final String src = tag.getAttributes().getString("src");
			if ((src != null) && (src.trim().length() == 0))
			{
				escalateWarning("Attribute 'src' should not be empty. Location: ", tag);
			}
		}

		// Some people are using a dot "wicket.xxx" instead of a colon
		// "wicket:xxx"
		final Iterator iter = tag.getAttributes().keySet().iterator();
		while (iter.hasNext())
		{
			String key = (String)iter.next();
			if (key != null)
			{
				key = key.toLowerCase();
				if (key.startsWith("wicket."))
				{
					escalateWarning(
							"You probably want 'wicket:xxx' rather than 'wicket.xxx'. Location: ",
							tag);
				}
			}
		}

		return tag;
	}

	/**
	 * Handle the issue. Depending the setting either log a warning, an error,
	 * throw an exception or ignore it.
	 * 
	 * @param msg
	 *            The message
	 * @param tag
	 *            The current tag
	 * @throws ParseException
	 */
	private void escalateWarning(final String msg, final ComponentTag tag) throws ParseException
	{
		if (problemEscalation == ERR_LOG_WARN)
		{
			log.warn(msg + tag.toUserDebugString());
		}
		else if (problemEscalation == ERR_LOG_ERROR)
		{
			log.error(msg + tag.toUserDebugString());
		}
		else if (problemEscalation == ERR_INGORE)
		{
			// no action required
		}
		else
		// if (problemEscalation == ERR_THROW_EXCEPTION)
		{
			throw new ParseException(msg + tag.toUserDebugString(), tag.getPos());
		}
	}
}
