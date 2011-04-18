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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.AppendingStringBuffer;
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
 * link.add(new AttributeAppender(&quot;class&quot;, new Model&lt;String&gt;(&quot;hot&quot;), &quot; &quot;));
 * link.add(new AttributeAppender(&quot;onmouseover&quot;, new Model&lt;String&gt;(&quot;foo();return false;&quot;), &quot;;&quot;));
 * </pre>
 * 
 * this will result in the following markup:
 * 
 * <pre>
 *     &lt;a href=&quot;#&quot; wicket:id=&quot;foo&quot; class=&quot;link hot&quot; onmouseover=&quot;doSomething();foo();return false;&quot;&gt;
 * </pre>
 * 
 * AttributeAppenders can also be instructed to prepend the given value:
 * 
 * <pre>
 * link.add(new AttributeAppender(&quot;class&quot;, new Model&lt;String&gt;(&quot;hot&quot;), &quot; &quot;, true));
 * link.add(new AttributeAppender(&quot;onmouseover&quot;, new Model&lt;String&gt;(&quot;foo();return false;&quot;), &quot;;&quot;, true));
 * </pre>
 * 
 * this will result in the following markup:
 * 
 * <pre>
 *     &lt;a href=&quot;#&quot; wicket:id=&quot;foo&quot; class=&quot;hot link&quot; onmouseover=&quot;foo();return false;doSomething();&quot;&gt;
 * </pre>
 * 
 * This is useful for instance to add a Javascript confirmation dialog before performing an action.
 * 
 * @author Martijn Dashorst
 */
public class AttributeAppender extends AttributeModifier
{
	/** For serialization. */
	private static final long serialVersionUID = 1L;

	/**
	 * Separates the existing attribute value and the append value.
	 */
	private final String separator;

	/**
	 * A flag indicating whether the new attribute value will be prepended
	 */
	private final boolean prepend;

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
	 */
	public AttributeAppender(String attribute, boolean addAttributeIfNotPresent,
		IModel<?> appendModel, String separator)
	{
		this(attribute, addAttributeIfNotPresent, appendModel, separator, false);
	}

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
	 * @param prepend
	 *            if true, the attribute modifier will prepend the attribute with the appendModel
	 */
	public AttributeAppender(String attribute, boolean addAttributeIfNotPresent,
		IModel<?> appendModel, String separator, boolean prepend)
	{
		super(attribute, addAttributeIfNotPresent, appendModel);
		this.separator = separator;
		this.prepend = prepend;
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
		this(attribute, true, appendModel, separator, false);
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
	 * @param prepend
	 *            if true, the attribute modifier will prepend the attribute with the appendModel
	 */
	public AttributeAppender(String attribute, IModel<?> appendModel, String separator,
		boolean prepend)
	{
		this(attribute, true, appendModel, separator, prepend);
	}


	/**
	 * @see org.apache.wicket.AttributeModifier#newValue(java.lang.String, java.lang.String)
	 */
	@Override
	protected String newValue(String currentValue, String appendValue)
	{
		// Shortcut for empty values
		if (Strings.isEmpty(currentValue))
		{
			return appendValue != null ? appendValue : "";
		}
		else if (Strings.isEmpty(appendValue))
		{
			return currentValue;
		}

		final AppendingStringBuffer sb = new AppendingStringBuffer(currentValue.length() +
			appendValue.length() + separator.length());

		if (prepend)
		{
			sb.append(appendValue);
			sb.append(separator);
			sb.append(currentValue);
		}
		else
		{
			sb.append(currentValue);
			sb.append(separator);
			sb.append(appendValue);
		}
		return sb.toString();
	}
}
