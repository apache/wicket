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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.markup.parser.XmlTag.TagType;
import org.apache.wicket.markup.parser.filter.HtmlHandler;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.lang.Generics;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.value.IValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A subclass of MarkupElement which represents a "significant" markup tag, such as a component open
 * tag. Insignificant markup tags (those which are merely concerned with markup formatting
 * operations and do not denote components or component nesting) are coalesced into instances of
 * RawMarkup (also a subclass of MarkupElement).
 * 
 * @author Jonathan Locke
 */
public class ComponentTag extends MarkupElement
{
	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(ComponentTag.class);

	/** True if a href attribute is available and autolinking is on */
	private final static int AUTOLINK = 0x0001;

	/** True, if attributes have been modified or added */
	private final static int MODIFIED = 0x0002;

	/** If true, then the MarkupParser will ignore (remove) it. Temporary working variable */
	private final static int IGNORE = 0x0004;

	/** If true, then the tag contain an automatically created wicket id */
	private final static int AUTO_COMPONENT = 0x0008;

	/** Some HTML tags are allow to have no close tag, e.g. 'br' */
	private final static int NO_CLOSE_TAG = 0x0010;

	/** Render the tag as RawMarkup even if no Component can be found */
	public final static int RENDER_RAW = 0x0020;

	/** If close tag, than reference to the corresponding open tag */
	private ComponentTag openTag;

	/** The underlying xml tag */
	protected final XmlTag xmlTag;

	/** Boolean flags. See above */
	private int flags = 0;

	/**
	 * By default this is equal to the wicket:id="xxx" attribute value, but may be provided e.g. for
	 * auto-tags
	 */
	private String id;

	/**
	 * In case of inherited markup, the base and the extended markups are merged and the information
	 * about the tags origin is lost. In some cases like wicket:head and wicket:link this
	 * information however is required.
	 */
	private WeakReference<Class<? extends Component>> markupClassRef = null;

	/** added behaviors */
	private List<Behavior> behaviors;

	/** Filters and Handlers may add their own attributes to the tag */
	private Map<String, Object> userData;

	/**
	 * Automatically create a XmlTag, assign the name and the type, and construct a ComponentTag
	 * based on this XmlTag.
	 * 
	 * @param name
	 *            The name of html tag
	 * @param type
	 *            The type of tag
	 */
	public ComponentTag(final String name, final XmlTag.TagType type)
	{
		final XmlTag tag = new XmlTag();
		tag.setName(name);
		tag.setType(type);
		xmlTag = tag;
	}

	/**
	 * Construct.
	 * 
	 * @param tag
	 *            The underlying xml tag
	 */
	public ComponentTag(final XmlTag tag)
	{
		super();
		xmlTag = tag;
	}

	/**
	 * Constructor
	 * 
	 * @param tag
	 *            The ComponentTag tag which this wicket tag is based upon.
	 */
	public ComponentTag(final ComponentTag tag)
	{
		this(tag.getXmlTag());
		tag.copyPropertiesTo(this);
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT!
	 * 
	 * @param flag
	 *            The flag to set
	 * @param set
	 *            True to turn the flag on, false to turn it off
	 */
	public final void setFlag(final int flag, final boolean set)
	{
		if (set)
		{
			flags |= flag;
		}
		else
		{
			flags &= ~flag;
		}
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT!
	 * 
	 * @param flag
	 *            The flag to test
	 * @return True if the flag is set
	 */
	public final boolean getFlag(final int flag)
	{
		return (flags & flag) != 0;
	}

	/**
	 * Adds a behavior to this component tag.
	 * 
	 * @param behavior
	 */
	public final void addBehavior(final Behavior behavior)
	{
		if (behavior == null)
		{
			throw new IllegalArgumentException("Argument [behavior] cannot be null");
		}

		if (behaviors == null)
		{
			behaviors = Generics.newArrayList();
		}
		behaviors.add(behavior);
	}

	/**
	 * @return true if this tag has any behaviors added, false otherwise
	 */
	public final boolean hasBehaviors()
	{
		return behaviors != null;
	}

	/**
	 * @return read only iterator over added behaviors
	 * @TODO change to return unmodifiable list which will never be null. See Component.getBehavior
	 */
	public final Iterator<? extends Behavior> getBehaviors()
	{
		if (behaviors == null)
		{
			List<Behavior> lst = Collections.emptyList();
			return lst.iterator();
		}

		return Collections.unmodifiableCollection(behaviors).iterator();
	}

	/**
	 * Gets whether this tag closes the provided open tag.
	 * 
	 * @param open
	 *            The open tag
	 * @return True if this tag closes the given open tag
	 */
	@Override
	public final boolean closes(final MarkupElement open)
	{
		if (open instanceof ComponentTag)
		{
			return (openTag == open) || getXmlTag().closes(((ComponentTag)open).getXmlTag());
		}

		return false;
	}

	/**
	 * If autolink is set to true, href attributes will automatically be converted into Wicket
	 * bookmarkable URLs.
	 * 
	 * @param autolink
	 *            enable/disable automatic href conversion
	 */
	public final void enableAutolink(final boolean autolink)
	{
		setFlag(AUTOLINK, autolink);
	}

	/**
	 * @see org.apache.wicket.markup.parser.XmlTag#getAttributes()
	 * @return The tag#s attributes
	 */
	public final IValueMap getAttributes()
	{
		return xmlTag.getAttributes();
	}

	/**
	 * A convenient method. The same as getAttributes().getString(name)
	 * 
	 * @param name
	 * @return The attributes value
	 */
	public final String getAttribute(String name)
	{
		return xmlTag.getAttributes().getString(name);
	}

	/**
	 * Get the tag's component id
	 * 
	 * @return The component id attribute of this tag
	 */
	public final String getId()
	{
		return id;
	}

	/**
	 * Gets the length of the tag in characters.
	 * 
	 * @return The tag's length
	 */
	public final int getLength()
	{
		return xmlTag.getLength();
	}

	/**
	 * @return The tag's name
	 */
	public final String getName()
	{
		return xmlTag.getName();
	}

	/**
	 * @return The tag's namespace
	 */
	public final String getNamespace()
	{
		return xmlTag.getNamespace();
	}

	/**
	 * If set, return the corresponding open tag (ComponentTag).
	 * 
	 * @return The corresponding open tag
	 */
	public final ComponentTag getOpenTag()
	{
		return openTag;
	}

	/**
	 * @see org.apache.wicket.markup.parser.XmlTag#getPos()
	 * @return Tag location (index in input string)
	 */
	public final int getPos()
	{
		return xmlTag.getPos();
	}

	/**
	 * @return the tag type (OPEN, CLOSE or OPEN_CLOSE).
	 */
	public final TagType getType()
	{
		return xmlTag.getType();
	}

	/**
	 * True if autolink is enabled and the tag contains a href attribute.
	 * 
	 * @return True, if the href contained should automatically be converted
	 */
	public final boolean isAutolinkEnabled()
	{
		return getFlag(AUTOLINK);
	}

	/**
	 * @see org.apache.wicket.markup.parser.XmlTag#isClose()
	 * @return True if this tag is a close tag
	 */
	public final boolean isClose()
	{
		return xmlTag.isClose();
	}

	/**
	 * @see org.apache.wicket.markup.parser.XmlTag#isOpen()
	 * @return True if this tag is an open tag
	 */
	public final boolean isOpen()
	{
		return xmlTag.isOpen();
	}

	/**
	 * @param id
	 *            Required component id
	 * @return True if this tag is an open tag with the given component name
	 * @see org.apache.wicket.markup.parser.XmlTag#isOpen()
	 */
	public final boolean isOpen(String id)
	{
		return xmlTag.isOpen() && this.id.equals(id);
	}

	/**
	 * @see org.apache.wicket.markup.parser.XmlTag#isOpenClose()
	 * @return True if this tag is an open and a close tag
	 */
	public final boolean isOpenClose()
	{
		return xmlTag.isOpenClose();
	}

	/**
	 * @param id
	 *            Required component id
	 * @return True if this tag is an openclose tag with the given component id
	 * @see org.apache.wicket.markup.parser.XmlTag#isOpenClose()
	 */
	public final boolean isOpenClose(String id)
	{
		return xmlTag.isOpenClose() && this.id.equals(id);
	}

	/**
	 * Makes this tag object immutable by making the attribute map unmodifiable. Immutable tags
	 * cannot be made mutable again. They can only be copied into new mutable tag objects.
	 */
	public final void makeImmutable()
	{
		xmlTag.makeImmutable();
	}

	/**
	 * Gets this tag if it is already mutable, or a mutable copy of this tag if it is immutable.
	 * 
	 * @return This tag if it is already mutable, or a mutable copy of this tag if it is immutable.
	 */
	public ComponentTag mutable()
	{
		if (xmlTag.isMutable())
		{
			return this;
		}
		else
		{
			ComponentTag tag = new ComponentTag(xmlTag.mutable());
			copyPropertiesTo(tag);
			return tag;
		}
	}

	/**
	 * Copies all internal properties from this tag to <code>dest</code>. This is basically cloning
	 * without instance creation.
	 * 
	 * @param dest
	 *            tag whose properties will be set
	 */
	void copyPropertiesTo(final ComponentTag dest)
	{
		dest.id = id;
		dest.flags = flags;
		if (markupClassRef != null)
		{
			dest.setMarkupClass(markupClassRef.get());
		}
		if (behaviors != null)
		{
			dest.behaviors = new ArrayList<Behavior>(behaviors);
		}
		if (userData != null)
		{
			dest.userData = new HashMap<String, Object>(userData);
		}
	}

	/**
	 * @see org.apache.wicket.markup.parser.XmlTag#put(String, boolean)
	 * @param key
	 *            The key
	 * @param value
	 *            The value
	 */
	public final void put(final String key, final boolean value)
	{
		xmlTag.put(key, value);
		setModified(true);
	}

	/**
	 * @see org.apache.wicket.markup.parser.XmlTag#put(String, int)
	 * @param key
	 *            The key
	 * @param value
	 *            The value
	 */
	public final void put(final String key, final int value)
	{
		xmlTag.put(key, value);
		setModified(true);
	}

	/**
	 * @see org.apache.wicket.markup.parser.XmlTag#put(String, CharSequence)
	 * @param key
	 *            The key
	 * @param value
	 *            The value
	 */
	public final void put(String key, CharSequence value)
	{
		checkIdAttribute(key);
		putInternal(key, value);
	}

	/**
	 * THIS METHOD IS NOT PART OF THE PUBLIC API, DO NOT CALL IT
	 * 
	 * @see org.apache.wicket.markup.parser.XmlTag#put(String, CharSequence)
	 * @param key
	 *            The key
	 * @param value
	 *            The value
	 */
	public final void putInternal(String key, CharSequence value)
	{
		xmlTag.put(key, value);
		setModified(true);
	}

	/**
	 * @param key
	 */
	private void checkIdAttribute(String key)
	{
		if ("id".equalsIgnoreCase(key))
		{
			log.warn("Please use component.setMarkupId(String) to change the tag's 'id' attribute.");
		}
	}

	/**
	 * Appends specified {@code value} to the attribute
	 * 
	 * @param key
	 *            The key
	 * @param value
	 *            The value
	 * @param separator
	 *            The separator used to append the value
	 */
	public final void append(String key, CharSequence value, String separator)
	{
		String current = getAttribute(key);
		if (Strings.isEmpty(current))
		{
			xmlTag.put(key, value);
		}
		else
		{
			xmlTag.put(key, current + separator + value);
		}

		setModified(true);
	}

	/**
	 * @see org.apache.wicket.markup.parser.XmlTag#put(String, StringValue)
	 * @param key
	 *            The key
	 * @param value
	 *            The value
	 */
	public final void put(String key, StringValue value)
	{
		xmlTag.put(key, value);
		setModified(true);
	}

	/**
	 * @see org.apache.wicket.markup.parser.XmlTag#putAll(Map)
	 * @param map
	 *            a key/value map
	 */
	public final void putAll(final Map<String, Object> map)
	{
		xmlTag.putAll(map);
		setModified(true);
	}

	/**
	 * @see org.apache.wicket.markup.parser.XmlTag#remove(String)
	 * @param key
	 *            The key to remove
	 */
	public final void remove(String key)
	{
		xmlTag.remove(key);
		setModified(true);
	}

	/**
	 * Gets whether this tag does not require a closing tag.
	 * 
	 * @return True if this tag does not require a closing tag
	 */
	public final boolean requiresCloseTag()
	{
		if (getNamespace() == null)
		{
			return HtmlHandler.requiresCloseTag(getName());
		}
		else
		{
			return HtmlHandler.requiresCloseTag(getNamespace() + ":" + getName());
		}
	}

	/**
	 * Set the component's id. The value is usually taken from the tag's id attribute, e.g.
	 * wicket:id="componentId".
	 * 
	 * @param id
	 *            The component's id assigned to the tag.
	 */
	public final void setId(final String id)
	{
		this.id = id;
	}

	/**
	 * @see org.apache.wicket.markup.parser.XmlTag#setName(String)
	 * @param name
	 *            New tag name
	 */
	public final void setName(String name)
	{
		xmlTag.setName(name);
	}

	/**
	 * @see org.apache.wicket.markup.parser.XmlTag#setNamespace(String)
	 * @param namespace
	 *            New tag name namespace
	 */
	public final void setNamespace(String namespace)
	{
		xmlTag.setNamespace(namespace);
	}

	/**
	 * Assuming this is a close tag, assign it's corresponding open tag.
	 * 
	 * @param tag
	 *            the open-tag
	 * @throws RuntimeException
	 *             if 'this' is not a close tag
	 */
	public final void setOpenTag(final ComponentTag tag)
	{
		openTag = tag;
		getXmlTag().setOpenTag(tag.getXmlTag());
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * @param type
	 *            The new type
	 */
	public final void setType(final TagType type)
	{
		if (type != xmlTag.getType())
		{
			xmlTag.setType(type);
			setModified(true);
		}
	}

	/**
	 * @return A synthetic close tag for this tag
	 */
	public final CharSequence syntheticCloseTagString()
	{
		AppendingStringBuffer buf = new AppendingStringBuffer();
		buf.append("</");
		if (getNamespace() != null)
		{
			buf.append(getNamespace()).append(':');
		}
		buf.append(getName()).append('>');

		return buf;
	}

	/**
	 * @see org.apache.wicket.markup.MarkupElement#toCharSequence()
	 */
	@Override
	public CharSequence toCharSequence()
	{
		return xmlTag.toCharSequence();
	}

	/**
	 * Converts this object to a string representation.
	 * 
	 * @return String version of this object
	 */
	@Override
	public final String toString()
	{
		return toCharSequence().toString();
	}

	/**
	 * Write the tag to the response
	 * 
	 * @param response
	 *            The response to write to
	 * @param stripWicketAttributes
	 *            if true, wicket:id are removed from output
	 * @param namespace
	 *            Wicket's namespace to use
	 */
	public final void writeOutput(final Response response, final boolean stripWicketAttributes,
		final String namespace)
	{
		response.write("<");

		if (getType() == TagType.CLOSE)
		{
			response.write("/");
		}

		if (getNamespace() != null)
		{
			response.write(getNamespace());
			response.write(":");
		}

		response.write(getName());

		String namespacePrefix = null;
		if (stripWicketAttributes == true)
		{
			namespacePrefix = namespace + ":";
		}

		if (getAttributes().size() > 0)
		{
			for (String key : getAttributes().keySet())
			{
				if (key == null)
				{
					continue;
				}

				if ((namespacePrefix == null) || (key.startsWith(namespacePrefix) == false))
				{
					response.write(" ");
					response.write(key);
					CharSequence value = getAttribute(key);

					// attributes without values are possible, e.g.' disabled'
					if (value != null)
					{
						response.write("=\"");
						value = Strings.escapeMarkup(value);
						response.write(value);
						response.write("\"");
					}
				}
			}
		}

		if (getType() == TagType.OPEN_CLOSE)
		{
			response.write("/");
		}

		response.write(">");
	}

	/**
	 * Converts this object to a string representation including useful information for debugging
	 * 
	 * @return String version of this object
	 */
	@Override
	public final String toUserDebugString()
	{
		return xmlTag.toUserDebugString();
	}

	/**
	 * @return Returns the underlying xml tag.
	 */
	public final XmlTag getXmlTag()
	{
		return xmlTag;
	}

	/**
	 * Manually mark the ComponentTag being modified. Flagging the tag being modified does not
	 * happen automatically.
	 * 
	 * @param modified
	 */
	public final void setModified(final boolean modified)
	{
		setFlag(MODIFIED, modified);
	}

	/**
	 * 
	 * @return True, if the component tag has been marked modified
	 */
	final boolean isModified()
	{
		return getFlag(MODIFIED);
	}

	/**
	 * 
	 * @return True if the HTML tag (e.g. br) has no close tag
	 */
	public boolean hasNoCloseTag()
	{
		return getFlag(NO_CLOSE_TAG);
	}

	/**
	 * True if the HTML tag (e.g. br) has no close tag
	 * 
	 * @param hasNoCloseTag
	 */
	public void setHasNoCloseTag(boolean hasNoCloseTag)
	{
		setFlag(NO_CLOSE_TAG, hasNoCloseTag);
	}

	/**
	 * In case of inherited markup, the base and the extended markups are merged and the information
	 * about the tags origin is lost. In some cases like wicket:head and wicket:link this
	 * information however is required.
	 * 
	 * @return wicketHeaderClass
	 */
	public Class<? extends Component> getMarkupClass()
	{
		return (markupClassRef == null ? null : markupClassRef.get());
	}

	/**
	 * Set the class of wicket component which contains the wicket:head tag.
	 * 
	 * @param <C>
	 * 
	 * @param wicketHeaderClass
	 *            wicketHeaderClass
	 */
	public <C extends Component> void setMarkupClass(Class<C> wicketHeaderClass)
	{
		if (wicketHeaderClass == null)
		{
			markupClassRef = null;
		}
		else
		{
			markupClassRef = new WeakReference<Class<? extends Component>>(wicketHeaderClass);
		}
	}

	/**
	 * @see org.apache.wicket.markup.MarkupElement#equalTo(org.apache.wicket.markup.MarkupElement)
	 */
	@Override
	public boolean equalTo(final MarkupElement element)
	{
		if (element instanceof ComponentTag)
		{
			final ComponentTag that = (ComponentTag)element;
			return getXmlTag().equalTo(that.getXmlTag());
		}
		return false;
	}

	/**
	 * Gets ignore.
	 * 
	 * @return If true than MarkupParser will remove it from the markup
	 */
	public boolean isIgnore()
	{
		return getFlag(IGNORE);
	}

	/**
	 * Sets ignore.
	 * 
	 * @param ignore
	 *            If true than MarkupParser will remove it from the markup
	 */
	public void setIgnore(boolean ignore)
	{
		setFlag(IGNORE, ignore);
	}

	/**
	 * @return True, if wicket:id has been automatically created (internal component)
	 */
	public boolean isAutoComponentTag()
	{
		return getFlag(AUTO_COMPONENT);
	}

	/**
	 * @param auto
	 *            True, if wicket:id has been automatically created (internal component)
	 */
	public void setAutoComponentTag(boolean auto)
	{
		setFlag(AUTO_COMPONENT, auto);
	}

	/**
	 * Gets userData.
	 * 
	 * @param key
	 *            The key to store and retrieve the value
	 * @return userData
	 */
	public Object getUserData(final String key)
	{
		if (userData == null)
		{
			return null;
		}

		return userData.get(key);
	}

	/**
	 * Sets userData.
	 * 
	 * @param key
	 *            The key to store and retrieve the value
	 * @param value
	 *            The user specific value to store
	 */
	public void setUserData(final String key, final Object value)
	{
		if (userData == null)
		{
			userData = new HashMap<String, Object>();
		}
		userData.put(key, value);
	}

	/**
	 * For subclasses to override. Gets called just before a Component gets rendered. It is
	 * guaranteed that the markupStream is set on the Component and determineVisibility is not yet
	 * called.
	 * 
	 * @param component
	 *            The component that is about to be rendered
	 * @param markupStream
	 *            The current markup stream
	 */
	public void onBeforeRender(final Component component, final MarkupStream markupStream)
	{
	}
}
