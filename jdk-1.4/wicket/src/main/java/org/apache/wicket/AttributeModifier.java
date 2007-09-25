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

import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.model.IComponentAssignedModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.portlet.PortletRequestContext;
import org.apache.wicket.util.value.IValueMap;

/**
 * This class allows a tag attribute of a component to be modified dynamically
 * with a value obtained from a model object. This concept can be used to
 * programatically alter the attributes of components, overriding the values
 * specified in the markup. The two primary uses of this class are to allow
 * overriding of markup attributes based on business logic and to support
 * dynamic localization. The replacement occurs as the component tag is rendered
 * to the response.
 * <p>
 * The attribute whose value is to be modified must be given on construction of
 * the instance of this class along with the model containing the value to
 * replace with. Optionally a pattern can be supplied that is a regular
 * expression that the existing value must match before the replacement can be
 * carried out.
 * <p>
 * If an attribute is not in the markup, this modifier will add an attribute to
 * the tag only if addAttributeIfNotPresent is true and the replacement value is
 * not null.
 * </p>
 * <p>
 * Instances of this class should be added to components via the
 * {@link org.apache.wicket.Component#add(AttributeModifier)} method after the
 * component has been constucted.
 * <p>
 * It is possible to create new subclasses of AttributeModifier by overriding
 * the newValue(String, String) method. For example, you could create an
 * AttributeModifier subclass which appends the replacement value like this:
 * <code>
 * 	new AttributeModifier("myAttribute", model)
 *  {
 * 		protected String newValue(final String currentValue, final String replacementValue)
 *      {
 *      	return currentValue + replacementValue;
 *      }
 *  };
 * </code>
 * 
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Jonathan Locke
 * @author Martijn Dashorst
 * @author Ralf Ebert
 */
public class AttributeModifier extends AbstractBehavior implements IClusterable
{
	/** Marker value to have an attribute without a value added. */
	public static final String VALUELESS_ATTRIBUTE_ADD = "VA_ADD";

	/** Marker value to have an attribute without a value removed. */
	public static final String VALUELESS_ATTRIBUTE_REMOVE = "VA_REMOVE";

	private static final long serialVersionUID = 1L;

	/** Whether to add the attribute if it is not an attribute in the markup. */
	private final boolean addAttributeIfNotPresent;

	/** Attribute specification. */
	private final String attribute;

	/** Modification information. */
	private boolean enabled;

	/** The pattern. */
	private final String pattern;

	/** The model that is to be used for the replacement. */
	private final IModel replaceModel;

	/**
	 * Create a new attribute modifier with the given attribute name and model
	 * to replace with. The additional boolean flag specifies whether to add the
	 * attribute if it is not present.
	 * 
	 * @param attribute
	 *            The attribute name to replace the value for
	 * @param addAttributeIfNotPresent
	 *            Whether to add the attribute if it is not present
	 * @param replaceModel
	 *            The model to replace the value with
	 */
	public AttributeModifier(final String attribute, final boolean addAttributeIfNotPresent,
			final IModel replaceModel)
	{
		this(attribute, null, addAttributeIfNotPresent, replaceModel);
	}

	/**
	 * Create a new attribute modifier with the given attribute name and model
	 * to replace with. The attribute will not be added if it is not present.
	 * 
	 * @param attribute
	 *            The attribute name to replace the value for
	 * @param replaceModel
	 *            The model to replace the value with
	 */
	public AttributeModifier(final String attribute, final IModel replaceModel)
	{
		this(attribute, null, false, replaceModel);
	}

	/**
	 * Create a new attribute modifier with the given attribute name and
	 * expected pattern to match plus the model to replace with. A null pattern
	 * will match the attribute regardless of its value. The additional boolean
	 * flag specifies whether to add the attribute if it is not present.
	 * 
	 * @param attribute
	 *            The attribute name to replace the value for
	 * @param pattern
	 *            The pattern of the current attribute value to match
	 * @param addAttributeIfNotPresent
	 *            Whether to add the attribute if it is not present and the
	 *            replacement value is not null
	 * @param replaceModel
	 *            The model to replace the value with
	 */
	public AttributeModifier(final String attribute, final String pattern,
			final boolean addAttributeIfNotPresent, final IModel replaceModel)
	{
		if (attribute == null)
		{
			throw new IllegalArgumentException("Attribute parameter cannot be null");
		}

		this.attribute = attribute;
		this.pattern = pattern;
		enabled = true;
		this.addAttributeIfNotPresent = addAttributeIfNotPresent;
		this.replaceModel = replaceModel;
	}

	/**
	 * Create a new attribute modifier with the given attribute name and
	 * expected pattern to match plus the model to replace with. A null pattern
	 * will match the attribute regardless of its value. The attribute will not
	 * be added if it is not present.
	 * 
	 * @param attribute
	 *            The attribute name to replace the value for
	 * @param pattern
	 *            The pattern of the current attribute value to match
	 * @param replaceModel
	 *            The model to replace the value with
	 */
	public AttributeModifier(final String attribute, final String pattern, final IModel replaceModel)
	{
		this(attribute, pattern, false, replaceModel);
	}

	/**
	 * Detach the model if it was a IDetachableModel Internal method. shouldn't
	 * be called from the outside. If the attribute modifier is shared, the
	 * detach method will be called multiple times.
	 * 
	 * @param component
	 *            the model that initiates the detachement
	 */
	public final void detach(Component component)
	{
		if (replaceModel != null)
		{
			replaceModel.detach();
		}
	}

	/**
	 * @return whether to add the attribute if it is not an attribute in the
	 *         markup
	 */
	public final boolean getAddAttributeIfNotPresent()
	{
		return addAttributeIfNotPresent;
	}

	/**
	 * @return the attribute name to replace the value for
	 */
	public final String getAttribute()
	{
		return attribute;
	}

	/**
	 * @return the pattern of the current attribute value to match
	 */
	public final String getPattern()
	{
		return pattern;
	}

	/**
	 * Checks whether this attribute modifier is enabled or not.
	 * 
	 * @return Whether enabled or not
	 * @deprecated
	 */
	public final boolean isEnabled()
	{
		return enabled;
	}

	/**
	 * Made final to support the parameterless variant.
	 * 
	 * @see org.apache.wicket.behavior.AbstractBehavior#isEnabled(org.apache.wicket.Component)
	 */
	public boolean isEnabled(Component component)
	{
		return enabled;
	}

	/**
	 * @see org.apache.wicket.behavior.IBehavior#onComponentTag(org.apache.wicket.Component,
	 *      org.apache.wicket.markup.ComponentTag)
	 */
	public final void onComponentTag(Component component, ComponentTag tag)
	{
		if (tag.getType() != XmlTag.CLOSE)
		{
			replaceAttibuteValue(component, tag);
		}
	}

	/**
	 * Checks the given component tag for an instance of the attribute to modify
	 * and if all criteria are met then replace the value of this attribute with
	 * the value of the contained model object.
	 * 
	 * @param component
	 *            The component
	 * @param tag
	 *            The tag to replace the attribute value for
	 */
	public final void replaceAttibuteValue(final Component component, final ComponentTag tag)
	{
		if (isEnabled(component))
		{
			final IValueMap attributes = tag.getAttributes();
			final Object replacementValue = getReplacementOrNull(component);

			if (VALUELESS_ATTRIBUTE_ADD.equals(replacementValue))
			{
				attributes.put(attribute, null);
			}
			else if (VALUELESS_ATTRIBUTE_REMOVE.equals(replacementValue))
			{
				attributes.remove(attribute);
			}
			else
			{
				if (attributes.containsKey(attribute))
				{
					final String value = toStringOrNull(attributes.get(attribute));
					if (pattern == null || value.matches(pattern))
					{
						final String newValue = newValue(value, toStringOrNull(replacementValue));
						if (newValue != null)
						{
							attributes.put(attribute, getContextRelativeValue(newValue));
						}
					}
				}
				else if (addAttributeIfNotPresent)
				{
					final String newValue = newValue(null, toStringOrNull(replacementValue));
					if (newValue != null)
					{
						attributes.put(attribute, getContextRelativeValue(newValue));
					}
				}
			}
		}
	}
	
	protected String getContextRelativeValue(String value)
	{
		if ("href".equals(attribute) || "src".equals(attribute))
		{
			RequestContext rc = RequestContext.get();
			if (rc.isPortletRequest() && !(value.startsWith("http://") || value.startsWith("https://")))
			{
				if ("href".equals(attribute))
				{
					value = ((PortletRequestContext)rc).encodeRenderURL(value).toString();
				}
				else
				{
					value = ((PortletRequestContext)rc).encodeSharedResourceURL(value).toString();
				}
			}
		}
		return value;
	}

	/**
	 * Sets whether this attribute modifier is enabled or not.
	 * 
	 * @param enabled
	 *            Whether enabled or not
	 */
	public final void setEnabled(final boolean enabled)
	{
		this.enabled = enabled;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[AttributeModifier attribute=" + attribute + ", enabled=" + enabled + ", pattern=" +
				pattern + ", replacementModel=" + replaceModel + "]";
	}

	/* gets replacement with null check. */
	private Object getReplacementOrNull(final Component component)
	{
		IModel model = replaceModel;
		if (model instanceof IComponentAssignedModel)
		{
			model = ((IComponentAssignedModel)model).wrapOnAssignment(component);
		}
		return (model != null) ? model.getObject() : null;
	}

	/* gets replacement as a string with null check. */
	private String toStringOrNull(final Object replacementValue)
	{
		return (replacementValue != null) ? replacementValue.toString() : null;
	}

	/**
	 * Gets the replacement model. Allows subclasses access to replace model.
	 * 
	 * @return the replace model of this attribute modifier
	 */
	protected final IModel getReplaceModel()
	{
		return replaceModel;
	}

	/**
	 * Gets the value that should replace the current attribute value. This
	 * gives users the ultimate means to customize what will be used as the
	 * attribute value. For instance, you might decide to append the replacement
	 * value to the current instead of just replacing it as is Wicket's default.
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
}
