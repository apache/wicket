package wicket.xtree.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.tree.TreeNode;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.RequestCycle;
import wicket.Response;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainer;
import wicket.response.NullResponse;
import wicket.xtree.table.ColumnLocation.Unit;

public class MiddleColumnsView extends WebMarkupContainer {

	private TreeNode node;
	
	public MiddleColumnsView(MarkupContainer parent, String id, TreeNode node) {
		super(parent, id);
		this.node = node;
	}


	private List<Column> columns = new ArrayList<Column>();
	private List<Component> components = new ArrayList<Component>();
	private List<Renderable> renderables = new ArrayList<Renderable>();	

	public void addColumn(Column column, Component component, Renderable renderable)
	{
		if (column.isVisible())
		{
			columns.add(column);
			components.add(component);
			renderables.add(renderable);					
		}
	}

	protected int[] computeColumnWidths()
	{
		int result[] = new int[columns.size()];
		Arrays.fill(result, 0);
		
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
	
		int spanColumn = 0;
		int spanLeft = 0;
		for (Column column : columns)
		{			
			int i = index;
			if (spanLeft > 0) 
			{
				i = spanColumn;
				--spanLeft;
			}
			result[i] += (int) Math.round(((double) column.getLocation().getSize()) / sum * whole);
			if (column.getSpan(node) > 1) 			
			{
				int maxSpan = columns.size() - columns.indexOf(column) ;				
				int span = column.getSpan(node) - 1;
				spanColumn = index;
				spanLeft = span < maxSpan ? span : maxSpan;
			}
			++index;
		}				
		
		int together = 0;
		for (int i = 0; i < result.length; ++i)
		{
			together += result[i];
		}
		
		if (together > 99) 
		{
			// this can happen - rounding error. just decrease the last one
			for (int i = result.length - 1; i >= 0; --i)
			{
				if (result[i] != 0)
				{
					result[i] -= together - 99;
					break;
				}
			}
			
		}
		
		return result;
	}
	
	@Override
	protected void onRender(final MarkupStream markupStream)
	{
		final int markupStart = markupStream.getCurrentIndex();
		Response response = RequestCycle.get().getResponse();
		int widths[] = computeColumnWidths();
		boolean rendered = false;	
		
		for (int i = 0; i < columns.size(); ++i)
		{
			Component component = components.get(i);
			Renderable renderable = renderables.get(i);
			Column column = columns.get(i);
			
			response.write("<span class=\"column\" style=\"width:" + widths[i] + "%\">");
			response.write("<span class=\"column-inner\">");						
			
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
			
			int span = column.getSpan(node);
			if (span > 1)
			{
				for (int j = 1; j < span && i < components.size(); ++j)
				{
					++i;
					if (components.get(i) != null)
					{
						Response old = RequestCycle.get().setResponse(NullResponse.getInstance());
						markupStream.setCurrentIndex(markupStart);
						components.get(i).render(markupStream);
						RequestCycle.get().setResponse(old);
						rendered = true;
					}
					
				}
				
			}
		}
		
		if (rendered == false)
		{
			markupStream.skipComponent();
		}		
	}
}
