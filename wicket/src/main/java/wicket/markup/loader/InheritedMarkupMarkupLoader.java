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
import wicket.Component;
import wicket.MarkupContainer;
import wicket.Page;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupElement;
import wicket.markup.MarkupException;
import wicket.markup.MarkupFragment;
import wicket.markup.MarkupNotFoundException;
import wicket.markup.MarkupResourceStream;
import wicket.markup.MarkupStream;
import wicket.markup.parser.XmlTag;
import wicket.markup.parser.filter.HtmlHeaderSectionHandler;
import wicket.markup.parser.filter.WicketMessageTagHandler;
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

	/** onunload attribute; according to XHTML all attrs are lowercase */
	private static final String ONUNLOAD = "onunload";

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
		// we need to read the base markup as well.
		final MarkupFragment extendFragment = requiresBaseMarkup(markup);
		if (extendFragment != null)
		{
			final MarkupFragment baseMarkup = getBaseMarkup(container, markup);

			// Merge base and derived markup
			if (log.isDebugEnabled())
			{
				final String derivedResource = Strings.afterLast(markup.getMarkup().getResource()
						.toString(), '/');
				final String baseResource = Strings.afterLast(baseMarkup.getMarkup().getResource()
						.toString(), '/');
				log.debug("Merge markup: derived markup: " + derivedResource + "; base markup: "
						+ baseResource);
			}

			// Merge derived and base markup
			markup = merge(markup, baseMarkup, extendFragment);

			// make the new markup immutable
			markup.makeImmutable();

			if (log.isDebugEnabled())
			{
				log.debug("Merged markup: " + markup.toString());
			}
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
		// Get the super class to than retrieve the markup associated with it
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

		// Register an after-load listener for base markup. The listener
		// implementation will remove the derived markup from the cache as
		// reloading the base markup invalidates the derived markup as well
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
	 * the first wicket tag in the markup.
	 * 
	 * @param markup
	 * @return Null, if no wicket:extend was found
	 */
	private MarkupFragment requiresBaseMarkup(final MarkupFragment markup)
	{
		return MarkupFragmentUtils.getWicketExtendTag(markup);
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
	private MarkupFragment merge(final MarkupFragment derivedMarkup,
			final MarkupFragment baseMarkup, final MarkupFragment extendFragment)
	{
		// Creating the merged markup starts with makeing a modifiable copy of
		// the base markup
		MarkupFragment mergedMarkup = baseMarkup.makeCopy();

		// Search for wicket:child. Make sure it is the wicket:child of the base
		// markup and not the base-base markup
		{
			final MarkupFragment childFragment = MarkupFragmentUtils
					.getWicketChildTag(mergedMarkup);
			if (childFragment == null)
			{
				throw new MarkupException(new MarkupStream(baseMarkup),
						"Didn't find <wicket:child> tag in base markup. Markup: "
								+ baseMarkup.toUserDebugString());
			}

			// Convert the body tag into open-body-close if necessary
			if (childFragment.getTag().isOpenClose())
			{
				ComponentTag bodyTag = childFragment.getTag().mutable();
				bodyTag.setType(XmlTag.Type.OPEN);
				bodyTag.makeImmutable();

				ComponentTag bodyCloseTag = bodyTag.mutable();
				bodyCloseTag.setType(XmlTag.Type.CLOSE);

				childFragment.removeMarkupElement(0);
				childFragment.addMarkupElement(bodyTag);
				childFragment.addMarkupElement(bodyCloseTag);
			}
			else
			{
				// Remove preview area
				// TODO check that only raw markup is removed
				while (childFragment.size() > 2)
				{
					childFragment.visitChildren(MarkupFragment.class, new MarkupFragment.IVisitor()
					{
						public Object visit(MarkupElement element, MarkupFragment parent)
						{
							MarkupStream markupStream = new MarkupStream(baseMarkup);
							while (markupStream.hasMore())
							{
								if (markupStream.next() == childFragment)
								{
									break;
								}
							}
							
							throw new MarkupException(markupStream,
									"No wicket components are allowed in the preview area in between the wicket:child tag");
						}
					});
					childFragment.removeMarkupElement(1);
				}
			}

			// copy wicket:extend into the body
			childFragment.addMarkupElement(1, extendFragment.makeCopy());
		}

		{
			// Get the body fragment from the derived markup
			MarkupFragment body1 = MarkupFragmentUtils.getBodyTag(derivedMarkup);
			if (body1 != null)
			{
				// Get the body fragment from the merged markup
				MarkupFragment body2 = MarkupFragmentUtils.getBodyTag(mergedMarkup);

				// Append the onload and onunload attributes
				ComponentTag newBodyTag = body2.getTag().mutable();

				String onload = Strings.join(";", newBodyTag.getAttributes().getString(ONLOAD),
						body1.getTag().getAttributes().getString(ONLOAD));

				String onunload = Strings.join(";", newBodyTag.getAttributes().getString(ONUNLOAD),
						body1.getTag().getAttributes().getString(ONUNLOAD));

				if (Strings.isEmpty(onload) == false)
				{
					newBodyTag.put(ONLOAD, onload);
				}

				if (Strings.isEmpty(onunload) == false)
				{
					newBodyTag.put(ONUNLOAD, onunload);
				}

				body2.removeMarkupElement(0);
				body2.addMarkupElement(0, newBodyTag);
			}
		}

		{
			// Copy wicket:head from derived markup
			// First get all wicket:head tags from the derived markup
			final List<MarkupFragment> headers = MarkupFragmentUtils
					.getWicketHeaders(derivedMarkup);

			// Now add them to the merged markup
			if (headers != null)
			{
				// Find the proper location
				MarkupFragmentUtils.LookupResult result = null;

				// If merged markup contains <wicket:head> already, than append
				// them
				// to the end
				result = MarkupFragmentUtils.getWicketHeadTagPosition(mergedMarkup);

				// Else, if markup contains <head>, than insert before the close
				// tag
				if (result == null)
				{
					result = MarkupFragmentUtils.getHeadTagPosition(mergedMarkup);
				}

				// Else, if markup contains <body>, than insert before the body
				// tag
				if (result == null)
				{
					result = MarkupFragmentUtils.getBodyTagPosition(mergedMarkup);
				}

				// Else, insert right at the beginning
				if (result == null)
				{
					// remove "empty" root fragment
					if ((mergedMarkup.size() == 1)
							&& (mergedMarkup.get(0) instanceof MarkupFragment))
					{
						mergedMarkup = (MarkupFragment)mergedMarkup.get(0);
					}

					result = new MarkupFragmentUtils.LookupResult(mergedMarkup, 0);
				}

				// Copy the header(s)
				if (result != null)
				{
					for (MarkupFragment fragment : headers)
					{
						result.fragment.addMarkupElement(result.index++, fragment);
					}
				}
			}
		}

		// If the markup contains wicket:head but no <head> than automatically
		// insert it.
		if (Page.class.isAssignableFrom(mergedMarkup.getMarkup().getResource().getContainerInfo()
				.getContainerClass()))
		{
			// If no <head>, than ...
			if (MarkupFragmentUtils.getHeadTag(mergedMarkup) == null)
			{
				// Search for <wicket:head>
				MarkupFragmentUtils.LookupResult result = MarkupFragmentUtils
						.getWicketHeadTagPosition(mergedMarkup);
				if (result != null)
				{
					ComponentTag openTag = new ComponentTag(new XmlTag());
					openTag.setName("head");
					openTag.setId(HtmlHeaderSectionHandler.HEADER_ID);
					openTag.setType(XmlTag.Type.OPEN);
					openTag.makeImmutable();

					ComponentTag closeTag = openTag.mutable();
					closeTag.setType(XmlTag.Type.CLOSE);
					closeTag.setOpenTag(openTag);

					MarkupFragment headerFragment = new MarkupFragment(result.fragment.getMarkup());
					headerFragment.addMarkupElement(openTag);
					headerFragment.addMarkupElement(closeTag);

					result.fragment.addMarkupElement(1, headerFragment);

					// Apply a simplistic approach. If more control is required,
					// ask the user should add a <head> tag. The simplistic
					// approach assumes that all wicket:head tags are in
					// sequence.
					while (result.index < result.fragment.size())
					{
						MarkupElement elem = result.fragment.get(result.index);
						if (elem instanceof MarkupFragment)
						{
							ComponentTag tag = ((MarkupFragment)elem).getTag();
							if (tag.isWicketHeadTag())
							{
								headerFragment.addMarkupElement(headerFragment.size() - 1, elem);
								result.fragment.removeMarkupElement(result.index);
							}
							else
							{
								break;
							}
						}
						else
						{
							break;
						}
					}
				}
			}
		}

		// remove "empty" root fragment
		if ((mergedMarkup.size() == 1) && (mergedMarkup.get(0) instanceof MarkupFragment))
		{
			mergedMarkup = (MarkupFragment)mergedMarkup.get(0);
		}

		// This is only needed as long as the old Header Container code exists
		// TODO remove when no longer needed. See
		// WebMarkupContainerWithAssociatedMarkup#renderHeadFromAssociatedMarkupFile()
		mergedMarkup.visitChildren(ComponentTag.class, new MarkupFragment.IVisitor()
		{
			public Object visit(final MarkupElement element, final MarkupFragment parent)
			{
				ComponentTag tag = (ComponentTag)element;
				tag.setMarkupClass(parent.getMarkup().getResource().getMarkupClass());
				return CONTINUE_TRAVERSAL;
			}
		});
		
		// Make sure that wicket:head tags don't have doublicate ids.
		mergedMarkup.visitChildren(MarkupFragment.class, new MarkupFragment.IVisitor()
		{
			private int index = 0;
			
			public Object visit(final MarkupElement element, final MarkupFragment parent)
			{
				MarkupFragment fragment = (MarkupFragment) element;
				ComponentTag tag = fragment.getTag();
				if ((tag != null) && tag.isWicketHeadTag())
				{
					String id = Component.AUTO_COMPONENT_PREFIX + tag.getName() + index++;
					tag.setId(id);
				}
				else if ((tag != null) && tag.isMessageTag())
				{
					String id = Component.AUTO_COMPONENT_PREFIX + tag.getName() + index++;
					tag.setId(id);
				}
				else if ((tag != null) && tag.getId().startsWith(WicketMessageTagHandler.WICKET_MESSAGE_CONTAINER_ID))
				{
					String id = WicketMessageTagHandler.WICKET_MESSAGE_CONTAINER_ID + index++;
					tag.setId(id);
				}
				return CONTINUE_TRAVERSAL;
			}
		});

		return mergedMarkup;
	}
}
