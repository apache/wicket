/*
 * $Id$ $Revision:
 * 4913 $ $Date$
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

import wicket.WicketRuntimeException;
import wicket.markup.parser.XmlTag;

/**
 * This is a utility class which merges the base markup and the derived markup,
 * which is required for markup inheritance.
 * 
 * @author Juergen Donnerstag
 */
public class MergedMarkup extends Markup
{
	/**
	 * Merge inherited and base markup.
	 * 
	 * @param markup
	 *            The inherited markup
	 * @param baseMarkup
	 *            The base markup
	 * @param extendIndex
	 *            Index where <wicket:extend> has been found
	 */
	MergedMarkup(final Markup markup, final Markup baseMarkup, int extendIndex)
	{
		// Copy setting from derived markup
		setResource(markup.getResource());
		setXmlDeclaration(markup.getXmlDeclaration());
		setEncoding(markup.getEncoding());
		setWicketNamespace(markup.getWicketNamespace());

		// Merge derived and base markup
		merge(markup, baseMarkup, extendIndex);

		// Initialize internals based on new markup
		initialize();
	}

	/**
	 * Return the body onLoad attribute of the markup
	 * 
	 * @param markup
	 * @return onLoad attribute
	 */
	private String getBodyOnLoadString(final Markup markup)
	{
		int i = 0;

		// The markup must have a <wicket:head> region, else copying the
		// body onLoad attributes doesn't make sense
		for (; i < markup.size(); i++)
		{
			MarkupElement elem = markup.get(i);
			if (elem instanceof WicketTag)
			{
				WicketTag tag = (WicketTag)elem;
				if (tag.isClose() && tag.isHeadTag())
				{
					// Ok, we found <wicket:head>
					break;
				}
				else if (tag.isPanelTag() || tag.isBorderTag())
				{
					// Short cut: We found <wicket:panel> or <wicket:border>.
					// There certainly will be no <wicket:head> later on.
					return null;
				}
			}
			else if (elem instanceof ComponentTag)
			{
				ComponentTag tag = (ComponentTag)elem;
				if (TagUtils.isBodyTag(tag))
				{
					// Short cut: We found <body> but no <wicket:head>.
					// There certainly will be no <wicket:head> later on.
					return null;
				}
			}
		}

		// Found </wicket:head> => get body onLoad
		for (; i < markup.size(); i++)
		{
			MarkupElement elem = markup.get(i);
			if (elem instanceof ComponentTag)
			{
				ComponentTag tag = (ComponentTag)elem;
				if (tag.isOpen() && TagUtils.isBodyTag(tag))
				{
					String onLoad = tag.getAttributes().getString("onLoad");
					return onLoad;
				}
			}
		}

		return null;
	}

	/**
	 * Merge inherited and base markup.
	 * 
	 * @param markup
	 *            The inherited markup
	 * @param baseMarkup
	 *            The base markup
	 * @param extendIndex
	 *            Index where <wicket:extend> has been found
	 */
	private void merge(final Markup markup, final Markup baseMarkup, int extendIndex)
	{
		// Get the body onLoad attribute from derived markup
		final String onLoad = getBodyOnLoadString(markup);

		// True if either <wicket:head> or <head> has been processed
		boolean wicketHeadProcessed = false;
		boolean headProcessed = false;

		// Add all elements from the base markup to the new list
		// until <wicket:child/> is found. Convert <wicket:child/>
		// into <wicket:child> and add it as well.
		WicketTag childTag = null;
		int baseIndex = 0;
		for (; baseIndex < baseMarkup.size(); baseIndex++)
		{
			MarkupElement element = baseMarkup.get(baseIndex);

			// Make sure all tags of the base markup remember where they are from
			if ((element instanceof ComponentTag) && (baseMarkup.getResource() != null))
			{
				ComponentTag tag = (ComponentTag) element;
				tag.setMarkupClass(baseMarkup.getResource().getMarkupClass());
			}
			
			if (element instanceof WicketTag)
			{
				WicketTag wtag = (WicketTag)element;
				if (wtag.isChildTag() && wtag.isOpenClose())
				{
					childTag = wtag;
					WicketTag childOpenTag = (WicketTag)wtag.mutable();
					childOpenTag.getXmlTag().setType(XmlTag.OPEN);
					addMarkupElement(childOpenTag);
					break;
				}
			}

			// If element in base markup is </wicket:head>, scan the derived
			// markup for <wicket:head> and add all elements of that tag body
			// the new markup list.
			if ((wicketHeadProcessed == false) && (element instanceof WicketTag))
			{
				final WicketTag tag = (WicketTag)element;

				// If we reached <wicket:panel> and have not yet seen
				// <wicket:head>, than
				// automatically add <wicket:head> into the stream
				boolean hitPanel = tag.isOpen() && (tag.isPanelTag() || tag.isBorderTag());
				WicketTag openTag = null;
				if (hitPanel)
				{
					openTag = new WicketTag(new XmlTag());
					openTag.setName("head");
					openTag.setNamespace(tag.getNamespace());
					openTag.setType(XmlTag.OPEN);

					addMarkupElement(openTag);
				}

				boolean hitHead = tag.isClose() && tag.isHeadTag();

				if (hitHead || hitPanel)
				{
					// Before close the tag, add the <wicket:head> body from the
					// derived markup
					boolean copy = false;
					for (int i = 0; i < extendIndex; i++)
					{
						MarkupElement elem = markup.get(i);
						if (elem instanceof WicketTag)
						{
							WicketTag etag = (WicketTag)elem;
							if (etag.isHeadTag())
							{
								if (etag.isOpen())
								{
									wicketHeadProcessed = true;
									copy = true;
								}
								else
								{
									copy = false;
									break;
								}

								continue;
							}
						}

						if (copy == true)
						{
							addMarkupElement(elem);
						}
					}
				}

				// If we reached <wicket:panel> and have not yet seen
				// <wicket:head>, than
				// automatically add <wicket:head> into the stream
				if (hitPanel)
				{
					WicketTag closeTag = new WicketTag(new XmlTag());
					closeTag.setName("head");
					closeTag.setNamespace(tag.getNamespace());
					closeTag.setType(XmlTag.CLOSE);
					closeTag.setOpenTag(openTag);

					addMarkupElement(closeTag);
				}
			}

			// If element in base markup is </head>, scan the derived
			// markup for <wicket:head> and add all elements of that tag body
			// the new markup list.
			if ((headProcessed == false) && (element instanceof ComponentTag))
			{
				final ComponentTag tag = (ComponentTag)element;

				if (tag.isClose() && TagUtils.isHeadTag(tag))
				{
					// Before close the tag, add the <wicket:head> body from the
					// derived markup
					boolean copy = false;
					for (int i = 0; i < extendIndex; i++)
					{
						MarkupElement elem = markup.get(i);
						if (elem instanceof WicketTag)
						{
							WicketTag etag = (WicketTag)elem;
							if (etag.isHeadTag())
							{
								if (etag.isOpen())
								{
									headProcessed = true;
									copy = true;
								}
								else
								{
									copy = false;
									break;
								}

								continue;
							}
						}

						if (copy == true)
						{
							addMarkupElement(elem);
						}
					}
				}
			}

			// Make sure the body onLoad attribute from the extended markup is
			// copied
			// to the new markup
			if (element instanceof ComponentTag)
			{
				ComponentTag tag = (ComponentTag)element;
				if (tag.isOpen() && TagUtils.isBodyTag(tag))
				{
					String onLoadBase = tag.getAttributes().getString("onLoad");
					if (onLoadBase == null)
					{
						if (onLoad != null)
						{
							tag = tag.mutable();
							tag.getAttributes().put("onLoad", onLoad);
							element = tag;
						}
					}
					else if (onLoad != null)
					{
						onLoadBase += onLoad;
						tag = tag.mutable();
						tag.getAttributes().put("onLoad", onLoadBase);
						element = tag;
					}
				}
			}

			// Add the element to the merged list
			addMarkupElement(element);
		}

		if (baseIndex == baseMarkup.size())
		{
			throw new WicketRuntimeException("Expected to find <wicket:child/> in base markup");
		}

		// Now append all elements from the derived markup starting with
		// <wicket:extend> until </wicket:extend> to the list
		for (; extendIndex < markup.size(); extendIndex++)
		{
			MarkupElement element = markup.get(extendIndex);
			addMarkupElement(element);

			if (element instanceof WicketTag)
			{
				WicketTag wtag = (WicketTag)element;
				if (wtag.isExtendTag() && wtag.isClose())
				{
					break;
				}
			}
		}

		if (extendIndex == markup.size())
		{
			throw new WicketRuntimeException("Missing close tag </wicket:extend> in derived markup");
		}

		// And now all remaining elements from the derived markup.
		// But first add </wicket:child>
		WicketTag childCloseTag = (WicketTag)childTag.mutable();
		childCloseTag.getXmlTag().setType(XmlTag.CLOSE);
		addMarkupElement(childCloseTag);

		for (baseIndex++; baseIndex < baseMarkup.size(); baseIndex++)
		{
			MarkupElement element = baseMarkup.get(baseIndex);
			addMarkupElement(element);

			// Make sure all tags of the base markup remember where they are from
			if ((element instanceof ComponentTag) && (baseMarkup.getResource() != null))
			{
				ComponentTag tag = (ComponentTag) element;
				tag.setMarkupClass(baseMarkup.getResource().getMarkupClass());
			}
		}
	}
}
