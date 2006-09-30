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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.MarkupContainer;
import wicket.Page;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupElement;
import wicket.markup.MarkupFragment;
import wicket.markup.MarkupNotFoundException;
import wicket.markup.MarkupResourceStream;
import wicket.markup.MergedMarkup;
import wicket.markup.RawMarkup;
import wicket.markup.parser.XmlTag;
import wicket.markup.parser.filter.HtmlHeaderSectionHandler;
import wicket.util.listener.IChangeListener;
import wicket.util.resource.ResourceStreamNotFoundException;
import wicket.util.string.Strings;

/**
 * Merge the 2+ markups involved in markup inheritance. From a users perspective
 * there is only one markup associated with the component, the merged one.
 * 
 * @author Juergen Donnerstag
 */
public class InheritedMarkupMarkupLoader extends AbstractMarkupLoader
{
	/** Log for reporting. */
	private static final Log log = LogFactory.getLog(InheritedMarkupMarkupLoader.class);

	/** onload attribute; according to XHTML all attrs are lowercase */
	private static final String ONLOAD = "onload";

	/** The Wicket application */
	private final Application application;

	/**
	 * Constructor.
	 * 
	 * @param application
	 * @param cache
	 */
	public InheritedMarkupMarkupLoader(final Application application)
	{
		this.application = application;
	}

	/**
	 * @see wicket.markup.loader.IMarkupLoader#loadMarkup(wicket.MarkupContainer,
	 *      wicket.markup.MarkupResourceStream)
	 */
	@Override
	public final MarkupFragment loadMarkup(final MarkupContainer container,
			final MarkupResourceStream markupResourceStream) throws IOException,
			ResourceStreamNotFoundException
	{
		// read and parse the markup
		MarkupFragment markup = super.loadMarkup(container, markupResourceStream);

		// Check if markup contains <wicket:extend> which tells us that
		// we need to read the inherited markup as well.
		final MarkupFragment extendFragment = requiresBaseMarkup(markup);
		if (extendFragment != null)
		{
			final MarkupFragment baseMarkup = getBaseMarkup(container, markup);

			// Merge base and derived markup
			markup = mergedMarkup(markup, baseMarkup, extendFragment);
		}
		return markup;
	}

	/**
	 * Load the base markup
	 * 
	 * @param container
	 *            The original requesting markup container
	 * @param markup
	 *            The markup to checked for inheritance
	 * @return A markup object with the the base markup elements resolved.
	 */
	@SuppressWarnings("unchecked")
	private MarkupFragment getBaseMarkup(final MarkupContainer container,
			final MarkupFragment markup)
	{
		// get the base markup
		final Class<? extends MarkupContainer> markupClass = (Class<? extends MarkupContainer>)markup
				.getMarkup().getResource().getMarkupClass().getSuperclass();

		final MarkupFragment baseMarkup = this.application.getMarkupCache().getMarkup(container,
				markupClass);
		if (baseMarkup == MarkupFragment.NO_MARKUP_FRAGMENT)
		{
			throw new MarkupNotFoundException(
					"Base markup of inherited markup not found. Component class: "
							+ markup.getMarkup().getResource().getContainerInfo()
									.getContainerClass().getName()
							+ " Enable debug messages for wicket.util.resource.Resource to get a list of all filenames tried.");
		}

		// register an after-load listener for base markup. The listener
		// implementation will remove the derived markup which must be merged
		// with the base markup
		this.application.getMarkupCache().addAfterLoadListener(
				baseMarkup.getMarkup().getResource(), new IChangeListener()
				{
					public void onChange()
					{
						if (log.isDebugEnabled())
						{
							log.debug("Remove derived markup from cache: "
									+ markup.getMarkup().getResource());
						}
						application.getMarkupCache().removeMarkup(markup.getMarkup().getResource());
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
						return markup.getMarkup().getResource().getCacheKey().hashCode();
					}
				});

		return baseMarkup;
	}

	/**
	 * Check if markup contains &lt;wicket:extend&gt; which tells us that we
	 * need to read the inherited markup as well. &lt;wicket:extend&gt; MUST BE
	 * the first wicket tag in the markup. Skip raw markup
	 * 
	 * @param markup
	 * @return == 0, if no wicket:extend was found
	 */
	private MarkupFragment requiresBaseMarkup(final MarkupFragment markup)
	{
		if (markup.get(0) instanceof ComponentTag)
		{
			ComponentTag tag = markup.getTag(0);
			if (tag.isExtendTag())
			{
				return markup;
			}
		}
		
		return (MarkupFragment)markup.visitChildren(MarkupFragment.class,
				new MarkupFragment.IVisitor()
				{
					public Object visit(final MarkupElement element)
					{
						MarkupFragment fragment = (MarkupFragment)element;
						if (fragment.get(0) instanceof ComponentTag)
						{
							ComponentTag tag = fragment.getTag(0);
							if (tag.isExtendTag())
							{
								return fragment;
							}
						}
						return MarkupFragment.IVisitor.CONTINUE_TRAVERSAL;
					}
				});
	}

	/**
	 * Merge the derived and the base markup
	 * 
	 * @param derivedMarkup
	 *            The derived markup
	 * @param baseMarkup
	 *            The base markup
	 * @param extendFragment
	 *            The <wicket:extend> markup fragment markup
	 * @return A new instance of Markup with the markups merged
	 */
	private MarkupFragment mergedMarkup(final MarkupFragment derivedMarkup,
			final MarkupFragment baseMarkup, final MarkupFragment extendFragment)
	{
		if (log.isDebugEnabled())
		{
			final String derivedResource = Strings.afterLast(derivedMarkup.getMarkup()
					.getResource().toString(), '/');
			final String baseResource = Strings.afterLast(baseMarkup.getMarkup().getResource()
					.toString(), '/');
			log.debug("Merge markup: derived markup: " + derivedResource + "; base markup: "
					+ baseResource);
		}

		// Merge derived and base markup
		MarkupFragment mergedMarkup = merge(derivedMarkup, baseMarkup, extendFragment);
		
		MergedMarkup markup = new MergedMarkup(derivedMarkup.getMarkup(), baseMarkup.getMarkup());
		for (MarkupElement element : mergedMarkup)
		{
			markup.getMarkupFragments().addMarkupElement(element);
		}
		markup.makeImmutable();
		
		mergedMarkup = markup.getMarkupFragments();
		
		if (log.isDebugEnabled())
		{
			log.debug("Merged markup: " + mergedMarkup.toString());
		}

		return mergedMarkup;
	}

	/**
	 * Merge inherited and base markup.
	 * 
	 * @param derivedMarkup
	 *            The derived markup
	 * @param baseMarkup
	 *            The base markup
	 * @param extendFragment
	 *            The <wicket:extend> markup fragment
	 * @return The merged markup
	 */
	// TODO modify to better fit markup fragments
	private MarkupFragment merge(final MarkupFragment derivedMarkup,
			final MarkupFragment baseMarkup, final MarkupFragment extendFragment)
	{
		MarkupFragment mergedMarkup = new MarkupFragment(derivedMarkup.getMarkup());

		// True if either <wicket:head> or <head> has been processed
		boolean wicketHeadProcessed = false;

		// Add all elements from the base markup to the new list
		// until <wicket:child/> is found. Convert <wicket:child/>
		// into <wicket:child> and add it as well.
		ComponentTag childTag = null;
		int baseIndex = 0;
		List<MarkupElement> baseMarkupList = baseMarkup.getAllElementsFlat();
		for (; baseIndex < baseMarkupList.size(); baseIndex++)
		{
			MarkupElement element = baseMarkupList.get(baseIndex);
			if (element instanceof RawMarkup)
			{
				// Add the element to the merged list
				mergedMarkup.addMarkupElement(element);
				continue;
			}

			final ComponentTag tag = (ComponentTag)element;

			// Make sure all tags of the base markup remember where they are
			// from
			if ((baseMarkup.getMarkup().getResource() != null) && (tag.getMarkupClass() == null))
			{
				tag.setMarkupClass(baseMarkup.getMarkup().getResource().getMarkupClass());
			}

			if (tag.isWicketTag())
			{
				// Found wicket.child in base markup. In case of 3+ level
				// inheritance make sure the child tag is not from one of the
				// deeper levels
				if (tag.isChildTag()
						&& (tag.getMarkupClass() == baseMarkup.getMarkup().getResource()
								.getMarkupClass()))
				{
					if (tag.isOpenClose())
					{
						// <wicket:child /> => <wicket:child>...</wicket:child>
						childTag = tag;
						final ComponentTag childOpenTag = tag.mutable();
						childOpenTag.setType(XmlTag.Type.OPEN);
						childOpenTag.setMarkupClass(baseMarkup.getMarkup().getResource()
								.getMarkupClass());
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
						copyWicketHead(mergedMarkup, derivedMarkup);

						// Do not add the current tag. It has already been
						// added.
						continue;
					}

					// if <wicket:panel> or ... in base markup
					if (tag.isOpen() && tag.isMajorWicketComponentTag())
					{
						wicketHeadProcessed = true;

						// Add the <wicket:head> body from the derived markup.
						copyWicketHead(mergedMarkup, derivedMarkup);
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
					copyWicketHead(mergedMarkup, derivedMarkup);
				}
			}

			// Make sure the body onLoad attribute from the extended markup is
			// copied to the new markup
			if (tag.isOpen() && tag.isBodyTag())
			{
				// Get the body onLoad attribute from derived markup
				final String onLoad = getBodyOnLoadString(derivedMarkup);

				String onLoadBase = tag.getAttributes().getString(ONLOAD);
				if (onLoadBase == null)
				{
					if (onLoad != null)
					{
						final ComponentTag mutableTag = tag.mutable();
						mutableTag.getAttributes().put(ONLOAD, onLoad);
						element = mutableTag;
					}
				}
				else if (onLoad != null)
				{
					onLoadBase += onLoad;
					final ComponentTag mutableTag = tag.mutable();
					mutableTag.getAttributes().put(ONLOAD, onLoadBase);
					element = mutableTag;
				}
			}

			// Add the element to the merged list
			mergedMarkup.addMarkupElement(element);
		}

		if (baseIndex == baseMarkupList.size())
		{
			throw new WicketRuntimeException("Expected to find <wicket:child/> in base markup: "
					+ baseMarkup.toString());
		}

		// Now append all elements from the derived markup starting with
		// <wicket:extend> until </wicket:extend> to the list
		for (MarkupElement elem : extendFragment.getAllElementsFlat())
		{
			mergedMarkup.addMarkupElement(elem);
		}

		// If <wicket:child> than skip the body and find </wicket:child>
		if (((ComponentTag)baseMarkupList.get(baseIndex)).isOpen())
		{
			for (baseIndex++; baseIndex < baseMarkupList.size(); baseIndex++)
			{
				final MarkupElement element = baseMarkupList.get(baseIndex);
				if (element instanceof ComponentTag)
				{
					final ComponentTag tag = (ComponentTag)element;
					if (tag.isChildTag() && tag.isClose())
					{
						// Ok, skipped the childs content
						tag.setMarkupClass(baseMarkup.getMarkup().getResource().getMarkupClass());
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
			if (baseIndex == baseMarkupList.size())
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
			childCloseTag.setMarkupClass(baseMarkup.getMarkup().getResource().getMarkupClass());
			childCloseTag.setWicketTag(true);
			mergedMarkup.addMarkupElement(childCloseTag);
		}

		for (baseIndex++; baseIndex < baseMarkupList.size(); baseIndex++)
		{
			final MarkupElement element = baseMarkupList.get(baseIndex);
			mergedMarkup.addMarkupElement(element);

			// Make sure all tags of the base markup remember where they are
			// from
			if ((element instanceof ComponentTag) && (baseMarkup.getMarkup().getResource() != null))
			{
				final ComponentTag tag = (ComponentTag)element;
				tag.setMarkupClass(baseMarkup.getMarkup().getResource().getMarkupClass());
			}
		}

		// Automatically add <head> if missing and required. On a Page
		// it must enclose ALL of the <wicket:head> tags.
		// Note: HtmlHeaderSectionHandler does something similar, but because
		// markup filters are not called for merged markup again, ...
		if (Page.class.isAssignableFrom(derivedMarkup.getMarkup().getResource().getMarkupClass()))
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

		return mergedMarkup;
	}

	/**
	 * Return the body onLoad attribute of the markup
	 * 
	 * @param markup
	 * @return onLoad attribute
	 */
	private String getBodyOnLoadString(final MarkupFragment markup)
	{
		String onLoad = (String)markup.visitChildren(ComponentTag.class,
				new MarkupFragment.IVisitor()
				{
					boolean foundHead = false;

					public Object visit(final MarkupElement element)
					{
						ComponentTag tag = (ComponentTag)element;
						if ((foundHead == true) && tag.isOpen() && tag.isBodyTag())
						{
							final String onLoad = tag.getAttributes().getString(ONLOAD);
							return onLoad;
						}
						else if (tag.isClose() && tag.isWicketHeadTag())
						{
							// Ok, we found <wicket:head>
							foundHead = true;
						}
						else if (tag.isMajorWicketComponentTag())
						{
							// Short cut: We found <wicket:panel> or
							// <wicket:border>.
							// There certainly will be no <wicket:head> later
							// on.
							return null;
						}
						else if (tag.isBodyTag())
						{
							// Short cut: We found <body> but no <wicket:head>.
							// There certainly will be no <wicket:head> later
							// on.
							return null;
						}
						return MarkupFragment.IVisitor.CONTINUE_TRAVERSAL;
					}
				});

		return onLoad;
	}

	/**
	 * Append the wicket:head regions from the extended markup to the current
	 * markup
	 * 
	 * @param mergedMarkup
	 *            The destination Markup object
	 * @param markup
	 *            The source Markup object
	 */
	private void copyWicketHead(final MarkupFragment mergedMarkup, final MarkupFragment markup)
	{
		markup.visitChildren(MarkupFragment.class, new MarkupFragment.IVisitor()
		{
			public Object visit(final MarkupElement element)
			{
				MarkupFragment fragment = (MarkupFragment)element;
				if (fragment.get(0) instanceof ComponentTag)
				{
					ComponentTag tag = fragment.getTag(0);
					if (tag.isWicketHeadTag())
					{
						for (MarkupElement elem : fragment.getAllElementsFlat())
						{
							mergedMarkup.addMarkupElement(elem);
						}
					}
				}
				return MarkupFragment.IVisitor.CONTINUE_TRAVERSAL;
			}
		});
	}
}
