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

import java.util.ArrayList;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.AutoLabelTagHandler;
import org.apache.wicket.markup.parser.IMarkupFilter;
import org.apache.wicket.markup.parser.IXmlPullParser;
import org.apache.wicket.markup.parser.filter.ConditionalCommentFilter;
import org.apache.wicket.markup.parser.filter.EnclosureHandler;
import org.apache.wicket.markup.parser.filter.HeadForceTagIdHandler;
import org.apache.wicket.markup.parser.filter.HtmlHandler;
import org.apache.wicket.markup.parser.filter.HtmlHeaderSectionHandler;
import org.apache.wicket.markup.parser.filter.InlineEnclosureHandler;
import org.apache.wicket.markup.parser.filter.OpenCloseTagExpander;
import org.apache.wicket.markup.parser.filter.RelativePathPrefixHandler;
import org.apache.wicket.markup.parser.filter.StyleAndScriptIdentifier;
import org.apache.wicket.markup.parser.filter.WicketLinkTagHandler;
import org.apache.wicket.markup.parser.filter.WicketMessageTagHandler;
import org.apache.wicket.markup.parser.filter.WicketNamespaceHandler;
import org.apache.wicket.markup.parser.filter.WicketRemoveTagHandler;
import org.apache.wicket.markup.parser.filter.WicketTagIdentifier;
import org.apache.wicket.util.lang.Objects;

/**
 * This is Wicket's default markup parser. It gets pre-configured with Wicket's default wicket
 * filters.
 * 
 * @see MarkupFactory
 * 
 * @author Juergen Donnerstag
 */
public class MarkupParser extends AbstractMarkupParser
{
	/** "wicket" */
	public final static String WICKET = "wicket";

	/**
	 * Constructor.
	 * 
	 * @param resource
	 *            The markup resource (file)
	 */
	public MarkupParser(final MarkupResourceStream resource)
	{
		super(resource);
	}

	/**
	 * Constructor. Usually for testing purposes only
	 * 
	 * @param markup
	 *            The markup resource.
	 */
	public MarkupParser(final String markup)
	{
		super(markup);
	}

	/**
	 * Constructor.
	 * 
	 * @param xmlParser
	 *            The streaming xml parser to read and parse the markup
	 * @param resource
	 *            The markup resource (file)
	 */
	public MarkupParser(final IXmlPullParser xmlParser, final MarkupResourceStream resource)
	{
		super(xmlParser, resource);
	}

	@Override
	public MarkupFilterList getMarkupFilters()
	{
		return (MarkupFilterList)super.getMarkupFilters();
	}

	/**
	 * Add a markup filter
	 * 
	 * @param filter
	 * @return true, if successful
	 */
	public final boolean add(final IMarkupFilter filter)
	{
		return getMarkupFilters().add(filter);
	}

	/**
	 * Add a markup filter before the 'beforeFilter'
	 * 
	 * @param filter
	 * @param beforeFilter
	 * @return true, if successful
	 */
	public final boolean add(final IMarkupFilter filter,
		final Class<? extends IMarkupFilter> beforeFilter)
	{
		return getMarkupFilters().add(filter, beforeFilter);
	}

	/**
	 * a) Allow subclasses to configure individual Wicket filters
	 * <p>
	 * b) Allows to disable Wicket filters via returning false
	 * 
	 * @param filter
	 * @return The filter to be added. Null to ignore.
	 */
	protected IMarkupFilter onAppendMarkupFilter(final IMarkupFilter filter)
	{
		return filter;
	}

	/**
	 * Initialize Wicket's MarkupParser with all necessary markup filters. You may subclass this
	 * method, to add your own filters to the list.
	 * 
	 * @param markup
	 * @return The list of markup filter
	 */
	@Override
	protected MarkupFilterList initializeMarkupFilters(final Markup markup)
	{
		// MarkupFilterList is a simple extension of ArrayList providing few additional helpers
		final MarkupFilterList filters = new MarkupFilterList();

		MarkupResourceStream markupResourceStream = markup.getMarkupResourceStream();

		filters.add(new WicketTagIdentifier(markupResourceStream));
		filters.add(new HtmlHandler());
		filters.add(new WicketRemoveTagHandler());
		filters.add(new WicketLinkTagHandler());
		filters.add(new AutoLabelTagHandler());
		filters.add(new WicketNamespaceHandler(markupResourceStream));
		filters.add(new WicketMessageTagHandler(markupResourceStream));

		// Provided the wicket component requesting the markup is known ...
		if ((markupResourceStream != null) && (markupResourceStream.getResource() != null))
		{
			final ContainerInfo containerInfo = markupResourceStream.getContainerInfo();
			if (containerInfo != null)
			{
				// Pages require additional handlers
				if (Page.class.isAssignableFrom(containerInfo.getContainerClass()))
				{
					filters.add(new HtmlHeaderSectionHandler(markup));
				}

				filters.add(new HeadForceTagIdHandler(containerInfo.getContainerClass()));
			}
		}

		filters.add(new OpenCloseTagExpander());
		filters.add(new RelativePathPrefixHandler(markupResourceStream));
		filters.add(new EnclosureHandler());
		filters.add(new InlineEnclosureHandler());

		// Append it. See WICKET-4390
		filters.add(new StyleAndScriptIdentifier(), StyleAndScriptIdentifier.class);
		filters.add(new ConditionalCommentFilter());

		return filters;
	}

	/**
	 * A simple extension to ArrayList to manage Wicket MarkupFilter's more easily
	 */
	public class MarkupFilterList extends ArrayList<IMarkupFilter>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public boolean add(final IMarkupFilter filter)
		{
			return add(filter, RelativePathPrefixHandler.class);
		}

		/**
		 * Insert a markup filter before a another one.
		 * 
		 * @param filter
		 * @param beforeFilter
		 * @return true, if successful
		 */
		public boolean add(IMarkupFilter filter, final Class<? extends IMarkupFilter> beforeFilter)
		{
			filter = onAdd(filter);
			if (filter == null)
			{
				return false;
			}

			int index = firstIndexOfClass(beforeFilter);
			if (index < 0)
			{
				return super.add(filter);
			}

			super.add(index, filter);
			return true;
		}

		/**
		 * Finds the index of the first entry which is from the same type as the passed
		 * {@literal filterClass} argument.
		 *
		 * @param filterClass
		 *      the class to search for
		 * @return the index of the first match or -1 otherwise
		 */
		private int firstIndexOfClass(final Class<? extends IMarkupFilter> filterClass)
		{
			int result = -1;
			if (filterClass != null)
			{
				final int size = size();
				for (int index = 0; index < size; index++) {
					Class<? extends IMarkupFilter> currentFilterClass = get(index).getClass();
					if (Objects.equal(filterClass, currentFilterClass))
					{
						result = index;
						break;
					}
				}
			}
			return result;
		}

		/**
		 * a) Allow subclasses to configure individual Wicket filters which otherwise can not be
		 * accessed.
		 * <p>
		 * b) Allows to disable Wicket filters via returning false
		 * 
		 * @param filter
		 * @return The filter to be added. Null to ignore
		 */
		protected IMarkupFilter onAdd(final IMarkupFilter filter)
		{
			return onAppendMarkupFilter(filter);
		}
	}
}
