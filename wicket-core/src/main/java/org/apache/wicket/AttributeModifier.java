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
package org.apache.wicket;

import java.io.Serializable;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.parser.XmlTag.TagType;
import org.apache.wicket.model.IComponentAssignedModel;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.value.IValueMap;

/**
 * This class allows a tag attribute of a component to be modified dynamically with a value obtained
 * from a model object. This concept can be used to programmatically alter the attributes of
 * components, overriding the values specified in the markup. The two primary uses of this class are
 * to allow overriding of markup attributes based on business logic and to support dynamic
 * localization. The replacement occurs as the component tag is rendered to the response.
 * <p>
 * The attribute whose value is to be modified must be given on construction of the instance of this
 * class along with the model containing the value to replace with.
 * <p>
 * If an attribute is not in the markup, this modifier will add an attribute.
 * <p>
 * Instances of this class should be added to components via the {@link Component#add(Behavior...)}
 * method after the component has been constructed.
 * <p>
 * It is possible to create new subclasses of {@code AttributeModifier} by overriding the
 * {@link #newValue(String, String)} method. For example, you could create an
 * {@code AttributeModifier} subclass which appends the replacement value like this:
 * 
 * <pre>
 * new AttributeModifier(&quot;myAttribute&quot;, model)
 * {
 * 	protected String newValue(final String currentValue, final String replacementValue)
 * 	{
 * 		return currentValue + replacementValue;
 * 	}
 * };
 * </pre>
 * 
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Jonathan Locke
 * @author Martijn Dashorst
 * @author Ralf Ebert
 */
public class AttributeModifier extends Behavior implements IClusterable
{
	/** Marker value to have an attribute without a value added. */
	public static final String VALUELESS_ATTRIBUTE_ADD = new String("VA_ADD");

	/** Marker value to have an attribute without a value removed. */
	public static final String VALUELESS_ATTRIBUTE_REMOVE = new String("VA_REMOVE");

	private static final long serialVersionUID = 1L;

	/** Attribute specification. */
	private final String attribute;

	/** The model that is to be used for the replacement. */
	private final IModel<?> replaceModel;

	/**
	 * Create a new attribute modifier with the given attribute name and model to replace with. The
	 * attribute will be added with the model value or the value will be replaced with the model
	 * value if the attribute is already present.
	 * 
	 * @param attribute
	 *            The attribute name to replace the value for
	 * @param replaceModel
	 *            The model to replace the value with
	 */
	public AttributeModifier(final String attribute, final IModel<?> replaceModel)
	{
		Args.notNull(attribute, "attribute");

		this.attribute = attribute;
		this.replaceModel = replaceModel;
	}

	/**
	 * Create a new attribute modifier with the given attribute name and model to replace with. The
	 * attribute will be added with the model value or the value will be replaced with the value if
	 * the attribute is already present.
	 * 
	 * @param attribute
	 *            The attribute name to replace the value for
	 * @param value
	 *            The value for the attribute
	 */
	public AttributeModifier(String attribute, Serializable value)
	{
		this(attribute, Model.of(value));
	}

	/**
	 * Detach the value if it was a {@link IDetachable}. Internal method, shouldn't be called from
	 * the outside. If the attribute modifier is shared, the detach method will be called multiple
	 * times.
	 * 
	 * @param component
	 *            the model that initiates the detachment
	 */
	@Override
	public final void detach(Component component)
	{
		if (replaceModel != null)
			replaceModel.detach();
	}

	/**
	 * @return the attribute name to replace the value for
	 */
	public final String getAttribute()
	{
		return attribute;
	}

	@Override
	public final void onComponentTag(Component component, ComponentTag tag)
	{
		if (tag.getType() != TagType.CLOSE)
			replaceAttributeValue(component, tag);
	}

	/**
	 * Checks the given component tag for an instance of the attribute to modify and if all criteria
	 * are met then replace the value of this attribute with the value of the contained model
	 * object.
	 * 
	 * @param component
	 *            The component
	 * @param tag
	 *            The tag to replace the attribute value for
	 */
	public final void replaceAttributeValue(final Component component, final ComponentTag tag)
	{
		if (isEnabled(component))
		{
			final IValueMap attributes = tag.getAttributes();
			final Object replacementValue = getReplacementOrNull(component);

			if (VALUELESS_ATTRIBUTE_ADD == replacementValue)
			{
				attributes.put(attribute, null);
			}
			else if (VALUELESS_ATTRIBUTE_REMOVE == replacementValue)
			{
				attributes.remove(attribute);
			}
			else
			{
				final String value = toStringOrNull(attributes.get(attribute));
				final String newValue = newValue(value, toStringOrNull(replacementValue));
				if (newValue == VALUELESS_ATTRIBUTE_REMOVE)
				{
					attributes.remove(attribute);
				}
				else if (newValue != null)
				{
					attributes.put(attribute, newValue);
				}
			}
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[AttributeModifier attribute=" + attribute + ", replaceModel=" + replaceModel + "]";
	}

	/**
	 * gets replacement with null check.
	 * 
	 * @param component
	 * @return replacement value
	 */
	private Object getReplacementOrNull(final Component component)
	{
		IModel<?> model = replaceModel;
		if (model instanceof IComponentAssignedModel)
		{
			model = ((IComponentAssignedModel<?>)model).wrapOnAssignment(component);
		}
		return (model != null) ? model.getObject() : null;
	}

	/**
	 * gets replacement as a string with null check.
	 * 
	 * @param replacementValue
	 * @return replacement value as a string
	 */
	private String toStringOrNull(final Object replacementValue)
	{
		return (replacementValue != null) ? replacementValue.toString() : null;
	}

	/**
	 * Gets the replacement model. Allows subclasses access to replace model.
	 * 
	 * @return the replace model of this attribute modifier
	 */
	protected final IModel<?> getReplaceModel()
	{
		return replaceModel;
	}

	/**
	 * Gets the value that should replace the current attribute value. This gives users the ultimate
	 * means to customize what will be used as the attribute value. For instance, you might decide
	 * to append the replacement value to the current instead of just replacing it as is Wicket's
	 * default.
	 * 
	 * @param currentValue
	 *            The current attribute value. This value might be null!
	 * @param replacementValue
	 *            The replacement value. This value might be null!
	 * @return The value that should replace the current attribute value
	 */
	protected String newValue(final String currentValue, final String replacementValue)
	{
		return replacementValue;
	}

	/**
	 * Creates a attribute modifier that replaces the current value with the given value.
	 * 
	 * @param attributeName
	 * @param value
	 * @return the attribute modifier
	 * @since 1.5
	 */
	public static AttributeModifier replace(String attributeName, IModel<?> value)
	{
		Args.notEmpty(attributeName, "attributeName");

		return new AttributeModifier(attributeName, value);
	}

	/**
	 * Creates a attribute modifier that replaces the current value with the given value.
	 * 
	 * @param attributeName
	 * @param value
	 * @return the attribute modifier
	 * @since 1.5
	 */
	public static AttributeModifier replace(String attributeName, Serializable value)
	{
		Args.notEmpty(attributeName, "attributeName");

		return new AttributeModifier(attributeName, value);
	}

	/**
	 * Creates a attribute modifier that appends the current value with the given {@code value}
	 * using a default space character (' ') separator.
	 * 
	 * @param attributeName
	 * @param value
	 * @return the attribute modifier
	 * @since 1.5
	 * @see AttributeAppender
	 */
	public static AttributeAppender append(String attributeName, IModel<?> value)
	{
		Args.notEmpty(attributeName, "attributeName");

		return new AttributeAppender(attributeName, value).setSeparator(" ");
	}

	/**
	 * Creates a attribute modifier that appends the current value with the given {@code value}
	 * using a default space character (' ') separator.
	 * 
	 * @param attributeName
	 * @param value
	 * @return the attribute modifier
	 * @since 1.5
	 * @see AttributeAppender
	 */
	public static AttributeAppender append(String attributeName, Serializable value)
	{
		Args.notEmpty(attributeName, "attributeName");

		return append(attributeName, Model.of(value));
	}

	/**
	 * Creates a attribute modifier that prepends the current value with the given {@code value}
	 * using a default space character (' ') separator.
	 * 
	 * @param attributeName
	 * @param value
	 * @return the attribute modifier
	 * @since 1.5
	 * @see AttributeAppender
	 */
	public static AttributeAppender prepend(String attributeName, IModel<?> value)
	{
		Args.notEmpty(attributeName, "attributeName");

		return new AttributeAppender(attributeName, value)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected String newValue(String currentValue, String replacementValue)
			{
				// swap currentValue and replacementValue in the call to the concatenator
				return super.newValue(replacementValue, currentValue);
			}
		}.setSeparator(" ");
	}

	/**
	 * Creates a attribute modifier that prepends the current value with the given {@code value}
	 * using a default space character (' ') separator.
	 * 
	 * @param attributeName
	 * @param value
	 * @return the attribute modifier
	 * @since 1.5
	 * @see AttributeAppender
	 */
	public static AttributeAppender prepend(String attributeName, Serializable value)
	{
		Args.notEmpty(attributeName, "attributeName");

		return prepend(attributeName, Model.of(value));
	}

	/**
	 * Creates a attribute modifier that removes an attribute with the specified name
	 * 
	 * @param attributeName
	 *            the name of the attribute to be removed
	 * @return the attribute modifier
	 * @since 1.5
	 */
	public static AttributeModifier remove(String attributeName)
	{
		Args.notEmpty(attributeName, "attributeName");

		return replace(attributeName, Model.of(VALUELESS_ATTRIBUTE_REMOVE));
	}
}
