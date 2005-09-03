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

import java.util.ArrayList;
import java.util.List;

import wicket.WicketRuntimeException;
import wicket.markup.parser.XmlTag;

/**
 * This is a utility class which merges the base markup and the derived markup,
 * which is required for markup inheritance.
 * 
 * @author Juergen Donnerstag
 */
public class InheritedMarkupMerger
{
	/**
	 * Constructor.
	 */
	InheritedMarkupMerger()
	{
	}

	/**
	 * Merge inherited and base markup. <p/>
	 * Note: There is no new Markup object created. Instead the list of markup 
	 * elements contained in the inherited markup gets replaced. Thus, the 
	 * 'markup' parameter and the return value are the same and the content 
	 * of parameter 'markup' gets modified.
	 * 
	 * @param markup
	 *            The inherited markup
	 * @param baseMarkup
	 *            The base markup
	 * @param extendIndex
	 *            Index where <wicket:extend> has been found
	 * @return The merged markup
	 */
	static Markup mergeMarkups(final Markup markup, final Markup baseMarkup, int extendIndex)
	{
		// The new list of merged elements
		List markupElements = new ArrayList();

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

			if (element instanceof WicketTag)
			{
				WicketTag wtag = (WicketTag)element;
				if (wtag.isChildTag() && wtag.isOpenClose())
				{
					childTag = wtag;
					WicketTag childOpenTag = (WicketTag)wtag.mutable();
					childOpenTag.getXmlTag().setType(XmlTag.OPEN);
					markupElements.add(childOpenTag);
					break;
				}
			}

			// If element in base markup is </wicket:head>, scan the derived
			// markup for <wicket:head> and add all elements of that tag body
			// the new markup list. 
			if ((wicketHeadProcessed == false) && (element instanceof WicketTag))
			{
			    final WicketTag tag = (WicketTag) element;
			    
			    if (tag.isClose() && "head".equalsIgnoreCase(tag.getName()) 
			            && (tag.getNamespace() != null))
			    {
			        // Before close the tag, add the <wicket:head> body from the 
			        // derived markup
			        boolean copy = false;
			        for (int i=0; i < extendIndex; i++)
			        {
			            MarkupElement elem = (MarkupElement) markup.get(i);
			            if (elem instanceof WicketTag)
			            {
			                WicketTag etag = (WicketTag) elem;
						    if ("head".equalsIgnoreCase(etag.getName()) 
						            && (etag.getNamespace() != null))
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
					        markupElements.add(elem);
					    }
			        }
			    }
			}

			// If element in base markup is </head>, scan the derived
			// markup for <wicket:head> and add all elements of that tag body
			// the new markup list. 
			if ((headProcessed == false) && (element instanceof ComponentTag))
			{
			    final ComponentTag tag = (ComponentTag) element;
			    
			    if (tag.isClose() && "head".equalsIgnoreCase(tag.getName()) 
			            && (tag.getNamespace() == null))
			    {
			        // Before close the tag, add the <wicket:head> body from the 
			        // derived markup
			        boolean copy = false;
			        for (int i=0; i < extendIndex; i++)
			        {
			            MarkupElement elem = (MarkupElement) markup.get(i);
			            if (elem instanceof WicketTag)
			            {
			                WicketTag etag = (WicketTag) elem;
						    if ("head".equalsIgnoreCase(etag.getName()) 
						            && (etag.getNamespace() != null))
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
					        markupElements.add(elem);
					    }
			        }
			    }
			}

			markupElements.add(element);
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
			markupElements.add(element);

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
		markupElements.add(childCloseTag);

		for (baseIndex++; baseIndex < baseMarkup.size(); baseIndex++)
		{
			MarkupElement element = baseMarkup.get(baseIndex);
			markupElements.add(element);
		}

		// Replace the markups elements.
		markup.setMarkup(markupElements);
		return markup;
	}

	/**
	 * Check if markup contains <wicket:extend>which tells us that we need to
	 * read the inherited markup as well. <wicket:extend>MUST BE the first
	 * wicket tag in the markup. Skip raw markup
	 * 
	 * @param markup
	 * @return == 0, if no wicket:extend was found
	 */
	static int requiresBaseMarkup(final Markup markup)
	{
	    for (int i=0; i < markup.size(); i++)
	    {
			if (markup.get(i) instanceof WicketTag)
			{
				WicketTag wtag = (WicketTag) markup.get(i);
				if (wtag.isExtendTag())
				{
					// Ok, inheritance is on and we must get the
					// inherited markup as well.
					return i;
				}
			}
	    }

		return -1;
	}
}
