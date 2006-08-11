/*
 * $Id: MarkupCache.java 4639 2006-02-26 01:44:07 -0800 (Sun, 26 Feb 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-02-26 01:44:07 -0800 (Sun, 26 Feb
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
package wicket.markup.loader;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.MarkupContainer;
import wicket.Page;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.IMarkup;
import wicket.markup.Markup;
import wicket.markup.MarkupCache;
import wicket.markup.MarkupElement;
import wicket.markup.MarkupNotFoundException;
import wicket.markup.MarkupResourceStream;
import wicket.markup.MarkupStream;
import wicket.markup.MergedMarkup;
import wicket.markup.RawMarkup;
import wicket.markup.parser.XmlTag;
import wicket.markup.parser.filter.HtmlHeaderSectionHandler;
import wicket.util.listener.IChangeListener;
import wicket.util.resource.ResourceStreamNotFoundException;
import wicket.util.string.Strings;

/**
 * Load markup and cache it for fast retrieval. If markup file changes, it'll be
 * removed and subsequently reloaded when needed.
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
public class InheritedMarkupMarkupLoader extends AbstractMarkupLoader
{
	/** Log for reporting. */
	private static final Log log = LogFactory.getLog(InheritedMarkupMarkupLoader.class);

	/** The Wicket application */
	private final Application application;

	/** Because of Markup inheritance we must be able to load new Markup */
	private final MarkupCache markupCache;

	/**
	 * Constructor.
	 * 
	 * @param application
	 * @param cache
	 */
	public InheritedMarkupMarkupLoader(final Application application, final MarkupCache cache)
	{
		this.application = application;
		this.markupCache = cache;
	}

	/**
	 * @see wicket.markup.loader.IMarkupLoader#loadMarkup(wicket.MarkupContainer,
	 *      wicket.markup.MarkupResourceStream)
	 */
	public final IMarkup loadMarkup(final MarkupContainer container,
			final MarkupResourceStream markupResourceStream) throws IOException,
			ResourceStreamNotFoundException
	{
		// read and parse the markup
		IMarkup markup = super.loadMarkup(container, markupResourceStream);

		// Check for markup inheritance. If it contains <wicket:extend>
		// the two markups get merged.
		markup = checkForMarkupInheritance(container, markup);

		return markup;
	}

	/**
	 * The markup has just been loaded and now we check if markup inheritance
	 * applies, which is if <wicket:extend> is found in the markup. If yes, than
	 * load the base markups and merge the markup elements to create an updated
	 * (merged) list of markup elements.
	 * 
	 * @param container
	 *            The original requesting markup container
	 * @param markup
	 *            The markup to checked for inheritance
	 * @return A markup object with the the base markup elements resolved.
	 */
	@SuppressWarnings("unchecked")
	private IMarkup checkForMarkupInheritance(final MarkupContainer container, final IMarkup markup)
	{
		// Check if markup contains <wicket:extend> which tells us that
		// we need to read the inherited markup as well.
		final int extendIndex = requiresBaseMarkup(markup);
		if (extendIndex == -1)
		{
			// return a MarkupStream for the markup
			return markup;
		}

		final Class<? extends MarkupContainer> markupClass = (Class<? extends MarkupContainer>)markup
				.getResource().getMarkupClass().getSuperclass();
		// get the base markup
		final IMarkup baseMarkup = this.markupCache.getMarkup(container, markupClass);

		if (baseMarkup == IMarkup.NO_MARKUP)
		{
			throw new MarkupNotFoundException(
					"Base markup of inherited markup not found. Component class: "
							+ markup.getResource().getContainerInfo().getContainerClass().getName()
							+ " Enable debug messages for wicket.util.resource.Resource to get a list of all filenames tried.");
		}

		// register an after-load listener for base markup. The listener
		// implementation will remove the derived markup which must be merged
		// with the base markup
		this.markupCache.addAfterLoadListener(baseMarkup.getResource(), new IChangeListener()
		{
			public void onChange()
			{
				if (log.isDebugEnabled())
				{
					log.debug("Remove derived markup from cache: " + markup.getResource());
				}
				InheritedMarkupMarkupLoader.this.markupCache.removeMarkup(markup.getResource());
			}

			/**
			 * Make sure there is only one listener per derived markup
			 * 
			 * @see java.lang.Object#equals(java.lang.Object)
			 */
			@Override
			public boolean equals(final Object obj)
			{
				return true;
			}

			/**
			 * Make sure there is only one listener per derived markup
			 * 
			 * @see java.lang.Object#hashCode()
			 */
			@Override
			public int hashCode()
			{
				return markup.getResource().getCacheKey().hashCode();
			}
		});

		// Merge base and derived markup
		final IMarkup mergedMarkup = mergedMarkup(markup, baseMarkup, extendIndex);
		return mergedMarkup;
	}

	/**
	 * Check if markup contains &lt;wicket:extend&gt; which tells us that we
	 * need to read the inherited markup as well. &lt;wicket:extend&gt; MUST BE
	 * the first wicket tag in the markup. Skip raw markup
	 * 
	 * @param markup
	 * @return == 0, if no wicket:extend was found
	 */
	private int requiresBaseMarkup(final IMarkup markup)
	{
		for (int i = 0; i < markup.size(); i++)
		{
			final MarkupElement elem = markup.get(i);
			if ((elem instanceof ComponentTag) && ((ComponentTag)elem).isExtendTag())
			{
				// Ok, inheritance is on and we must get the
				// inherited markup as well.
				return i;
			}
		}
		return -1;
	}

	/**
	 * Merge the derived and the base markup
	 * 
	 * @param derivedMarkup
	 *            The derived markup
	 * @param baseMarkup
	 *            The base markup
	 * @param extendIndex
	 *            The index where <wicket:extend> was found in the derived
	 *            markup
	 * @return A new instance of Markup with the markups merged
	 */
	private IMarkup mergedMarkup(final IMarkup derivedMarkup, final IMarkup baseMarkup,
			final int extendIndex)
	{
		MergedMarkup markup = new MergedMarkup(derivedMarkup, baseMarkup);

		if (log.isDebugEnabled())
		{
			final String derivedResource = Strings.afterLast(markup.getResource().toString(), '/');
			final String baseResource = Strings.afterLast(baseMarkup.getResource().toString(), '/');
			log.debug("Merge markup: derived markup: " + derivedResource + "; base markup: "
					+ baseResource);
		}

		// Merge derived and base markup
		merge(markup, derivedMarkup, baseMarkup, extendIndex);

		if (log.isDebugEnabled())
		{
			log.debug("Merge markup: " + markup.toDebugString());
		}

		return markup;
	}

	/**
	 * Merge inherited and base markup.
	 * 
	 * @param mergedMarkup
	 *            The "empty" instance of MergedMarkup which now will be filled
	 *            with MarkupElements
	 * @param derivedMarkup
	 *            The derived markup
	 * @param baseMarkup
	 *            The base markup
	 * @param extendIndex
	 *            Index where <wicket:extend> has been found
	 */
	private void merge(final Markup mergedMarkup, final IMarkup derivedMarkup,
			final IMarkup baseMarkup, int extendIndex)
	{
		// True if either <wicket:head> or <head> has been processed
		boolean wicketHeadProcessed = false;

		// Add all elements from the base markup to the new list
		// until <wicket:child/> is found. Convert <wicket:child/>
		// into <wicket:child> and add it as well.
		ComponentTag childTag = null;
		int baseIndex = 0;
		for (; baseIndex < baseMarkup.size(); baseIndex++)
		{
			MarkupElement element = baseMarkup.get(baseIndex);
			if (element instanceof RawMarkup)
			{
				// Add the element to the merged list
				mergedMarkup.addMarkupElement(element);
				continue;
			}

			final ComponentTag tag = (ComponentTag)element;

			// Make sure all tags of the base markup remember where they are
			// from
			if ((baseMarkup.getResource() != null) && (tag.getMarkupClass() == null))
			{
				tag.setMarkupClass(baseMarkup.getResource().getMarkupClass());
			}

			if (tag.isWicketTag())
			{
				// Found wicket.child in base markup. In case of 3+ level
				// inheritance make sure the child tag is not from one of the
				// deeper levels
				if (tag.isChildTag()
						&& (tag.getMarkupClass() == baseMarkup.getResource().getMarkupClass()))
				{
					if (tag.isOpenClose())
					{
						// <wicket:child /> => <wicket:child>...</wicket:child>
						childTag = tag;
						final ComponentTag childOpenTag = tag.mutable();
						childOpenTag.setType(XmlTag.Type.OPEN);
						childOpenTag.setMarkupClass(baseMarkup.getResource().getMarkupClass());
						childOpenTag.setWicketTag(true);
						mergedMarkup.addMarkupElement(childOpenTag);
						break;
					}
					else if (tag.isOpen())
					{
						// <wicket:child>
						mergedMarkup.addMarkupElement(tag);
						break;
					}
					else
					{
						throw new WicketRuntimeException("Did not expect a </wicket:child> tag in "
								+ baseMarkup.toString());
					}
				}

				// Process the head of the extended markup only once
				if (wicketHeadProcessed == false)
				{
					// if </wicket:head> in base markup
					if (tag.isClose() && tag.isWicketHeadTag())
					{
						wicketHeadProcessed = true;

						// Add the current close tag
						mergedMarkup.addMarkupElement(tag);

						// Add the <wicket:head> body from the derived markup.
						copyWicketHead(mergedMarkup, derivedMarkup, extendIndex);

						// Do not add the current tag. It has already been
						// added.
						continue;
					}

					// if <wicket:panel> or ... in base markup
					if (tag.isOpen() && tag.isMajorWicketComponentTag())
					{
						wicketHeadProcessed = true;

						// Add the <wicket:head> body from the derived markup.
						copyWicketHead(mergedMarkup, derivedMarkup, extendIndex);
					}
				}
			}

			// Process the head of the extended markup only once
			if (wicketHeadProcessed == false)
			{
				// if <head> in base markup
				if ((tag.isClose() && tag.isHeadTag()) || (tag.isOpen() && tag.isBodyTag()))
				{
					wicketHeadProcessed = true;

					// Add the <wicket:head> body from the derived markup.
					copyWicketHead(mergedMarkup, derivedMarkup, extendIndex);
				}
			}

			// Make sure the body onLoad attribute from the extended markup is
			// copied to the new markup
			if (tag.isOpen() && tag.isBodyTag())
			{
				// Get the body onLoad attribute from derived markup
				final String onLoad = getBodyOnLoadString(derivedMarkup);

				String onLoadBase = tag.getAttributes().getString("onload");
				if (onLoadBase == null)
				{
					if (onLoad != null)
					{
						final ComponentTag mutableTag = tag.mutable();
						mutableTag.getAttributes().put("onload", onLoad);
						element = mutableTag;
					}
				}
				else if (onLoad != null)
				{
					onLoadBase += onLoad;
					final ComponentTag mutableTag = tag.mutable();
					mutableTag.getAttributes().put("onload", onLoadBase);
					element = mutableTag;
				}
			}

			// Add the element to the merged list
			mergedMarkup.addMarkupElement(element);
		}

		if (baseIndex == baseMarkup.size())
		{
			throw new WicketRuntimeException("Expected to find <wicket:child/> in base markup: "
					+ baseMarkup.toString());
		}

		// Now append all elements from the derived markup starting with
		// <wicket:extend> until </wicket:extend> to the list
		for (; extendIndex < derivedMarkup.size(); extendIndex++)
		{
			final MarkupElement element = derivedMarkup.get(extendIndex);
			mergedMarkup.addMarkupElement(element);

			if (element instanceof ComponentTag)
			{
				final ComponentTag tag = (ComponentTag)element;
				if (tag.isExtendTag() && tag.isClose())
				{
					break;
				}
			}
		}

		if (extendIndex == derivedMarkup.size())
		{
			throw new WicketRuntimeException(
					"Missing close tag </wicket:extend> in derived markup: "
							+ derivedMarkup.toString());
		}

		// If <wicket:child> than skip the body and find </wicket:child>
		if (((ComponentTag)baseMarkup.get(baseIndex)).isOpen())
		{
			for (baseIndex++; baseIndex < baseMarkup.size(); baseIndex++)
			{
				final MarkupElement element = baseMarkup.get(baseIndex);
				if (element instanceof ComponentTag)
				{
					final ComponentTag tag = (ComponentTag)element;
					if (tag.isChildTag() && tag.isClose())
					{
						// Ok, skipped the childs content
						tag.setMarkupClass(baseMarkup.getResource().getMarkupClass());
						mergedMarkup.addMarkupElement(tag);
						break;
					}
					else
					{
						throw new WicketRuntimeException(
								"Wicket tags like <wicket:xxx> are not allowed in between <wicket:child> and </wicket:child> tags: "
										+ derivedMarkup.toString());
					}
				}
				else if (element instanceof ComponentTag)
				{
					throw new WicketRuntimeException(
							"Wicket tags identified by wicket:id are not allowed in between <wicket:child> and </wicket:child> tags: "
									+ derivedMarkup.toString());
				}
			}

			// </wicket:child> not found
			if (baseIndex == baseMarkup.size())
			{
				throw new WicketRuntimeException(
						"Expected to find </wicket:child> in base markup: " + baseMarkup.toString());
			}
		}
		else if (childTag != null)
		{
			// And now all remaining elements from the derived markup.
			// But first add </wicket:child>
			final ComponentTag childCloseTag = childTag.mutable();
			childCloseTag.setType(XmlTag.Type.CLOSE);
			childCloseTag.setMarkupClass(baseMarkup.getResource().getMarkupClass());
			childCloseTag.setWicketTag(true);
			mergedMarkup.addMarkupElement(childCloseTag);
		}

		for (baseIndex++; baseIndex < baseMarkup.size(); baseIndex++)
		{
			final MarkupElement element = baseMarkup.get(baseIndex);
			mergedMarkup.addMarkupElement(element);

			// Make sure all tags of the base markup remember where they are
			// from
			if ((element instanceof ComponentTag) && (baseMarkup.getResource() != null))
			{
				final ComponentTag tag = (ComponentTag)element;
				tag.setMarkupClass(baseMarkup.getResource().getMarkupClass());
			}
		}

		// Automatically add <head> if missing and required. On a Page
		// it must enclose ALL of the <wicket:head> tags.
		// Note: HtmlHeaderSectionHandler does something similar, but because
		// markup filters are not called for merged markup again, ...
		if (Page.class.isAssignableFrom(derivedMarkup.getResource().getMarkupClass()))
		{
			// Find the position inside the markup for first <wicket:head>,
			// last </wicket:head> and <head>
			int hasOpenWicketHead = -1;
			int hasCloseWicketHead = -1;
			int hasHead = -1;
			for (int i = 0; i < mergedMarkup.size(); i++)
			{
				final MarkupElement element = mergedMarkup.get(i);
				if (element instanceof ComponentTag)
				{
					final ComponentTag tag = (ComponentTag)element;
					if ((hasOpenWicketHead == -1) && tag.isWicketHeadTag())
					{
						hasOpenWicketHead = i;
					}
					else if (tag.isWicketHeadTag() && tag.isClose())
					{
						hasCloseWicketHead = i;
					}
					else if ((hasHead == -1) && tag.isHeadTag())
					{
						hasHead = i;
					}

					if ((hasHead != -1) && (hasOpenWicketHead != -1))
					{
						break;
					}
				}
			}

			// If a <head> tag is missing, insert it automatically
			if ((hasOpenWicketHead != -1) && (hasHead == -1))
			{
				final XmlTag headOpenTag = new XmlTag();
				headOpenTag.setName("head");
				headOpenTag.setType(XmlTag.Type.OPEN);
				final ComponentTag openTag = new ComponentTag(headOpenTag);
				openTag.setId(HtmlHeaderSectionHandler.HEADER_ID);

				final XmlTag headCloseTag = new XmlTag();
				headCloseTag.setName(headOpenTag.getName());
				headCloseTag.setType(XmlTag.Type.CLOSE);
				final ComponentTag closeTag = new ComponentTag(headCloseTag);
				closeTag.setOpenTag(openTag);
				closeTag.setId(HtmlHeaderSectionHandler.HEADER_ID);

				mergedMarkup.addMarkupElement(hasOpenWicketHead, openTag);
				mergedMarkup.addMarkupElement(hasCloseWicketHead + 2, closeTag);
			}
		}
	}

	/**
	 * Return the body onLoad attribute of the markup
	 * 
	 * @param markup
	 * @return onLoad attribute
	 */
	private String getBodyOnLoadString(final IMarkup markup)
	{
		final MarkupStream markupStream = new MarkupStream(markup);

		// The markup must have a <wicket:head> region, else copying the
		// body onLoad attributes doesn't make sense
		while (markupStream.hasMoreComponentTags())
		{
			final ComponentTag tag = markupStream.getTag();
			if (tag.isClose() && tag.isWicketHeadTag())
			{
				// Ok, we found <wicket:head>
				break;
			}
			else if (tag.isMajorWicketComponentTag())
			{
				// Short cut: We found <wicket:panel> or <wicket:border>.
				// There certainly will be no <wicket:head> later on.
				return null;
			}
			else if (tag.isBodyTag())
			{
				// Short cut: We found <body> but no <wicket:head>.
				// There certainly will be no <wicket:head> later on.
				return null;
			}
		}

		// Found </wicket:head> => get body onLoad
		while (markupStream.hasMoreComponentTags())
		{
			final ComponentTag tag = markupStream.getTag();
			if (tag.isOpen() && tag.isBodyTag())
			{
				final String onLoad = tag.getAttributes().getString("onload");
				return onLoad;
			}
		}

		return null;
	}

	/**
	 * Append the wicket:head regions from the extended markup to the current
	 * markup
	 * 
	 * @param mergedMarkup The destination Markup object
	 * @param markup The source Markup object
	 * @param extendIndex startIndex
	 */
	private void copyWicketHead(final Markup mergedMarkup, final IMarkup markup, final int extendIndex)
	{
		boolean copy = false;
		for (int i = 0; i < extendIndex; i++)
		{
			final MarkupElement elem = markup.get(i);
			if (elem instanceof ComponentTag)
			{
				final ComponentTag etag = (ComponentTag)elem;
				if (etag.isWicketHeadTag())
				{
					if (etag.isOpen())
					{
						copy = true;
					}
					else
					{
						mergedMarkup.addMarkupElement(elem);
						break;
					}
				}
			}

			if (copy == true)
			{
				mergedMarkup.addMarkupElement(elem);
			}
		}
	}
}
