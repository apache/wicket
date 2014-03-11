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

import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.markup.parser.filter.EnclosureHandler;
import org.apache.wicket.markup.parser.filter.WicketLinkTagHandler;
import org.apache.wicket.markup.parser.filter.WicketRemoveTagHandler;
import org.apache.wicket.markup.resolver.FragmentResolver;
import org.apache.wicket.markup.resolver.HtmlHeaderResolver;
import org.apache.wicket.markup.resolver.MarkupInheritanceResolver;
import org.apache.wicket.markup.resolver.WicketContainerResolver;
import org.apache.wicket.markup.resolver.WicketMessageResolver;

/**
 * WicketTag extends ComponentTag and will be created by a MarkupParser whenever it parses a tag in
 * the wicket namespace. By default, this namespace is "wicket", so wicket tags are then of the form
 * &lt;wicket:*&gt;
 * <p>
 * Note 1: you need to add an XHTML doctype to your markup and use &lt;html xmlns:wicket&gt; to
 * create a XHTML conform namespace for such tags.
 * <p>
 * Note 2: The namespace name is configurable. E.g. &lt;html xmlns:wcn="http://wicket"&gt;
 * 
 * @author Juergen Donnerstag
 */
public class WicketTag extends ComponentTag
{
	/**
	 * Constructor
	 * 
	 * @param tag
	 *            The XML tag which this wicket tag is based upon.
	 */
	public WicketTag(final XmlTag tag)
	{
		super(tag);
	}

	/**
	 * Constructor
	 * 
	 * @param tag
	 *            The ComponentTag tag which this wicket tag is based upon.
	 */
	public WicketTag(final ComponentTag tag)
	{
		super(tag.getXmlTag());
		tag.copyPropertiesTo(this);
	}


	/**
	 * Get the tag's name attribute: e.g. &lt;wicket:region name=panel&gt;
	 * 
	 * @return The tag's name attribute
	 */
	public final String getNameAttribute()
	{
		return getAttributes().getString("name");
	}

	/**
	 * @return True, if tag name equals 'wicket:container'
	 */
	public final boolean isContainerTag()
	{
		return WicketContainerResolver.CONTAINER.equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'wicket:link'
	 */
	public final boolean isLinkTag()
	{
		return WicketLinkTagHandler.LINK.equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'wicket:remove'
	 */
	public final boolean isRemoveTag()
	{
		return WicketRemoveTagHandler.REMOVE.equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'wicket:body'
	 */
	public final boolean isBodyTag()
	{
		return Border.BODY.equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'wicket:child'
	 */
	public final boolean isChildTag()
	{
		return MarkupInheritanceResolver.CHILD.equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'wicket:extend'
	 */
	public final boolean isExtendTag()
	{
		return MarkupInheritanceResolver.EXTEND.equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'wicket:head'
	 */
	public final boolean isHeadTag()
	{
		return HtmlHeaderResolver.HEAD.equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'wicket:header-items'
	 */
	public final boolean isHeaderItemsTag()
	{
		return HtmlHeaderResolver.HEADER_ITEMS.equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'wicket:message'
	 */
	public final boolean isMessageTag()
	{
		return WicketMessageResolver.MESSAGE.equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'wicket:panel'
	 */
	public final boolean isPanelTag()
	{
		return Panel.PANEL.equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'wicket:border'
	 */
	public final boolean isBorderTag()
	{
		return Border.BORDER.equalsIgnoreCase(getName());
	}

	/**
	 * @return True if &lt;wicket:fragment&gt;
	 */
	public final boolean isFragmentTag()
	{
		return FragmentResolver.FRAGMENT.equalsIgnoreCase(getName());
	}

	/**
	 * @return true if &lt;wicket:enclsoure&gt;
	 */
	public final boolean isEnclosureTag()
	{
		return EnclosureHandler.ENCLOSURE.equalsIgnoreCase(getName());
	}

	/**
	 * @return True if <wicket:panel>, <wicket:border>, <wicket:ex
	 */
	public final boolean isMajorWicketComponentTag()
	{
		return isPanelTag() || isBorderTag() || isExtendTag();
	}

	/**
	 * Gets this tag if it is already mutable, or a mutable copy of this tag if it is immutable.
	 * 
	 * @return This tag if it is already mutable, or a mutable copy of this tag if it is immutable.
	 */
	@Override
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
			tag.setAutoComponentTag(isAutoComponentTag());
			return tag;
		}
	}
}
