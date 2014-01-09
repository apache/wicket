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

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.parser.AbstractMarkupFilter;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.apache.wicket.util.collections.ArrayListStack;
import org.apache.wicket.util.string.StringValueConversionException;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.value.IValueMap;


/**
 * This is a markup inline filter. It identifies xml tags which include a href attribute and which
 * are not Wicket specific components and flags these tags (ComponentTag) as autolink enabled. A
 * component resolver will later resolve the href and assign a BookmarkablePageLink<?> to it
 * (automatically).
 * <p>
 * An application setting is used as default value, which might be modified for specific regions.
 * These regions are identified by &lt;wicket:link&gt; tags with an optional 'autolink' attribute.
 * The default value for the attribute is true, thus enabling autolinking. An open-close
 * &lt;wicket:link/&gt tag will change the autolink status until the end of the markup document or
 * the next &lt;wicket:link&gt; tag respectively. &lt;wicket:link&gt; regions may be nested.
 * 
 * @author Juergen Donnerstag
 */
public class WicketLinkTagHandler extends AbstractMarkupFilter implements IComponentResolver
{
	private static final long serialVersionUID = 1L;

	/** */
	public static final String LINK = "link";

	/** The id of autolink components */
	public static final String AUTOLINK_ID = "_autolink_";

	/** Allow to have link regions within link regions */
	private ArrayListStack<Boolean> autolinkStatus;

	/** Current status */
	private boolean autolinking = true;

	/**
	 * Construct.
	 */
	public WicketLinkTagHandler()
	{
		this(null);
	}

	public WicketLinkTagHandler(MarkupResourceStream resourceStream)
	{
		super(resourceStream);
		setAutomaticLinking(Application.get().getMarkupSettings().getAutomaticLinking());
	}

	/**
	 * Set the default value for autolinking
	 * 
	 * @param enable
	 *            if true, autolinks are enabled
	 */
	public void setAutomaticLinking(final boolean enable)
	{
		autolinking = enable;
	}

	@Override
	protected final MarkupElement onComponentTag(ComponentTag tag) throws ParseException
	{
		// Only xml tags not already identified as Wicket components will be
		// considered for autolinking. This is because it is assumed that Wicket
		// components like images or all other kind of Wicket Links will handle
		// it themselves.
		// Subclass analyzeAutolinkCondition() to implement your own
		// implementation and register the new tag handler with the markup
		// parser through Application.newMarkupParser().
		if ((autolinking == true) && (analyzeAutolinkCondition(tag) == true))
		{
			// Mark it as autolink enabled
			tag.enableAutolink(true);

			// Just a dummy name. The ComponentTag will not be forwarded.
			tag.setId(AUTOLINK_ID);
			tag.setAutoComponentTag(true);
			tag.setModified(true);
			return tag;
		}

		// For all <wicket:link ..> tags which probably change the
		// current autolink status.
		if (tag instanceof WicketTag)
		{
			final WicketTag wtag = (WicketTag)tag;
			if (wtag.isLinkTag())
			{
				// Beginning of the region
				if (tag.isOpen() || tag.isOpenClose())
				{
					if (tag.isOpen())
					{
						if (autolinkStatus == null)
						{
							autolinkStatus = new ArrayListStack<Boolean>();
						}

						// remember the current setting to be reset after the
						// region
						autolinkStatus.push(autolinking);
					}

					// html allows to represent true in different ways
					final String autolink = tag.getAttributes().getString("autolink");
					try
					{
						autolinking = Strings.isEmpty(autolink) || Strings.isTrue(autolink);
					}
					catch (StringValueConversionException e)
					{
						throw new WicketRuntimeException("Invalid autolink attribute value \"" +
							autolink + "\"");
					}
				}
				else if (tag.isClose())
				{
					// restore the autolink setting from before the region
					autolinking = autolinkStatus.pop();
				}

				return wtag;
			}
		}

		return tag;
	}

	/**
	 * Analyze the tag. If return value == true, a autolink component will be created.
	 * <p>
	 * Subclass analyzeAutolinkCondition() to implement you own implementation and register the new
	 * tag handler with the markup parser through Application.newMarkupParser().
	 * 
	 * @param tag
	 *            The current tag being parsed
	 * @return If true, tag will become auto-component
	 */
	protected boolean analyzeAutolinkCondition(final ComponentTag tag)
	{
		if (tag.getId() == null)
		{
			IValueMap attributes = tag.getAttributes();
			String ref = attributes.getString("href");
			if (checkRef(ref))
			{
				return true;
			}
			ref = attributes.getString("src");
			if (checkRef(ref))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * 
	 * @param ref
	 * @return true if ref is not null and does not contain namespace
	 */
	private final boolean checkRef(String ref)
	{
		return (ref != null) && (!ref.contains(":"));
	}

	@Override
	public Component resolve(final MarkupContainer container, final MarkupStream markupStream,
		final ComponentTag tag)
	{
		if (tag instanceof WicketTag)
		{
			WicketTag wtag = (WicketTag)tag;
			if (wtag.isLinkTag() && (wtag.getNamespace() != null))
			{
				String id = tag.getId() + "-" + container.getPage().getAutoIndex();

				return new TransparentWebMarkupContainer(id);
			}
		}

		// We were not able to handle the tag
		return null;
	}
}
