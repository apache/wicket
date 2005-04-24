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

import wicket.AttributeModifier;
import wicket.Component;
import wicket.WicketRuntimeException;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.TextField;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * 
 */
public class BeanPanel extends Panel
{
	/** Mode that indicates the panel is in read only mode. */
	public static final int MODE_READ_ONLY = 0;

	/** Mode that indicates the panel is in edit mode. */
	public static final int MODE_EDIT = 1;

	/** the current mode; MODE_READ_ONLY, MODE_EDIT or a custom mode. */
	private int mode = MODE_EDIT;

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
		if(descriptor instanceof IndexedPropertyDescriptor)
		{
			throw new WicketRuntimeException("index properties not supported yet ");
		}
		else
		{
			return new SimplePropertyPanel(panelId, getModel(), descriptor);
		}
	}

	/**
	 * Gets the current mode.
	 * @return mode
	 */
	public int getMode()
	{
		return mode;
	}

	/**
	 * Sets the current mode.
	 * @param mode the mode
	 * @return This
	 */
	public BeanPanel setMode(int mode)
	{
		this.mode = mode;
		return this;
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
	private final class SimplePropertyPanel extends Panel
	{
		/**
		 * Construct.
		 * @param id
		 * @param beanModel 
		 * @param descriptor
		 */
		public SimplePropertyPanel(String id, IModel beanModel, final PropertyDescriptor descriptor)
		{
			super(id, beanModel);
			Class type = descriptor.getPropertyType();
			TextField valueTextField = new TextField("value",
					new PropertyDescriptorModel(beanModel, descriptor), type);
			Model replacementModel = new Model()
			{
				public Object getObject(Component component)
				{
					if(mode == MODE_READ_ONLY || descriptor.getWriteMethod() == null)
					{
						return "disabled";
					}
					return null;
				}
			};
			valueTextField.add(new AttributeModifier("disabled", false, replacementModel));
			add(valueTextField);
		}
	}
}
