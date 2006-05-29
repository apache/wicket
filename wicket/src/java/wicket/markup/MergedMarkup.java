/*
 * $Id: MergedMarkup.java 5771 2006-05-19 12:04:06 +0000 (Fri, 19 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-19 12:04:06 +0000 (Fri, 19 May
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Page;
import wicket.WicketRuntimeException;
import wicket.markup.parser.XmlTag;
import wicket.markup.parser.filter.HtmlHeaderSectionHandler;
import wicket.util.string.Strings;

/**
 * A Markup class which represents merged markup, as it is required for markup
 * inheritance.
 * <p>
 * The Markups are merged at load time. Deep markup hierarchies are supported.
 * Multiple inheritance is not.
 * <p>
 * The markup resource file, which is associated with the markup, will be the
 * resource of the requested markup file. The base markup resources are not.
 * <p>
 * Base Markup must have a &lt;wicket:hild/&gt; tag which the position where the
 * derived markup is inserted. From the derived markup all tags in between
 * &lt;wicket:extend&gt; and &lt;/wicket:extend&gt; will be inserted.
 * <p>
 * In addition, all &lt;wicket:head> regions are copied as well as the body
 * onLoad attribute. This allows to develop completely self-contained plug &
 * play components including javascript etc.
 * 
 * @author Juergen Donnerstag
 */
public class MergedMarkup extends Markup
{
	private final static Log log = LogFactory.getLog(MergedMarkup.class);

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
		// Copy settings from derived markup
		setResource(markup.getResource());
		setXmlDeclaration(markup.getXmlDeclaration());
		setEncoding(markup.getEncoding());
		setWicketNamespace(markup.getWicketNamespace());

		if (log.isDebugEnabled())
		{
			String derivedResource = Strings.afterLast(markup.getResource().toString(), '/');
			String baseResource = Strings.afterLast(baseMarkup.getResource().toString(), '/');
			log.debug("Merge markup: derived markup: " + derivedResource + "; base markup: "
					+ baseResource);
		}

		// Merge derived and base markup
		merge(markup, baseMarkup, extendIndex);

		// Initialize internals based on new markup
		initialize();

		if (log.isDebugEnabled())
		{
			log.debug("Merge markup: " + toDebugString());
		}
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
				else if (tag.isMajorWicketComponentTag())
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
					String onLoad = tag.getAttributes().getString("onload");
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
		// True if either <wicket:head> or <head> has been processed
		boolean wicketHeadProcessed = false;

		// Add all elements from the base markup to the new list
		// until <wicket:child/> is found. Convert <wicket:child/>
		// into <wicket:child> and add it as well.
		WicketTag childTag = null;
		int baseIndex = 0;
		for (; baseIndex < baseMarkup.size(); baseIndex++)
		{
			MarkupElement element = baseMarkup.get(baseIndex);
			if (element instanceof RawMarkup)
			{
				// Add the element to the merged list
				addMarkupElement(element);
				continue;
			}

			final ComponentTag tag = (ComponentTag)element;

			// Make sure all tags of the base markup remember where they are
			// from
			if ((baseMarkup.getResource() != null) && (tag.getMarkupClass() == null))
			{
				tag.setMarkupClass(baseMarkup.getResource().getMarkupClass());
			}

			if (element instanceof WicketTag)
			{
				WicketTag wtag = (WicketTag)element;

				// Found wicket.child in base markup. In case of 3+ level
				// inheritance make sure the child tag is not from one of the
				// deeper levels
				if (wtag.isChildTag()
						&& (tag.getMarkupClass() == baseMarkup.getResource().getMarkupClass()))
				{
					if (wtag.isOpenClose())
					{
						// <wicket:child /> => <wicket:child>...</wicket:child>
						childTag = wtag;
						WicketTag childOpenTag = (WicketTag)wtag.mutable();
						childOpenTag.getXmlTag().setType(XmlTag.Type.OPEN);
						childOpenTag.setMarkupClass(baseMarkup.getResource().getMarkupClass());
						addMarkupElement(childOpenTag);
						break;
					}
					else if (wtag.isOpen())
					{
						// <wicket:child>
						addMarkupElement(wtag);
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
					if (wtag.isClose() && wtag.isHeadTag())
					{
						wicketHeadProcessed = true;

						// Add the current close tag
						addMarkupElement(wtag);

						// Add the <wicket:head> body from the derived markup.
						copyWicketHead(markup, extendIndex);

						// Do not add the current tag. It has already been
						// added.
						continue;
					}

					// if <wicket:panel> or ... in base markup
					if (wtag.isOpen() && wtag.isMajorWicketComponentTag())
					{
						wicketHeadProcessed = true;

						// Add the <wicket:head> body from the derived markup.
						copyWicketHead(markup, extendIndex);
					}
				}
			}

			// Process the head of the extended markup only once
			if (wicketHeadProcessed == false)
			{
				// if <head> in base markup
				if ((tag.isClose() && TagUtils.isHeadTag(tag))
						|| (tag.isOpen() && TagUtils.isBodyTag(tag)))
				{
					wicketHeadProcessed = true;

					// Add the <wicket:head> body from the derived markup.
					copyWicketHead(markup, extendIndex);
				}
			}

			// Make sure the body onLoad attribute from the extended markup is
			// copied to the new markup
			if (tag.isOpen() && TagUtils.isBodyTag(tag))
			{
				// Get the body onLoad attribute from derived markup
				final String onLoad = getBodyOnLoadString(markup);

				String onLoadBase = tag.getAttributes().getString("onload");
				if (onLoadBase == null)
				{
					if (onLoad != null)
					{
						ComponentTag mutableTag = tag.mutable();
						mutableTag.getAttributes().put("onload", onLoad);
						element = mutableTag;
					}
				}
				else if (onLoad != null)
				{
					onLoadBase += onLoad;
					ComponentTag mutableTag = tag.mutable();
					mutableTag.getAttributes().put("onload", onLoadBase);
					element = mutableTag;
				}
			}

			// Add the element to the merged list
			addMarkupElement(element);
		}

		if (baseIndex == baseMarkup.size())
		{
			throw new WicketRuntimeException("Expected to find <wicket:child/> in base markup: "
					+ baseMarkup.toString());
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
			throw new WicketRuntimeException(
					"Missing close tag </wicket:extend> in derived markup: " + markup.toString());
		}

		// If <wicket:child> than skip the body and find </wicket:child>
		if (((ComponentTag)baseMarkup.get(baseIndex)).isOpen())
		{
			for (baseIndex++; baseIndex < baseMarkup.size(); baseIndex++)
			{
				MarkupElement element = baseMarkup.get(baseIndex);
				if (element instanceof WicketTag)
				{
					WicketTag tag = (WicketTag)element;
					if (tag.isChildTag() && tag.isClose())
					{
						// Ok, skipped the childs content
						tag.setMarkupClass(baseMarkup.getResource().getMarkupClass());
						addMarkupElement(tag);
						break;
					}
					else
					{
						throw new WicketRuntimeException(
								"Wicket tags like <wicket:xxx> are not allowed in between <wicket:child> and </wicket:child> tags: "
										+ markup.toString());
					}
				}
				else if (element instanceof ComponentTag)
				{
					throw new WicketRuntimeException(
							"Wicket tags identified by wicket:id are not allowed in between <wicket:child> and </wicket:child> tags: "
									+ markup.toString());
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
			WicketTag childCloseTag = (WicketTag)childTag.mutable();
			childCloseTag.getXmlTag().setType(XmlTag.Type.CLOSE);
			childCloseTag.setMarkupClass(baseMarkup.getResource().getMarkupClass());
			addMarkupElement(childCloseTag);
		}

		for (baseIndex++; baseIndex < baseMarkup.size(); baseIndex++)
		{
			MarkupElement element = baseMarkup.get(baseIndex);
			addMarkupElement(element);

			// Make sure all tags of the base markup remember where they are
			// from
			if ((element instanceof ComponentTag) && (baseMarkup.getResource() != null))
			{
				ComponentTag tag = (ComponentTag)element;
				tag.setMarkupClass(baseMarkup.getResource().getMarkupClass());
			}
		}

		// Automatically add <head> if missing and required. On a Page
		// it must enclose ALL of the <wicket:head> tags.
		// Note: HtmlHeaderSectionHandler does something similar, but because
		// markup filters are not called for merged markup again, ...
		if (Page.class.isAssignableFrom(markup.getResource().getMarkupClass()))
		{
			// Find the position inside the markup for first <wicket:head>,
			// last </wicket:head> and <head>
			int hasOpenWicketHead = -1;
			int hasCloseWicketHead = -1;
			int hasHead = -1;
			for (int i = 0; i < size(); i++)
			{
				MarkupElement element = get(i);

				if ((hasOpenWicketHead == -1) && (element instanceof WicketTag)
						&& ((WicketTag)element).isHeadTag())
				{
					hasOpenWicketHead = i;
				}
				else if ((element instanceof WicketTag) && ((WicketTag)element).isHeadTag()
						&& ((WicketTag)element).isClose())
				{
					hasCloseWicketHead = i;
				}
				else if ((hasHead == -1) && (element instanceof ComponentTag)
						&& TagUtils.isHeadTag((ComponentTag)element))
				{
					hasHead = i;
				}
				else if ((hasHead != -1) && (hasOpenWicketHead != -1))
				{
					break;
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

				addMarkupElement(hasOpenWicketHead, openTag);
				addMarkupElement(hasCloseWicketHead + 2, closeTag);
			}
		}
	}

	/**
	 * Append the wicket:head regions from the extended markup to the current
	 * markup
	 * 
	 * @param markup
	 * @param extendIndex
	 */
	private void copyWicketHead(final Markup markup, int extendIndex)
	{
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
						copy = true;
					}
					else
					{
						addMarkupElement(elem);
						break;
					}
				}
			}

			if (copy == true)
			{
				addMarkupElement(elem);
			}
		}
	}
}
