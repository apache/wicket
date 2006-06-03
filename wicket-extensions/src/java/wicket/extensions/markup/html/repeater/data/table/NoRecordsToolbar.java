/*
 * $Id: NoRecordsToolbar.java 5840 2006-05-24 13:49:09 -0700 (Wed, 24 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-24 13:49:09 -0700 (Wed, 24 May
 * 2006) $
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
package wicket.extensions.markup.html.repeater.data.table;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.behavior.SimpleAttributeModifier;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.IModel;

/**
 * A toolbar that displays a "no records found" message when the data table
 * contains no rows.
 * <p>
 * The message can be overridden by providing a resource with key
 * <code>datatable.no-records-found</code>
 * 
 * @see DefaultDataTable
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class NoRecordsToolbar extends AbstractToolbar
{
	private static final long serialVersionUID = 1L;

	private static final IModel DEFAULT_MESSAGE_MODEL = new AbstractReadOnlyModel()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public Object getObject(Component component)
		{
			return component.getLocalizer().getString("datatable.no-records-found", component,
					"No Records Found");
		}
	};


	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent component
	 * @param id
	 * 
	 * @param table
	 *            data table this toolbar will be attached to
	 */
	public NoRecordsToolbar(MarkupContainer parent, String id, final DataTable table)
	{
		this(parent, id, table, DEFAULT_MESSAGE_MODEL);
	}

	/**
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 * @param table
	 *            data table this toolbar will be attached to
	 * @param messageModel
	 *            model that will be used to display the "no records found"
	 *            message
	 */
	public NoRecordsToolbar(MarkupContainer parent, String id, final DataTable table,
			IModel messageModel)
	{
		super(parent, id, table);
		WebMarkupContainer td = new WebMarkupContainer(this, "td");

		td.add(new SimpleAttributeModifier("colspan", String.valueOf(table.getColumns().length)));
		new Label(td, "msg", messageModel);
	}

	/**
	 * Only shows this toolbar when there are no rows
	 * 
	 * @see wicket.Component#isVisible()
	 */
	@Override
	public boolean isVisible()
	{
		return getTable().getRowCount() == 0;
	}

}
