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

import org.apache.wicket.markup.parser.XmlTag;

/**
 * WicketTag extends ComponentTag and will be created by a MarkupParser whenever
 * it parses a tag in the org.apache.wicket namespace. By default, this namespace is
 * "org.apache.wicket", so org.apache.wicket tags are then of the form &lt;org.apache.wicket:*&gt;
 * <p>
 * Note 1: you need to add an XHTML doctype to your markup and use &lt;html
 * xmlns:org.apache.wicket&gt; to create a XHTML conformant namespace for such tags.
 * <p>
 * Note 2: The namespace name is configurable. E.g. &lt;html
 * xmlns:wcn="http://org.apache.wicket.sourcefourge.net"&gt;
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
	 * Get the tag's name attribute: e.g. &lt;org.apache.wicket:region name=panel&gt;
	 * 
	 * @return The tag's name attribute
	 */
	public final String getNameAttribute()
	{
		return this.getAttributes().getString("name");
	}

	/**
	 * @return True, if tag name equals 'org.apache.wicket:component'
	 */
	public final boolean isComponentTag()
	{
		return "component".equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'org.apache.wicket:link'
	 */
	public final boolean isLinkTag()
	{
		return "link".equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'org.apache.wicket:remove'
	 */
	public final boolean isRemoveTag()
	{
		return "remove".equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'org.apache.wicket:body'
	 */
	public final boolean isBodyTag()
	{
		return "body".equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'org.apache.wicket:container'
	 */
	public final boolean isContainerTag()
	{
		return "container".equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'org.apache.wicket:child'
	 */
	public final boolean isChildTag()
	{
		return "child".equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'org.apache.wicket:extend'
	 */
	public final boolean isExtendTag()
	{
		return "extend".equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'org.apache.wicket:extend'
	 */
	public final boolean isHeadTag()
	{
		return "head".equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'org.apache.wicket:message'
	 */
	public final boolean isMessageTag()
	{
		return "message".equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'org.apache.wicket:panel'
	 */
	public final boolean isPanelTag()
	{
		return "panel".equalsIgnoreCase(getName());
	}

	/**
	 * @return True, if tag name equals 'org.apache.wicket:border'
	 */
	public final boolean isBorderTag()
	{
		return "border".equalsIgnoreCase(getName());
	}

	/**
	 * @return True if &lt;org.apache.wicket:fragment&gt;
	 */
	public final boolean isFragementTag()
	{
		return "fragment".equalsIgnoreCase(getName());
	}

	/**
	 * @return true if &lt;org.apache.wicket:enclsoure&gt;
	 */
	public final boolean isEnclosureTag()
	{
		return "enclosure".equalsIgnoreCase(getName());
	}

	/**
	 * @return True if <org.apache.wicket:panel>, <org.apache.wicket:border>, <org.apache.wicket:ex
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