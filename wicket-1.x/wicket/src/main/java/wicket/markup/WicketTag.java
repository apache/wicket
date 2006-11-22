/*
 * $Id$ $Revision$
 * $Date$
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
 * WicketTag extends ComponentTag and will be created by a MarkupParser whenever
 * it parses a tag in the wicket namespace. By default, this namespace is
 * "wicket", so wicket tags are then of the form &lt;wicket:*&gt;
 * <p>
 * Note 1: you need to add an XHTML doctype to your markup and use &lt;html
 * xmlns:wicket&gt; to create a XHTML conformant namespace for such tags.
 * <p>
 * Note 2: The namespace name is configurable. E.g. &lt;html
 * xmlns:wcn="http://wicket.sourcefourge.net"&gt;
 * 
 * @author Juergen Donnerstag
 */
public class WicketTag extends ComponentTag
{
	/**
	 * Constructor
	 * 
	 * @param tag
	 *            The XML tag which this component tag is based upon.
	 */
	public WicketTag(final XmlTag tag)
	{
		super(tag);
	}

	/**
	 * Get the tag's name attribute: e.g. &lt;wicket:region name=panel&gt;
	 * 
	 * @return The tag's name attribute
	 */
	public final String getNameAttribute()
	{
		return this.getAttributes().getString("name");
	}

	/**
	 * @return True, if tag name equals 'wicket:component'
	 */
	public final boolean isComponentTag()
	{
		return "component".equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'wicket:link'
	 */
	public final boolean isLinkTag()
	{
		return "link".equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'wicket:remove'
	 */
	public final boolean isRemoveTag()
	{
		return "remove".equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'wicket:body'
	 */
	public final boolean isBodyTag()
	{
		return "body".equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'wicket:child'
	 */
	public final boolean isChildTag()
	{
		return "child".equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'wicket:extend'
	 */
	public final boolean isExtendTag()
	{
		return "extend".equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'wicket:extend'
	 */
	public final boolean isHeadTag()
	{
		return "head".equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'wicket:message'
	 */
	public final boolean isMessageTag()
	{
		return "message".equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'wicket:panel'
	 */
	public final boolean isPanelTag()
	{
		return "panel".equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'wicket:border'
	 */
	public final boolean isBorderTag()
	{
		return "border".equalsIgnoreCase(getName());
	}

	/**
	 * @return True if &lt;wicket:fragment&gt;
	 */
	public final boolean isFragementTag()
	{
		return "fragment".equalsIgnoreCase(getName());
	}

	/**
	 * @return True if <wicket:panel>, <wicket:border>, <wicket:ex
	 */
	public final boolean isMajorWicketComponentTag()
	{
		return isPanelTag() || isBorderTag() || isExtendTag();
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
		else
		{
			final WicketTag tag = new WicketTag(xmlTag.mutable());
			tag.setId(getId());
			return tag;
		}
	}
}