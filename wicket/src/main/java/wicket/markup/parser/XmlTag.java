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
package wicket.markup.parser;

import java.util.Iterator;
import java.util.Map;

import wicket.markup.MarkupElement;
import wicket.util.lang.EnumeratedType;
import wicket.util.string.AppendingStringBuffer;
import wicket.util.string.StringValue;
import wicket.util.string.Strings;
import wicket.util.value.AttributeMap;

/**
 * A subclass of MarkupElement which represents a tag including namespace and
 * its optional attributes. XmlTags are returned by the XML parser.
 * 
 * @author Jonathan Locke
 */
public class XmlTag extends MarkupElement
{
	/** A close tag, like &lt;/TAG&gt;. */
	public static final Type CLOSE = new Type("CLOSE");
	
	/** An open tag, like &lt;TAG componentId = "xyz"&gt;. */
	public static final Type OPEN = new Type("OPEN");

	/** An open/close tag, like &lt;TAG componentId = "xyz"/&gt;. */
	public static final Type OPEN_CLOSE = new Type("OPEN_CLOSE");

	/** Attribute map. */
	private AttributeMap attributes;

	/** Column number. */
	int columnNumber;

	/** Length of this tag in characters. */
	int length;

	/** Line number. */
	int lineNumber;

	/** Name of tag, such as "img" or "input". */
	String name;

	/** Namespace of the tag, if available, such as &lt;wicket:link ...&gt; */
	String namespace;

	/** Position of this tag in the input that was parsed. */
	int pos;

	/** Full text of tag. */
	String text;

	/** The tag type (OPEN, CLOSE or OPEN_CLOSE). */
	Type type;

	/** Any component tag that this tag closes. */
	private XmlTag closes;

	/** If mutable, the immutable tag that this tag is a mutable copy of. */
	private XmlTag copyOf = this;

	/** True if this tag is mutable, false otherwise. */
	private boolean isMutable = true;

	/** True if the name of this tag was changed. */
	private boolean nameChanged = false;

	/**
	 * Enumerated type for different kinds of component tags.
	 */
	public static final class Type extends EnumeratedType
	{
		private static final long serialVersionUID = 1L;
		/**
		 * Construct.
		 * 
		 * @param name
		 *			  name of type
		 */
		Type(final String name)
		{
			super(name);
		}
	}

	/**
	 * Construct.
	 */
	public XmlTag()
	{
		super();
	}

	/**
	 * Gets whether this tag closes the provided open tag.
	 * 
	 * @param open
	 *			  The open tag
	 * @return True if this tag closes the given open tag
	 */
	public final boolean closes(final XmlTag open)
	{
		return (closes == open) || (closes == open.copyOf);
	}

	/**
	 * Gets a hashmap of this tag's attributes.
	 * 
	 * @return The tag's attributes
	 */
	public AttributeMap getAttributes()
	{
		if (attributes == null)
		{
			if (copyOf == this)
			{
				attributes = new AttributeMap();
			}
			else
			{
				attributes = new AttributeMap(copyOf.attributes);
			}
		}
		return attributes;
	}

	/**
	 * Get the column number.
	 * 
	 * @return Returns the columnNumber.
	 */
	public int getColumnNumber()
	{
		return columnNumber;
	}

	/**
	 * Gets the length of the tag in characters.
	 * 
	 * @return The tag's length
	 */
	public int getLength()
	{
		return length;
	}

	/**
	 * Get the line number.
	 * 
	 * @return Returns the lineNumber.
	 */
	public int getLineNumber()
	{
		return lineNumber;
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
	 * Get whether the name of this component tag was changed.
	 * 
	 * @return Returns true if the name of this component tag was changed
	 */
	public boolean getNameChanged()
	{
		return nameChanged;
	}

	/**
	 * Namespace of the tag, if available.	For example, &lt;wicket:link&gt;.
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
		return pos;
	}

	/**
	 * Get a string attribute.
	 * 
	 * @param key
	 *			  The key
	 * @return The string value
	 */
	public CharSequence getString(final String key)
	{
		return getAttributes().getCharSequence(key);
	}

	/**
	 * Get the tag type.
	 * 
	 * @return the tag type (OPEN, CLOSE or OPEN_CLOSE).
	 */
	public Type getType()
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
		return type == CLOSE;
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
		return type == OPEN;
	}

	/**
	 * Gets whether this tag is an open/ close tag.
	 * 
	 * @return True if this tag is an open and a close tag
	 */
	public boolean isOpenClose()
	{
		return type == OPEN_CLOSE;
	}

	/**
	 * Compare tag name including namespace
	 * 
	 * @param tag
	 * @return true if name and namespace are equal 
	 */
	public boolean hasEqualTagName(final XmlTag tag)
	{
		if (!getName().equalsIgnoreCase(tag.getName()))
		{
			return false;
		}
		
		if ((getNamespace() == null) && (tag.getNamespace() == null))
		{
			return true;
		}
		
		if ((getNamespace() != null) && (tag.getNamespace() != null))
		{
			return getNamespace().equalsIgnoreCase(tag.getNamespace());
		}
		
		return false;
	}
	
	/**
	 * Makes this tag object immutable by making the attribute map unmodifiable.
	 * Immutable tags cannot be made mutable again. They can only be copied into
	 * new mutable tag objects.
	 */
	public void makeImmutable()
	{
		if (isMutable)
		{
			isMutable = false;
			if (attributes != null)
			{
				attributes.makeImmutable();
			}
		}
	}

	/**
	 * Gets this tag if it is already mutable, or a mutable copy of this tag if
	 * it is immutable.
	 * 
	 * @return This tag if it is already mutable, or a mutable copy of this tag
	 *		   if it is immutable.
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

			tag.namespace = namespace;
			tag.name = name;
			tag.pos = pos;
			tag.length = length;
			tag.text = text;
			tag.type = type;
			tag.isMutable = true;
			tag.closes = closes;
			tag.copyOf = copyOf;

			return tag;
		}
	}

	/**
	 * Puts a boolean attribute.
	 * 
	 * @param key
	 *			  The key
	 * @param value
	 *			  The value
	 * @return previous value associated with specified key, or null if there
	 *		   was no mapping for key. A null return can also indicate that the
	 *		   map previously associated null with the specified key, if the
	 *		   implementation supports null values.
	 */
	public Object put(final String key, final boolean value)
	{
		return put(key, Boolean.toString(value));
	}

	/**
	 * Puts an int attribute.
	 * 
	 * @param key
	 *			  The key
	 * @param value
	 *			  The value
	 * @return previous value associated with specified key, or null if there
	 *		   was no mapping for key. A null return can also indicate that the
	 *		   map previously associated null with the specified key, if the
	 *		   implementation supports null values.
	 */
	public Object put(final String key, final int value)
	{
		return put(key, Integer.toString(value));
	}

	/**
	 * Puts a string attribute.
	 * 
	 * @param key
	 *			  The key
	 * @param value
	 *			  The value
	 * @return previous value associated with specified key, or null if there
	 *		   was no mapping for key. A null return can also indicate that the
	 *		   map previously associated null with the specified key, if the
	 *		   implementation supports null values.
	 */
	public Object put(final String key, final CharSequence value)
	{
		return getAttributes().put(key, value);
	}

	/**
	 * Puts a {@link StringValue}attribute.
	 * 
	 * @param key
	 *			  The key
	 * @param value
	 *			  The value
	 * @return previous value associated with specified key, or null if there
	 *		   was no mapping for key. A null return can also indicate that the
	 *		   map previously associated null with the specified key, if the
	 *		   implementation supports null values.
	 */
	public Object put(final String key, final StringValue value)
	{
		return getAttributes().put(key, (value != null) ? value.toString() : null);
	}

	/**
	 * Puts all attributes in map
	 * 
	 * @param map
	 *			  A key/value map
	 */
	public void putAll(final Map map)
	{
		for (final Iterator iterator = map.keySet().iterator(); iterator.hasNext(); )
		{
			final String key = (String)iterator.next();
			Object value = map.get(key);
			put(key, (value != null) ? value.toString() : null);
		}
	}

	/**
	 * Removes an attribute.
	 * 
	 * @param key
	 *			  The key to remove
	 */
	public void remove(final String key)
	{
		getAttributes().remove(key);
	}

	/**
	 * Sets the tag name.
	 * 
	 * @param name
	 *			  New tag name
	 */
	public void setName(final String name)
	{
		if (isMutable)
		{
			this.name = name;
			this.nameChanged = true;
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
	 *			  New tag name
	 */
	public void setNamespace(final String namespace)
	{
		if (isMutable)
		{
			this.namespace = namespace;
			this.nameChanged = true;
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
	 *			  the open-tag
	 * @throws RuntimeException
	 *			   if 'this' is not a close tag
	 */
	public void setOpenTag(final XmlTag tag)
	{
		this.closes = tag;
	}

	/**
	 * Sets type of this tag if it is not immutable.
	 * 
	 * @param type
	 *			  The new type
	 */
	public void setType(final Type type)
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
		return "[Tag name = " + name + ", pos = " + pos + ", line = " + lineNumber + ", length = "
				+ length + ", attributes = [" + getAttributes() + "], type = " + type + "]";
	}

	/**
	 * Converts this object to a string representation.
	 * 
	 * @return String version of this object
	 */
	public String toString()
	{
		return toCharSequence().toString();
	}
	
	/**
	 * @see wicket.markup.MarkupElement#toCharSequence()
	 */
	public CharSequence toCharSequence()
	{
		if (!isMutable && (text != null))
		{
			return text;
		}

		return toXmlString(null);
	}

	/**
	 * Converts this object to a string representation.
	 * 
	 * @return String version of this object
	 */
	public String toUserDebugString()
	{
		return "'" + toString() + "' (line " + lineNumber + ", column " + columnNumber + ")";
	}

	/**
	 * Assuming some attributes have been changed, toXmlString() rebuilds the
	 * String on based on the tags informations.
	 * 
	 * @param attributeToBeIgnored	
	 * @return A xml string matching the tag
	 */
	public CharSequence toXmlString(final String attributeToBeIgnored)
	{
		final AppendingStringBuffer buffer = new AppendingStringBuffer();

		buffer.append('<');

		if (type == CLOSE)
		{
			buffer.append('/');
		}

		if (namespace != null)
		{
			buffer.append(namespace);
			buffer.append(':');
		}

		buffer.append(name);

		final AttributeMap attributes = getAttributes();
		if (attributes.size() > 0)
		{
			final Iterator iterator = attributes.keySet().iterator();
			for (; iterator.hasNext();)
			{
				final String key = (String)iterator.next();
				if ((key != null) && ((attributeToBeIgnored == null) || 
						!key.equalsIgnoreCase(attributeToBeIgnored)))
				{
					buffer.append(" ");
					buffer.append(key);
					CharSequence value = getString(key);
					
					// Attributes without values are possible, e.g. 'disabled'
					if (value != null) 
					{
						buffer.append("=\"");
						value = Strings.replaceAll(value,"\"", "\\\"");
						buffer.append(value);
						buffer.append("\"");
					}
				}
			}
		}

		if (type == OPEN_CLOSE)
		{
			buffer.append('/');
		}

		buffer.append('>');

		return buffer;
	}
}
