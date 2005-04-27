/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ================================================================================
 * Copyright (c)
 * All rechten voorbehouden.
 */
package objectedit;

import java.beans.IndexedPropertyDescriptor;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import wicket.AttributeModifier;
import wicket.WicketRuntimeException;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.TextField;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * Panel for generic bean displaying/ editing.
 *
 * @author Eelco Hillenius
 */
public class BeanPanel extends Panel
{
	/** cache for editors. */
	private static Map cachedEditors = new HashMap();

	/** edit mode. */
	private EditMode editMode = new EditMode(EditMode.MODE_READ_ONLY);

	/**
	 * Construct.
	 * @param id component id
	 * @param bean JavaBean to be edited or displayed
	 */
	public BeanPanel(String id, Serializable bean)
	{
		this(id, new Model(bean));
	}

	/**
	 * Construct.
	 * @param id component id
	 * @param beanModel model with the JavaBean to be edited or displayed
	 */
	public BeanPanel(String id, IModel beanModel)
	{
		super(id, beanModel);
		add(new Label("displayName", new DisplayNameModel(beanModel)));
		add(new PropertyList("propertiesList", new PropertyDescriptorListModel(beanModel)));
	}

	/**
	 * Gets the editor for the given property.
	 * @param panelId id of panel; must be used for constructing any panel
	 * @param descriptor property descriptor
	 * @return the editor
	 */
	protected Panel getPropertyEditor(String panelId, PropertyDescriptor descriptor)
	{
		Class type = descriptor.getPropertyType();
		PropertyEditor editor = (PropertyEditor)cachedEditors.get(type);
		if (editor != null)
		{
			return editor;
		}

		editor = findCustomEditor(panelId, descriptor);

		if (editor == null)
		{
			if (descriptor instanceof IndexedPropertyDescriptor)
			{
				throw new WicketRuntimeException("index properties not supported yet ");
			}
			else
			{
				editor = new SimplePropertyPanel(panelId, getModel(), descriptor);
			}
		}

		cachedEditors.put(type, editor);

		return editor;
	}

	/**
	 * Finds a possible custom editor by looking for the type name + 'PropertyEditor'
	 * (e.g. mypackage.MyBean has editor mypackage.MyBeanEditor).
	 * @param panelId id of panel; must be used for constructing any panel
	 * @param descriptor property descriptor
	 * @return PropertyEditor if found or null
	 */
	private PropertyEditor findCustomEditor(String panelId, PropertyDescriptor descriptor)
	{
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if (classLoader == null)
		{
			classLoader = getClass().getClassLoader();
		}

		Class type = descriptor.getPropertyType();
		String editorTypeName = type.getName() + "PropertyEditor";
		try
		{
			Class editorClass = classLoader.loadClass(editorTypeName);
			try
			{
				Constructor constructor = editorClass.getConstructor(
						new Class[]{String.class, IModel.class, PropertyDescriptor.class});
				Object[] args = new Object[]{panelId, BeanPanel.this.getModel(), descriptor};
				PropertyEditor editor = (PropertyEditor)constructor.newInstance(args);
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
	 * Lists all properties of the target object.
	 */
	private final class PropertyList extends ListView
	{
		/**
		 * Construct.
		 * @param id component name
		 * @param model the model
		 */
		public PropertyList(String id, PropertyDescriptorListModel model)
		{
			super(id, model);
		}

		/**
		 * @see wicket.markup.html.list.ListView#populateItem(wicket.markup.html.list.ListItem)
		 */
		protected void populateItem(ListItem item)
		{
			PropertyDescriptor descriptor = (PropertyDescriptor)item.getModelObject();
			item.add(new Label("displayName", descriptor.getDisplayName()));
			Panel propertyEditor = getPropertyEditor("editor", descriptor);
			item.add(propertyEditor);
		}
	}

	/**
	 * Default panel for a simple property.
	 */
	private final class SimplePropertyPanel extends PropertyEditor
	{
		/**
		 * Construct.
		 * @param id component id
		 * @param beanModel model with the target bean
		 * @param descriptor property descriptor
		 */
		public SimplePropertyPanel(String id, IModel beanModel, final PropertyDescriptor descriptor)
		{
			super(id, beanModel, descriptor);
			Class type = descriptor.getPropertyType();
			TextField valueTextField = new TextField("value",
					new PropertyDescriptorModel(beanModel, descriptor), type);
			EditModeReplacementModel replacementModel =
				new EditModeReplacementModel(editMode, descriptor);
			valueTextField.add(new AttributeModifier("disabled", false, replacementModel));
			add(valueTextField);
		}
	}
}
