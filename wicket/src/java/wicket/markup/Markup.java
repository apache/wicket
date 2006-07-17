/*
 * $Id$ $Revision$ $Date:
 * 2006-05-26 00:41:52 +0200 (vr, 26 mei 2006) $
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
public class Markup implements IMarkup
{
	private static final Log log = LogFactory.getLog(Markup.class);

	/** The list of markup elements */
	private MarkupFragment markup;

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
	 * Used at markup load time to maintain the current component path (not id)
	 * while adding markup elements to this Markup instance
	 */
	private StringBuffer currentPath;

	/**
	 * A cache which maps (componentPath + id) to the componentTags index in the
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
	 * Initialize the index where <head> can be found.
	 */
	protected void initialize()
	{
		// Reset
		this.componentMap = null;

		if (markup != null)
		{
			// HTML tags like <img> may not have a close tag. But because that
			// can only be detected until later on in the sequential markup
			// reading loop, we only can do it now.
			StringBuffer componentPath = null;
			for (int i = 0; i < this.markup.size(); i++)
			{
				MarkupElement elem = this.markup.get(i);
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
		this.currentPath = null;
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
		else
		{
			return "(unknown resource)";
		}
	}

	/**
	 * @see wicket.markup.IMarkup#toDebugString()
	 */
	public String toDebugString()
	{
		return this.markup.toString();
	}

	/**
	 * @see wicket.markup.IMarkup#get(int)
	 */
	public MarkupElement get(final int index)
	{
		return markup.get(index);
	}

	/**
	 * Gets the resource that contains this markup
	 * 
	 * @return The resource where this markup came from
	 */
	public final MarkupResourceStream getResource()
	{
		return resource;
	}

	/**
	 * @see wicket.markup.IMarkup#size()
	 */
	public int size()
	{
		return markup.size();
	}

	/**
	 * @see wicket.markup.IMarkup#getXmlDeclaration()
	 */
	public String getXmlDeclaration()
	{
		return xmlDeclaration;
	}

	/**
	 * @see wicket.markup.IMarkup#getEncoding()
	 */
	public String getEncoding()
	{
		return encoding;
	}

	/**
	 * @see wicket.markup.IMarkup#getWicketNamespace()
	 */
	public String getWicketNamespace()
	{
		return this.wicketNamespace;
	}

	/**
	 * @see wicket.markup.IMarkup#findComponentIndex(java.lang.String, java.lang.String)
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
		// delegate to the various components recursivly to search within there
		// realm only for the components markup. ListItems could then simply
		// do nothing and delegate to their parents.
		String completePath = (path == null || path.length() == 0 ? id : path + ":" + id);

		// s/:\d+//g
		Pattern re = Pattern.compile(":\\d+");
		Matcher matcher = re.matcher(completePath);
		completePath = matcher.replaceAll("");

		// All component tags are registered with the cache
		if (this.componentMap == null)
		{
			// not found
			return -1;
		}

		final Integer value = this.componentMap.get(completePath);
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
	final void addMarkupElement(final int pos, final MarkupElement markupElement)
	{
		this.markup.addMarkupElement(pos, markupElement);
	}

	/**
	 * Add the tag to the local cache if open or open-close and if wicket:id is
	 * present
	 * 
	 * @param index
	 * @param tag
	 */
	private void addToCache(final int index, final ComponentTag tag)
	{
		// Only if the tag has wicket:id="xx" and open or open-close
		if ((tag.isOpen() || tag.isOpenClose()) && tag.getAttributes().containsKey(wicketId))
		{
			// Add the tag to the cache
			if (this.componentMap == null)
			{
				this.componentMap = new HashMap<String, Integer>();
			}

			final String key;
			if (tag.getPath() != null)
			{
				key = tag.getPath() + ":" + tag.getId();
			}
			else
			{
				key = tag.getId();
			}
			this.componentMap.put(key, new Integer(index));
		}
	}

	/**
	 * Set the components path within the markup and add the component tag to
	 * the local cache
	 * 
	 * @param componentPath
	 * @param tag
	 * @return componentPath
	 */
	private StringBuffer setComponentPathForTag(final StringBuffer componentPath,
			final ComponentTag tag)
	{
		// Only if the tag has wicket:id="xx" and open or open-close
		if ((tag.isOpen() || tag.isOpenClose()) && tag.getAttributes().containsKey(wicketId))
		{
			// With open-close the path does not change. It can/will not have
			// children. The same is true for HTML tags like <br> or <img>
			// which might not have close tags.
			if (tag.isOpenClose() || tag.hasNoCloseTag())
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
			// For example <wicket:message> does not have an id
			if ((tag.getOpenTag() == null)
					|| tag.getOpenTag().getAttributes().containsKey(wicketId))
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

		return this.currentPath;
	}

	/**
	 * Make all tags immutable and the list of elements unmodifable.
	 */
	final void makeImmutable()
	{
		this.markup.makeImmutable();

		// We assume all markup elements have now been added. It is
		// now time to initialize all remaining variables based
		// on the markup loaded, which could not be initialized
		// earlier on.
		initialize();
	}

	/**
	 * Reset the markup to its defaults, except for the wicket namespace which
	 * remains unchanged.
	 */
	final void reset()
	{
		this.markup = new MarkupFragment(this);
		this.resource = null;
		this.xmlDeclaration = null;
		this.encoding = null;
		this.currentPath = null;
	}
}
