/*
 * $Id$ $Revision:
 * 1.16 $ $Date$
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

import wicket.markup.parser.XmlTag;

/**
 * WicketTag extends ComponentTag ...
 * 
 * @author Juergen Donnerstag
 */
public class WicketHeaderTag extends WicketTag
{
    /** True if <head> is surrounding <wicket:head> */
    private boolean requiresHtmlHeadTag;
    
	/**
	 * Constructor
	 * 
	 * @param tag
	 *            The XML tag which this component tag is based upon.
	 */
	public WicketHeaderTag(final XmlTag tag)
	{
		super(tag);
	}

	/**
	 * Gets this tag if it is already mutable, or a mutable copy of this tag if
	 * it is immutable.
	 * 
	 * @return This tag if it is already mutable, or a mutable copy of this tag
	 *         if it is immutable.
	 */
	public ComponentTag mutable()
	{
		if (xmlTag.isMutable())
		{
			return this;
		}

		final WicketHeaderTag tag = new WicketHeaderTag(xmlTag.mutable());
		tag.setId(getId());
		return tag;
	}
	
	/**
	 * @return True, if <head> surrouding <wicket:head>
	 */
	public boolean isRequiresHtmlHeadTag()
	{
		return requiresHtmlHeadTag;
	}
	
	/**
	 * @param hasHtmlHeadTag The hasHtmlHeadTag to set.
	 */
	public void setRequiresHtmlHeadTag(boolean hasHtmlHeadTag)
	{
		this.requiresHtmlHeadTag = hasHtmlHeadTag;
	}
}