package wicket.xtree.table;

import java.util.ArrayList;
import java.util.List;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.RequestCycle;
import wicket.Response;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainer;
import wicket.xtree.table.ColumnLocation.Alignment;
import wicket.xtree.table.ColumnLocation.Unit;

public class SideColumnsView extends WebMarkupContainer {

	public SideColumnsView(MarkupContainer parent, String id) {
		super(parent, id);
		setRenderBodyOnly(true);
	}

	private List<Column> columns = new ArrayList<Column>();
	private List<Component> components = new ArrayList<Component>();

	public void addColumn(Column column, Component component)
	{
		if (column.isVisible())
		{
			columns.add(column);
			components.add(component);
		}
	}
	
	private String renderUnit(Unit unit)
	{
		if (unit == Unit.EM)
		{
			return "em";
		}
		else if (unit == Unit.PX)
		{
			return "px";
		}
		else if (unit == Unit.PERCENT)
		{
			return "%";
		}
		else 
		{
			throw new IllegalStateException("Wrong column unit for column aligned left or right.");
		}
	}
	
	private String renderColumnWidth(Column column)
	{
		ColumnLocation location = column.getLocation();
		return "" + location.getSize() + renderUnit(location.getUnit());
	}
	
	private String renderColumnFloat(Column column)
	{
		ColumnLocation location = column.getLocation();
		if (location.getAlignment() == Alignment.LEFT)
		{
			return "left";
		}
		else if (location.getAlignment() == Alignment.RIGHT)
		{
			return "right";
		}
		else
		{
			throw new IllegalStateException("Wrong column allignment.");
		}
	}
	
	private String renderColumnStyle(Column column)
	{
		return "width:" + renderColumnWidth(column) + "; float: " + renderColumnFloat(column);
	}
	
	@Override
	protected void onRender(final MarkupStream markupStream)
	{
		final int markupStart = markupStream.getCurrentIndex();
		Response response = RequestCycle.get().getResponse();
	
		boolean firstLeft = true; // whether there was no left column rendered yet
		
		if (columns.isEmpty() == false)
		{
			for (int i = 0; i < columns.size(); ++i)
			{
				Column column = columns.get(i);
				Component component = components.get(i);
				
				response.write("<span class=\"column\" style=\"" + renderColumnStyle(column) + "\">");
				if (column.getLocation().getAlignment() == Alignment.LEFT && firstLeft == true)
				{
					response.write("<span class=\"column-inner-first\">");
					firstLeft = false;
				}				
				else
				{
					response.write("<span class=\"column-inner\">");
				}

				markupStream.setCurrentIndex(markupStart);				
				component.render(markupStream);
				
				response.write("</span></span>\n");
			}
		}
		else
		{
			markupStream.skipComponent();
		}		
	}
	
}
