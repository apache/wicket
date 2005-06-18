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

import wicket.util.resource.IResourceStream;

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
	/** The list of markup elements */
	private final List markup;
	
	/** The markup's resource stream for diagnostic purposes */
	private final IResourceStream resource;
	
	/** If found in the markup, the <?xml ...?> string */
	private final String xmlDeclaration;
	
	/** The encoding as found in <?xml ... encoding="" ?>.	Null, else */
	private final String encoding;

	/** Wicket namespace: <html xmlns:wicket="http://wicket.sourceforge.net"> */
	private final String wicketNamespace;
	
	/** The Class of the directly associated component/container */
	private Class containerClass;
	
    /** Default value: The markup has not been search for the header */ 
    public final static int HEADER_NOT_YET_EVALUATED = -1;
    
    /** Markup have been searched for the header, but it doesn't contain any */
    public final static int HEADER_NO_HEADER_FOUND = -2;

	/** If the markup contains a header section, the index will point to
	 * the MarkupElement.
	 */
	private int headerIndex = HEADER_NOT_YET_EVALUATED;
	
	/** Placeholder that indicates no markup */
	public static final Markup NO_MARKUP = new Markup(null, null, null, null, ComponentTag.DEFAULT_WICKET_NAMESPACE);

	/**
	 * Constructor
	 * @param resource The resource where the markup was found
	 * @param markup The markup elements
	 * @param xmlDeclaration The <?xml ...?> string from markup, if avaiable
	 * @param encoding The encoding of the markup file read taken from <?xml ..encoding=".." ?>
	 * @param wicketNamespace Wicket namespace taken from xmlns:wicket="http://wicket.sourceforge.net"
	 */
	Markup(final IResourceStream resource, final List markup, final String xmlDeclaration, 
	        final String encoding, final String wicketNamespace)
	{
		this.resource = resource;
		this.markup = markup;
		this.xmlDeclaration = xmlDeclaration;
		this.encoding = encoding;
		this.wicketNamespace = wicketNamespace;
	}

	/**
	 * Set the component/container's class directly associated with the markup
	 * 
	 * @param containerClass
	 */
	void setContainerClass(final Class containerClass)
	{
	    this.containerClass = containerClass;
	}

	/**
	 * Get the component/container's class directly associated with the markup
	 * 
	 * @return The component's class
	 */
	Class getContainerClass()
	{
	    return this.containerClass;
	}
	
	/**
	 * @return String representation of markup list
	 */
	public String toString()
	{
		return resource.toString();
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
	IResourceStream getResource()
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
	 * Set the index pointing to the header element of the markup
	 * 
	 * @param index
	 */
	public void setHeaderIndex(final int index)
	{
	    this.headerIndex = index;
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
}
