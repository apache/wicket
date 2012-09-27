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
package org.apache.wicket.markup.parser.filter;

import java.text.ParseException;
import java.util.regex.Pattern;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.RawMarkup;
import org.apache.wicket.markup.parser.AbstractMarkupFilter;
import org.apache.wicket.markup.parser.XmlPullParser;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.util.lang.Args;


/**
 * An IMarkupFilter that wraps the body of all &lt;style&gt; elements and &lt;script&gt;
 * elements which are plain JavaScript in CDATA blocks. This allows the user application
 * to use unescaped XML characters without caring that those may break Wicket's XML Ajax
 * response.
 * 
 * @author Juergen Donnerstag
 */
public final class StyleAndScriptIdentifier extends AbstractMarkupFilter
{
	/**
	 * Constructor.
	 */
	public StyleAndScriptIdentifier()
	{
	}

	@Override
	protected final MarkupElement onComponentTag(final ComponentTag tag) throws ParseException
	{
		if (tag.getNamespace() != null)
		{
			return tag;
		}

		String tagName = tag.getName();
		boolean isScript = XmlPullParser.SCRIPT.equalsIgnoreCase(tagName);
		boolean isStyle = XmlPullParser.STYLE.equalsIgnoreCase(tagName);
		if (isScript || isStyle)
		{
			if (tag.isOpen() && tag.getId() == null && ((isScript && tag.getAttribute("src") == null) || isStyle))
			{
				// Not needed, but must not be null
				tag.setId("_ScriptStyle");
				tag.setModified(true);
				tag.setAutoComponentTag(true);
			}

			tag.setFlag(ComponentTag.RENDER_RAW, true);
			tag.setUserData("STYLE_OR_SCRIPT", Boolean.TRUE);
		}

		return tag;
	}

	@Override
	public void postProcess(Markup markup)
	{
		for (int i = 0; i < markup.size(); i++)
		{
			MarkupElement elem = markup.get(i);
			if (elem instanceof ComponentTag)
			{
				ComponentTag open = (ComponentTag)elem;

				if (shouldProcess(open))
				{
					if (open.isOpen() && ((i + 2) < markup.size()))
					{
						MarkupElement body = markup.get(i + 1);
						MarkupElement tag2 = markup.get(i + 2);

						if ((body instanceof RawMarkup) && (tag2 instanceof ComponentTag))
						{
							ComponentTag close = (ComponentTag)tag2;
							if (close.closes(open))
							{
								String text = body.toString().trim();
								if (shouldWrapInCdata(text))
								{
									text = JavaScriptUtils.SCRIPT_CONTENT_PREFIX + body.toString() +
										JavaScriptUtils.SCRIPT_CONTENT_SUFFIX;
									markup.replace(i + 1, new RawMarkup(text));
								}
							}
						}
					}
				}
			}
		}
	}

	// OES == optional empty space

	// OES<!--OES
	private static final Pattern HTML_START_COMMENT = Pattern.compile("^\\s*<!--\\s*.*", Pattern.DOTALL);

	// OES<![CDATA[OES
	private static final Pattern CDATA_START_COMMENT = Pattern.compile("^\\s*<!\\[CDATA\\[\\s*.*", Pattern.DOTALL);

	// OES/*OES<![CDATA[OES*/OES
	private static final Pattern JS_CDATA_START_COMMENT = Pattern.compile("^\\s*\\/\\*\\s*<!\\[CDATA\\[\\s*\\*\\/\\s*.*", Pattern.DOTALL);

	boolean shouldWrapInCdata(final String elementBody)
	{
		Args.notNull(elementBody, "elementBody");

		boolean shouldWrap = true;

		if (
				HTML_START_COMMENT.matcher(elementBody).matches() ||
				CDATA_START_COMMENT.matcher(elementBody).matches() ||
				JS_CDATA_START_COMMENT.matcher(elementBody).matches()
			)
		{
			shouldWrap = false;
		}

		return shouldWrap;
	}

	private boolean shouldProcess(ComponentTag openTag)
	{
		// do not wrap in CDATA any <script> which has special MIME type. WICKET-4425
		String typeAttribute = openTag.getAttribute("type");
		boolean shouldProcess =
				// style elements should be processed
				"style".equals(openTag.getName()) ||

				// script elements should be processed only if they have no type (HTML5 recommendation)
				// or the type is "text/javascript"
				(typeAttribute == null || "text/javascript".equalsIgnoreCase(typeAttribute));

		return shouldProcess && openTag.getUserData("STYLE_OR_SCRIPT") != null;
	}
}