package wicket.xtree.table;

import java.util.ArrayList;
import java.util.List;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.RequestCycle;
import wicket.Response;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainer;
import wicket.xtree.table.ColumnLocation.Unit;

public class MiddleColumnsView extends WebMarkupContainer {

	public MiddleColumnsView(MarkupContainer parent, String id) {
		super(parent, id);
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

	protected int[] computeColumnWidths()
	{
		int result[] = new int[columns.size()];
		
		double sum = 0d;
		double whole = 99d; // we can't do 100%, might cause formating errors
		
		for (Column column : columns)
		{
			// check if the unit is right
			if (column.getLocation().getUnit() != Unit.PROPORTIONAL)
			{
				throw new IllegalStateException("Middle columns must have PROPORTIONAL unit set.");
			}
			sum += column.getLocation().getSize();
		}
		
		int index = 0;
		int together = 0;
		for (Column column : columns)
		{
			together += result[index] = (int) Math.round(((double) column.getLocation().getSize()) / sum * whole);			
			++index;
		}				
		
		if (together > 99) 
		{
			// this can happen - rounding error. just decrease the last one
			result[result.length - 1] -= together - 99;
		}
		
		return result;
	}
	
	@Override
	protected void onRender(final MarkupStream markupStream)
	{
		final int markupStart = markupStream.getCurrentIndex();
		Response response = RequestCycle.get().getResponse();
		int widths[] = computeColumnWidths();
			
		if (columns.isEmpty() == false)
		{
			for (int i = 0; i < columns.size(); ++i)
			{
				Component component = components.get(i);
				
				response.write("<span class=\"column\" style=\"width:" + widths[i] + "%\">");
				response.write("<span class=\"column-inner\">");

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
