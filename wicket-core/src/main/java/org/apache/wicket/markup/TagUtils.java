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

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.util.value.IValueMap;
import org.apache.wicket.util.value.ValueMap;


/**
 * Some utils to handle tags which otherwise would bloat the Tag API.
 * 
 * @author Juergen Donnerstag
 */
public class TagUtils
{
	private static final String DEFAULT_ATTRIBUTE_SEPARATOR = "; ";
	/**
	 * A map that keeps the separators which should be used for the different HTML
	 * element attributes.
	 */
	// 'public' so that user applications can add/modify the entries, if needed
	public static final IValueMap ATTRIBUTES_SEPARATORS = new ValueMap();
	static {
		ATTRIBUTES_SEPARATORS.put("class", " ");
		ATTRIBUTES_SEPARATORS.put("style", DEFAULT_ATTRIBUTE_SEPARATOR);
		ATTRIBUTES_SEPARATORS.put("onclick", DEFAULT_ATTRIBUTE_SEPARATOR);
	}

	/**
	 * Constructor
	 */
	public TagUtils()
	{
	}

	/**
	 * @return True, if tag name equals '&lt;body ...&gt;'
	 * 
	 * @param tag
	 */
	public static final boolean isBodyTag(final ComponentTag tag)
	{
		return ("body".equalsIgnoreCase(tag.getName()) && (tag.getNamespace() == null));
	}

	/**
	 * 
	 * @param elem
	 * @return True, if tag name equals '&lt;head ...&gt;'
	 */
	public static final boolean isHeadTag(final MarkupElement elem)
	{
		if (elem instanceof ComponentTag)
		{
			ComponentTag tag = (ComponentTag)elem;
			if ("head".equalsIgnoreCase(tag.getName()) && (tag.getNamespace() == null))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param markup
	 * @param i
	 * @return True if the markup element at index 'i' is a WicketTag
	 */
	public static final boolean isWicketTag(final IMarkupFragment markup, final int i)
	{
		MarkupElement elem = markup.get(i);
		return elem instanceof WicketTag;
	}

	/**
	 * 
	 * @param markup
	 * @param i
	 * @return True if the markup element at index 'i' is a &lt;wicket:extend&gt; tag
	 */
	public static final boolean isExtendTag(final IMarkupFragment markup, final int i)
	{
		MarkupElement elem = markup.get(i);
		if (elem instanceof WicketTag)
		{
			WicketTag wtag = (WicketTag)elem;
			return wtag.isExtendTag();
		}
		return false;
	}

	/**
	 * 
	 * @param elem
	 * @return True if the current markup element is a &lt;wicket:head&gt; tag
	 */
	public static final boolean isWicketHeadTag(final MarkupElement elem)
	{
		if (elem instanceof WicketTag)
		{
			WicketTag wtag = (WicketTag)elem;
			if (wtag.isHeadTag())
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param elem
	 * @return True if the current markup element is a &lt;wicket:body&gt; tag
	 */
	public static final boolean isWicketBodyTag(final MarkupElement elem)
	{
		if (elem instanceof WicketTag)
		{
			WicketTag wtag = (WicketTag)elem;
			if (wtag.isBodyTag())
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param elem
	 * @return True if the current markup element is a &lt;wicket:border&gt; tag
	 */
	public static final boolean isWicketBorderTag(final MarkupElement elem)
	{
		if (elem instanceof WicketTag)
		{
			WicketTag wtag = (WicketTag)elem;
			if (wtag.isBorderTag())
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Copy attributes from e.g. &lt;wicket:panel&gt; (or border) to the "calling" tag.
	 *
	 * @see <a href="http://issues.apache.org/jira/browse/WICKET-2874">WICKET-2874</a>
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-3812">WICKET-3812</a>
	 *
	 * @param component
	 *      the markup container which attributes will be copied
	 * @param tag
	 *      the component tag where the attributes will be applied
	 */
	public static void copyAttributes(final MarkupContainer component, final ComponentTag tag)
	{
		IMarkupFragment markup = component.getMarkup(null);
		String namespace = markup.getMarkupResourceStream().getWicketNamespace() + ":";

		MarkupElement elem = markup.get(0);
		if (elem instanceof ComponentTag)
		{
			ComponentTag panelTag = (ComponentTag)elem;
			for (String key : panelTag.getAttributes().keySet())
			{
				// exclude "wicket:XX" attributes
				if (key.startsWith(namespace) == false)
				{
					String separator = ATTRIBUTES_SEPARATORS.getString(key, DEFAULT_ATTRIBUTE_SEPARATOR);
					tag.append(key, panelTag.getAttribute(key), separator);
				}
			}
		}
		else
		{
			throw new MarkupException(markup.getMarkupResourceStream(),
				"Expected a Tag but found raw markup: " + elem.toString());
		}
	}
}