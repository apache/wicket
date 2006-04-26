/*
 * $Id$ $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.extensions.markup.html.beanedit;

import java.beans.IndexedPropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import wicket.AttributeModifier;
import wicket.WicketRuntimeException;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.CheckBox;
import wicket.markup.html.form.TextField;
import wicket.markup.html.link.Link;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.Panel;

/**
 * Panel for generic bean displaying/ editing.
 * 
 * @author Eelco Hillenius
 */
public class BeanPanel extends AbstractBeanPanel
{
	private static final long serialVersionUID = 1L;

	/** boolean types. */
	private static final Class[] BOOL_TYPES = new Class[] { Boolean.class, Boolean.TYPE };

	/** basic java types. */
	private static final Class[] BASE_TYPES = new Class[] { String.class, Number.class,
			Integer.TYPE, Double.TYPE, Long.TYPE, Float.TYPE, Short.TYPE, Byte.TYPE, Date.class };

	/** edit mode. */
	private EditMode editMode = EditMode.READ_WRITE;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 * @param bean
	 *            JavaBean to be edited or displayed
	 */
	public BeanPanel(String id, Serializable bean)
	{
		this(id, new BeanModel(bean));
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 * @param beanModel
	 *            model with the JavaBean to be edited or displayed
	 */
	public BeanPanel(String id, BeanModel beanModel)
	{
		super(id, beanModel);
		setRenderBodyOnly(true);
		Panel header = newHeader("header", beanModel);
		if (header == null)
		{
			throw new IllegalArgumentException("Header must be not null");
		}
		add(header);
		add(new PropertyList("propertiesList", new BeanPropertiesListModel(beanModel)));
	}

	/**
	 * Gets the header panel of this editor.
	 * 
	 * @param panelId
	 *            id of panel; must be used for constructing any panel
	 * @param beanModel
	 *            model with the JavaBean to be edited or displayed
	 * @return the header panel
	 */
	protected Panel newHeader(String panelId, BeanModel beanModel)
	{
		return new DefaultBeanHeaderPanel(panelId, beanModel);
	}

	/**
	 * Gets the editor for the given property.
	 * 
	 * @param panelId
	 *            id of panel; must be used for constructing any panel
	 * @param propertyMeta
	 *            property descriptor
	 * @return the editor
	 */
	protected Panel newPropertyEditor(String panelId, PropertyMeta propertyMeta)
	{
		Class type = propertyMeta.getPropertyType();
		BeanPropertyEditor editor = findCustomEditor(panelId, propertyMeta);

		if (editor == null)
		{
			if (propertyMeta.getPropertyDescriptor() instanceof IndexedPropertyDescriptor)
			{
				throw new WicketRuntimeException("indexed properties not supported yet ");
			}
			else
			{
				editor = newDefaultEditor(panelId, propertyMeta);
			}
		}

		return editor;
	}


	/**
	 * Gets the editMode.
	 * 
	 * @return editMode
	 */
	protected EditMode getEditMode()
	{
		return editMode;
	}

	/**
	 * Sets the editMode.
	 * 
	 * @param editMode
	 *            editMode
	 */
	protected void setEditMode(EditMode editMode)
	{
		this.editMode = editMode;
	}

	/**
	 * Gets a default property editor panel.
	 * 
	 * @param panelId
	 *            component id
	 * @param propertyMeta
	 *            property descriptor
	 * @return a property editor
	 */
	protected final BeanPropertyEditor newDefaultEditor(final String panelId,
			final PropertyMeta propertyMeta)
	{
		BeanPropertyEditor editor;
		final Class type = propertyMeta.getPropertyType();
		if (checkAssignableFrom(BOOL_TYPES, type))
		{
			editor = new PropertyCheckBox(panelId, propertyMeta);
		}
		if (checkAssignableFrom(BASE_TYPES, type))
		{
			editor = new PropertyInput(panelId, propertyMeta);
		}
		else
		{
			return new ButtonToMoreDetails(panelId, propertyMeta);
		}
		return editor;
	}

	/**
	 * Does isAssignableFrom check on given class array for given type.
	 * 
	 * @param types
	 *            array of types
	 * @param type
	 *            type to check against
	 * @return true if one of the types matched
	 */
	private boolean checkAssignableFrom(Class[] types, Class type)
	{
		int len = types.length;
		for (int i = 0; i < len; i++)
		{
			if (types[i].isAssignableFrom(type))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Finds a possible custom editor by looking for the type name + 'Editor'
	 * (e.g. mypackage.MyBean has editor mypackage.MyBeanEditor).
	 * 
	 * @param panelId
	 *            id of panel; must be used for constructing any panel
	 * @param propertyMeta
	 *            property descriptor
	 * @return PropertyEditor if found or null
	 */
	protected final BeanPropertyEditor findCustomEditor(String panelId, PropertyMeta propertyMeta)
	{
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if (classLoader == null)
		{
			classLoader = getClass().getClassLoader();
		}

		Class type = propertyMeta.getPropertyType();
		String editorTypeName = type.getName() + "Editor";
		try
		{
			Class editorClass = classLoader.loadClass(editorTypeName);
			try
			{
				// get constructor
				Constructor constructor = editorClass.getConstructor(new Class[] { String.class,
						BeanModel.class, PropertyMeta.class, EditMode.class });

				// construct arguments
				Object[] args = new Object[] { panelId, BeanPanel.this.getModel(), propertyMeta,
						getEditMode() };

				// create editor instance
				BeanPropertyEditor editor = (BeanPropertyEditor)constructor.newInstance(args);
				return editor;
			}
			catch (SecurityException e)
			{
				throw new WicketRuntimeException(e);
			}
			catch (NoSuchMethodException e)
			{
				throw new WicketRuntimeException(e);
			}
			catch (InstantiationException e)
			{
				throw new WicketRuntimeException(e);
			}
			catch (IllegalAccessException e)
			{
				throw new WicketRuntimeException(e);
			}
			catch (InvocationTargetException e)
			{
				throw new WicketRuntimeException(e);
			}
		}
		catch (ClassNotFoundException e)
		{
			// ignore; there just is no custom editor
		}

		return null;
	}

	/**
	 * Lists all properties of the target object.
	 */
	private final class PropertyList extends ListView
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param id
		 *            component name
		 * @param model
		 *            the model
		 */
		public PropertyList(String id, BeanPropertiesListModel model)
		{
			super(id, model);
			setReuseItems(true);
		}

		/**
		 * @see wicket.markup.html.list.ListView#populateItem(wicket.markup.html.list.ListItem)
		 */
		protected void populateItem(ListItem item)
		{
			PropertyMeta propertyMeta = (PropertyMeta)item.getModelObject();
			item.add(new Label("displayName", propertyMeta.getDisplayName()));
			Panel propertyEditor = newPropertyEditor("editor", propertyMeta);
			if (propertyEditor == null)
			{
				throw new IllegalStateException("Value propertyEditor must be not null");
			}
			item.add(propertyEditor);
		}
	}

	/**
	 * Panel for an input field.
	 */
	static final class PropertyInput extends BeanPropertyEditor
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param id
		 *            component id
		 * @param propertyMeta
		 *            property descriptor
		 */
		public PropertyInput(String id, final PropertyMeta propertyMeta)
		{
			super(id, propertyMeta);
			setRenderBodyOnly(true);
			Class type = propertyMeta.getPropertyType();
			TextField valueTextField = new TextField("value", new BeanPropertyModel(propertyMeta),
					type);
			EditModeReplacementModel replacementModel = new EditModeReplacementModel(propertyMeta);
			valueTextField.add(new AttributeModifier("disabled", true, replacementModel));
			add(valueTextField);
		}
	}

	/**
	 * Panel for a check box.
	 */
	static final class PropertyCheckBox extends BeanPropertyEditor
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param id
		 *            component id
		 * @param propertyMeta
		 *            property descriptor
		 */
		public PropertyCheckBox(String id, final PropertyMeta propertyMeta)
		{
			super(id, propertyMeta);
			setRenderBodyOnly(true);
			Class type = propertyMeta.getPropertyType();
			CheckBox valueTextField = new CheckBox("value", new BeanPropertyModel(propertyMeta));
			EditModeReplacementModel replacementModel = new EditModeReplacementModel(propertyMeta);
			valueTextField.add(new AttributeModifier("disabled", true, replacementModel));
			add(valueTextField);
		}
	}

	/**
	 * Panel for a button to more details.
	 */
	static final class ButtonToMoreDetails extends BeanPropertyEditor
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param id
		 *            component id
		 * @param propertyMeta
		 *            property descriptor
		 */
		public ButtonToMoreDetails(String id, final PropertyMeta propertyMeta)
		{
			super(id, propertyMeta);
			add(new Link("button")
			{
				private static final long serialVersionUID = 1L;

				public void onClick()
				{
				}
			});
		}
	}
}
