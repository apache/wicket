/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.markup;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.Application;
import org.apache.wicket.markup.parser.IMarkupFilter;
import org.apache.wicket.markup.parser.IXmlPullParser;
import org.apache.wicket.markup.parser.XmlPullParser;
import org.apache.wicket.markup.parser.filter.RootMarkupFilter;
import org.apache.wicket.settings.def.MarkupSettings;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.resource.StringResourceStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a base MarkupParser specifically for (X)HTML. It makes use of a streaming XML parser to
 * read the markup and IMarkupFilters to remove comments, identify Wicket relevant tags, apply html
 * specific treatments etc.. Please see WicketMarkupParser for a parser preconfigured for Wicket.
 * <p>
 * The result will be an Markup object, which is basically a list, containing Wicket relevant tags
 * and RawMarkup.
 * 
 * @see IMarkupFilter
 * @see MarkupFactory
 * @see MarkupSettings
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
public abstract class AbstractMarkupParser
{
	/** Log for reporting. */
	private static final Logger log = LoggerFactory.getLogger(AbstractMarkupParser.class);

	/** Opening a conditional comment section, which is NOT treated as a comment section */
	public static final Pattern CONDITIONAL_COMMENT_OPENING = Pattern.compile("(s?)^[^>]*?<!--\\[if.*?\\]>(-->)?(<!.*?-->)?");

	private static final Pattern PRE_BLOCK = Pattern.compile("<pre>.*?</pre>", Pattern.DOTALL | Pattern.MULTILINE);

	/** The XML parser to use */
	private final IXmlPullParser xmlParser;

	/** The markup handler chain: each filter has a specific task */
	private IMarkupFilter markupFilterChain;

	/** The markup created by reading the markup file */
	private final Markup markup;

	/** Temporary variable: Application.get().getMarkupSettings() */
	private final MarkupSettings markupSettings;

	private final List<IMarkupFilter> filters;

	/**
	 * Constructor.
	 * 
	 * @param resource
	 *            The markup resource (file)
	 */
	public AbstractMarkupParser(final MarkupResourceStream resource)
	{
		this(new XmlPullParser(), resource);
	}

	/**
	 * Constructor. Usually for testing purposes only
	 * 
	 * @param markup
	 *            The markup resource.
	 */
	public AbstractMarkupParser(final String markup)
	{
		this(new XmlPullParser(), new MarkupResourceStream(new StringResourceStream(markup)));
	}

	/**
	 * Constructor.
	 * 
	 * @param xmlParser
	 *            The streaming xml parser to read and parse the markup
	 * @param resource
	 *            The markup resource (file)
	 */
	public AbstractMarkupParser(final IXmlPullParser xmlParser, final MarkupResourceStream resource)
	{
		this.xmlParser = xmlParser;
		markupSettings = Application.get().getMarkupSettings();

		markup = new Markup(resource);

		// The root of all filters is the xml parser
		markupFilterChain = new RootMarkupFilter(xmlParser, resource);

		// Initialize the markup filter chain
		filters = initializeMarkupFilters(markup);
	}

	/**
	 * @return Gets the list of markup filters
	 */
	public List<IMarkupFilter> getMarkupFilters()
	{
		return filters;
	}

	/**
	 * In case you want to analyze markup which BY DEFAULT does not use "wicket" to find relevant
	 * tags.
	 * 
	 * @param namespace
	 */
	public final void setWicketNamespace(final String namespace)
	{
		markup.getMarkupResourceStream().setWicketNamespace(namespace);
	}

	/**
	 * Applications which subclass initFilterChain() might also wish to access the markup resource
	 * stream.
	 * 
	 * @return The markup resource stream
	 */
	protected MarkupResourceStream getMarkupResourceStream()
	{
		return markup.getMarkupResourceStream();
	}

	/**
	 * Create a new markup filter chain and initialize with all default filters required.
	 * 
	 * @param markup
	 * @return The list of markup filters to be considered by the markup parser
	 */
	protected abstract List<IMarkupFilter> initializeMarkupFilters(final Markup markup);

	/**
	 * Reads and parses markup from a file.
	 * 
	 * @return The markup
	 * @throws IOException
	 * @throws ResourceStreamNotFoundException
	 */
	public final Markup parse() throws IOException, ResourceStreamNotFoundException
	{
		// The root of all markup filters is the xml parser
		markupFilterChain = new RootMarkupFilter(xmlParser, markup.getMarkupResourceStream());

		// Convert the list of markup filters into a chain
		for (IMarkupFilter filter : getMarkupFilters())
		{
			filter.setNextFilter(markupFilterChain);
			markupFilterChain = filter;
		}

		// Initialize the xml parser
		MarkupResourceStream markupResourceStream = markup.getMarkupResourceStream();
		xmlParser.parse(markupResourceStream.getResource().getInputStream(),
			markupSettings.getDefaultMarkupEncoding());

		// parse the xml markup and tokenize it into wicket relevant markup
		// elements
		parseMarkup();

		markupResourceStream.setEncoding(xmlParser.getEncoding());
		markupResourceStream.setDoctype(xmlParser.getDoctype());

		if (xmlParser.getEncoding() == null)
		{
			String a = "The markup file does not have a XML declaration prolog with 'encoding' attribute";
			String b = ". E.g. <?xml version=\"1.0\" encoding=\"UTF-8\" ?>";

			if (markupSettings.getThrowExceptionOnMissingXmlDeclaration())
			{
				throw new MarkupException(markupResourceStream.getResource(), a + b);
			}
			else
			{
				log.debug(a + ":" + markupResourceStream.getResource() + ". It is safer to use it" +
					b);
			}
		}

		return markup;
	}

	/**
	 * Get the next tag from the markup file
	 * 
	 * @return The next tag
	 * @throws ParseException
	 */
	private MarkupElement getNextTag() throws ParseException
	{
		return markupFilterChain.nextElement();
	}

	/**
	 * Scans the given markup and extracts balancing tags.
	 */
	private void parseMarkup()
	{
		try
		{
			// always remember the latest index (size)
			int size = markup.size();

			// Loop through tags
			MarkupElement elem;
			while (null != (elem = getNextTag()))
			{
				if (elem instanceof HtmlSpecialTag)
				{
					elem = new ComponentTag(((HtmlSpecialTag)elem).getXmlTag());
				}

				if (elem instanceof ComponentTag)
				{
					ComponentTag tag = (ComponentTag)elem;

					boolean add = (tag.getId() != null);
					if (!add && tag.isClose())
					{
						add = ((tag.getOpenTag() != null) && (tag.getOpenTag().getId() != null));
					}

					// Add tag to list?
					if (add || tag.isModified() || (markup.size() != size))
					{
						// Add text from last position to the current tag position
						CharSequence text = xmlParser.getInputFromPositionMarker(tag.getPos());
						if (text.length() > 0)
						{
							text = handleRawText(text.toString());

							// Make sure you add it at the correct location.
							// IMarkupFilters might have added elements as well.
							markup.addMarkupElement(size, new RawMarkup(text));
						}

						xmlParser.setPositionMarker();

						if (add)
						{
							// Add to the markup unless the tag has been flagged as
							// to be removed from the markup. (e.g. <wicket:remove>
							if (tag.isIgnore() == false)
							{
								markup.addMarkupElement(tag);
							}
						}
						else if (tag.isModified())
						{
							markup.addMarkupElement(new RawMarkup(tag.toCharSequence()));
						}
						else
						{
							xmlParser.setPositionMarker(tag.getPos());
						}
					}

					// always remember the latest index (size)
					size = markup.size();
				}
			}
		}
		catch (final ParseException ex)
		{
			// Add remaining input string
			final CharSequence text = xmlParser.getInputFromPositionMarker(-1);
			if (text.length() > 0)
			{
				markup.addMarkupElement(new RawMarkup(text));
			}

			markup.getMarkupResourceStream().setEncoding(xmlParser.getEncoding());
			markup.getMarkupResourceStream().setDoctype(xmlParser.getDoctype());

			final MarkupStream markupStream = new MarkupStream(markup);
			markupStream.setCurrentIndex(markup.size() - 1);
			throw new MarkupException(markupStream, ex.getMessage(), ex);
		}

		// Add tail?
		CharSequence text = xmlParser.getInputFromPositionMarker(-1);
		if (text.length() > 0)
		{
			text = handleRawText(text.toString());

			// Make sure you add it at the correct location.
			// IMarkupFilters might have added elements as well.
			markup.addMarkupElement(new RawMarkup(text));
		}

		postProcess(markup);

		// Make all tags immutable and the list of elements unmodifiable
		markup.makeImmutable();
	}

	/**
	 * 
	 * @param markup
	 */
	protected void postProcess(final Markup markup)
	{
		IMarkupFilter filter = markupFilterChain;
		while (filter != null)
		{
			filter.postProcess(markup);
			filter = filter.getNextFilter();
		}
	}

	/**
	 * 
	 * @param rawMarkup
	 * @return The modified raw markup
	 */
	protected CharSequence handleRawText(String rawMarkup)
	{
		// Get relevant settings from the Application
		final boolean stripComments = markupSettings.getStripComments();
		final boolean compressWhitespace = markupSettings.getCompressWhitespace();

		if (stripComments)
		{
			rawMarkup = removeComment(rawMarkup);
		}

		if (compressWhitespace)
		{
			rawMarkup = compressWhitespace(rawMarkup);
		}

		return rawMarkup;
	}

	/**
	 * Remove whitespace from the raw markup
	 * 
	 * @param rawMarkup
	 * @return rawMarkup
	 */
	protected String compressWhitespace(String rawMarkup)
	{
		// We don't want to compress whitespace inside <pre> tags, so we look
		// for matches and:
		// - Do whitespace compression on everything before the first match.
		// - Append the <pre>.*?</pre> match with no compression.
		// - Loop to find the next match.
		// - Append with compression everything between the two matches.
		// - Repeat until no match, then special-case the fragment after the
		// last <pre>.
		Matcher m = PRE_BLOCK.matcher(rawMarkup);
		int lastend = 0;
		StringBuilder sb = null;
		while (true)
		{
			boolean matched = m.find();
			String nonPre = matched ? rawMarkup.substring(lastend, m.start())
				: rawMarkup.substring(lastend);
			nonPre = nonPre.replaceAll("[ \\t]+", " ");
			nonPre = nonPre.replaceAll("( ?[\\r\\n] ?)+", "\n");

			// Don't create a StringBuilder if we don't actually need one.
			// This optimizes the trivial common case where there is no <pre>
			// tag at all down to just doing the replaceAlls above.
			if (lastend == 0)
			{
				if (matched)
				{
					sb = new StringBuilder(rawMarkup.length());
				}
				else
				{
					return nonPre;
				}
			}
			sb.append(nonPre);
			if (matched)
			{
				sb.append(m.group());
				lastend = m.end();
			}
			else
			{
				break;
			}
		}
		return sb.toString();
	}


	/**
	 * Remove all comment sections (&lt;!-- .. --&gt;) from the raw markup.
	 * 
	 * @param rawMarkup
	 * @return raw markup
	 */
	private static String removeComment(String rawMarkup)
	{
		int pos1 = rawMarkup.indexOf("<!--");
		while (pos1 != -1)
		{
			final StringBuilder buf = new StringBuilder(rawMarkup.length());
			final String possibleComment = rawMarkup.substring(pos1);
			Matcher matcher = CONDITIONAL_COMMENT_OPENING.matcher(possibleComment);
			if (matcher.find())
			{
				pos1 = pos1 + matcher.end();
			}
			else
			{
				int pos2 = rawMarkup.indexOf("-->", pos1 + 4);
				buf.append(rawMarkup.substring(0, pos1));
				if (rawMarkup.length() >= pos2 + 3)
				{
					buf.append(rawMarkup.substring(pos2 + 3));
				}
				rawMarkup = buf.toString();
			}
			pos1 = rawMarkup.indexOf("<!--", pos1);
		}
		return rawMarkup;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return markup.toString();
	}
}
