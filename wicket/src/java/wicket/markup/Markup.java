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

	/** The new world: markup relativ path => markup fragment */
	private Map<String, MarkupFragment> pathToFragment = new HashMap<String, MarkupFragment>();

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
	 * Add a MarkupElement
	 * 
	 * @param markupElement
	 */
	public final void addMarkupElement(final MarkupElement markupElement)
	{
		this.markup.addMarkupElement(markupElement);
	}

	/**
	 * Add a MarkupElement
	 * 
	 * @param pos
	 * @param markupElement
	 */
	public final void addMarkupElement(final int pos, final MarkupElement markupElement)
	{
		this.markup.addMarkupElement(pos, markupElement);
	}

	/**
	 * Make all tags immutable and the list of elements unmodifable.
	 */
	final void makeImmutable()
	{
		this.markup.makeImmutable();
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
	 * @see wicket.markup.IMarkup#findComponentIndex(java.lang.String,
	 *      java.lang.String)
	 */
	public int findTag(final String path)
	{
		if ((path == null) || (path.length() == 0))
		{
			throw new IllegalArgumentException("Parameter 'path' must not be null");
		}

		if ((this.markup != null) && (this.componentMap == null))
		{
			initialize();
		}

		// All component tags are registered with the cache
		if (this.componentMap == null)
		{
			// not found
			return -1;
		}

		final Integer value = this.componentMap.get(path);
		if (value == null)
		{
			// not found
			return -1;
		}

		// return the components position in the markup stream
		return value.intValue();
	}

	/**
	 * Find the markup fragment for the component with 'path'
	 * 
	 * @param path
	 *            The markup path to find the fragment
	 * @param throwException
	 *            If true, throw an exception if fragment not found
	 * @return MarkupFragment Might be null, if fragment not found
	 */
	public MarkupFragment findMarkupFragment(final String path, final boolean throwException)
	{
		if ((path == null) || (path.length() == 0))
		{
			throw new IllegalArgumentException("Parameter 'path' must not be null");
		}

		if ((this.markup != null) && (this.markupFragments == null))
		{
			initialize();
		}

		// All component tags are registered with the cache
		if (this.pathToFragment == null)
		{
			if (throwException == true)
			{
				throw new MarkupNotFoundException("Markup not found for tag with path: " + path
						+ "; The markup does not have any Wicket tag");
			}

			// not found
			return null;
		}

		// Get the fragment from the cache
		MarkupFragment fragment = this.pathToFragment.get(path);
		if (fragment == null)
		{
			if (throwException == true)
			{
				throw new MarkupNotFoundException("Markup not found for tag with path: " + path);
			}

			return null;
		}

		return fragment;
	}

	/**
	 * Update internal Maps to find wicket tags easily
	 */
	private void initialize()
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

			initializeMarkupFragments();
		}
	}

	/**
	 * Until we started with AJAX we iterated over the markup and tried to find
	 * the component. With AJAX however we need to find the Markup for a
	 * Component. In an attempt to change Wicket internals step by step,
	 * initializeMarkupFragments() creates a hierarchal structure of
	 * MarkupFragments and other MarkupElements based on the simple List of
	 * elements per Markup file which we have today.
	 */
	private void initializeMarkupFragments()
	{
		// The root element for the markup file. The root element should be only
		// fragment which not necessarily has a ComponentTag as first element
		this.markupFragments = new MarkupFragment(this);
		MarkupFragment current = this.markupFragments;

		// Remember the path associated with a ComponentTag to properly walk up
		// and down the hierarchy of wicket markup tags
		Stack<String> stack = new Stack<String>();
		String basePath = null;

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
						+ IMarkup.TAG_PATH_SEPARATOR + tag.getId());

				// Depending on tag type (open, close, open-close) ...
				if (tag.isOpen())
				{
					// Open tags with no close tags (HTML) are treated like
					// open-close.
					if (tag.hasNoCloseTag())
					{
						MarkupFragment fragment = new MarkupFragment(this, current, tag);
						this.pathToFragment.put(path, fragment);
					}
					else
					{
						// If open tag and auto component (BODY, HEAD, etc.)
						// than the markup path gets not updated as the markup
						// for BODY e.g. does not have a wicket:id.
						current = new MarkupFragment(this, current, tag);
						stack.push(basePath);
						if (tag.getId().startsWith(Component.AUTO_COMPONENT_PREFIX))
						{
							this.pathToFragment.put(path, current);
						}
						else
						{
							basePath = path;
							this.pathToFragment.put(basePath, current);
						}
					}
				}
				else if (tag.isOpenClose())
				{
					MarkupFragment fragment = new MarkupFragment(this, current, tag);
					this.pathToFragment.put(path, fragment);
				}
				else
				// if (tag.isClose()
				{
					current.addMarkupElement(tag);
					current = current.getParentFragment();
					basePath = stack.pop();
				}
			}
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
				tagPath.append(TAG_PATH_SEPARATOR);
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
				final int index = tagPath.lastIndexOf(String.valueOf(TAG_PATH_SEPARATOR));
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
