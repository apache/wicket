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
package org.apache.wicket.behavior;

import java.io.Serializable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;

/**
 * AttributeModifier that appends the given value, rather than replace it. This is especially useful
 * for adding CSS classes to markup elements, or adding JavaScript snippets to existing element
 * handlers.
 * 
 * <pre>
 *     &lt;a href=&quot;#&quot; wicket:id=&quot;foo&quot; class=&quot;link&quot; onmouseover=&quot;doSomething()&quot;&gt;
 * </pre>
 * 
 * can be modified with these AttributeAppenders:
 * 
 * <pre>
 * link.add(new AttributeAppender(&quot;class&quot;, Model.of(&quot;hot&quot;)));
 * link.add(new AttributeAppender(&quot;onmouseover&quot;, Model.of(&quot;foo();return false;&quot;)).setSeparator(&quot;;&quot;));
 * </pre>
 * 
 * this will result in the following markup:
 * 
 * <pre>
 *     &lt;a href=&quot;#&quot; wicket:id=&quot;foo&quot; class=&quot;link hot&quot; onmouseover=&quot;doSomething();foo();return false;&quot;&gt;
 * </pre>
 * 
 * @see AttributeModifier#append(String, IModel)
 * @see AttributeModifier#append(String, Serializable)
 * @see AttributeModifier#prepend(String, IModel)
 * @see AttributeModifier#prepend(String, Serializable)
 * 
 * @author Martijn Dashorst
 */
public class AttributeAppender extends AttributeModifier
{
	private static final long serialVersionUID = 1L;

	/**
	 * Separator between the current value and the concatenated value, typically a space ' ' or
	 * colon ';'.
	 */
	private String separator;

	/**
	 * Creates an AttributeModifier that appends the appendModel's value to the current value of the
	 * attribute, and will add the attribute when addAttributeIfNotPresent is true.
	 * 
	 * @param attribute
	 *            the attribute to append the appendModels value to
	 * @param addAttributeIfNotPresent
	 *            when true, adds the attribute to the tag
	 * @param appendModel
	 *            the model supplying the value to append
	 * @param separator
	 *            the separator string, comes between the original value and the append value
	 * @deprecated use {@link #AttributeAppender(String, IModel)} instead.
	 */
	@Deprecated
	public AttributeAppender(String attribute, boolean addAttributeIfNotPresent,
		IModel<?> appendModel, String separator)
	{
		this(attribute, appendModel, separator);
	}

	/**
	 * Creates an attribute modifier that concatenates the {@code replaceModel} to the attribute's
	 * current value, optionally separated by the {@link #getSeparator() separator}.
	 * 
	 * @param attribute
	 * @param replaceModel
	 */
	public AttributeAppender(String attribute, IModel<?> replaceModel)
	{
		super(attribute, replaceModel);
	}

	/**
	 * Creates an attribute modifier that appends the {@code value} to the attribute's current
	 * value, optionally separated by the {@link #getSeparator() separator}.
	 * 
	 * @param attribute
	 * @param value
	 */
	public AttributeAppender(String attribute, Serializable value)
	{
		super(attribute, value);
	}

	/**
	 * Creates an AttributeModifier that appends the value to the current value of the
	 * attribute, and will add the attribute when it is not there already.
	 *
	 * @param attribute
	 *            the attribute to append the appendModels value to
	 * @param value
	 *            the value to append
	 * @param separator
	 *            the separator string, comes between the original value and the append value
	 */
	public AttributeAppender(String attribute, Serializable value, String separator)
	{
		super(attribute, value);
		setSeparator(separator);
	}

	/**
	 * Creates an AttributeModifier that appends the appendModel's value to the current value of the
	 * attribute, and will add the attribute when it is not there already.
	 * 
	 * @param attribute
	 *            the attribute to append the appendModels value to
	 * @param appendModel
	 *            the model supplying the value to append
	 * @param separator
	 *            the separator string, comes between the original value and the append value
	 */
	public AttributeAppender(String attribute, IModel<?> appendModel, String separator)
	{
		super(attribute, appendModel);
		setSeparator(separator);
	}

	/**
	 * Gets the separator used by attribute appenders and prependers.
	 * 
	 * @return the separator used by attribute appenders and prependers.
	 */
	public String getSeparator()
	{
		return separator;
	}

	/**
	 * Sets the separator used by attribute appenders and prependers.
	 * 
	 * @param separator
	 *            a space, semicolon or other character used to separate the current value and the
	 *            appended/prepended value.
	 * @return this
	 */
	public AttributeAppender setSeparator(String separator)
	{
		this.separator = separator;
		return this;
	}

	@Override
	protected String newValue(String currentValue, String appendValue)
	{
		// Short circuit when one of the values is empty: return the other value.
		if (Strings.isEmpty(currentValue))
			return appendValue != null ? appendValue : null;
		else if (Strings.isEmpty(appendValue))
			return currentValue != null ? currentValue : null;

		StringBuilder sb = new StringBuilder(currentValue);
		sb.append((getSeparator() == null ? "" : getSeparator()));
		sb.append(appendValue);
		return sb.toString();
	}

	@Override
	public String toString()
	{
		String attributeModifier = super.toString();
		attributeModifier = attributeModifier.substring(0, attributeModifier.length() - 2) +
			", separator=" + separator + "]";
		return attributeModifier;
	}
}
