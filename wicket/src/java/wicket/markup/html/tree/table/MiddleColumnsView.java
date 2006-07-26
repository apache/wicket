package wicket.markup.html.tree.table;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.tree.TreeNode;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.RequestCycle;
import wicket.Response;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.tree.table.ColumnLocation.Unit;
import wicket.response.NullResponse;

/**
 * Class that renders cells of columns aligned in the middle. This class also
 * takes care of counting their widths and of column spans.
 * 
 * @author Matej Knopp
 */
final class MiddleColumnsView extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	private final List<IColumn> columns = new ArrayList<IColumn>();

	private final List<Component> components = new ArrayList<Component>();

	private TreeNode node;

	private final List<IRenderable> renderables = new ArrayList<IRenderable>();

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            The parent component
	 * @param id
	 *            The component id
	 * @param node
	 *            The tree node
	 */
	public MiddleColumnsView(MarkupContainer parent, String id, TreeNode node)
	{
		super(parent, id);
		this.node = node;
	}

	/**
	 * Adds a column to be rendered.
	 * 
	 * @param column
	 *            The column to render
	 * @param component
	 *            The component
	 * @param renderable
	 *            The renderer
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
	 * Computes the percentagle widths of columns. If a column spans over other
	 * columns, the widths of those columns will be zero.
	 * 
	 * @return widths of columns
	 */
	protected double[] computeColumnWidths()
	{
		// initialize the columns array
		double result[] = new double[columns.size()];
		Arrays.fill(result, 0d);

		// the sum of weights of all columns
		double sum = 0d;
		double whole = 99.8d;

		// go over all columns, check their alignment and count sum of their
		// weights
		for (IColumn column : columns)
		{
			// check if the unit is right
			if (column.getLocation().getUnit() != Unit.PROPORTIONAL)
			{
				throw new IllegalStateException("Middle columns must have PROPORTIONAL unit set.");
			}
			sum += column.getLocation().getSize();
		}


		int index = 0; // index of currently processed column

		int spanColumn = 0; // index of column that is spanning over currently
		// processed column (if any)
		int spanLeft = 0; // over how many columns does the spanning column
		// span

		for (IColumn column : columns)
		{
			int i = index; // to which column should we append the size
			if (spanLeft > 0) // is there a column spanning over current
			// column?
			{
				i = spanColumn; // the size should be appended to the spanning
				// column
				--spanLeft;
			}
			// add the percentage size to the column
			result[i] += Math.round((column.getLocation().getSize()) / sum * whole);

			// wants this column to span and no other column is spanning over
			// this column?
			if (spanLeft == 0 && column.getSpan(node) > 1)
			{
				int maxSpan = columns.size() - columns.indexOf(column); // how
				// many
				// columns
				// left
				int span = column.getSpan(node) - 1; // how much columns want
				// the column to span
				// over
				spanColumn = index; // index of column that is spanning
				spanLeft = span < maxSpan ? span : maxSpan; // set the number of
				// columns spanned
				// over
			}
			++index;
		}

		// count the sum
		double together = 0d;
		for (double element : result)
		{
			together += element;
		}


		// is it bigger than 99.8? that can cause layout problems in IE
		if (together > 99.8d)
		{
			// this can happen - rounding error. just decrease the last one
			for (int i = result.length - 1; i >= 0; --i)
			{
				if (result[i] != 0d)
				{
					result[i] -= together - 99.8d;
					break;
				}
			}

		}

		return result;
	}

	/**
	 * Renders all columns.
	 * 
	 * @param markupStream
	 *            The markup stream of this component
	 */
	@Override
	protected void onRender(final MarkupStream markupStream)
	{
		final int markupStart = markupStream.getCurrentIndex();
		Response response = RequestCycle.get().getResponse();
		double widths[] = computeColumnWidths();

		boolean rendered = false; // has been at least one column (component,
		// not renderable) rendered?

		NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
		nf.setMaximumFractionDigits(0);
		nf.setMaximumFractionDigits(2);

		for (int i = 0; i < columns.size(); ++i)
		{
			Component component = components.get(i);
			IRenderable renderable = renderables.get(i);
			IColumn column = columns.get(i);

			// write the wrapping column markup
			response.write("<span class=\"b_\" style=\"width:" + nf.format(widths[i]) + "%\">");
			response.write("<span class=\"c_\">");

			if (component != null) // is there a component for current column?
			{
				// render the component
				markupStream.setCurrentIndex(markupStart);
				component.render(markupStream);
				rendered = true;
			}
			else if (renderable != null) // no component - try to render
			// renderable
			{
				renderable.render(node, response);
			}
			else
			{
				// no renderable or component. fail
				throw new IllegalStateException(
						"Either renderable or cell component must be created for this noode");
			}

			// end of wrapping markup
			response.write("</span></span>\n");

			// does this component span over other columns
			int span = column.getSpan(node);
			if (span > 1)
			{
				// iterate through the columns and if any of them has a
				// component,
				// render the component to null response (otherwise the
				// component will
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
