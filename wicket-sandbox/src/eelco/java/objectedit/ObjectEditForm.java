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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;

import wicket.markup.html.form.Form;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.CompoundPropertyModel;
import wicket.model.Model;

/**
 * 
 */
public class ObjectEditForm extends Form
{

	/**
	 * Construct.
	 * @param id component id
	 * @param object object to be edited
	 * @param feedback feedback receiver
	 */
	public ObjectEditForm(String id, Serializable object, FeedbackPanel feedback)
	{
		super(id, new CompoundPropertyModel(object), feedback);
	}

	/**
	 * Lists all properties of the target object.
	 */
	private final class PropertyList extends ListView
	{
		/**
		 * Construct.
		 * @param id component name
		 * @param object target object
		 */
		public PropertyList(String id, Serializable object)
		{
			super(id, new ObjectPropertyModel(object));
		}

		/**
		 * @see wicket.markup.html.list.ListView#populateItem(wicket.markup.html.list.ListItem)
		 */
		protected void populateItem(ListItem item)
		{
		}
	}

	/**
	 * model that gets all public fields and put their field names in a list that
	 * acts as the model object.
	 */
	private final class ObjectPropertyModel extends Model
	{
		/**
		 * Construct.
		 * @param object
		 */
		public ObjectPropertyModel(Serializable object)
		{
			ArrayList fieldNames = new ArrayList();
			Class objectClass = object.getClass();
			Field[] fields = objectClass.getFields();
			int len = fields.length;
			for(int i = 0; i< len; i++)
			{
				fieldNames.add(fields[i].getName());
			}
			setObject(fieldNames);
		}
	}
}
