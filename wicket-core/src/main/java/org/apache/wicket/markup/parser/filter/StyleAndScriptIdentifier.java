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

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.RawMarkup;
import org.apache.wicket.markup.parser.AbstractMarkupFilter;
import org.apache.wicket.markup.parser.XmlPullParser;
import org.apache.wicket.util.string.JavaScriptUtils;


/**
 * 
 * @author Juergen Donnerstag
 */
public final class StyleAndScriptIdentifier extends AbstractMarkupFilter
{
	private final Markup markup;

	/**
	 * Construct.
	 * 
	 * @param markup
	 */
	public StyleAndScriptIdentifier(final Markup markup)
	{
		this.markup = markup;
	}

	@Override
	protected final MarkupElement onComponentTag(final ComponentTag tag) throws ParseException
	{
		if (tag.getNamespace() != null)
		{
			return tag;
		}

		if (XmlPullParser.SCRIPT.equalsIgnoreCase(tag.getName()) ||
			(XmlPullParser.STYLE.equalsIgnoreCase(tag.getName())))
		{
			if (tag.isOpen() && (tag.getId() == null))
			{
				// Not needed, but must not be null
				tag.setId("_ScriptStyle");
				tag.setModified(true);
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
				if (open.getUserData("STYLE_OR_SCRIPT") != null)
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
								if (!text.startsWith("<!--") && !text.startsWith("<![CDATA[") &&
									!text.startsWith("/*<![CDATA[*/"))
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
}
