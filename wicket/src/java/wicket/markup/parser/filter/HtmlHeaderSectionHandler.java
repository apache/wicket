/*
 * $Id: WicketTagIdentifier.java,v 1.4 2005/02/04 07:22:53 jdonnerstag
 * Exp $ $Revision$ $Date$
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
package wicket.markup.parser.filter;

import java.text.ParseException;
import java.util.List;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupElement;
import wicket.markup.parser.AbstractMarkupFilter;
import wicket.markup.parser.IMarkupFilter;
import wicket.markup.parser.XmlTag;

/**
 * This is a markup inline filter. It assumes that WicketTagIdentifier has been
 * called first and search for a &lt;head&gt; tag (note: not wicket:head). 
 * Provided the markup contains a &lt;body&gt; tag it will
 * automatically prepend a &lt;head&gt; tag if missing.
 * <p>
 * 
 * @author Juergen Donnerstag
 */
public final class HtmlHeaderSectionHandler extends AbstractMarkupFilter
{
	/** The automatically assigned wicket:id to &gt;head&lt; tag */
	public static final String HEADER_ID = "_header";

	private boolean foundHead = false;
	private boolean ignoreTheRest = false;

	/**
	 * In case you want to add extra Components to the markup, just add them to
	 * the list. MarkupParser will handle it.
	 */
	private List tagList;

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The next MarkupFilter in the chain
	 */
	public HtmlHeaderSectionHandler(final IMarkupFilter parent)
	{
		super(parent);
	}

	/**
	 * In order to add ComponentTags which are NOT from markup file.
	 * 
	 * @param tagList
	 */
	public void setTagList(final List tagList)
	{
		this.tagList = tagList;
	}

	/**
	 * Get the next tag from the next MarkupFilter in the chain and search for
	 * Wicket specific tags.
	 * <p>
	 * 
	 * @see wicket.markup.parser.IMarkupFilter#nextTag()
	 * @return The next tag from markup to be processed. If null, no more tags
	 *         are available
	 */
	public MarkupElement nextTag() throws ParseException
	{
		// Get the next tag from the markup.
		// If null, no more tags are available
		ComponentTag tag = (ComponentTag)getParent().nextTag();
		if (tag == null)
		{
			return tag;
		}

		// Whatever there is left in the markup, ignore it
		if (ignoreTheRest == true)
		{
			return tag;
		}

		// if it is <head> or </head>
		if (("head".equalsIgnoreCase(tag.getName()) == true) && (tag.getNamespace() == null))
		{
			// it is <head>
			if (tag.isClose())
			{
				foundHead = true;
			}

			// Usually <head> is not a wicket special tag. But because we want
			// transparent header support we insert it automatically if mmissing
			// and while rendering its content all child components are asked if 
			// they want to contribute something to the header. Thus we have to
			// handle <head> accordingly.
			tag.setId(HEADER_ID);

			return tag;
		}

		// if it is <body>
		if (("body".equalsIgnoreCase(tag.getName()) == true) && (tag.getNamespace() == null))
		{
			// we found no <head> . But because we found <body> we assume it 
		    // could be a page. And because we need to auto-add <head> to 
		    // pages only (if missing) ... Note: no one prevent a designer
		    // to put a <body> in a panel component, for previewabilty.
		    // Thus more markups than actually required might now have
			// that tag. It should be a problem, but ... you never know.
			if (foundHead == false)
			{
				insertHeadTag();
			}

			// <head> must always be before <body>
			ignoreTheRest = true;
			return tag;
		}

		return tag;
	}

	/**
	 * Insert <head> open and close tag (with empty body) to the
	 * current position.
	 */
	private void insertHeadTag()
	{
		final XmlTag headOpenTag = new XmlTag();
		headOpenTag.setName("head");
		headOpenTag.setType(XmlTag.OPEN);
		final ComponentTag openTag = new ComponentTag(headOpenTag);
		openTag.setId(HEADER_ID);

		final XmlTag headCloseTag = new XmlTag();
		headCloseTag.setName(headOpenTag.getName());
		headCloseTag.setType(XmlTag.CLOSE);
		final ComponentTag closeTag = new ComponentTag(headCloseTag);
		closeTag.setOpenTag(openTag);
		closeTag.setId(HEADER_ID);

		// insert the tags into the markup stream
		tagList.add(openTag);
		tagList.add(closeTag);
	}
}
