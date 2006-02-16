/*
 * $Id$ $Revision$
 * $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Holds markup as a resource (the stream that the markup came from) and a list
 * of MarkupElements (the markup itself).
 * 
 * @see MarkupElement
 * @see ComponentTag
 * @see wicket.markup.RawMarkup
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
public final class Markup
{
	private static final Log log = LogFactory.getLog(Markup.class);

	/** Placeholder that indicates no markup */
	public static final Markup NO_MARKUP = new Markup();

	/** The list of markup elements */
	private/* final */List markup;

	/** The markup's resource stream for diagnostic purposes */
	private MarkupResourceStream resource;

	/** If found in the markup, the <?xml ...?> string */
	private String xmlDeclaration;

	/** The encoding as found in <?xml ... encoding="" ?>. Null, else */
	private String encoding;

	/** Wicket namespace: <html xmlns:wicket="http://wicket.sourceforge.net"> */
	private String wicketNamespace;

	/** == wicketNamespace + ":id" */
	private String wicketId;

	/** Markup has been searched for the header, but it doesn't contain any */
	public final static int NO_HEADER_FOUND = -1;

	/**
	 * If the markup contains a header section, the index will point to the
	 * MarkupElement.
	 */
	private int headerIndex = NO_HEADER_FOUND;

	/**
	 * Used at markup load time to maintain the current component path (not id)
	 * while adding markup elements to this Markup instance
	 */
	private StringBuffer currentPath;

	/**
	 * A cache which maps (componentPath + id) to the componentTags index in the
	 * markup
	 */
	private Map componentMap;

	/**
	 * Kind of copy constructor, though new markup elements are attached
	 * 
	 * @param markup
	 *            The Markup which variables are copied
	 * @param markupElements
	 *            The new list of markup elements
	 */
	Markup(final Markup markup, final List markupElements)
	{
		this.resource = markup.resource;
		this.xmlDeclaration = markup.xmlDeclaration;
		this.encoding = markup.encoding;
		setWicketNamespace(markup.wicketNamespace);
		
		// Because the markup elements are not added, they are copied in,
		// the componentMap has to be manually re-created
		this.markup = new ArrayList();
		for (int i=0; i < markupElements.size(); i++)
		{
			Object element = markupElements.get(i);
			if(element instanceof ComponentTag)
			{
				addMarkupElement((ComponentTag)element);
			}
			else
			{
				addMarkupElement((MarkupElement)element);
			}
		}

		initialize();
	}

	/**
	 * Constructor
	 */
	Markup()
	{
		this.markup = new ArrayList();
		setWicketNamespace(ComponentTag.DEFAULT_WICKET_NAMESPACE);
	}

	/**
	 * Initialize the index where <head> can be found.
	 */
	private void initialize()
	{
		if (markup != null)
		{
			// Initialize the index where <wicket:extend> can be found.
			for (int i = 0; i < markup.size(); i++)
			{
				MarkupElement elem = (MarkupElement)markup.get(i);
				if (elem instanceof WicketTag)
				{
					WicketTag tag = (WicketTag)elem;
					if ((tag.isHeadTag() == true) && (tag.getNamespace() != null))
					{
						headerIndex = i;
						break;
					}
				}
			}
		}

		// The variable is only needed while adding markup elements.
		// initialize() is invoked after all elements have been added.
		this.currentPath = null;
	}

	/**
	 * @return String representation of markup list
	 */
	public String toString()
	{
		if (resource != null)
		{
			return resource.toString();
		}
		else
		{
			return "(unknown resource)";
		}
	}

	/**
	 * For Wicket it would be sufficient for this method to be package
	 * protected. However to allow wicket-bench easy access to the information
	 * ...
	 * 
	 * @param index
	 *            Index into markup list
	 * @return Markup element
	 */
	public MarkupElement get(final int index)
	{
		return (MarkupElement)markup.get(index);
	}

	/**
	 * Gets the resource that contains this markup
	 * 
	 * @return The resource where this markup came from
	 */
	MarkupResourceStream getResource()
	{
		return resource;
	}

	/**
	 * For Wicket it would be sufficient for this method to be package
	 * protected. However to allow wicket-bench easy access to the information
	 * ...
	 * 
	 * @return Number of markup elements
	 */
	public int size()
	{
		return markup.size();
	}

	/**
	 * Return the XML declaration string, in case if found in the markup.
	 * 
	 * @return Null, if not found.
	 */
	public String getXmlDeclaration()
	{
		return xmlDeclaration;
	}

	/**
	 * Gets the markup encoding. A markup encoding may be specified in a markup
	 * file with an XML encoding specifier of the form &lt;?xml ...
	 * encoding="..." ?&gt;.
	 * 
	 * @return Encoding, or null if not found.
	 */
	public String getEncoding()
	{
		return encoding;
	}

	/**
	 * Get the current index pointing to the start element of the header
	 * section.
	 * 
	 * @return index
	 */
	public int getHeaderIndex()
	{
		return this.headerIndex;
	}

	/**
	 * Get the wicket namespace valid for this specific markup
	 * 
	 * @return wicket namespace
	 */
	public String getWicketNamespace()
	{
		return this.wicketNamespace;
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

		// TODO General: a component path e.g. "panel:label" does not match 1:1
		// with the markup in case of ListView, where the path contains a number
		// for each list item. E.g. list:0:label. What we currently do is simply
		// remove the number from the path and hope that no user uses an integer
		// for a component id. This is a hack only. A much better solution would
		// delegate to the various components recursivly to search within there
		// realm only for the components markup. ListItem could than simply
		// do nothing and delegate to there parents.
		String completePath = (path == null || path.length() == 0 ? id : path + ":" + id);

		// s/:\d+//g
		Pattern re = Pattern.compile(":\\d+");
		Matcher matcher = re.matcher(completePath);
		completePath = matcher.replaceAll("");

		// All component tags are registered with the cache
		final Integer value = (Integer)this.componentMap.get(completePath);
		if (value == null)
		{
			// not found
			return -1;
		}

		// return the components position in the markup stream
		return value.intValue();
	}

	/**
	 * Sets encoding.
	 * 
	 * @param encoding
	 *            encoding
	 */
	final void setEncoding(final String encoding)
	{
		this.encoding = encoding;
	}

	/**
	 * Sets wicketNamespace.
	 * 
	 * @param wicketNamespace
	 *            wicketNamespace
	 */
	final void setWicketNamespace(final String wicketNamespace)
	{
		this.wicketNamespace = wicketNamespace;
		this.wicketId = wicketNamespace + ":id";

		if (!ComponentTag.DEFAULT_WICKET_NAMESPACE.equals(wicketNamespace))
		{
			log.info("You are using a non-standard component name: " + wicketNamespace);
		}
	}

	/**
	 * Sets xmlDeclaration.
	 * 
	 * @param xmlDeclaration
	 *            xmlDeclaration
	 */
	final void setXmlDeclaration(final String xmlDeclaration)
	{
		this.xmlDeclaration = xmlDeclaration;
	}

	/**
	 * Sets the resource stream associated with the markup. It is for diagnostic
	 * purposes only.
	 * 
	 * @param resource
	 *            the markup resource stream
	 */
	final void setResource(final MarkupResourceStream resource)
	{
		this.resource = resource;
	}

	/**
	 * Add a MarkupElement
	 * 
	 * @param markupElement
	 */
	final void addMarkupElement(final MarkupElement markupElement)
	{
		this.markup.add(markupElement);
	}

	/**
	 * Add a ComponentTag
	 * 
	 * @param tag
	 */
	final void addMarkupElement(final ComponentTag tag)
	{
		this.markup.add(tag);

		// Set the tags components path and add it to the local cache
		setComponentPath(tag);
		
		// Add to the local cache to be found fast if required
		addToCache(tag);
	}

	/**
	 * Add the tag to the local cache if open or open-close and if wicket:id is
	 * present
	 * 
	 * @param tag
	 */
	private void addToCache(final ComponentTag tag)
	{
		// Only if the tag has wicket:id="xx" and open or open-close
		if ((tag.isOpen() || tag.isOpenClose()) && tag.getAttributes().containsKey(wicketId))
		{
			// Add the tag to the cache
			if (this.componentMap == null)
			{
				this.componentMap = new HashMap();
			}

			final String key = (tag.getPath() != null ? tag.getPath() + ":" + tag.getId() : tag
					.getId());
			this.componentMap.put(key, new Integer(this.markup.size() - 1));
		}
	}

	/**
	 * Set the components path within the markup and add the component tag to
	 * the local cache
	 * 
	 * @param tag
	 */
	private void setComponentPath(final ComponentTag tag)
	{
		// Only if the tag has wicket:id="xx" and open or open-close
		if ((tag.isOpen() || tag.isOpenClose()) && tag.getAttributes().containsKey(wicketId))
		{
			// With open-close the path does not change. It can/will not have
			// children
			if (tag.isOpenClose())
			{
				// Set the components path.
				if ((this.currentPath != null) && (this.currentPath.length() > 0))
				{
					tag.setPath(this.currentPath.toString());
				}
			}
			else
			{
				// Set the components path.
				if (this.currentPath == null)
				{
					this.currentPath = new StringBuffer(100);
				}
				else if (this.currentPath.length() > 0)
				{
					tag.setPath(this.currentPath.toString());
					this.currentPath.append(':');
				}

				// .. and append the tags id to the component path for the
				// children to come
				this.currentPath.append(tag.getId());
			}
		}
		else if (tag.isClose() && (this.currentPath != null))
		{
			// Remove the last element from the component path
			int index = this.currentPath.lastIndexOf(":");
			if (index != -1)
			{
				this.currentPath.setLength(index);
			}
			else
			{
				this.currentPath.setLength(0);
			}
		}
	}

	/**
	 * Make all tags immutable and the list of elements unmodifable
	 */
	final void makeImmutable()
	{
		for (int i = 0; i < this.markup.size(); i++)
		{
			MarkupElement elem = (MarkupElement)this.markup.get(i);
			if (elem instanceof ComponentTag)
			{
				((ComponentTag)elem).makeImmutable();
			}
		}

		this.markup = Collections.unmodifiableList(this.markup);

		initialize();
	}

	/**
	 * Reset the markup to its defaults, except for the wicket namespace which
	 * remains unchanged.
	 */
	final void reset()
	{
		this.markup = new ArrayList();
		this.resource = null;
		this.xmlDeclaration = null;
		this.encoding = null;
		this.headerIndex = NO_HEADER_FOUND;
		this.currentPath = null;
	}

	/**
	 * Create an iterator for the component tags in the markup.
	 * 
	 * @param startIndex
	 *            The index to start with
	 * @param matchClass
	 *            Iterate over elements matching the class
	 * @return ComponentTagIterator
	 */
	public Iterator componentTagIterator(final int startIndex, final Class matchClass)
	{
		return new Iterator()
		{
			int index = startIndex - 1;

			public boolean hasNext()
			{
				while (++index < size())
				{
					MarkupElement element = get(index);
					if (matchClass.isInstance(element))
					{
						return true;
					}
				}
				return false;
			}

			public Object next()
			{
				return get(index);
			}

			public void remove()
			{
				throw new IllegalArgumentException("remove() is not supported");
			}
		};
	}
}
