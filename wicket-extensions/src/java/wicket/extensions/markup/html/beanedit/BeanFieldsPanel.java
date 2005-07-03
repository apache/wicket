/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.extensions.markup.html.beanedit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import wicket.AttributeModifier;
import wicket.WicketRuntimeException;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.CheckBox;
import wicket.markup.html.form.TextField;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;
import wicket.model.PropertyModel;


/**
 * Panel for generic bean displaying/ editing that partially (a subset of its properties)
 * edits a bean. It reuses the markup of BeanPanel.
 *
 * @author Eelco Hillenius
 */
public class BeanFieldsPanel extends Panel
{
	/** fields to be edited/ displayed. */
	private BeanFields fields;

	/**
	 * Construct.
	 * @param id component id
	 * @param fields fields to be edited/ displayed
	 */
	public BeanFieldsPanel(String id, BeanFields fields)
	{
		super(id);
		setModel(fields.getBeanModel());
		this.fields = fields;
		add(new Label("displayName", fields.getDisplayName()));
		add(new PropertyList("propertiesList", fields.list()));
	}

	/**
	 * Gets the fields to be edited/ displayed.
	 * @return fields fields to be edited/ displayed
	 */
	public final BeanFields getFields()
	{
		return fields;
	}

	/**
	 * Lists all properties of the target object.
	 */
	private final class PropertyList extends ListView
	{
		/**
		 * Construct.
		 * @param id component name
		 * @param model the model
		 */
		public PropertyList(String id, List model)
		{
			super(id, model);
			setOptimizeItemRemoval(true);
		}

		/**
		 * @see wicket.markup.html.list.ListView#populateItem(wicket.markup.html.list.ListItem)
		 */
		protected void populateItem(ListItem item)
		{
			AbstractBeanField field = (AbstractBeanField)item.getModelObject();
			item.add(new Label("displayName", field.getDisplayName()));
			Panel propertyEditor = getPropertyEditor("editor", field);
			item.add(propertyEditor);
		}
	}

	/**
	 * Gets the editor for the given field.
	 * @param panelId id of panel; must be used for constructing any panel
	 * @param field the field
	 * @return the editor
	 */
	protected Panel getPropertyEditor(String panelId, AbstractBeanField field)
	{
		BeanPropertyEditor editor = findCustomEditor(panelId, field);

		if (editor == null)
		{
			editor = getDefaultEditor(panelId, field);
		}

		return editor;
	}

	/**
	 * Gets a default property editor panel.
	 * @param panelId component id
	 * @param field the field
	 * @return a property editor
	 */
	protected final BeanPropertyEditor getDefaultEditor(String panelId, AbstractBeanField field)
	{
		BeanPropertyEditor editor;

		if (field instanceof BeanField)
		{
			BeanField singleField = (BeanField)field;
			Class type = singleField.getType();
			if (Boolean.class.isAssignableFrom(type) || Boolean.TYPE == type)
			{
				editor = new PropertyCheckBox(panelId, (BeanModel)getModel(), singleField);
			}
			else
			{
				editor = new PropertyInput(panelId, (BeanModel)getModel(), singleField);
			}
		}
//		else if (field instanceof ChoiceField)
//		{
//			
//		}
		else
		{
			throw new IllegalArgumentException(field + " is not of a known field type");
		}

		return editor;
	}

	/**
	 * Finds a possible custom editor by looking for the type name + 'Editor'
	 * (e.g. mypackage.MyField has editor mypackage.MyFieldEditor).
	 * @param panelId id of panel; must be used for constructing any panel
	 * @param field the field
	 * @return PropertyEditor if found or null
	 */
	protected final BeanPropertyEditor findCustomEditor(String panelId, final AbstractBeanField field)
	{
		Class type = field.getClass();
		if(type == BeanField.class)
		{
			return null; // no override
		}

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if (classLoader == null)
		{
			classLoader = getClass().getClassLoader();
		}

		String editorTypeName = type.getName() + "Editor";
		try
		{
			Class editorClass = classLoader.loadClass(editorTypeName);
			try
			{
				// get the constructor
				Constructor constructor = editorClass.getConstructor(
						new Class[]{String.class, IModel.class, BeanField.class});

				// construct arguments
				Object[] args = new Object[]{panelId, BeanFieldsPanel.this.getModel(), field};

				// create the editor
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
		catch(ClassNotFoundException e)
		{
			// ignore; there just is no custom editor
		}

		return null;
	}

	/**
	 * Panel for an input field.
	 */
	private final class PropertyInput extends BeanPropertyEditor
	{
		/**
		 * Construct.
		 * @param id component id
		 * @param beanModel model with the target bean
		 * @param field the field
		 */
		public PropertyInput(String id, final BeanModel beanModel, final BeanField field)
		{
			super(id, beanModel, field);
			Class type = field.getType();
			TextField valueTextField = new TextField("value",
					new PropertyModel(beanModel, field.getName(), type), type);
			EditModeReplacementModel replacementModel =
				new EditModeReplacementModel(field.getEditMode());
			valueTextField.add(new AttributeModifier("disabled", true, replacementModel));
			add(valueTextField);
		}
	}

	/**
	 * Panel for a check box.
	 */
	public final class PropertyCheckBox extends BeanPropertyEditor
	{
		/**
		 * Construct.
		 * @param id component id
		 * @param beanModel model with the target bean
		 * @param field field
		 */
		public PropertyCheckBox(String id, BeanModel beanModel, BeanField field)
		{
			super(id, beanModel, field);
			CheckBox valueTextField = new CheckBox("value",
					new PropertyModel(beanModel, field.getName(), field.getType()));
			EditModeReplacementModel replacementModel =
				new EditModeReplacementModel(field.getEditMode());
			valueTextField.add(new AttributeModifier("disabled", true, replacementModel));
			add(valueTextField);
		}
	}
}
