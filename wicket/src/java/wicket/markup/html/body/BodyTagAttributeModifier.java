/*
 * $Id: BodyContainer.java 5436 2006-04-17 17:50:44Z jdonnerstag $ $Revision:
 * 5436 $ $Date: 2006-04-17 19:50:44 +0200 (Mo, 17 Apr 2006) $
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
package wicket.markup.html.body;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.Page;
import wicket.model.IModel;

/**
 * An attribute modifier specifically for body tags.
 * <p>
 * Panel have associated markup files and if that contains &lt;wicket:head&gt;
 * and &lt;body onLoad="..."&gt; than the body onLoad attribute will be appended
 * to the page's body onLoad attribute. That appending happens by means of an
 * AttributeModifier which the Panel add to the body container. In case the
 * Panel is removed or replaced, than the AttributeModifier must be removed or
 * at least disabled. That exactly is what this special purpose
 * AttributeModifier does. It disables itself if the owner component (Panel) is
 * removed or replaced.
 * 
 * @author Juergen Donnerstag
 */
public final class BodyTagAttributeModifier extends AttributeModifier
{
	private static final long serialVersionUID = 1L;

	/**
	 * Make sure we don't keep a reference to the component longer than realy
	 * needed.
	 */
	private transient WeakReference<Component> componentReference;

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
	 * @param behaviorOwner
	 *            The component which created (owns) the modifier
	 */
	public BodyTagAttributeModifier(final String attribute, final boolean addAttributeIfNotPresent,
			final IModel<? extends CharSequence> replaceModel, final Component behaviorOwner)
	{
		super(attribute, addAttributeIfNotPresent, replaceModel);
		init(behaviorOwner);
	}

	/**
	 * Create a new attribute modifier with the given attribute name and model
	 * to replace with. The attribute will not be added if it is not present.
	 * 
	 * @param attribute
	 *            The attribute name to replace the value for
	 * @param replaceModel
	 *            The model to replace the value with
	 * @param behaviorOwner
	 *            The component which created (owns) the modifier
	 */
	public BodyTagAttributeModifier(final String attribute, final IModel<? extends CharSequence> replaceModel,
			final Component behaviorOwner)
	{
		super(attribute, replaceModel);
		init(behaviorOwner);
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
	 * @param behaviorOwner
	 *            The component which created (owns) the modifier
	 */
	public BodyTagAttributeModifier(final String attribute, final String pattern,
			final boolean addAttributeIfNotPresent, final IModel<? extends CharSequence> replaceModel,
			final Component behaviorOwner)
	{
		super(attribute, pattern, addAttributeIfNotPresent, replaceModel);
		init(behaviorOwner);
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
	 * @param behaviorOwner
	 *            The component which created (owns) the modifier
	 */
	public BodyTagAttributeModifier(final String attribute, final String pattern,
			final IModel<? extends CharSequence> replaceModel, final Component behaviorOwner)
	{
		super(attribute, pattern, replaceModel);
		init(behaviorOwner);
	}

	/**
	 * Initialize
	 * 
	 * @param behaviorOwner
	 *            The component which creates (owns) the modifier
	 */
	private void init(final Component behaviorOwner)
	{
		if (behaviorOwner != null)
		{
			this.componentReference = new WeakReference<Component>(behaviorOwner);
		}
	}

	/**
	 * @see wicket.AttributeModifier#newValue(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	protected String newValue(final String currentValue, final String replacementValue)
	{
		// If no behavior owner has been provided, than behave as if this
		// were a standard normal attribute modifier
		if (this.componentReference != null)
		{
			// Get the owner of the attribute modifier (e.g. the Panel, not the
			// Body)
			final Component<?> behaviorOwner = componentReference.get();

			// If case the components memory has been GCed already, than disable
			// the attribute modifier and return the attribute value unchanged.
			if (behaviorOwner == null)
			{
				setEnabled(false);
				return currentValue;
			}

			// It must have a Page, otherwise one of its parents has been
			// removed. No Page, than disable the attribute modifier and
			// return the attribute value unchanged.

			// Component.findPage() is 'protected'. But this works as well.
			if (behaviorOwner.findParent(Page.class) == null)
			{
				setEnabled(false);
				return currentValue;
			}

			// And the "Panel" must be visible. Wicket core tests only
			// that the body (the component the attribute modifier is
			// attached to) is visible.
			if (behaviorOwner.isVisibleInHierarchy() == false)
			{
				return currentValue;
			}
		}
		if (currentValue != null && !currentValue.trim().endsWith(";"))
		{
			return currentValue + ";" + replacementValue;
		}
		return (currentValue == null ? replacementValue : currentValue + replacementValue);
	}

	/**
	 * AttributeModifiers must be Serialzable but WeakReferences are not. Hence,
	 * we need to implement our read/write methods to properly support it.
	 * 
	 * @see Serializable
	 * 
	 * @param inputStream
	 *            The input stream to read the object from
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(final ObjectInputStream inputStream) throws IOException,
			ClassNotFoundException
	{
		inputStream.defaultReadObject();

		final Object object = inputStream.readObject();
		if (object instanceof Component)
		{
			componentReference = new WeakReference<Component>((Component)object);
		}
	}

	/**
	 * AttributeModifiers must be Serialzable but WeakReferences are not. Hence,
	 * we need to implement our read/write methods to properly support it.
	 * 
	 * @see Serializable
	 * 
	 * @param outputStream
	 * @throws IOException
	 */
	private void writeObject(final ObjectOutputStream outputStream) throws IOException
	{
		outputStream.defaultWriteObject();
		if (componentReference != null)
		{
			outputStream.writeObject(componentReference.get());
		}
		else
		{
			outputStream.writeObject(null);
		}
	}
}