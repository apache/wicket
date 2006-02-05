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
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Holds markup as a resource (the stream that the markup came from) and a list
 * of MarkupElements (the markup itself).
 * 
 * @see MarkupElement
 * @see ComponentTag
 * @see wicket.markup.RawMarkup
 * @author Jonathan Locke
 */
public final class Markup
{
	private final static Log log = LogFactory.getLog(Markup.class);

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

	/** Markup has been searched for the header, but it doesn't contain any */
	public final static int NO_HEADER_FOUND = -1;

	/**
	 * If the markup contains a header section, the index will point to the
	 * MarkupElement.
	 */
	private int headerIndex = NO_HEADER_FOUND;

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
		this.markup = markupElements;
		this.xmlDeclaration = markup.xmlDeclaration;
		this.encoding = markup.encoding;
		this.wicketNamespace = markup.wicketNamespace;

		initialize();
	}

	/**
	 * Constructor
	 */
	Markup()
	{
		this.markup = new ArrayList();
		this.wicketNamespace = ComponentTag.DEFAULT_WICKET_NAMESPACE;
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
	 * For Wicket it would be sufficient for this method to be 
	 * package protected. However to allow wicket-bench easy access
	 * to the information ...
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
	 * For Wicket it would be sufficient for this method to be 
	 * package protected. However to allow wicket-bench easy access
	 * to the information ...
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
	public int findComponentIndex(String path, final String id)
	{
		final String wicketId = ComponentTag.DEFAULT_WICKET_NAMESPACE + ":id";

		// Find the tag. Rebuild the tree structure
		Stack markupElements = new Stack();

		// The path of the current tag
		String elementsPath = "";

		// The return value
		int position = -1;

		// Iterate through all markup elements
		for (int pos = 0; pos < markup.size(); pos++)
		{
			// Only ComponentTags are of interest
			MarkupElement element = (MarkupElement)markup.get(pos);
			if (element instanceof ComponentTag)
			{
				ComponentTag tag = (ComponentTag)element;
				if (tag.isOpen() || tag.isOpenClose())
				{
					// If has wicket:id ..
					boolean hasWicketId = tag.getAttributes().containsKey(wicketId);
					if (hasWicketId)
					{
						// .. and wicket:id is equals to what I'm looking for
						if (tag.getId().equals(id))
						{
							// .. and the path is right as well
							if (elementsPath.equals(path))
							{
								// .. than we found it.
								position = pos;
								break;
							}
						}
					}

					// If open tag, put the path of the current element onto the
					// stack and adjust the path (walk into the subdirectory)
					if (tag.isOpen())
					{
						markupElements.push(elementsPath);
						if (hasWicketId)
						{
							if (elementsPath.length() > 0)
							{
								elementsPath += ":";
							}
							elementsPath += tag.getId();
						}
					}
				}
				else if (tag.isClose())
				{
					// return to the parent "directory"
					elementsPath = (String)markupElements.pop();
				}
			}
		}

		return position;
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
