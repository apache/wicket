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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.util.string.AppendingStringBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A list of markup elements associated with a Markup. Might be all elements of a markup resource,
 * might be just the elements associated with a specific tag.
 * 
 * @see org.apache.wicket.markup.MarkupResourceData
 * @see org.apache.wicket.markup.MarkupElement
 * @see org.apache.wicket.markup.ComponentTag
 * @see org.apache.wicket.markup.RawMarkup
 * 
 * @author Juergen Donnerstag
 */
public class Markup
{
	private static final Logger log = LoggerFactory.getLogger(Markup.class);

	/** Placeholder that indicates no markup */
	public static final Markup NO_MARKUP = new Markup(MarkupResourceData.NO_MARKUP_RESOURCE_DATA);

	/** The list of markup elements */
	private/* final */List<MarkupElement> markupElements;

	/** The associated markup */
	private final MarkupResourceData markupResourceData;

	/** A cache which maps (componentPath + id) to the componentTags index in the markup */
	private Map<String, Integer> componentMap;

	/**
	 * Used at markup load time to maintain the current component path (not id) while adding markup
	 * elements to this Markup instance
	 */
	private StringBuffer currentPath;

	/**
	 * Constructor
	 * 
	 * @param markupResourceData
	 *            The associated Markup
	 */
	Markup(final MarkupResourceData markupResourceData)
	{
		this.markupResourceData = markupResourceData;
		markupElements = new ArrayList<MarkupElement>();
	}

	/**
	 * For Wicket it would be sufficient for this method to be package protected. However to allow
	 * wicket-bench easy access to the information ...
	 * 
	 * @param index
	 *            Index into markup list
	 * @return Markup element
	 */
	public final MarkupElement get(final int index)
	{
		return markupElements.get(index);
	}

	/**
	 * Gets the associate markup
	 * 
	 * @return The associated markup
	 */
	public final MarkupResourceData getMarkupResourceData()
	{
		return markupResourceData;
	}
	
	/**
	 * Allowing the markup to return its own location allows special types of Markup
	 * (i.e. MergedMarkup) to override their location, as they are composed of multiple
	 * Markups in different locations.
	 * SEE WICKET-1507 (Jeremy Thomerson)
	 * @return the location of this markup
	 */
	public String locationAsString()
	{
		return markupResourceData.getResource().locationAsString();
	}


	/**
	 * For Wicket it would be sufficient for this method to be package protected. However to allow
	 * wicket-bench easy access to the information ...
	 * 
	 * @return Number of markup elements
	 */
	public int size()
	{
		return markupElements.size();
	}

	/**
	 * Add a MarkupElement
	 * 
	 * @param markupElement
	 */
	final public void addMarkupElement(final MarkupElement markupElement)
	{
		markupElements.add(markupElement);
	}

	/**
	 * Add a MarkupElement
	 * 
	 * @param pos
	 * @param markupElement
	 */
	final public void addMarkupElement(final int pos, final MarkupElement markupElement)
	{
		markupElements.add(pos, markupElement);
	}

	/**
	 * Make all tags immutable and the list of elements unmodifiable.
	 */
	final void makeImmutable()
	{
		for (int i = 0; i < markupElements.size(); i++)
		{
			MarkupElement elem = markupElements.get(i);
			if (elem instanceof ComponentTag)
			{
				// Make the tag immutable
				((ComponentTag)elem).makeImmutable();
			}
		}

		markupElements = Collections.unmodifiableList(markupElements);
		initialize();
	}

	/**
	 * Add the tag to the local cache if open or open-close and if wicket:id is present
	 * 
	 * @param index
	 * @param tag
	 */
	private void addToCache(final int index, final ComponentTag tag)
	{
		// Only if the tag has wicket:id="xx" and open or open-close
		if ((tag.isOpen() || tag.isOpenClose()) &&
			tag.getAttributes().containsKey(getMarkupResourceData().getWicketId()))
		{
			// Add the tag to the cache
			if (componentMap == null)
			{
				componentMap = new HashMap<String, Integer>();
			}

			/*
			 * XXX cleanup - this fragment check probably needs to be in
			 * componenttag.isWantToBeDirectMarkupChild() or something similar instead of being here
			 */
			final boolean fragment = (tag instanceof WicketTag && ((WicketTag)tag).isFragementTag());

			final String key;

			if (tag.getPath() != null && !fragment/* WICKET-404 */)
			{
				key = tag.getPath() + ":" + tag.getId();
			}
			else
			{
				key = tag.getId();
			}
			componentMap.put(key, new Integer(index));
		}
	}

	/**
	 * Set the components path within the markup and add the component tag to the local cache
	 * 
	 * @param componentPath
	 * @param tag
	 * @return componentPath
	 */
	private StringBuffer setComponentPathForTag(final StringBuffer componentPath,
		final ComponentTag tag)
	{
		// Only if the tag has wicket:id="xx" and open or open-close
		if ((tag.isOpen() || tag.isOpenClose()) &&
			tag.getAttributes().containsKey(markupResourceData.getWicketId()))
		{
			// With open-close the path does not change. It can/will not have
			// children. The same is true for HTML tags like <br> or <img>
			// which might not have close tags.
			if (tag.isOpenClose() || tag.hasNoCloseTag())
			{
				// Set the components path.
				if ((currentPath != null) && (currentPath.length() > 0))
				{
					tag.setPath(currentPath.toString());
				}
			}
			else
			{
				// Set the components path.
				if (currentPath == null)
				{
					currentPath = new StringBuffer(100);
				}
				else if (currentPath.length() > 0)
				{
					tag.setPath(currentPath.toString());
					currentPath.append(':');
				}

				// .. and append the tags id to the component path for the
				// children to come
				currentPath.append(tag.getId());
			}
		}
		else if (tag.isClose() && (currentPath != null))
		{
			// For example <wicket:message> does not have an id
			if ((tag.getOpenTag() == null) ||
				tag.getOpenTag().getAttributes().containsKey(markupResourceData.getWicketId()))
			{
				// Remove the last element from the component path
				int index = currentPath.lastIndexOf(":");
				if (index != -1)
				{
					currentPath.setLength(index);
				}
				else
				{
					currentPath.setLength(0);
				}
			}
		}

		return currentPath;
	}

	/**
	 * Find the markup element index of the component with 'path'
	 * 
	 * @param path
	 *            The component path expression
	 * @param id
	 *            The component's id to search for
	 * @return -1, if not found
	 */
	public int findComponentIndex(final String path, final String id)
	{
		if ((id == null) || (id.length() == 0))
		{
			throw new IllegalArgumentException("Parameter 'id' must not be null");
		}

		// TODO Post 1.2: A component path e.g. "panel:label" does not match 1:1
		// with the markup in case of ListView, where the path contains a number
		// for each list item. E.g. list:0:label. What we currently do is simply
		// remove the number from the path and hope that no user uses an integer
		// for a component id. This is a hack only. A much better solution would
		// delegate to the various components recursively to search within there
		// realm only for the components markup. ListItems could then simply
		// do nothing and delegate to their parents.
		String completePath = (path == null || path.length() == 0 ? id : path + ":" + id);

		// s/:\d+//g
		Pattern re = Pattern.compile(":\\d+");
		Matcher matcher = re.matcher(completePath);
		completePath = matcher.replaceAll("");

		// All component tags are registered with the cache
		if (componentMap == null)
		{
			// not found
			return -1;
		}

		final Integer value = componentMap.get(completePath);
		if (value == null)
		{
			// not found
			return -1;
		}

		// return the components position in the markup stream
		return value.intValue();
	}

	/**
	 * @param that
	 *            The markup to compare with
	 * @return True if the two markups are equal
	 */
	public boolean equalTo(final Markup that)
	{
		final MarkupStream thisStream = new MarkupStream(this);
		final MarkupStream thatStream = new MarkupStream(that);

		// Compare the streams
		return thisStream.equalTo(thatStream);
	}

	/**
	 * Initialize the index where wicket tags can be found
	 */
	protected void initialize()
	{
		// Reset
		componentMap = null;

		if (markupElements != null)
		{
			// HTML tags like <img> may not have a close tag. But because that
			// can only be detected until later on in the sequential markup
			// reading loop, we only can do it now.
			StringBuffer componentPath = null;
			for (int i = 0; i < size(); i++)
			{
				MarkupElement elem = get(i);
				if (elem instanceof ComponentTag)
				{
					ComponentTag tag = (ComponentTag)elem;

					// Set the tags components path
					componentPath = setComponentPathForTag(componentPath, tag);

					// and add it to the local cache to be found fast if
					// required
					addToCache(i, tag);
				}
			}
		}

		// The variable is only needed while adding markup elements.
		// initialize() is invoked after all elements have been added.
		currentPath = null;
	}

	/**
	 * @return String representation of markup list
	 */
	@Override
	public final String toString()
	{
		final AppendingStringBuffer buf = new AppendingStringBuffer(400);
		buf.append(markupResourceData.toString());
		buf.append("\n");

		final Iterator<MarkupElement> iter = markupElements.iterator();
		while (iter.hasNext())
		{
			buf.append(iter.next());
		}

		return buf.toString();
	}
}
