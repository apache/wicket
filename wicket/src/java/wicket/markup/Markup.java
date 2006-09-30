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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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

	/** The list of markup elements */
	private MarkupFragment markup;

	/** The new world: One fragment per Component */
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
	 * A cache which maps (tag path + id) to the componentTags index in the
	 * markup
	 */
	private Map<String, Integer> componentMap;

	/**
	 * Constructor
	 */
	Markup()
	{
		this.markup = new MarkupFragment(this);
		setWicketNamespace(ComponentTag.DEFAULT_WICKET_NAMESPACE);
	}

	/**
	 * @see wicket.markup.IMarkup#get(int)
	 */
	public MarkupElement get(final int index)
	{
		return this.markup.get(index);
	}

	/**
	 * This is realy for the old world only and will be removed once the changes
	 * have been made. It return a flat list of all MarkupElements in the Markup
	 * instead of a tree-like structure which is equal to the markup structure.
	 * 
	 * @return MarkupFragment which contains a single list of all MarkupElements
	 *         of the Markup
	 */
	public final MarkupFragment getAllMarkupElementsFlat()
	{
		return this.markup;
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
	 * @see wicket.markup.IMarkup#size()
	 */
	public int size()
	{
		if ((this.markup != null) && (this.markupFragments == null))
		{
			initialize();
		}
		return this.markup.size();
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
		// TODO This is realy for historical reasons only and should be removed
		// once the markup fragment changes have been finished.
		// See MarkupParser.parseMarkup() last line.
		if ((this.markup != null) && (this.markup.size() == 0) && (this.markupFragments != null)
				&& (this.markupFragments.size() != 0))
		{
			this.markup = this.markupFragments;
			this.markupFragments = null;
		}

		if ((this.markup != null) && (this.markupFragments == null))
		{
			initialize();
		}

		this.markup.makeImmutable();

		if (this.markupFragments != null)
		{
			this.markupFragments.makeImmutable();
		}
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
			markup.markup = new MarkupFragment(markup);
			for (MarkupElement elem : this.markup)
			{
				markup.markup.addMarkupElement(elem);
			}
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
		if ((this.markup != null) && (this.markupFragments == null))
		{
			initialize();
		}

		return this.markupFragments;
	}

	/**
	 * Iterator for MarkupElements
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<MarkupElement> iterator()
	{
		return this.markup.iterator();
	}

	/**
	 * Update internal Maps to find wicket tags easily
	 */
	private void initialize()
	{
		initializeComponentPathCache();
		initializeMarkupFragments();
	}

	/**
	 * Initialize the internal component-path-for-tag cache
	 */
	private void initializeComponentPathCache()
	{
		// Reset
		this.componentMap = new HashMap<String, Integer>();

		if (markup != null)
		{
			// The current tag path
			StringBuffer componentPath = new StringBuffer(100);

			// For each ComponentTag
			for (int i = 0; i < this.markup.size(); i++)
			{
				final MarkupElement elem = this.markup.get(i);
				if (elem instanceof ComponentTag)
				{
					// Based on the tag and the current tag path, create a
					// cache entry and update the tag path.
					final ComponentTag tag = (ComponentTag)elem;
					componentPath = setComponentPathForTag(componentPath, tag, i);
				}
			}
		}
	}

	/**
	 * In an attempt to change Wicket internals step by step,
	 * initializeMarkupFragments() creates a hierarchal structure of
	 * MarkupFragments and other MarkupElements based on the simple List of
	 * elements per Markup file which we have today.
	 */
	private void initializeMarkupFragments()
	{
		if (markup == null)
		{
			return;
		}

		this.markupFragments = new MarkupFragment(this);

		// Remember the path associated with a ComponentTag to properly walk up
		// and down the hierarchy of wicket markup tags
		Stack<String> stack = new Stack<String>();
		String basePath = null;

		Stack<MarkupFragment> fragmentStack = new Stack<MarkupFragment>();
		MarkupFragment current = this.markupFragments;

		// For all markup element in the external markup file
		for (MarkupElement elem : this.markup)
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

		if ((this.markupFragments.size() == 1)
				&& (this.markupFragments.get(0) instanceof MarkupFragment))
		{
			this.markupFragments = (MarkupFragment)this.markupFragments.get(0);
		}
	}

	/**
	 * Based on the tag and its current tag path create a cache entry and update
	 * the tag path again depending on the tag
	 * 
	 * @param tagPath
	 *            The current tag path in the markup
	 * @param tag
	 *            The current tag
	 * @param tagIndex
	 *            The index of the tag within the markup
	 * @return Updated tag path for the next ComponentTag in the markup
	 */
	private StringBuffer setComponentPathForTag(final StringBuffer tagPath, final ComponentTag tag,
			final int tagIndex)
	{
		// Only if the tag has wicket:id="xx" and open or open-close
		if ((tag.isOpen() || tag.isOpenClose()) && tag.getAttributes().containsKey(wicketId))
		{
			int size = tagPath.length();
			if (size > 0)
			{
				tagPath.append(Component.PATH_SEPARATOR);
			}
			tagPath.append(tag.getId());

			this.componentMap.put(tagPath.toString(), Integer.valueOf(tagIndex));

			// With open-close the path does not change. It can/will not have
			// children. The same is true for HTML tags like <br> or <img>
			// which might not have close tags.
			if (tag.isOpenClose() || tag.hasNoCloseTag())
			{
				tagPath.setLength(size);
			}
		}
		else if (tag.isClose() && (tagPath != null))
		{
			// For example <wicket:message> does not have an id
			if ((tag.getOpenTag() == null)
					|| tag.getOpenTag().getAttributes().containsKey(wicketId))
			{
				// Remove the last element from the component path
				final int index = tagPath.lastIndexOf(String.valueOf(Component.PATH_SEPARATOR));
				if (index != -1)
				{
					tagPath.setLength(index);
				}
				else
				{
					tagPath.setLength(0);
				}
			}
		}

		return tagPath;
	}

	/**
	 * @see wicket.markup.IMarkup#toDebugString()
	 */
	public String toDebugString()
	{
		return this.markup.toString();
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
