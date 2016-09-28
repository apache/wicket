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
package org.apache.wicket.markup.parser;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.ContainerInfo;
import org.apache.wicket.markup.HtmlSpecialTag;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupParser;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.request.cycle.RequestCycle;


/**
 * Base class for markup filters
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
public abstract class AbstractMarkupFilter implements IMarkupFilter
{
	/** The markup created by reading the markup file */
	private final MarkupResourceStream markupResourceStream;

	/** The next MarkupFilter in the chain */
	private IMarkupFilter parent;

	/**
	 *  A key for a request-relative map of counters.
	 *  As map keys we use the class name of the {@link org.apache.wicket.markup.MarkupResourceStream}'s owner 
	 *  container (see {@link org.apache.wicket.markup.MarkupResourceStream#getContainerInfo()}), 
	 *  meaning that each container has its own counter. 
	 *  The counters are used by {@link #getRequestUniqueId()} to get unique ids for markup tags.
	 * **/
	private final static MetaDataKey<Map<String, AtomicInteger>> REQUEST_COUNTER_KEY = new MetaDataKey<Map<String, AtomicInteger>>()
	{
		private static final long serialVersionUID = 1L;
	};

	/**
	 * Construct.
	 */
	public AbstractMarkupFilter()
	{
		this(null);
	}

	public AbstractMarkupFilter(final MarkupResourceStream markupResourceStream)
	{
		this.markupResourceStream = markupResourceStream;
	}


	/**
	 * @return The next MarkupFilter in the chain
	 */
	@Override
	public IMarkupFilter getNextFilter()
	{
		return parent;
	}

	/**
	 * Set new parent.
	 * 
	 * @param parent
	 *            The parent of this component The next element in the chain
	 */
	@Override
	public void setNextFilter(final IMarkupFilter parent)
	{
		this.parent = parent;
	}

	/**
	 * Get the next xml element from the markup. If eof, than retun null. Ignore raw markup. Invoke
	 * nextTag(tag) if a tag was found.
	 */
	@Override
	public MarkupElement nextElement() throws ParseException
	{
		MarkupElement elem = getNextFilter().nextElement();
		if (elem != null)
		{
			if (elem instanceof ComponentTag)
			{
				elem = onComponentTag((ComponentTag)elem);
			}
			else if (elem instanceof HtmlSpecialTag)
			{
				elem = onSpecialTag((HtmlSpecialTag)elem);
			}
		}
		return elem;
	}

	/**
	 * Invoked when a ComponentTag was found.
	 * <p>
	 * By default this method is also called for WicketTags.
	 * 
	 * @param tag
	 * @return Usually the same as the tag attribute
	 * @throws ParseException
	 */
	protected abstract MarkupElement onComponentTag(ComponentTag tag) throws ParseException;

	/**
	 * Invoked when a WicketTag was found.
	 * 
	 * @param tag
	 * @return Usually the same as the tag attribute
	 * @throws ParseException
	 */

	/**
	 * Invoked when a tags (e.g. DOCTYPE, PROCESSING_INSTRUCTIION, etc. which have been identified
	 * as special tags by the xml parser.
	 * 
	 * @param tag
	 * @return Usually the same as the tag attribute
	 * @throws ParseException
	 */
	protected MarkupElement onSpecialTag(final HtmlSpecialTag tag) throws ParseException
	{
		return tag;
	}

	@Override
	public void postProcess(final Markup markup)
	{
	}

	protected MarkupResourceStream getMarkupResourceStream() {
		return markupResourceStream;
	}

	/**
	 * Extracts the markup namespace from the MarkupResourceStream
	 * passed at creation time.
	 *
	 * <p>
	 *     There are two versions of this method because most IMarkupFilter's
	 *     have dual personality - {@link IMarkupFilter} (one instance per MarkupParser)
	 *     and {@link org.apache.wicket.markup.resolver.IComponentResolver} (one
	 *     instance per application).
	 * </p>
	 *
	 * @return the namespace of the loaded markup
	 */
	protected String getWicketNamespace()
	{
		return getWicketNamespace(null);
	}

	/**
	 * Extracts the markup namespace from the passed MarkupStream if available,
	 * or from the MarkupResourceStream passed at creation time.
	 *
	 * <p>
	 *     There are two versions of this method because most IMarkupFilter's
	 *     have dual personality - {@link IMarkupFilter} (one instance per MarkupParser)
	 *     and {@link org.apache.wicket.markup.resolver.IComponentResolver} (one
	 *     instance per application).
	 * </p>
	 *
	 * @param markupStream
	 *      the markup stream
	 * @return namespace extracted from the markup
	 */
	protected String getWicketNamespace(final MarkupStream markupStream)
	{
		String wicketNamespace = MarkupParser.WICKET;
		if (markupStream != null)
		{
			wicketNamespace = markupStream.getWicketNamespace();
		}
		else if (markupResourceStream != null)
		{
			wicketNamespace = markupResourceStream.getWicketNamespace();
		}
		return wicketNamespace;
	}

	/**
	 * Returns an id using the request-relative counter associated with the 
	 * underlying {@link org.apache.wicket.markup.MarkupResourceStream}'s owner container 
	 * (see {@link org.apache.wicket.markup.MarkupResourceStream#getContainerInfo()}). 
	 * This can be useful for autocomponent tags that need to get a tag id.
	 * 
	 * @return
	 * 		the request-relative id
	 */
	protected int getRequestUniqueId()
	{
		RequestCycle requestCycle = RequestCycle.get();
		Map<String, AtomicInteger> markupUniqueCounters = requestCycle.getMetaData(REQUEST_COUNTER_KEY);
		ContainerInfo containerInfo = getMarkupResourceStream().getContainerInfo();
		String cacheKey = containerInfo != null ? containerInfo.getContainerClass().getCanonicalName() : null;
		
		if (markupUniqueCounters == null)
		{
			markupUniqueCounters = new HashMap<>();
			
			requestCycle.setMetaData(REQUEST_COUNTER_KEY, markupUniqueCounters);
		}
		
		AtomicInteger counter = markupUniqueCounters.get(cacheKey);
		
		if (counter == null)
		{
			counter = new AtomicInteger();
			markupUniqueCounters.put(cacheKey, counter);
		}
		
 	    int cacheHash = cacheKey == null ? 0 : cacheKey.hashCode();
		
 	    //add the counter value to the string hash 
 	    //using the same algorithm of String#hashCode() 
 	    return  cacheHash * 31 + counter.getAndIncrement();
	}
}
