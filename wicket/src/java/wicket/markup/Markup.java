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

import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;

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
public class Markup implements IMarkup
{
	private static final Log log = LogFactory.getLog(Markup.class);

	/** Tree of markup fragments; one per component */
	private MarkupFragment markupFragments;

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

	/**
	 * Constructor
	 */
	Markup()
	{
		this.markupFragments = new MarkupFragment(this);
		setWicketNamespace(ComponentTag.DEFAULT_WICKET_NAMESPACE);
	}

	/**
	 * Gets the resource that contains this markup
	 * 
	 * @return The resource where this markup came from
	 */
	public final MarkupResourceStream getResource()
	{
		return this.resource;
	}

	/**
	 * @see wicket.markup.IMarkup#getXmlDeclaration()
	 */
	public String getXmlDeclaration()
	{
		return this.xmlDeclaration;
	}

	/**
	 * @see wicket.markup.IMarkup#getEncoding()
	 */
	public String getEncoding()
	{
		return this.encoding;
	}

	/**
	 * @see wicket.markup.IMarkup#getWicketNamespace()
	 */
	public String getWicketNamespace()
	{
		return this.wicketNamespace;
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
	public final void setWicketNamespace(final String wicketNamespace)
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
	 * Make all tags immutable and the list of elements unmodifable.
	 */
	public final void makeImmutable()
	{
		this.markupFragments.makeImmutable();
	}

	/**
	 * Create a mutable copy of the Markup
	 * 
	 * @param emptyCopy
	 *            If true, do NOT copy the markup elements
	 * @return markup
	 */
	public final Markup mutable(final boolean emptyCopy)
	{
		Markup markup = new Markup();

		markup.resource = this.resource;
		markup.xmlDeclaration = this.xmlDeclaration;
		markup.encoding = this.encoding;
		markup.wicketNamespace = this.wicketNamespace;
		markup.wicketId = this.wicketId;

		if (emptyCopy == false)
		{
			// Create a mutable copy of the fragments
			this.markupFragments = this.markupFragments.mutable();
		}

		return markup;
	}

	/**
	 * Get the markup fragments associated with the markup file
	 * 
	 * @return Tree of MarkupFragments and RawMarkup
	 */
	public final MarkupFragment getMarkupFragments()
	{
		return this.markupFragments;
	}

	/**
	 * Update internal Maps to find wicket tags easily
	 */
	private void initialize()
	{
		convertFlatIntoTreeStructure();
	}

	/**
	 * MarkupParser until now creates a flat list of RawMarkup and ComponentTag
	 * elements. However, what we want is a tree like structure with one
	 * fragment per Component.
	 */
	private void convertFlatIntoTreeStructure()
	{
		MarkupFragment rootFragment = new MarkupFragment(this);

		// Remember the path associated with a ComponentTag to properly walk up
		// and down the hierarchy of wicket markup tags
		Stack<String> stack = new Stack<String>();
		String basePath = null;

		Stack<MarkupFragment> fragmentStack = new Stack<MarkupFragment>();
		MarkupFragment current = rootFragment;

		// For all markup element in the external markup file
		for (MarkupElement elem : this.markupFragments)
		{
			// If RawMarkup simply add the element to the current fragment
			if (elem instanceof RawMarkup)
			{
				current.addMarkupElement(elem);
			}
			else
			// if (elem instanceof ComponentTag)
			{
				// Construct the markup path for the tag
				final ComponentTag tag = (ComponentTag)elem;
				final String path = (basePath == null ? tag.getId() : basePath
						+ Component.PATH_SEPARATOR + tag.getId());

				// Depending on tag type (open, close, open-close) ...
				if (tag.isOpen())
				{
					// Open tags with no close tags (HTML) are treated like
					// open-close.
					if (tag.hasNoCloseTag())
					{
						current.addMarkupElement(new MarkupFragment(this, tag));
					}
					else
					{
						// If open tag and auto component (BODY, HEAD, etc.)
						// than the markup path gets not updated as the markup
						// for BODY e.g. does not have a wicket:id.

						stack.push(basePath);
						fragmentStack.push(current);

						MarkupFragment newFragment = new MarkupFragment(this, tag);
						current.addMarkupElement(newFragment);
						current = newFragment;

						if (!tag.getId().startsWith(Component.AUTO_COMPONENT_PREFIX))
						{
							basePath = path;
						}
					}
				}
				else if (tag.isOpenClose())
				{
					MarkupFragment newFragment = new MarkupFragment(this, tag);
					current.addMarkupElement(newFragment);
				}
				else
				// if (tag.isClose()
				{
					current.addMarkupElement(tag);
					current = fragmentStack.pop();
					basePath = stack.pop();
				}
			}
		}

		if ((rootFragment.size() == 1) && (rootFragment.get(0) instanceof MarkupFragment))
		{
			rootFragment = (MarkupFragment)rootFragment.get(0);
		}

		this.markupFragments = rootFragment;
	}

	/**
	 * @see wicket.markup.IMarkup#toDebugString()
	 */
	public String toDebugString()
	{
		return this.markupFragments.toString();
	}

	/**
	 * @see wicket.markup.IMarkup#toString()
	 */
	@Override
	public String toString()
	{
		if (resource != null)
		{
			return resource.toString();
		}
		return "(unknown resource)";
	}
}
