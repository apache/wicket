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

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.markup.parser.XmlTag.TagType;
import org.apache.wicket.markup.parser.filter.HtmlHeaderSectionHandler;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Markup class which represents merged markup, as it is required for markup inheritance.
 * <p>
 * The Markups are merged at load time. Deep markup hierarchies are supported. Multiple inheritance
 * is not.
 * <p>
 * The markup resource file, which is associated with the markup, will be the resource of the
 * requested markup file. The base markup resources are not.
 * <p>
 * Base Markup must have a &lt;wicket:child/&gt; tag at the position where the derived markup should
 * be inserted. From the derived markup all tags in between &lt;wicket:extend&gt; and
 * &lt;/wicket:extend&gt; will be inserted.
 * <p>
 * In addition, all &lt;wicket:head> regions are copied as well. This allows to develop completely
 * self-contained plug & play components including javascript etc.
 * 
 * @author Juergen Donnerstag
 */
public class MergedMarkup extends Markup
{
	private final static Logger log = LoggerFactory.getLogger(MergedMarkup.class);

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
	public MergedMarkup(final Markup markup, final Markup baseMarkup, int extendIndex)
	{
		super(markup.getMarkupResourceStream());

		getMarkupResourceStream().setBaseMarkup(baseMarkup);

		// Copy settings from derived markup
		MarkupResourceStream baseResourceStream = baseMarkup.getMarkupResourceStream();
		getMarkupResourceStream().setEncoding(baseResourceStream.getEncoding());
		getMarkupResourceStream().setWicketNamespace(baseResourceStream.getWicketNamespace());

		if (log.isDebugEnabled())
		{
			String derivedResource = Strings.afterLast(markup.getMarkupResourceStream()
				.getResource()
				.toString(), '/');
			String baseResource = Strings.afterLast(baseMarkup.getMarkupResourceStream()
				.getResource()
				.toString(), '/');
			log.debug("Merge markup: derived markup: " + derivedResource + "; base markup: " +
				baseResource);
		}

		// Merge derived and base markup
		merge(markup, baseMarkup, extendIndex);

		if (log.isDebugEnabled())
		{
			log.debug("Merge markup: " + toString());
		}
	}

	@Override
	public String locationAsString()
	{
		/*
		 * Uses both resource locations so that if the child does not have a style and the parent
		 * does, the location is unique to this combination (or vice versa) SEE WICKET-1507 (Jeremy
		 * Thomerson)
		 */
		String l1 = getMarkupResourceStream().getBaseMarkup().locationAsString();
		String l2 = getMarkupResourceStream().locationAsString();

		if ((l1 == null) && (l2 == null))
		{
			return null;
		}

		return l1 + ":" + l2;
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
	private void merge(final IMarkupFragment markup, final IMarkupFragment baseMarkup,
		int extendIndex)
	{
		// True if either <wicket:head> or <head> has been processed
		boolean wicketHeadProcessed = false;

		// True, if <head> was found
		boolean foundHeadTag = false;

		// Add all elements from the base markup to the new list
		// until <wicket:child/> is found. Convert <wicket:child/>
		// into <wicket:child> and add it as well.
		WicketTag childTag = null;
		int baseIndex = 0;
		MarkupResourceStream markupResourceStream = baseMarkup.getMarkupResourceStream();
		IResourceStream resource = markupResourceStream.getResource();
		Class<? extends Component> markupClass = markupResourceStream.getMarkupClass();

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
			if (resource != null && tag.getMarkupClass() == null)
			{
				tag.setMarkupClass(markupClass);
			}

			if (element instanceof WicketTag)
			{
				WicketTag wtag = (WicketTag)element;

				// Found org.apache.wicket.child in base markup. In case of 3+
				// level inheritance make sure the child tag is not from one of
				// the deeper levels
				if (wtag.isChildTag() && tag.getMarkupClass() == markupClass)
				{
					if (wtag.isOpenClose())
					{
						// <wicket:child /> => <wicket:child>...</wicket:child>
						childTag = wtag;
						WicketTag childOpenTag = (WicketTag)wtag.mutable();
						childOpenTag.getXmlTag().setType(TagType.OPEN);
						childOpenTag.setMarkupClass(markupClass);
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
						throw new WicketRuntimeException(
							"Did not expect a </wicket:child> tag in " + baseMarkup.toString());
					}
				}

				// Process the head of the extended markup only once
				if (wicketHeadProcessed == false)
				{
					// if </wicket:head> in base markup and no <head>
					if (wtag.isClose() && wtag.isHeadTag() && (foundHeadTag == false))
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
				// Remember that we found <head> in the base markup
				if (tag.isOpen() && TagUtils.isHeadTag(tag))
				{
					foundHeadTag = true;
				}

				// if <head> in base markup
				if ((tag.isClose() && TagUtils.isHeadTag(tag)) ||
					(tag.isOpen() && TagUtils.isBodyTag(tag)))
				{
					wicketHeadProcessed = true;

					// Add the <wicket:head> body from the derived markup.
					copyWicketHead(markup, extendIndex);
				}
			}

			// Add the element to the merged list
			addMarkupElement(element);
		}

		if (baseIndex == baseMarkup.size())
		{
			throw new WicketRuntimeException("Expected to find <wicket:child/> in base markup: " +
				baseMarkup.toString());
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
						tag.setMarkupClass(markupClass);
						addMarkupElement(tag);
						break;
					}
					else
					{
						throw new WicketRuntimeException(
							"Wicket tags like <wicket:xxx> are not allowed in between <wicket:child> and </wicket:child> tags: " +
								markup.toString());
					}
				}
				else if (element instanceof ComponentTag)
				{
					throw new WicketRuntimeException(
						"Wicket tags identified by wicket:id are not allowed in between <wicket:child> and </wicket:child> tags: " +
							markup.toString());
				}
			}

			// </wicket:child> not found
			if (baseIndex == baseMarkup.size())
			{
				throw new WicketRuntimeException(
					"Expected to find </wicket:child> in base markup: " + baseMarkup.toString());
			}
		}
		else
		{
			// And now all remaining elements from the derived markup.
			// But first add </wicket:child>
			WicketTag childCloseTag = (WicketTag)childTag.mutable();
			childCloseTag.getXmlTag().setType(TagType.CLOSE);
			childCloseTag.setMarkupClass(markupClass);
			addMarkupElement(childCloseTag);
		}

		for (baseIndex++; baseIndex < baseMarkup.size(); baseIndex++)
		{
			MarkupElement element = baseMarkup.get(baseIndex);
			addMarkupElement(element);

			// Make sure all tags of the base markup remember where they are
			// from
			if (element instanceof ComponentTag && resource != null)
			{
				ComponentTag tag = (ComponentTag)element;
				tag.setMarkupClass(markupClass);
			}
		}

		// Automatically add <head> if missing and required. On a Page
		// it must enclose ALL of the <wicket:head> tags.
		// Note: HtmlHeaderSectionHandler does something similar, but because
		// markup filters are not called for merged markup again, ...
		if (Page.class.isAssignableFrom(markup.getMarkupResourceStream().getMarkupClass()))
		{
			// Find the position inside the markup for first <wicket:head>,
			// last </wicket:head> and <head>
			int hasOpenWicketHead = -1;
			int hasCloseWicketHead = -1;
			int hasHead = -1;
			for (int i = 0; i < size(); i++)
			{
				MarkupElement element = get(i);

				boolean isHeadTag = (element instanceof WicketTag) && ((WicketTag) element).isHeadTag();
				if ((hasOpenWicketHead == -1) && isHeadTag)
				{
					hasOpenWicketHead = i;
				}
				else if (isHeadTag && ((ComponentTag)element).isClose())
				{
					hasCloseWicketHead = i;
				}
				else if ((hasHead == -1) && (element instanceof ComponentTag) &&
					TagUtils.isHeadTag(element))
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
				headOpenTag.setType(TagType.OPEN);
				final ComponentTag openTag = new ComponentTag(headOpenTag);
				openTag.setId(HtmlHeaderSectionHandler.HEADER_ID);
				openTag.setAutoComponentTag(true);

				final XmlTag headCloseTag = new XmlTag();
				headCloseTag.setName(headOpenTag.getName());
				headCloseTag.setType(TagType.CLOSE);
				final ComponentTag closeTag = new ComponentTag(headCloseTag);
				closeTag.setOpenTag(openTag);
				closeTag.setId(HtmlHeaderSectionHandler.HEADER_ID);

				addMarkupElement(hasOpenWicketHead, openTag);
				addMarkupElement(hasCloseWicketHead + 2, closeTag);
			}
		}
	}

	/**
	 * Append the wicket:head regions from the extended markup to the current markup
	 * 
	 * @param markup
	 * @param extendIndex
	 */
	private void copyWicketHead(final IMarkupFragment markup, int extendIndex)
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

			if (copy)
			{
				addMarkupElement(elem);
			}
		}
	}
}
