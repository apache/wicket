/*
 * $Id$
 * $Revision$ $Date$
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
package wicket;

import java.io.Serializable;

import wicket.markup.ComponentTag;
import wicket.model.IModel;
import wicket.util.value.ValueMap;

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
 * {@link wicket.Component#add(AttributeModifier)}method after the component
 * has been constucted.
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
 */
public class AttributeModifier implements Serializable
{
	/** The next attribute modifier in any chain */
	AttributeModifier next;

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
		if (replaceModel == null)
		{
			throw new IllegalArgumentException("ReplaceModel parameter cannot be null");
		}

		this.attribute = attribute;
		this.pattern = pattern;
		this.enabled = true;
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
	 * Checks whether this attribute modifier is enabled or not.
	 * 
	 * @return Whether enabled or not
	 */
	public final boolean isEnabled()
	{
		return enabled;
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
	 * Gets the value that should replace the current attribute value.
	 * 
	 * @param currentValue
	 *            The current attribute value
	 * @param replacementValue
	 *            The replacement value
	 * @return The value that should replace the current attribute value
	 */
	protected String newValue(final String currentValue, final String replacementValue)
	{
		return replacementValue;
	}

	/**
	 * Detach the model if it was a IDetachableModel Internal method. shouldn't
	 * be called from the outside
	 */
	final void detachModel()
	{
		replaceModel.detach();
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
	final void replaceAttibuteValue(final Component component, final ComponentTag tag)
	{
		if (enabled)
		{
			final ValueMap attributes = tag.getAttributes();
			final Object replacementValue = replaceModel.getObject(component);

			// Only do something when we have a replacement
			if (replacementValue != null)
			{
				if (attributes.containsKey(attribute))
				{
					final String value = attributes.get(attribute).toString();
					if (pattern == null || value.matches(pattern))
					{
						attributes.put(attribute, newValue(value, replacementValue.toString()));
					}
				}
				else if (addAttributeIfNotPresent)
				{
					attributes.put(attribute, newValue(null, replacementValue.toString()));
				}
			}
		}
	}
}
