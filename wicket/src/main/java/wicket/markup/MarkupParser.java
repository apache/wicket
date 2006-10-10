/*
 * $Id: MarkupParser.java 5791 2006-05-20 00:32:57 +0000 (Sat, 20 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-20 00:32:57 +0000 (Sat, 20 May
 * 2006) $
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
import java.util.regex.Pattern;

import wicket.Page;
import wicket.markup.parser.IMarkupFilter;
import wicket.markup.parser.IXmlPullParser;
import wicket.markup.parser.XmlPullParser;
import wicket.markup.parser.filter.BodyOnLoadHandler;
import wicket.markup.parser.filter.HeadForceTagIdHandler;
import wicket.markup.parser.filter.HtmlHandler;
import wicket.markup.parser.filter.HtmlHeaderSectionHandler;
import wicket.markup.parser.filter.TagTypeHandler;
import wicket.markup.parser.filter.WicketLinkTagHandler;
import wicket.markup.parser.filter.WicketMessageTagHandler;
import wicket.markup.parser.filter.WicketNamespaceHandler;
import wicket.markup.parser.filter.WicketRemoveTagHandler;
import wicket.markup.parser.filter.WicketTagIdentifier;
import wicket.settings.IMarkupSettings;
import wicket.util.resource.ResourceStreamNotFoundException;
import wicket.util.string.AppendingStringBuffer;

/**
 * This is a Wicket MarkupParser specifically for (X)HTML. It makes use of a
 * streaming XML parser to read the markup and IMarkupFilters to remove
 * comments, identify Wicket relevant tags, apply html specific treatments etc..
 * <p>
 * The result will be an Markup object, which is basically a list, containing
 * Wicket relevant tags and RawMarkup.
 * 
 * @see IMarkupFilter
 * @see IMarkupParserFactory
 * @see IMarkupSettings
 * @see Markup
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
public class MarkupParser
{
	/** Conditional comment section, which is NOT treated as a comment section */
	private static final Pattern CONDITIONAL_COMMENT = Pattern
			.compile("\\[if .+\\]>(.|\n|\r)*<!\\[endif\\]");

	/** The XML parser to use */
	private final IXmlPullParser xmlParser;

	/** The markup handler chain: each filter has a specific task */
	private IMarkupFilter markupFilterChain;

	/** The markup created by reading the markup file */
	private final Markup markup;

	/** The root markup fragment of the markup being parsed */
	private final MarkupFragment rootFragment;

	/** True if comments are to be removed */
	private boolean stripComments;

	/** True if whitespaces are to be compressed */
	private boolean compressWhitespace;

	/**
	 * The default markup encoding which is replaced by MarkupParserFactory with
	 * the Application default
	 */
	private String defaultMarkupEncoding = "UTF-8";

	/**
	 * Constructor.
	 * 
	 * @param resource
	 *            The markup resource (file)
	 */
	public MarkupParser(final MarkupResourceStream resource)
	{
		this(new XmlPullParser(), resource);
	}

	/**
	 * Constructor.
	 * 
	 * @param xmlParser
	 *            The XML parser to use instead of the default on
	 * @param resource
	 *            The markup resource (file)
	 */
	public MarkupParser(final IXmlPullParser xmlParser, final MarkupResourceStream resource)
	{
		// Create the markup before we initialize the chain
		this.markup = new Markup();
		this.markup.setResource(resource);
		this.rootFragment = new MarkupFragment(this.markup);

		// Create a XML parser
		this.xmlParser = xmlParser;

		// Initialize the markup filter chain
		initializeMarkupFilters();
	}

	/**
	 * In case you want to analyze markup which BY DEFAULT does not use "wicket"
	 * to find relevant tags.
	 * 
	 * @param namespace
	 */
	public final void setWicketNamespace(final String namespace)
	{
		this.markup.setWicketNamespace(namespace);
	}

	/**
	 * Sets compressWhitespace.
	 * 
	 * @param compressWhitespace
	 *            compressWhitespace
	 */
	public final void setCompressWhitespace(final boolean compressWhitespace)
	{
		this.compressWhitespace = compressWhitespace;
	}

	/**
	 * Sets defaultMarkupEncoding.
	 * 
	 * @param defaultMarkupEncoding
	 *            defaultMarkupEncoding
	 */
	public final void setDefaultMarkupEncoding(final String defaultMarkupEncoding)
	{
		this.defaultMarkupEncoding = defaultMarkupEncoding;
	}

	/**
	 * Sets stripComments.
	 * 
	 * @param stripComments
	 *            stripComments
	 */
	public final void setStripComments(final boolean stripComments)
	{
		this.stripComments = stripComments;
	}

	/**
	 * Like internal filters, user provided filters might need access to the
	 * markup as well.
	 * 
	 * @return The markup resource stream
	 * @deprecated Since 2.0 please use getMarkupFragment().getMarkup() instead
	 */
	public final IMarkup getMarkup()
	{
		return this.rootFragment.getMarkup();
	}

	/**
	 * Like internal filters, user provided filters might need access to the
	 * markup as well.
	 * 
	 * @return The markup resource stream
	 */
	public final MarkupFragment getMarkupFragment()
	{
		return this.rootFragment;
	}

	/**
	 * Create a new markup filter chain and initialize with all default filters
	 * required.
	 */
	private final void initializeMarkupFilters()
	{
		// Chain together all the different markup filters and configure them
		this.markupFilterChain = xmlParser;

		registerMarkupFilter(new WicketTagIdentifier(markup));
		registerMarkupFilter(new TagTypeHandler());
		registerMarkupFilter(new HtmlHandler());
		registerMarkupFilter(new WicketRemoveTagHandler());
		registerMarkupFilter(new WicketLinkTagHandler());
		registerMarkupFilter(new WicketNamespaceHandler(markup));

		// Provided the wicket component requesting the markup is known ...
		final MarkupResourceStream resource = markup.getResource();
		if (resource != null)
		{
			final ContainerInfo containerInfo = resource.getContainerInfo();
			if (containerInfo != null)
			{
				registerMarkupFilter(new WicketMessageTagHandler());

				registerMarkupFilter(new BodyOnLoadHandler());

				// Pages require additional handlers
				if (Page.class.isAssignableFrom(containerInfo.getContainerClass()))
				{
					registerMarkupFilter(new HtmlHeaderSectionHandler(this.rootFragment));
				}

				registerMarkupFilter(new HeadForceTagIdHandler(containerInfo.getContainerClass()));
			}
		}
	}

	/**
	 * Append a new filter to the list of already pre-configured markup filters.
	 * To be used by subclasses which implement {@link #initFilterChain()}.
	 * 
	 * @param filter
	 *            The filter to be appended
	 * @deprecated since 2.0 please use registerMarkupFilter() instead
	 */
	public final void appendMarkupFilter(final IMarkupFilter filter)
	{
		filter.setParent(this.markupFilterChain);
		this.markupFilterChain = filter;
	}

	/**
	 * Append a new filter to the list of already pre-configured markup filters.
	 * To be used by subclasses which implement {@link #initFilterChain()}.
	 * 
	 * @param filter
	 *            The filter to be appended
	 */
	public final void registerMarkupFilter(final IMarkupFilter filter)
	{
		filter.setParent(this.markupFilterChain);
		this.markupFilterChain = filter;
	}

	/**
	 * Reads and parses markup from a file.
	 * 
	 * @param resource
	 *            The file
	 * @return The markup
	 * @throws IOException
	 * @throws ResourceStreamNotFoundException
	 */
	public final MarkupFragment readAndParse() throws IOException, ResourceStreamNotFoundException
	{
		// Initialize the xml parser
		this.xmlParser
				.parse(this.markup.getResource().getInputStream(), this.defaultMarkupEncoding);

		// parse the xml markup and tokenize it into wicket relevant markup
		// elements
		final MarkupFragment fragment = parseMarkup();

		this.markup.setEncoding(this.xmlParser.getEncoding());
		this.markup.setXmlDeclaration(this.xmlParser.getXmlDeclaration());

		return fragment;
	}

	/**
	 * Scans the given markup and extracts balancing tags.
	 * 
	 * @return The markup associated with the resource
	 */
	private MarkupFragment parseMarkup()
	{
		try
		{
			// allways remember the latest index (size)
			int size = rootFragment.size();

			// Loop through tags
			for (ComponentTag tag; null != (tag = (ComponentTag)this.markupFilterChain.nextTag());)
			{
				boolean add = (tag.getId() != null);
				if (!add && tag.getXmlTag().isClose())
				{
					add = ((tag.getOpenTag() != null) && (tag.getOpenTag().getId() != null));
				}

				// Add tag to list?
				if (add || tag.isModified())
				{
					final CharSequence text = this.xmlParser.getInputFromPositionMarker(tag
							.getPos());

					// Add text from last position to tag position
					if (text.length() > 0)
					{
						String rawMarkup = text.toString();

						if (this.stripComments)
						{
							rawMarkup = removeComment(rawMarkup);
						}

						if (this.compressWhitespace)
						{
							rawMarkup = compressWhitespace(rawMarkup);
						}

						// Make sure you add it at the correct location.
						// IMarkupFilters might have added elements as well.
						rootFragment.addMarkupElement(size, new RawMarkup(rawMarkup));
					}

					if (add)
					{
						// Add to list unless preview component tag remover
						// flagged
						// as removed
						if (!WicketRemoveTagHandler.IGNORE.equals(tag.getId()))
						{
							rootFragment.addMarkupElement(tag);
						}
					}
					else if (tag.isModified())
					{
						rootFragment.addMarkupElement(new RawMarkup(tag.toCharSequence()));
					}

					this.xmlParser.setPositionMarker();
				}

				// allways remember the latest index (size)
				size = rootFragment.size();
			}
		}
		catch (final ParseException ex)
		{
			// Add remaining input string
			final CharSequence text = this.xmlParser.getInputFromPositionMarker(-1);
			if (text.length() > 0)
			{
				rootFragment.addMarkupElement(new RawMarkup(text));
			}

			this.markup.setEncoding(this.xmlParser.getEncoding());
			this.markup.setXmlDeclaration(this.xmlParser.getXmlDeclaration());

			final MarkupStream markupStream = new MarkupStream(rootFragment);
			markupStream.setCurrentIndex(rootFragment.size() - 1);
			throw new MarkupException(markupStream, ex.getMessage(), ex);
		}

		// Add tail?
		final CharSequence text = this.xmlParser.getInputFromPositionMarker(-1);
		if (text.length() > 0)
		{
			String rawMarkup = text.toString();

			if (this.stripComments)
			{
				rawMarkup = removeComment(rawMarkup);
			}

			if (this.compressWhitespace)
			{
				rawMarkup = compressWhitespace(rawMarkup);
			}

			rootFragment.addMarkupElement(new RawMarkup(rawMarkup));
		}

		// Convert the list of MarkupElements into a tree like structure with
		// one MarkupFragment per Wicket Component.
		MarkupFragment fragment = rootFragment.translateFlatIntoTreeStructure();

		// Make all tags immutable and the list of elements unmodifable
		fragment.makeImmutable();

		return fragment;
	}

	/**
	 * Remove whitespaces from the raw markup
	 * 
	 * @param rawMarkup
	 * @return rawMarkup
	 */
	protected String compressWhitespace(String rawMarkup)
	{
		rawMarkup = rawMarkup.replaceAll("[ \\t]+", " ");
		rawMarkup = rawMarkup.replaceAll("( ?[\\r\\n] ?)+", "\n");
		return rawMarkup;
	}

	/**
	 * Remove all comment sections (&lt;!-- .. --&gt;) from the raw markup. For
	 * reasons I don't understand, the following regex
	 * <code>"<!--(.|\n|\r)*-->"<code>
	 * causes a stack overflow in some circumstances (jdk 1.5)
	 * <p>
	 * Conditional comments such as <ocde>&lt;!--[if IE]&gt;...&lt;![endif]&gt;<code>
	 * are NOT treated as comments. 
	 * 
	 * @param rawMarkup
	 * @return raw markup
	 */
	private String removeComment(String rawMarkup)
	{
		int pos1 = rawMarkup.indexOf("<!--");
		while (pos1 >= 0)
		{
			final int pos2 = rawMarkup.indexOf("-->", pos1 + 4);

			final AppendingStringBuffer buf = new AppendingStringBuffer(rawMarkup.length());
			if ((pos2 >= 0) && (pos1 > 0))
			{
				final String comment = rawMarkup.substring(pos1 + 4, pos2);
				if (CONDITIONAL_COMMENT.matcher(comment).matches() == false)
				{
					buf.append(rawMarkup.substring(0, pos1 - 1));
					buf.append(rawMarkup.substring(pos2 + 4));
					rawMarkup = buf.toString();
				}
			}
			pos1 = rawMarkup.indexOf("<!--", pos1 + 4);
		}
		return rawMarkup;
	}
}
