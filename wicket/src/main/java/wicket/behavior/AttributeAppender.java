/*
 * $Id: AttributeAppender.java 5791 2006-05-20 00:32:57 +0000 (Sat, 20 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-20 00:32:57 +0000 (Sat, 20 May
 * 2006) $
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
package wicket.behavior;

import wicket.AttributeModifier;
import wicket.model.IModel;
import wicket.util.string.AppendingStringBuffer;
import wicket.util.string.Strings;

/**
 * AttributeModifier that appends the given value, rather than replace it. This
 * is especially useful for adding CSS classes to markup elements, or adding
 * JavaScript snippets to existing element handlers.
 * 
 * <pre>
 *        &lt;a href=&quot;#&quot; wicket:id=&quot;foo&quot; class=&quot;link&quot; onmouseover=&quot;doSomething()&quot;&gt;
 * </pre>
 * 
 * can be modified with these AttributeAppenders:
 * 
 * <pre>
 * link.add(new AttributeAppender(&quot;class&quot;, new Model(&quot;hot&quot;), &quot; &quot;));
 * link.add(new AttributeAppender(&quot;onmouseover&quot;, new Model(&quot;foo();return false;&quot;), &quot;;&quot;));
 * </pre>
 * 
 * this will result in the following markup:
 * 
 * <pre>
 *        &lt;a href=&quot;#&quot; wicket:id=&quot;foo&quot; class=&quot;link hot&quot; onmouseover=&quot;doSomething();foo();return false;&quot;&gt;
 * </pre>
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
	 * Creates an AttributeModifier that appends the appendModel's value to the
	 * current value of the attribute, and will add the attribute when
	 * addAttributeIfNotPresent is true.
	 * 
	 * @param attribute
	 *            the attribute to append the appendModels value to
	 * @param addAttributeIfNotPresent
	 *            when true, adds the attribute to the tag
	 * @param appendModel
	 *            the model supplying the value to append
	 * @param separator
	 *            the separator string, comes between the original value and the
	 *            append value
	 */
	public AttributeAppender(String attribute, boolean addAttributeIfNotPresent,
			IModel<String> appendModel, String separator)
	{
		super(attribute, addAttributeIfNotPresent, appendModel);
		this.separator = separator;
	}

	/**
	 * Creates an AttributeModifier that appends the appendModel's value to the
	 * current value of the attribute, and will add the attribute when it is not
	 * there already.
	 * 
	 * @param attribute
	 *            the attribute to append the appendModels value to
	 * @param appendModel
	 *            the model supplying the value to append
	 * @param separator
	 *            the separator string, comes between the original value and the
	 *            append value
	 */
	public AttributeAppender(String attribute, IModel<String> appendModel, String separator)
	{
		super(attribute, true, appendModel);
		this.separator = separator;
	}

	/**
	 * @see wicket.AttributeModifier#newValue(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	protected String newValue(String currentValue, String appendValue)
	{
		final int appendValueLen = (appendValue == null) ? 0 : appendValue.length();

		final AppendingStringBuffer sb;
		if (currentValue == null)
		{
			sb = new AppendingStringBuffer(appendValueLen + separator.length());
		}
		else
		{
			sb = new AppendingStringBuffer(currentValue.length() + appendValueLen
					+ separator.length());
			sb.append(currentValue);
		}

		// if the current value or the append value is empty, the separator is
		// not needed.
		if (!Strings.isEmpty(currentValue) && !Strings.isEmpty(appendValue))
		{
			sb.append(separator);
		}

		// only append the value when it is not empty.
		if (!Strings.isEmpty(appendValue))
		{
			sb.append(appendValue);
		}
		return sb.toString();
	}
}
