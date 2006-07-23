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

/**
 * Class that renders cells of columns aligned in the middle. This class also takes care of 
 * counting their widths and of column spans.  
 * 
 * @author Matej Knopp
 */
class MiddleColumnsView extends WebMarkupContainer {

	private TreeNode node;
	
	/**
	 * Constructor.
	 */
	public MiddleColumnsView(MarkupContainer parent, String id, TreeNode node) {
		super(parent, id);
		this.node = node;
	}


	private List<IColumn> columns = new ArrayList<IColumn>();
	private List<Component> components = new ArrayList<Component>();
	private List<IRenderable> renderables = new ArrayList<IRenderable>();	

	/**
	 * Adds a column to be rendered.
	 */
	public void addColumn(IColumn column, Component component, IRenderable renderable)
	{
		if (column.isVisible())
		{
			columns.add(column);
			components.add(component);
			renderables.add(renderable);					
		}
	}

	/**
	 * Computes the percentagle widths of columns. If a column spans over other columns, 
	 * the widths of those columns will be zero.
	 * @return
	 */
	protected int[] computeColumnWidths()
	{
		// initialize the columns array
		int result[] = new int[columns.size()];
		Arrays.fill(result, 0);
		
		// the sum of weights of all columns
		double sum = 0d;
		double whole = 99d; // we can't do 100%, might cause formating errors
		
		// go over all columns, check their alignment and count sum of their weights
		for (IColumn column : columns)			
		{
			// check if the unit is right
			if (column.getLocation().getUnit() != Unit.PROPORTIONAL)
			{
				throw new IllegalStateException("Middle columns must have PROPORTIONAL unit set.");
			}
			sum += column.getLocation().getSize();
		}
		
		
		int index = 0;  // index of currently processed column
	
		int spanColumn = 0; // index of column that is spanning over currently processed column (if any)
		int spanLeft = 0;   // over how many columns does the spanning column span  
		
		for (IColumn column : columns)
		{			
			int i = index; // to which column should we append the size
			if (spanLeft > 0)  // is there a column spanning over current column?
			{
				i = spanColumn;  // the size should be appended to the spanning column
				--spanLeft;  
			}
			// add the percentage size to the column
			result[i] += (int) Math.round(((double) column.getLocation().getSize()) / sum * whole);
			
			// wants this column to span and no other column is spanning over this column?
			if (spanLeft == 0 && column.getSpan(node) > 1) 			
			{				
				int maxSpan = columns.size() - columns.indexOf(column); // how many columns left 				
				int span = column.getSpan(node) - 1; // how much columns want the column to span over
				spanColumn = index; // index of column that is spanning
				spanLeft = span < maxSpan ? span : maxSpan;  // set the number of columns spanned over
			}
			++index;
		}				
		
		// count the sum
		int together = 0;
		for (int i = 0; i < result.length; ++i)
		{
			together += result[i];
		}
		
		// is it bigger than 99? that can cause layout problems
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
	
	/**
	 * Renders all columns.
	 */
	@Override
	protected void onRender(final MarkupStream markupStream)
	{		
		final int markupStart = markupStream.getCurrentIndex();
		Response response = RequestCycle.get().getResponse();
		int widths[] = computeColumnWidths();
				
		boolean rendered = false; // has been at least one column (component, not renderable) rendered?	
				
		for (int i = 0; i < columns.size(); ++i)
		{
			Component component = components.get(i);
			IRenderable renderable = renderables.get(i);
			IColumn column = columns.get(i);
		
			// write the wrapping column markup
			response.write("<span class=\"column\" style=\"width:" + widths[i] + "%\">");
			response.write("<span class=\"column-inner\">");						
			
			if (component != null) // is there a component for current column?
			{
				// render the component
				markupStream.setCurrentIndex(markupStart);
				component.render(markupStream);
				rendered = true;
			}
			else if (renderable != null) // no component - try to render renderable
			{
				renderable.render(node, response);
			}
			else
			{
				// no renderable or component. fail
				throw new IllegalStateException("Either renderable or cell component must be created for this noode");
			}
			
			// end of wrapping markup
			response.write("</span></span>\n");
			
			// does this component span over other columns
			int span = column.getSpan(node);
			if (span > 1)
			{
				// iterate through the columns and if any of them has a component,
				// render the component to null response (otherwise the component will
				// complain that it hasn't been rendered
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
		
		// if no component was rendered just advance in the markup stream
		if (rendered == false)
		{
			markupStream.skipComponent();
		}		
	}
}
