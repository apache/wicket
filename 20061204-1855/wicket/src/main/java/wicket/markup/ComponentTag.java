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
package wicket.markup;

import java.util.Iterator;
import java.util.Map;

import wicket.Response;
import wicket.markup.parser.XmlTag;
import wicket.markup.parser.XmlTag.Type;
import wicket.markup.parser.filter.HtmlHandler;
import wicket.util.string.AppendingStringBuffer;
import wicket.util.string.StringValue;
import wicket.util.string.Strings;
import wicket.util.value.ValueMap;

/**
 * A subclass of MarkupElement which represents a "significant" markup tag, such
 * as a component open tag. Insignificant markup tags (those which are merely
 * concerned with markup formatting operations and do not denote components or
 * component nesting) are coalesced into instances of RawMarkup (also a subclass
 * of MarkupElement).
 * 
 * @author Jonathan Locke
 */
public class ComponentTag extends MarkupElement
{
	/**
	 * Standard component id attribute always available for components
	 * regardless of user ApplicationSettings for id attribute; value ==
	 * 'wicket'.
	 */
	public static final String DEFAULT_WICKET_NAMESPACE = "wicket";

	/**
	 * Assuming this is a open (or open-close) tag, 'closes' refers to the
	 * ComponentTag which closes it.
	 */
	protected ComponentTag closes;

	/** The underlying xml tag */
	protected final XmlTag xmlTag;

	/** True if a href attribute is available and autolinking is on */
	private boolean autolink = false;

	/** The component's id identified by wicket:id="xxx" */
	private String id;

	/** The component's path in the markup */
	private String path;

	/** True, if attributes have been modified or added */
	private boolean modified = false;
	
	/**
	 * In case of inherited markup, the base and the extended markups are merged
	 * and the information about the tags origin is lost. In some cases like
	 * wicket:head and wicket:link this information however is required.
	 */
	private Class markupClass;

	/**
	 * Tags which are detected to have only an open tag, which is allowed with
	 * some HTML tags like 'br' for example
	 */
	private boolean hasNoCloseTag = false;

	/**
	 * True if the tag has not wicket namespace and no wicket:id. E.g. <head> or
	 * <body>
	 */
	private boolean internalTag = false;

	/**
	 * Automatically create a XmlTag, assign the name and the type, and
	 * construct a ComponentTag based on this XmlTag.
	 * 
	 * @param name
	 *            The name of html tag
	 * @param type
	 *            The type of tag
	 */
	public ComponentTag(final String name, final XmlTag.Type type)
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
	 * Gets whether this tag closes the provided open tag.
	 * 
	 * @param open
	 *            The open tag
	 * @return True if this tag closes the given open tag
	 */
	public final boolean closes(final MarkupElement open)
	{
		if (open instanceof ComponentTag)
		{
			return (closes == open) || getXmlTag().closes(((ComponentTag)open).getXmlTag());
		}

		return false;
	}

	/**
	 * If autolink is set to true, href attributes will automatically be
	 * converted into Wicket bookmarkable URLs.
	 * 
	 * @param autolink
	 *            enable/disable automatic href conversion
	 */
	public final void enableAutolink(final boolean autolink)
	{
		this.autolink = autolink;
	}

	/**
	 * @see wicket.markup.parser.XmlTag#getAttributes()
	 * @return The tag#s attributes
	 */
	public final ValueMap getAttributes()
	{
		return xmlTag.getAttributes();
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
	 * @see wicket.markup.parser.XmlTag#getName()
	 * @return The tag's name
	 */
	public final String getName()
	{
		return xmlTag.getName();
	}

	/**
	 * @see wicket.markup.parser.XmlTag#getNameChanged()
	 * @return Returns true if the name of this component tag was changed
	 */
	public final boolean getNameChanged()
	{
		return xmlTag.getNameChanged();
	}

	/**
	 * @see wicket.markup.parser.XmlTag#getNamespace()
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
		return closes;
	}

	/**
	 * @see wicket.markup.parser.XmlTag#getPos()
	 * @return Tag location (index in input string)
	 */
	public final int getPos()
	{
		return xmlTag.getPos();
	}

	/**
	 * @see wicket.markup.parser.XmlTag#getString(String)
	 * @param key
	 *            The key
	 * @return The string value
	 */
	public final CharSequence getString(String key)
	{
		return xmlTag.getString(key);
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * <p>
	 * 
	 * @see wicket.markup.parser.XmlTag#getType()
	 * @return the tag type (OPEN, CLOSE or OPEN_CLOSE).
	 */
	public final Type getType()
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
		return this.autolink;
	}

	/**
	 * @see wicket.markup.parser.XmlTag#isClose()
	 * @return True if this tag is a close tag
	 */
	public final boolean isClose()
	{
		return xmlTag.isClose();
	}

	/**
	 * @see wicket.markup.parser.XmlTag#isOpen()
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
	 * @see wicket.markup.parser.XmlTag#isOpen()
	 */
	public final boolean isOpen(String id)
	{
		return xmlTag.isOpen() && this.id.equals(id);
	}

	/**
	 * @see wicket.markup.parser.XmlTag#isOpenClose()
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
	 * @see wicket.markup.parser.XmlTag#isOpenClose()
	 */
	public final boolean isOpenClose(String id)
	{
		return xmlTag.isOpenClose() && this.id.equals(id);
	}

	/**
	 * Compare tag name including namespace
	 * 
	 * @param tag
	 * @return true if name and namespace are equal
	 */
	public boolean hasEqualTagName(final ComponentTag tag)
	{
		return xmlTag.hasEqualTagName(tag.getXmlTag());
	}

	/**
	 * Makes this tag object immutable by making the attribute map unmodifiable.
	 * Immutable tags cannot be made mutable again. They can only be copied into
	 * new mutable tag objects.
	 */
	public final void makeImmutable()
	{
		xmlTag.makeImmutable();
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
			final ComponentTag tag = new ComponentTag(xmlTag.mutable());
			tag.id = id;
			tag.setMarkupClass(this.markupClass);
			tag.setHasNoCloseTag(this.hasNoCloseTag);
			tag.setPath(this.path);
			return tag;
		}
	}

	/**
	 * @see wicket.markup.parser.XmlTag#put(String, boolean)
	 * @param key
	 *            The key
	 * @param value
	 *            The value
	 */
	public final void put(String key, boolean value)
	{
		xmlTag.put(key, value);
	}

	/**
	 * @see wicket.markup.parser.XmlTag#put(String, int)
	 * @param key
	 *            The key
	 * @param value
	 *            The value
	 */
	public final void put(String key, int value)
	{
		xmlTag.put(key, value);
	}

	/**
	 * @see wicket.markup.parser.XmlTag#put(String, String)
	 * @param key
	 *            The key
	 * @param value
	 *            The value
	 */
	public final void put(String key, CharSequence value)
	{
		xmlTag.put(key, value);
	}

	/**
	 * @see wicket.markup.parser.XmlTag#put(String, StringValue)
	 * @param key
	 *            The key
	 * @param value
	 *            The value
	 */
	public final void put(String key, StringValue value)
	{
		xmlTag.put(key, value);
	}

	/**
	 * @see wicket.markup.parser.XmlTag#putAll(Map)
	 * @param map
	 *            a key/value map
	 */
	public final void putAll(final Map map)
	{
		xmlTag.putAll(map);
	}

	/**
	 * @see wicket.markup.parser.XmlTag#remove(String)
	 * @param key
	 *            The key to remove
	 */
	public final void remove(String key)
	{
		xmlTag.remove(key);
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
	 * Set the component's id. The value is usually taken from the tag's id
	 * attribute, e.g. wicket:id="componentId".
	 * 
	 * @param id
	 *            The component's id assigned to the tag.
	 */
	public final void setId(final String id)
	{
		this.id = id;
	}

	/**
	 * @param flag
	 *            True if the tag has not wicket namespace and no wicket:id.
	 *            E.g. &lt;head&gt; or &lt;body&gt;
	 */
	public void setInternalTag(final boolean flag)
	{
		this.internalTag = flag;
	}

	/**
	 * @see wicket.markup.parser.XmlTag#setName(String)
	 * @param name
	 *            New tag name
	 */
	public final void setName(String name)
	{
		xmlTag.setName(name);
	}

	/**
	 * @see wicket.markup.parser.XmlTag#setNamespace(String)
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
		this.closes = tag;
		getXmlTag().setOpenTag(tag.getXmlTag());
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * @param type
	 *            The new type
	 */
	public final void setType(final Type type)
	{
		xmlTag.setType(type);
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
			buf.append(getNamespace()).append(":");
		}
		buf.append(getName()).append(">");

		return buf;
	}
	
	/**
	 * @see wicket.markup.MarkupElement#toCharSequence()
	 */
	public CharSequence toCharSequence()
	{
		return xmlTag.toCharSequence();
	}

	/**
	 * Converts this object to a string representation.
	 * 
	 * @return String version of this object
	 */
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

		if (getType() == XmlTag.CLOSE)
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
			final Iterator iterator = getAttributes().keySet().iterator();
			while (iterator.hasNext())
			{
				final String key = (String)iterator.next();
				if (key == null)
				{
					continue;
				}
				
				if ((namespacePrefix == null) || (key.startsWith(namespacePrefix) == false))
				{
					response.write(" ");
					response.write(key);
					CharSequence value = getString(key);

					// attributes without values are possible, e.g. 'disabled'
					if (value != null)
					{
						response.write("=\"");
						value = Strings.replaceAll(value, "\"", "\\\"");
						response.write(value);
						response.write("\"");
					}
				}
			}
		}

		if (getType() == XmlTag.OPEN_CLOSE)
		{
			response.write("/");
		}

		response.write(">");
	}

	/**
	 * Converts this object to a string representation including useful
	 * information for debugging
	 * 
	 * @return String version of this object
	 */
	public final String toUserDebugString()
	{
		return xmlTag.toUserDebugString();
	}

	/**
	 * @return Returns the underlying xml tag.
	 */
	final XmlTag getXmlTag()
	{
		return xmlTag;
	}

	/**
	 * Manually mark the ComponentTag being modified. Flagging the tag being
	 * modified does not happen automatically.
	 * 
	 * @param modified
	 */
	public final void setModified(final boolean modified)
	{
		this.modified = modified;
	}

	/**
	 * 
	 * @return True, if the component tag has been marked modified
	 */
	public final boolean isModified()
	{
		return this.modified;
	}

	/**
	 * Gets the component path of wicket elements
	 * 
	 * @return path
	 */
	public String getPath()
	{
		return path;
	}

	/**
	 * Sets the component path of wicket elements
	 * 
	 * @param path
	 *            path
	 */
	void setPath(final String path)
	{
		this.path = path;
	}

	/**
	 * 
	 * @return True if the HTML tag (e.g. br) has no close tag
	 */
	public boolean hasNoCloseTag()
	{
		return hasNoCloseTag;
	}

	/**
	 * True if the HTML tag (e.g. br) has no close tag
	 * 
	 * @param hasNoCloseTag 
	 */
	public void setHasNoCloseTag(boolean hasNoCloseTag)
	{
		this.hasNoCloseTag = hasNoCloseTag;
	}

	/**
	 * In case of inherited markup, the base and the extended markups are merged
	 * and the information about the tags origin is lost. In some cases like
	 * wicket:head and wicket:link this information however is required.
	 * 
	 * @return wicketHeaderClass
	 */
	public Class getMarkupClass()
	{
		return markupClass;
	}

	/**
	 * Set the class of wicket component which contains the wicket:head tag.
	 * 
	 * @param wicketHeaderClass
	 *            wicketHeaderClass
	 */
	public void setMarkupClass(Class wicketHeaderClass)
	{
		this.markupClass = wicketHeaderClass;
	}
}
