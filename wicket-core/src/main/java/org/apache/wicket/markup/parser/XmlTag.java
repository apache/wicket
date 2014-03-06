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
package org.apache.wicket.markup.parser;

import java.util.Iterator;
import java.util.Map;

import org.apache.wicket.markup.parser.IXmlPullParser.HttpTagType;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.value.IValueMap;
import org.apache.wicket.util.value.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A subclass of MarkupElement which represents a tag including namespace and its optional
 * attributes. XmlTags are returned by the XML parser.
 * 
 * @author Jonathan Locke
 */
public class XmlTag
{
	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(XmlTag.class);

	/**
	 * Enumerated type for different kinds of component tags.
	 */
	public static enum TagType {
		/** A close tag, like &lt;/TAG&gt;. */
		CLOSE("CLOSE"),

		/** An open tag, like &lt;TAG componentId = "xyz"&gt;. */
		OPEN("OPEN"),

		/** An open/close tag, like &lt;TAG componentId = "xyz"/&gt;. */
		OPEN_CLOSE("OPEN_CLOSE");

		private String name;

		TagType(final String name)
		{
			this.name = name;
		}
	}

	TextSegment text;

	/** Attribute map. */
	private IValueMap attributes;

	/** Name of tag, such as "img" or "input". */
	String name;

	/** Namespace of the tag, if available, such as &lt;wicket:link ...&gt; */
	String namespace;

	/** The tag type (OPEN, CLOSE or OPEN_CLOSE). */
	TagType type;

	/** Any component tag that this tag closes. */
	private XmlTag closes;

	/** If mutable, the immutable tag that this tag is a mutable copy of. */
	private XmlTag copyOf = this;

	/** True if this tag is mutable, false otherwise. */
	private boolean isMutable = true;

	private HttpTagType httpTagType;

	/**
	 * Construct.
	 */
	public XmlTag()
	{
		super();
	}

	/**
	 * Construct.
	 * 
	 * @param text
	 * @param type
	 */
	public XmlTag(final TextSegment text, final TagType type)
	{
		this.text = text;
		this.type = type;
	}

	/**
	 * Gets whether this tag closes the provided open tag.
	 * 
	 * @param open
	 *            The open tag
	 * @return True if this tag closes the given open tag
	 */
	public final boolean closes(final XmlTag open)
	{
		return (closes == open) || ((closes == open.copyOf) && (this != open));
	}

	/**
	 * @param element
	 * @return true, if namespace, name and attributes are the same
	 */
	public final boolean equalTo(final XmlTag element)
	{
		final XmlTag that = element;
		if (!Objects.equal(getNamespace(), that.getNamespace()))
		{
			return false;
		}
		if (!getName().equals(that.getName()))
		{
			return false;
		}
		return getAttributes().equals(that.getAttributes());
	}

	/**
	 * Gets a hashmap of this tag's attributes.
	 * 
	 * @return The tag's attributes
	 */
	public IValueMap getAttributes()
	{
		if (attributes == null)
		{
			if ((copyOf == this) || (copyOf == null) || (copyOf.attributes == null))
			{
				attributes = new ValueMap();
			}
			else
			{
				attributes = new ValueMap(copyOf.attributes);
			}
		}
		return attributes;
	}

	/**
	 * @return true if there 1 or more attributes.
	 */
	public boolean hasAttributes()
	{
		return attributes != null && attributes.size() > 0;
	}

	/**
	 * Get the column number.
	 * 
	 * @return Returns the columnNumber.
	 */
	public int getColumnNumber()
	{
		return (text != null ? text.columnNumber : 0);
	}

	/**
	 * Gets the length of the tag in characters.
	 * 
	 * @return The tag's length
	 */
	public int getLength()
	{
		return (text != null ? text.text.length() : 0);
	}

	/**
	 * Get the line number.
	 * 
	 * @return Returns the lineNumber.
	 */
	public int getLineNumber()
	{
		return (text != null ? text.lineNumber : 0);
	}

	/**
	 * Gets the name of the tag, for example the tag <code>&lt;b&gt;</code>'s name would be 'b'.
	 * 
	 * @return The tag's name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Namespace of the tag, if available. For example, &lt;wicket:link&gt;.
	 * 
	 * @return The tag's namespace
	 */
	public String getNamespace()
	{
		return namespace;
	}

	/**
	 * Assuming this is a close tag, return the corresponding open tag
	 * 
	 * @return The open tag. Null, if no open tag available
	 */
	public final XmlTag getOpenTag()
	{
		return closes;
	}

	/**
	 * Gets the location of the tag in the input string.
	 * 
	 * @return Tag location (index in input string)
	 */
	public int getPos()
	{
		return (text != null ? text.pos : 0);
	}

	/**
	 * Get a string attribute.
	 * 
	 * @param key
	 *            The key
	 * @return The string value
	 */
	public CharSequence getAttribute(final String key)
	{
		return getAttributes().getCharSequence(key);
	}

	/**
	 * Get the tag type.
	 * 
	 * @return the tag type (OPEN, CLOSE or OPEN_CLOSE).
	 */
	public TagType getType()
	{
		return type;
	}

	/**
	 * Gets whether this is a close tag.
	 * 
	 * @return True if this tag is a close tag
	 */
	public boolean isClose()
	{
		return type == TagType.CLOSE;
	}

	/**
	 * 
	 * @return True, if tag is mutable
	 */
	public final boolean isMutable()
	{
		return isMutable;
	}

	/**
	 * Gets whether this is an open tag.
	 * 
	 * @return True if this tag is an open tag
	 */
	public boolean isOpen()
	{
		return type == TagType.OPEN;
	}

	/**
	 * Gets whether this tag is an open/ close tag.
	 * 
	 * @return True if this tag is an open and a close tag
	 */
	public boolean isOpenClose()
	{
		return type == TagType.OPEN_CLOSE;
	}

	/**
	 * Makes this tag object immutable by making the attribute map unmodifiable. Immutable tags
	 * cannot be made mutable again. They can only be copied into new mutable tag objects.
	 * 
	 * @return this
	 */
	public XmlTag makeImmutable()
	{
		if (isMutable)
		{
			isMutable = false;
			if (attributes != null)
			{
				attributes.makeImmutable();
				text = null;
			}
		}
		return this;
	}

	/**
	 * Gets this tag if it is already mutable, or a mutable copy of this tag if it is immutable.
	 * 
	 * @return This tag if it is already mutable, or a mutable copy of this tag if it is immutable.
	 */
	public XmlTag mutable()
	{
		if (isMutable)
		{
			return this;
		}
		else
		{
			final XmlTag tag = new XmlTag();
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
	void copyPropertiesTo(final XmlTag dest)
	{
		dest.namespace = namespace;
		dest.name = name;
		dest.text = text;
		dest.type = type;
		dest.isMutable = true;
		dest.closes = closes;
		dest.copyOf = copyOf;
		if (attributes != null)
		{
			dest.attributes = new ValueMap(attributes);
		}
	}

	/**
	 * Puts a boolean attribute.
	 * 
	 * @param key
	 *            The key
	 * @param value
	 *            The value
	 * @return previous value associated with specified key, or null if there was no mapping for
	 *         key. A null return can also indicate that the map previously associated null with the
	 *         specified key, if the implementation supports null values.
	 */
	public Object put(final String key, final boolean value)
	{
		return put(key, Boolean.toString(value));
	}

	/**
	 * Puts an int attribute.
	 * 
	 * @param key
	 *            The key
	 * @param value
	 *            The value
	 * @return previous value associated with specified key, or null if there was no mapping for
	 *         key. A null return can also indicate that the map previously associated null with the
	 *         specified key, if the implementation supports null values.
	 */
	public Object put(final String key, final int value)
	{
		return put(key, Integer.toString(value));
	}

	/**
	 * Puts a string attribute.
	 * 
	 * @param key
	 *            The key
	 * @param value
	 *            The value
	 * @return previous value associated with specified key, or null if there was no mapping for
	 *         key. A null return can also indicate that the map previously associated null with the
	 *         specified key, if the implementation supports null values.
	 */
	public Object put(final String key, final CharSequence value)
	{
		return getAttributes().put(key, value);
	}

	/**
	 * Puts a {@link StringValue}attribute.
	 * 
	 * @param key
	 *            The key
	 * @param value
	 *            The value
	 * @return previous value associated with specified key, or null if there was no mapping for
	 *         key. A null return can also indicate that the map previously associated null with the
	 *         specified key, if the implementation supports null values.
	 */
	public Object put(final String key, final StringValue value)
	{
		return getAttributes().put(key, (value != null) ? value.toString() : null);
	}

	/**
	 * Puts all attributes in map
	 * 
	 * @param map
	 *            A key/value map
	 */
	public void putAll(final Map<String, Object> map)
	{
		for (final Map.Entry<String, Object> entry : map.entrySet())
		{
			Object value = entry.getValue();
			put(entry.getKey(), (value != null) ? value.toString() : null);
		}
	}

	/**
	 * Removes an attribute.
	 * 
	 * @param key
	 *            The key to remove
	 */
	public void remove(final String key)
	{
		getAttributes().remove(key);
	}

	/**
	 * Sets the tag name.
	 * 
	 * @param name
	 *            New tag name
	 */
	public void setName(final String name)
	{
		if (isMutable)
		{
			this.name = name.intern();
		}
		else
		{
			throw new UnsupportedOperationException("Attempt to set name of immutable tag");
		}
	}

	/**
	 * Sets the tag namespace.
	 * 
	 * @param namespace
	 *            New tag name
	 */
	public void setNamespace(final String namespace)
	{
		if (isMutable)
		{
			this.namespace = namespace.intern();
		}
		else
		{
			throw new UnsupportedOperationException("Attempt to set namespace of immutable tag");
		}
	}

	/**
	 * Assuming this is a close tag, assign it's corresponding open tag.
	 * 
	 * @param tag
	 *            the open-tag
	 * @throws RuntimeException
	 *             if 'this' is not a close tag
	 */
	public void setOpenTag(final XmlTag tag)
	{
		closes = tag;
	}

	/**
	 * Sets type of this tag if it is not immutable.
	 * 
	 * @param type
	 *            The new type
	 */
	public void setType(final TagType type)
	{
		if (isMutable)
		{
			this.type = type;
		}
		else
		{
			throw new UnsupportedOperationException("Attempt to set type of immutable tag");
		}
	}

	/**
	 * Converts this object to a string representation.
	 * 
	 * @return String version of this object
	 */
	public String toDebugString()
	{
		return "[Tag name = " + name + ", pos = " + text.pos + ", line = " + text.lineNumber +
			", attributes = [" + getAttributes() + "], type = " + type + "]";
	}

	/**
	 * Converts this object to a string representation.
	 * 
	 * @return String version of this object
	 */
	@Override
	public String toString()
	{
		return toCharSequence().toString();
	}

	/**
	 * @return The string representation of the tag
	 */
	public CharSequence toCharSequence()
	{
		if (!isMutable && (text != null))
		{
			return text.text;
		}

		return toXmlString(null);
	}

	/**
	 * String representation with line and column number
	 * 
	 * @return String version of this object
	 */
	public String toUserDebugString()
	{
		return " '" + toString() + "' (line " + getLineNumber() + ", column " + getColumnNumber() +
			")";
	}

	/**
	 * Assuming some attributes have been changed, toXmlString() rebuilds the String on based on the
	 * tags informations.
	 * 
	 * @param attributeToBeIgnored
	 * @return A xml string matching the tag
	 */
	public CharSequence toXmlString(final String attributeToBeIgnored)
	{
		final AppendingStringBuffer buffer = new AppendingStringBuffer();

		buffer.append('<');

		if (type == TagType.CLOSE)
		{
			buffer.append('/');
		}

		if (namespace != null)
		{
			buffer.append(namespace);
			buffer.append(':');
		}

		buffer.append(name);

		final IValueMap attributes = getAttributes();
		if (attributes.size() > 0)
		{
			final Iterator<String> iterator = attributes.keySet().iterator();
			for (; iterator.hasNext();)
			{
				final String key = iterator.next();
				if ((key != null) &&
					((attributeToBeIgnored == null) || !key.equalsIgnoreCase(attributeToBeIgnored)))
				{
					buffer.append(" ");
					buffer.append(key);
					CharSequence value = getAttribute(key);

					// Attributes without values are possible, e.g. 'disabled'
					if (value != null)
					{
						buffer.append("=\"");
						value = Strings.escapeMarkup(value);
						buffer.append(value);
						buffer.append("\"");
					}
				}
			}
		}

		if (type == TagType.OPEN_CLOSE)
		{
			buffer.append('/');
		}

		buffer.append('>');
		return buffer;
	}

	static class TextSegment
	{
		/** Column number. */
		final int columnNumber;

		/** Line number. */
		final int lineNumber;

		/** Position of this tag in the input that was parsed. */
		final int pos;

		/** Full text of tag. */
		final CharSequence text;

		TextSegment(final CharSequence text, final int pos, final int line, final int col)
		{
			this.text = text;
			this.pos = pos;
			lineNumber = line;
			columnNumber = col;
		}

		/**
		 * 
		 * @return The xml markup text
		 */
		public final CharSequence getText()
		{
			return text;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return text.toString();
		}
	}
}
