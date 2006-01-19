/*
 * $Id$
 * $Revision$ $Date$
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

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import wicket.Application;
import wicket.Page;
import wicket.markup.parser.IMarkupFilter;
import wicket.markup.parser.IXmlPullParser;
import wicket.markup.parser.filter.BodyOnLoadHandler;
import wicket.markup.parser.filter.HtmlHandler;
import wicket.markup.parser.filter.HtmlHeaderSectionHandler;
import wicket.markup.parser.filter.TagTypeHandler;
import wicket.markup.parser.filter.WicketLinkTagHandler;
import wicket.markup.parser.filter.WicketMessageTagHandler;
import wicket.markup.parser.filter.WicketParamTagHandler;
import wicket.markup.parser.filter.WicketRemoveTagHandler;
import wicket.markup.parser.filter.WicketTagIdentifier;
import wicket.util.resource.ResourceStreamNotFoundException;
import wicket.util.value.ValueMap;


/**
 * This is a Wicket MarkupParser specifically for (X)HTML. It makes use of a
 * streaming XML parser to read the markup and IMarkupFilters to remove
 * comments, identify Wicket relevant tags, apply html specific treatments etc..
 * <p>
 * The result will be an Markup object, which is basically a list, containing
 * Wicket relevant tags and RawMarkup.
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
public class MarkupParser
{
	/** The XML parser to use */
	private final IXmlPullParser xmlParser;

	/** The markup handler chain: each filter has a specific task */
	private IMarkupFilter markupFilterChain;

	/** The markup created by reading the markup file */
	private final Markup markup;

	/**
	 * Markup settings are required by various markup filters. Note: We can not
	 * use Application.get() as reading the markup happens in another java
	 * thread due to the ModificationWatcher.
	 */
	private Application application;

	/**
	 * Constructor.
	 * 
	 * @param application
	 *            The wicket application object
	 * @param xmlParser
	 *            The streaming xml parser to read and parse the markup
	 */
	public MarkupParser(final Application application, final IXmlPullParser xmlParser)
	{
		this.application = application;
		this.xmlParser = xmlParser;
		this.markup = new Markup();
	}

	/**
	 * In case you want to analyze markup which by default does not use "wicket"
	 * to find relevant tags.
	 * 
	 * @param namespace
	 */
	public final void setWicketNamespace(final String namespace)
	{
		this.markup.setWicketNamespace(namespace);
	}

	/**
	 * Create a new markup filter chain and initialize with all default filters
	 * required.
	 * 
	 * @param tagList
	 * @return a preconfigured markup filter chain
	 */
	private final IMarkupFilter newFilterChain(final List tagList)
	{
		// Chain together all the different markup filters and configure them
		IMarkupFilter filter = new WicketTagIdentifier(markup, xmlParser);

		filter = new TagTypeHandler(filter);
		filter = new HtmlHandler(filter);
		filter = new WicketParamTagHandler(filter, application.getMarkupSettings());
		filter = new WicketRemoveTagHandler(filter);
		filter = new WicketLinkTagHandler(filter, application.getMarkupSettings());

		// Provided the wicket component requesting the markup is known ...
		MarkupResourceStream resource = markup.getResource();
		if ((resource != null) && (resource.getContainerInfo() != null))
		{
			if (WicketMessageTagHandler.enable)
			{
				filter = new WicketMessageTagHandler(filter, resource.getContainerInfo(),
						application.getResourceSettings());
			}

			filter = new BodyOnLoadHandler(filter);

			// Pages require additional handlers
			if (Page.class.isAssignableFrom(resource.getContainerInfo().getContainerClass()))
			{
				filter = new HtmlHeaderSectionHandler(tagList, filter);
			}
		}

		return filter;
	}

	/**
	 * By default don't do anything. Subclasses may append additional markup
	 * filter if required.
	 */
	protected void initFilterChain()
	{
	}

	/**
	 * Append a new filter to the list of already pre-configured markup filters.
	 * 
	 * @param filter
	 *            The filter to be appended
	 */
	public final void appendMarkupFilter(final IMarkupFilter filter)
	{
		filter.setParent(markupFilterChain);
		markupFilterChain = filter;
	}

	/**
	 * Reads and parses markup from a file.
	 * 
	 * @param resource
	 *            The file
	 * @return The markup
	 * @throws ParseException
	 * @throws IOException
	 * @throws ResourceStreamNotFoundException
	 */
	final Markup readAndParse(final MarkupResourceStream resource) throws ParseException,
			IOException, ResourceStreamNotFoundException
	{
		// Remove all existing markup elements
		this.markup.reset();

		// For diagnostic purposes
		this.markup.setResource(resource);

		// Initialize the xml parser
		this.xmlParser.parse(resource);

		// parse the xml markup and tokenize it into wicket relevant markup
		// elements
		parseMarkup();

		this.markup.setEncoding(xmlParser.getEncoding());
		this.markup.setXmlDeclaration(xmlParser.getXmlDeclaration());

		return this.markup;
	}

	/**
	 * Parse the markup.
	 * 
	 * @param string
	 *            The markup
	 * @return The markup
	 * @throws ParseException
	 * @throws IOException
	 * @throws ResourceStreamNotFoundException
	 */
	final Markup parse(final String string) throws ParseException, IOException,
			ResourceStreamNotFoundException
	{
		// Remove all existing markup elements
		this.markup.reset();

		// Initialize the xml parser
		this.xmlParser.parse(string);

		// parse the xml markup and tokenize it into wicket relevant markup
		// elements
		parseMarkup();

		this.markup.setEncoding(xmlParser.getEncoding());
		this.markup.setXmlDeclaration(xmlParser.getXmlDeclaration());

		return this.markup;
	}

	/**
	 * Scans the given markup string and extracts balancing tags.
	 * 
	 * @throws ParseException
	 *             Thrown if markup is malformed or tags don't balance
	 */
	private void parseMarkup() throws ParseException
	{
		final List autoAddList = new ArrayList();

		this.markupFilterChain = newFilterChain(autoAddList);
		initFilterChain();

		boolean stripComments = application.getMarkupSettings().getStripComments();
		boolean compressWhitespace = application.getMarkupSettings().getCompressWhitespace();

		try
		{
			// Loop through tags
			for (ComponentTag tag; null != (tag = (ComponentTag)markupFilterChain.nextTag());)
			{
				boolean add = (tag.getId() != null);
				if (!add && tag.getXmlTag().isClose())
				{
					add = ((tag.getOpenTag() != null) && (tag.getOpenTag().getId() != null));
				}

				// Determine wicket namespace: <html
				// xmlns:wicket="http://wicket.sourceforge.net">
				RawMarkup replaceTag = null;
				if (tag.isOpen() && "html".equals(tag.getName().toLowerCase()))
				{
					// if add already true, do not make it false
					add |= determineWicketNamespace(tag);

					// If add and tag has no wicket:id, than
					if ((add == true) && (tag.getId() == null))
					{
						// Replace the current tag
						replaceTag = new RawMarkup(tag.toString());
					}
				}

				// Add tag to list?
				if (add || (autoAddList.size() > 0) || tag.isModified())
				{
					final CharSequence text = xmlParser.getInputFromPositionMarker(tag.getPos());

					// Add text from last position to tag position
					if (text.length() > 0)
					{
						String rawMarkup = text.toString();

						if (stripComments)
						{
							rawMarkup = rawMarkup.replaceAll("<!--(.|\n|\r)*?-->", "");
						}

						if (compressWhitespace)
						{
							rawMarkup = rawMarkup.replaceAll("[ \\t]+", " ");
							rawMarkup = rawMarkup.replaceAll("( ?[\\r\\n] ?)+", "\n");
						}

						this.markup.addMarkupElement(new RawMarkup(rawMarkup));
					}

					if ((add == false) && (autoAddList.size() > 0))
					{
						xmlParser.setPositionMarker(tag.getPos());
					}

					for (int i = 0; i < autoAddList.size(); i++)
					{
						this.markup.addMarkupElement((MarkupElement)autoAddList.get(i));
					}
					autoAddList.clear();
				}

				if (add)
				{
					// Add to list unless preview component tag remover flagged
					// as removed
					if (!WicketRemoveTagHandler.IGNORE.equals(tag.getId()))
					{
						if (replaceTag != null)
						{
							this.markup.addMarkupElement(replaceTag);
						}
						else
						{
							this.markup.addMarkupElement(tag);
						}
					}

					xmlParser.setPositionMarker();
				}
				else if (tag.isModified())
				{
					this.markup.addMarkupElement(new RawMarkup(tag.toString()));
					xmlParser.setPositionMarker();
				}
			}
		}
		catch (ParseException ex)
		{
			// Add remaining input string
			final CharSequence text = xmlParser.getInputFromPositionMarker(-1);
			if (text.length() > 0)
			{
				this.markup.addMarkupElement(new RawMarkup(text));
			}

			this.markup.setEncoding(xmlParser.getEncoding());
			this.markup.setXmlDeclaration(xmlParser.getXmlDeclaration());

			MarkupStream markupStream = new MarkupStream(markup);
			markupStream.setCurrentIndex(this.markup.size() - 1);
			throw new MarkupException(markupStream, ex.getMessage());
		}

		// Add tail?
		final CharSequence text = xmlParser.getInputFromPositionMarker(-1);
		if (text.length() > 0)
		{
			this.markup.addMarkupElement(new RawMarkup(text));
		}

		// Make all tags immutable and the list of elements unmodifable
		this.markup.makeImmutable();
	}

	/**
	 * Determine wicket namespace from xmlns:wicket or
	 * xmlns:wicket="http://wicket.sourceforge.net"
	 * 
	 * @param tag
	 * @return true, if tag has been modified
	 */
	private boolean determineWicketNamespace(final ComponentTag tag)
	{
		String attrValue = null;
		final ValueMap attributes = tag.getAttributes();
		final Iterator it = attributes.keySet().iterator();
		while (it.hasNext())
		{
			final String attributeName = (String)it.next();
			if (attributeName.startsWith("xmlns:"))
			{
				final String xmlnsUrl = attributes.getString(attributeName);
				if ((xmlnsUrl == null) || (xmlnsUrl.trim().length() == 0)
						|| xmlnsUrl.toLowerCase().startsWith("http://wicket.sourceforge.net"))
				{
					String namespace = attributeName.substring(6);
					markup.setWicketNamespace(namespace);
					attrValue = attributeName;
				}
			}
		}

		// Note: <html ...> are usually no wicket tags and thus treated as raw
		// markup and thus removing xmlns:wicket from markup does not have any
		// effect. The solution approach does not work.
		if ((attrValue != null) && Application.get().getMarkupSettings().getStripWicketTags())
		{
			attributes.remove(attrValue);
			return true;
		}

		return false;
	}
}
