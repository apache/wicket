/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.markup;

import java.util.List;
import java.util.Stack;

/**
 * Holds markup as a resource (the stream that the markup came from) and 
 * a list of MarkupElements (the markup itself). 
 * 
 * @see MarkupElement
 * @see ComponentTag
 * @see wicket.markup.RawMarkup
 * @author Jonathan Locke
 */
public final class Markup
{
	/** Placeholder that indicates no markup */
	public static final Markup NO_MARKUP = new Markup(null, null, null, null, ComponentTag.DEFAULT_WICKET_NAMESPACE);
	
	/** The list of markup elements */
	private /* final */ List markup;
	
	/** The markup's resource stream for diagnostic purposes */
	private final MarkupResourceStream resource;
	
	/** If found in the markup, the <?xml ...?> string */
	private final String xmlDeclaration;
	
	/** The encoding as found in <?xml ... encoding="" ?>.	Null, else */
	private final String encoding;

	/** Wicket namespace: <html xmlns:wicket="http://wicket.sourceforge.net"> */
	private final String wicketNamespace;
    
    /** Markup has been searched for the header, but it doesn't contain any */
    public final static int NO_HEADER_FOUND = -1;

	/** If the markup contains a header section, the index will point to
	 * the MarkupElement.
	 */
	private int headerIndex = NO_HEADER_FOUND;
	
	/**
	 * Constructor
	 * 
	 * @param resource The resource where the markup was found
	 * @param markup The markup elements
	 * @param xmlDeclaration The <?xml ...?> string from markup, if avaiable
	 * @param encoding The encoding of the markup file read taken from <?xml ..encoding=".." ?>
	 * @param wicketNamespace Wicket namespace taken from xmlns:wicket="http://wicket.sourceforge.net"
	 */
	Markup(final MarkupResourceStream resource, final List markup, final String xmlDeclaration, 
	        final String encoding, final String wicketNamespace)
	{
		this.resource = resource;
		this.markup = markup;
		this.xmlDeclaration = xmlDeclaration;
		this.encoding = encoding;
		this.wicketNamespace = wicketNamespace;
		
		initialize();
	}
	
	/**
	 * Kind of copy constructor, though new markup elements are attached
	 * 
	 * @param markup The Markup which variables are copied
	 * @param markupElements The new list of markup elements
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
	 * Initialize the index where <head> can be found.
	 */
	private void initialize()
	{
	    if (markup != null)
	    {
	   	 	// Initialize the index where <wicket:extend> can be found.
		    for (int i=0; i < markup.size(); i++)
		    {
		        MarkupElement elem = (MarkupElement) markup.get(i);
		        if (elem instanceof WicketTag)
		        {
		            WicketTag tag = (WicketTag) elem;
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
	 * @param index Index into markup list
	 * @return Markup element
	 */
	MarkupElement get(final int index)
	{
		return (MarkupElement)markup.get(index);
	}

	/**
	 * Gets the resource that contains this markup
	 * @return The resource where this markup came from
	 */
	MarkupResourceStream getResource()
	{
		return resource;
	}

	/**
	 * @return Number of markup elements
	 */
	int size()
	{
		return markup.size();
	}

	/**
	 * Return the XML declaration string, in case if found in the
	 * markup.
	 * 
	 * @return Null, if not found.
	 */
	public String getXmlDeclaration()
	{
		return xmlDeclaration;
	}

	/**
	 * Gets the markup encoding.  A markup encoding may be specified in
	 * a markup file with an XML encoding specifier of the form
	 * &lt;?xml ... encoding="..." ?&gt;.
	 *
	 * @return Encoding, or null if not found.
	 */
	public String getEncoding()
	{
		return encoding;
	}

	/**
	 * Get the current index pointing to the start element of the 
	 * header section.
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
	 * @param path The component path expression
	 * @param id The component's id to search for
	 * @return -1, if not found
	 */
	public int findComponent(String path, final String id)
	{
		final String wicketId = ComponentTag.DEFAULT_WICKET_NAMESPACE + ":id";

		// Find the tag. Rebuild the tree structure
		Stack markupElements = new Stack();
		
		// The path of the current tag
		String elementsPath = "";
		
		// The return value
		int position = -1;
		
		// Iterate through all markup elements
		for (int pos = 0; pos < markup.size(); pos ++)
		{
			// Only ComponentTags are of interest
			MarkupElement element = (MarkupElement)markup.get(pos);
			if (element instanceof ComponentTag)
			{
				ComponentTag tag = (ComponentTag) element;
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
					
					// If open tag, put the path of the current element onto the stack
					// and adjust the path (walk into the subdirectory)
					if (tag.isOpen())
					{
						markupElements.push(elementsPath);
						if (hasWicketId)
						{
							elementsPath += ":" + tag.getId();
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
}
