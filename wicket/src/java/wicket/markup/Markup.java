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

import wicket.util.resource.IResource;

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
	private final IResource resource;
	
	/** If found in the markup, the <?xml ...?> string */
	private final String xmlDeclaration;
	
	/** The encoding as found in <?xml ... encoding="" ?>.	Null, else */
	private final String encoding;
	
	/** Placeholder that indicates no markup */
	public static final Markup NO_MARKUP = new Markup(null, null, null, null);

	/**
	 * Constructor
	 * @param resource The resource where the markup was found
	 * @param markup The markup elements
	 * @param xmlDeclaration The <?xml ...?> string from markup, if avaiable
	 * @param encoding The encoding of the markup file read taken from <?xml ..encoding=".." ?>
	 */
	Markup(final IResource resource, final List markup, final String xmlDeclaration, final String encoding)
	{
		this.resource = resource;
		this.markup = markup;
		this.xmlDeclaration = xmlDeclaration;
		this.encoding = encoding;
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
	IResource getResource()
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
}
