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

	private List<IColumn> columns = new ArrayList<IColumn>();
	private List<Component> components = new ArrayList<Component>();
	private List<IRenderable> renderables = new ArrayList<IRenderable>();

	public void addColumn(IColumn column, Component component, IRenderable renderable)
	{
		if (column.isVisible())
		{
			if (column.getLocation().getAlignment() == Alignment.LEFT)
			{
				columns.add(column);
				components.add(component);
				renderables.add(renderable);
			}
			else
			{
				columns.add(0, column);
				components.add(0, component);
				renderables.add(0, renderable);
			}
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
	
	private String renderColumnWidth(IColumn column)
	{
		ColumnLocation location = column.getLocation();
		return "" + location.getSize() + renderUnit(location.getUnit());
	}
	
	private String renderColumnFloat(IColumn column)
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
	
	private String renderColumnStyle(IColumn column)
	{
		return "width:" + renderColumnWidth(column) + "; float: " + renderColumnFloat(column);
	}
	
	@Override
	protected void onRender(final MarkupStream markupStream)
	{
		final int markupStart = markupStream.getCurrentIndex();
		Response response = RequestCycle.get().getResponse();
	
		boolean firstLeft = true; // whether there was no left column rendered yet
		boolean rendered = false;
		
		for (int i = 0; i < columns.size(); ++i)
		{
			IColumn column = columns.get(i);
			Component component = components.get(i);
			IRenderable renderable = renderables.get(i);
			
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
			
			if (component != null)
			{
				markupStream.setCurrentIndex(markupStart);
				component.render(markupStream);
				rendered = true;
			}
			else if (renderable != null)
			{
				renderable.render(response);
			}
			else
			{
				throw new IllegalStateException("Either renderable or cell component must be created for this noode");
			}
			
			response.write("</span></span>\n");
		}
		
		if (rendered == false)
		{
			markupStream.skipComponent();
		}		
	}
	
}
