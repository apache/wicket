package wicket.extensions.markup.html.repeater.data.table;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.IModel;
import wicket.model.Model;

public class NoRecordsToolbar extends Toolbar
{
	private static final long serialVersionUID = 1L;

	private static final IModel DEFAULT_MESSAGE_MODEL = new AbstractReadOnlyModel()
	{
		private static final long serialVersionUID = 1L;

		public Object getObject(Component component)
		{
			return component.getLocalizer().getString("datatable.no-records-found", component,
					"No Records Found");
		}
	};


	public NoRecordsToolbar(final AbstractDataTable table)
	{
		this(table, DEFAULT_MESSAGE_MODEL);
	}

	public NoRecordsToolbar(final AbstractDataTable table, IModel messageModel)
	{
		super(AbstractDataTable.TOOLBAR_COMPONENT_ID);
		WebMarkupContainer tr=new WebMarkupContainer("tr") {

			private static final long serialVersionUID = 1L;
			
			public boolean isVisible()
			{
				return table.getItemCount()==0;
			}
			
		};
		add(tr);
		
		WebMarkupContainer td = new WebMarkupContainer("td");
		tr.add(td);
		
		td.add(new AttributeModifier("colspan", true, new Model(String.valueOf(table.getColumns().length))));
		td.add(new Label("msg", messageModel));
	}

}
